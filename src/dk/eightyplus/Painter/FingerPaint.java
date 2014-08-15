package dk.eightyplus.Painter;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import dk.eightyplus.Painter.action.ActionBarClickListener;
import dk.eightyplus.Painter.action.State;
import dk.eightyplus.Painter.action.Undo;
import dk.eightyplus.Painter.component.Component;
import dk.eightyplus.Painter.component.Picture;
import dk.eightyplus.Painter.component.Text;
import dk.eightyplus.Painter.dialog.ColorPickerDialog;
import dk.eightyplus.Painter.fragment.ButtonSelectorFragment;
import dk.eightyplus.Painter.fragment.ColorPaletteFragment;
import dk.eightyplus.Painter.fragment.EditorFragment;
import dk.eightyplus.Painter.fragment.NoteListFragment;
import dk.eightyplus.Painter.fragment.SliderFragment;
import dk.eightyplus.Painter.utilities.Compatibility;
import dk.eightyplus.Painter.utilities.Storage;
import dk.eightyplus.Painter.view.DrawingView;
import dk.eightyplus.Painter.view.MoveView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;

public class FingerPaint extends FragmentActivity implements ColorPickerDialog.OnColorChangedListener, Callback {

  private static final String TAG = FingerPaint.class.toString();

  private final Stack<Undo> undo = new Stack<Undo>();
  private final Stack<Undo> redo = new Stack<Undo>();

  private ViewGroup layout;
  private DrawingView view;
  private MoveView moveView;

  private State state = State.DrawPath;
  private ViewGroup visibleLayer;
  private int color = 0;
  private String cameraFileName;
  private String saveFileNamePrefix;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    layout = (ViewGroup) findViewById(R.id.main);
    visibleLayer = (ViewGroup) findViewById(R.id.visible_layer);
    view = new DrawingView(getApplicationContext(), this);
    visibleLayer.addView(view);

    ViewGroup touchLayer = (ViewGroup) layout.findViewById(R.id.touch_layer);
    TouchView touchView = new TouchView(getApplicationContext());
    touchLayer.addView(touchView);

    setupActionBar();

    showButtonSelector();
    /*
    boolean show = savedInstanceState == null || !savedInstanceState.getBoolean("TESTCODE", false);
    if (show) {
      components.add(new Text());
      Composite composite = new Composite();
      composite.add(new Text("composite element"));
      composite.move(300, 300);
      composite.add(new Text("composite element 2"));
      composite.move(100, 100);
      Polygon polygon = new Polygon();
      composite.add(polygon);
      polygon.getPath().quadTo(100, 100, 100, 100);
      components.add(composite);
    }

    List<Integer> drawables = Storage.getStorage(getApplicationContext()).getDrawableResources();
    for (int i = 50; i < 100; i+=10) {
      int resource = drawables.get(i); // android.R.drawable.star_big_off;

      Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resource);
      view.add(new Picture(bitmap));
    }

    try {
      Bitmap bitmap = Storage.getStorage(getApplicationContext()).loadFromFile(getApplicationContext());
      if (bitmap !=null) {
        view.add(new Picture(bitmap));
      }
    } catch (IOException e) {

    }
    */
  }

  /**
   * TouchView delegates user interaction to correct views from user toggles
   */
  public class TouchView extends View  {
    public TouchView(Context context) {
      super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

      switch (getState()) {
        case Delete: {
          if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();
            Component deleteComponent = view.findComponent(x, y);

            if (deleteComponent != null) {
              view.remove(deleteComponent);
              add(new Undo(deleteComponent, State.Delete));
              view.redraw();
            }
          }
          return true;
        }
        case Move: {
          if (moveView == null && event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            Component moveComponent = view.findComponent(x, y);

            if (moveComponent != null) {
              moveComponent.setVisible(false);
              moveView = new MoveView(getApplicationContext(), moveComponent, FingerPaint.this);
              visibleLayer.addView(moveView);
              view.redraw();
              moveView.onTouchEvent(event);
            }
          } else if (moveView != null &&
              (event.getAction() == MotionEvent.ACTION_DOWN ||
                  event.getAction() == MotionEvent.ACTION_MOVE ||
                  event.getAction() == MotionEvent.ACTION_UP)) {
            moveView.onTouchEvent(event);
          }
          return true;
        }
        case Text:
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isEditorVisible()) {
              hideEditor();
            } else {
              float x = event.getX();
              float y = event.getY();
              Component editComponent = view.findComponent(x, y);

              showEditor(editComponent, event.getX(), event.getY());
            }
          }
          return true;
        case DrawPath:
        default:
            return view.onTouchEvent(event);
          }
      }
  }


  private boolean isEditorVisible() {
    return getSupportFragmentManager().findFragmentByTag(Tags.FRAGMENT_EDITOR) != null;
  }

  private void showEditor (Component component, float x, float y) {
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(Tags.FRAGMENT_EDITOR);

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    if (fragment == null) {
      EditorFragment editorFragment = new EditorFragment(FingerPaint.this, component, x, y);
      transaction.replace(R.id.configuration_top, editorFragment, Tags.FRAGMENT_EDITOR);
    } else {
      transaction.remove(fragment);
    }

    transaction.commit();
  }

  private void hideEditor() {
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(Tags.FRAGMENT_EDITOR);

    if (fragment != null) {
      EditorFragment editorFragment = (EditorFragment) fragment;
      Pair<Text, Undo> textChanges = editorFragment.getTextChanges();
      if (textChanges.first != null) {
        if (color != 0) {
          textChanges.first.setColor(color);
        }
        view.add(textChanges.first);
        view.redraw();
        if (textChanges.second != null) {
          add(textChanges.second);
        }
      }
    }
    if (fragment != null) {
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(fragment);
      transaction.commit();
    }

    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
  }

  private void showWidthSlider() {
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(Tags.FRAGMENT_SLIDER);
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    if (fragment == null) {
      SliderFragment sliderFragment = new SliderFragment(this, view.getStrokeWidth());
      transaction.replace(R.id.configuration_top, sliderFragment, Tags.FRAGMENT_SLIDER);
    } else {
      transaction.remove(fragment);
    }
    transaction.commit();
  }

  @SuppressWarnings("unused")
  private void hideWidthSlider() {
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(Tags.FRAGMENT_SLIDER);
    if (fragment != null) {
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(fragment);
      transaction.commit();
    }
  }

  private void showColorPalette() {
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(Tags.FRAGMENT_COLOR);
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    if (fragment == null) {
      ColorPaletteFragment sliderFragment = new ColorPaletteFragment();
      transaction.replace(R.id.configuration_top, sliderFragment, Tags.FRAGMENT_COLOR);
    } else {
      transaction.remove(fragment);
    }
    transaction.commit();
  }

  private void showNotesList() {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    NoteListFragment noteListFragment = new NoteListFragment();
    noteListFragment.show(transaction, Tags.FRAGMENT_LIST);
  }

  private void removeNotesList() {
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(Tags.FRAGMENT_LIST);
    if (fragment != null) {
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(fragment);
      transaction.commit();
    }
  }

  private void showButtonSelector() {
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(Tags.FRAGMENT_SLIDER);
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    if (fragment == null) {
      int[] icons = new int[]{
          android.R.drawable.ic_menu_edit,
          android.R.drawable.ic_menu_more,
          android.R.drawable.ic_menu_delete,
          android.R.drawable.ic_menu_sort_alphabetically,
      };

      String[] tags = new String[] {
          State.DrawPath.name(),
          State.Move.name(),
          State.Delete.name(),
          State.Text.name(),
      };

      ButtonSelectorFragment buttonSelectorFragment = new ButtonSelectorFragment(icons, tags);
      transaction.replace(R.id.button_selector, buttonSelectorFragment, Tags.FRAGMENT_SELECTOR);
    } else {
      transaction.remove(fragment);
    }
    transaction.commit();
  }


  private void showSpinner(final boolean show) {
    final ProgressBar pb = (ProgressBar) findViewById(R.id.progress);
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        pb.setVisibility( show ? View.VISIBLE : View.GONE);
      }
    });
  }

  @Override
  public void textEditDone() {

    hideEditor();
  }

  private void setupActionBar() {
    if (Compatibility.get().supportActionBar()) {
      getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
      getActionBar().setCustomView(R.layout.action_bar_layout);

      /*
      final View actionBar = getActionBar().getCustomView().findViewById(R.id.action_bar_toggle_group);
      final View editButton = actionBar.findViewById(R.id.action_bar_edit);
      final View moveButton = actionBar.findViewById(R.id.action_bar_move);
      final View deleteButton = actionBar.findViewById(R.id.action_bar_delete);
      final View textButton = actionBar.findViewById(R.id.action_bar_text);

      List<View> actionBarButtons = new ArrayList<View>();
      actionBarButtons.add(editButton);
      actionBarButtons.add(moveButton);
      actionBarButtons.add(deleteButton);
      actionBarButtons.add(textButton);

      editButton.setSelected(true);
      editButton.setOnClickListener(new ActionBarClickListener(actionBarButtons, State.DrawPath, this));
      moveButton.setOnClickListener(new ActionBarClickListener(actionBarButtons, State.Move, this));
      deleteButton.setOnClickListener(new ActionBarClickListener(actionBarButtons, State.Delete, this));
      textButton.setOnClickListener(new ActionBarClickListener(actionBarButtons, State.Text, this));
      */
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    hideEditor();
    //hideWidthSlider();
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    view.onRestoreInstanceState(savedInstanceState);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    view.onSaveInstanceState(outState);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case Tags.SELECT_PICTURE:
          try {
            Bitmap bitmap = Storage.getStorage(getApplicationContext()).loadBitmapFromIntent(this, data, 1);
            Picture picture = new Picture(bitmap);
            view.add(picture);
            add(new Undo(picture, State.Add));
          } catch (IOException e) {
            Log.d(TAG, getString(R.string.log_error_exception), e);
            Toast.makeText(getApplicationContext(), getString(R.string.error_loading_image), Toast.LENGTH_LONG).show();
          }
          break;
        case Tags.CAMERA_REQUEST:
          try {
            // TODO add delete image function?
            Bitmap bitmap = Storage.getStorage(getApplicationContext()).loadFromFile(cameraFileName);
            Storage.getStorage(getApplicationContext()).addImageToGallery(this, cameraFileName);
            Picture picture = new Picture(bitmap);
            view.add(picture);
            add(new Undo(picture, State.Add));
          } catch (IOException e) {
            Log.d(TAG, getApplicationContext().getString(R.string.log_error_exception), e);
            Toast.makeText(getApplicationContext(), getString(R.string.error_taking_photo), Toast.LENGTH_LONG).show();
          }
          break;
      }
      view.redraw();
    }
  }

  public void colorChanged(int color) {
    this.color = color;
    view.setColor(color);
  }

  @Override
  public void move(Component component, float dx, float dy, float scale) {
    component.setVisible(true);
    view.move(component, dx, dy, scale);
    visibleLayer.removeView(moveView);
    moveView.destroy();
    moveView = null;
  }

  @Override
  public State getState() {
    return state;
  }

  @Override
  public void setState(State state) {
    this.state = state;
  }

  @Override
  public void setStrokeWidth(int width) {
    view.setStrokeWidth(width);
  }

  @Override
  public void add(Undo undo) {
    this.undo.add(undo);
  }

  @Override
  public void load(final String fileName) {

    new Thread(new Runnable() {
      @Override
      public void run() {
        removeNotesList();
        showSpinner(true);
        try {
          saveFileNamePrefix = fileName.substring(0, fileName.lastIndexOf("."));
          clearUndos();
          Storage.getStorage(getApplicationContext()).loadFromFile(view, fileName);
          view.redraw();
        } catch (IOException e) {
          Log.e(TAG, getString(R.string.log_error_exception), e);
        } catch (ClassNotFoundException e) {
          Log.e(TAG, getString(R.string.log_error_exception), e);
        }
        showSpinner(false);
      }
    }).start();
  }

  private void save() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        showSpinner(true);
        try {
          //writeToFile(getApplicationContext(), view.getBitmap());
          if (saveFileNamePrefix == null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String date = formatter.format(Calendar.getInstance().getTime());
            saveFileNamePrefix = String.format("file-%s", date);
          }

          Storage storage = Storage.getStorage(getApplicationContext());
          storage.writeToFile(view, String.format("%s.note", saveFileNamePrefix));
          storage.writeToFile(view.getBitmap(), String.format("%s.png", saveFileNamePrefix), 30);
        } catch (IOException e) {
          Log.e(TAG, getString(R.string.log_error_exception), e);
        }
        showSpinner(false);
      }
    }).start();
  }

  public boolean undo() {
    if (undo.size() > 0) {
      Undo undo = this.undo.pop();
      if (undo.undo(view)) {
        redo.add(undo);
        view.redraw();
        return true;
      }
    }
    return false;
  }

  public boolean redo() {
    if (redo.size() > 0) {
      Undo redo = this.redo.pop();
      if (redo.redo(view)) {
        undo.add(redo);
        view.redraw();
        return true;
      }
    }
    return false;
  }

  private void clearUndos() {
    undo.clear();
    clearRedos();
  }

  private void clearRedos() {
    redo.clear();
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);

    setupDrawPathMenuButton(menu);
    setupWidthMenuButton(menu);
    setupMoveMenuButton(menu);
    setupRedrawMenuButton(menu);
    setupShareMenuButton(menu);
    setupCameraMenuButton(menu);
    setupGalleryMenuButton(menu);
    setupSaveMenuButton(menu);
    setupLoadMenuButton(menu);
    setupColorMenuButton(menu);
    setupDeleteMenuButton(menu);
    setupUndoMenuButton(menu);
    setupRedoMenuButton(menu);
    setupReplayMenuButton(menu);
    setupNewMenuButton(menu);
    setupMenuListButton(menu);
    setupMenuFeedbackButton(menu);

    return true;
  }

  private void setupDrawPathMenuButton(Menu menu) {
    MenuItem path = menu.findItem(R.id.menu_path);
    if (path != null) {
      path.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          state = State.DrawPath;
          return true;
        }
      });
    }
  }

  private void setupWidthMenuButton(Menu menu) {
    MenuItem menuWidth = menu.findItem(R.id.menu_width);
    if (menuWidth != null) {
      menuWidth.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          showWidthSlider();
          return true;
        }
      });
    }
  }

  private void setupMoveMenuButton(Menu menu) {
    MenuItem menuMove = menu.findItem(R.id.menu_move);
    if (menuMove != null) {
      menuMove.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          state = State.Move;
          return true;
        }
      });
    }
  }

  private void setupRedrawMenuButton(Menu menu) {
    MenuItem menuRedraw = menu.findItem(R.id.menu_redraw);
    if (menuRedraw != null) {
      menuRedraw.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          view.redraw(50, true);
          return true;
        }
      });
    }
  }

  private void setupShareMenuButton(Menu menu) {
    MenuItem menuShare = menu.findItem(R.id.menu_share);
    if (menuShare != null) {
      menuShare.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          try {
            File file = Storage.getStorage(getApplicationContext()).writeToFile(view.getBitmap());
            final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/png");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_information)));
          } catch (IOException e) {
            Log.e(TAG, getApplicationContext().getString(R.string.log_error_exception), e);
          }
          return true;
        }
      });
    }
  }

  private void setupCameraMenuButton(Menu menu) {
    MenuItem menuCamera = menu.findItem(R.id.menu_camera);
    if (menuCamera != null) {
      menuCamera.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          String date = formatter.format(Calendar.getInstance().getTime());
          cameraFileName = String.format("camera-%s.jpeg", date);
          File cameraFile = Storage.getStorage(getApplicationContext()).getFilename(cameraFileName);
          Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
          cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
          startActivityForResult(cameraIntent, Tags.CAMERA_REQUEST);
          return true;
        }
      });
    }
  }

  private void setupGalleryMenuButton(Menu menu) {
    MenuItem menuGallery = menu.findItem(R.id.menu_gallery);
    if (menuGallery != null) {
      menuGallery.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          Intent intent = new Intent();
          intent.setType("image/*");
          intent.setAction(Intent.ACTION_GET_CONTENT);
          startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), Tags.SELECT_PICTURE);
          return true;
        }
      });
    }
  }

  private void setupSaveMenuButton(Menu menu) {
    MenuItem menuSave = menu.findItem(R.id.menu_save);
    if (menuSave != null) {
      menuSave.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          save();
          return true;
        }
      });
    }
  }

  private void setupLoadMenuButton(Menu menu) {
    MenuItem menuLoad = menu.findItem(R.id.menu_load);
    if (menuLoad != null) {
      menuLoad.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          try {
            //writeToFile(getApplicationContext(), view.getBitmap());
            Storage.getStorage(getApplicationContext()).loadFromFile(view, "file.note");
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                view.redraw();
              }
            });
          } catch (IOException e) {
            Log.e(TAG, getString(R.string.log_error_exception), e);
          } catch (ClassNotFoundException e) {
            Log.e(TAG, getString(R.string.log_error_exception), e);
          }
          return true;
        }
      });
    }
  }

  private void setupColorMenuButton(Menu menu) {
    MenuItem menuColor = menu.findItem(R.id.menu_color);
    if (menuColor != null) {
      menuColor.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          //new ColorPickerDialog(FingerPaint.this, FingerPaint.this, view.getColor()).show();
          showColorPalette();
          return true;
        }
      });
    }
  }

  private void setupDeleteMenuButton(Menu menu) {
    MenuItem menuDelete = menu.findItem(R.id.menu_delete);
    if (menuDelete != null) {
      menuDelete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          state = State.Delete;
          return true;
        }
      });
    }
  }

  private void setupUndoMenuButton(Menu menu) {
    MenuItem menuUndo = menu.findItem(R.id.menu_undo);
    if (menuUndo != null) {
      menuUndo.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          undo();
          return true;
        }
      });
    }
  }

  private void setupRedoMenuButton(Menu menu) {
    MenuItem menuRedo = menu.findItem(R.id.menu_redo);
    if (menuRedo != null) {
      menuRedo.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          redo();
          return true;
        }
      });
    }
  }

  private void setupReplayMenuButton(Menu menu) {
    MenuItem menuReplay = menu.findItem(R.id.menu_replay);
    if (menuReplay != null) {
      menuReplay.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          try {
            //writeToFile(getApplicationContext(), view.getBitmap());
            Storage.getStorage(getApplicationContext()).loadFromFile(view, "file.note", true);
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                view.redraw(100, false);
              }
            });
          } catch (IOException e) {
            Log.e(TAG, getString(R.string.log_error_exception), e);
          } catch (ClassNotFoundException e) {
            Log.e(TAG, getString(R.string.log_error_exception), e);
          }
          return true;
        }
      });
    }
  }

  private void setupNewMenuButton(Menu menu) {
    MenuItem menuNew = menu.findItem(R.id.menu_new);
    if (menuNew != null) {
      menuNew.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          clearUndos();
          saveFileNamePrefix = null;
          view.newDrawing();
          return true;
        }
      });
    }
  }

  private void setupMenuListButton(Menu menu) {
    MenuItem menuItem = menu.findItem(R.id.menu_list);
    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        showNotesList();
        return true;
      }
    });
  }

  private void setupMenuFeedbackButton(Menu menu) {
    MenuItem menuItem = menu.findItem(R.id.menu_feedback);
    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        String url = getApplicationContext().getString(R.string.url_feedback);
        WebPageActivity.startWebPageActivity(FingerPaint.this, url);
        return true;
      }
    });

  }

}
