/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.eightyplus.Painter;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import dk.eightyplus.Painter.action.State;
import dk.eightyplus.Painter.action.Undo;
import dk.eightyplus.Painter.component.Component;
import dk.eightyplus.Painter.component.Text;
import dk.eightyplus.Painter.dialog.ColorPickerDialog;
import dk.eightyplus.Painter.fragment.EditorFragment;
import dk.eightyplus.Painter.fragment.SliderFragment;
import dk.eightyplus.Painter.utilities.Compatibility;
import dk.eightyplus.Painter.utilities.Storage;
import dk.eightyplus.Painter.view.DrawingView;
import dk.eightyplus.Painter.view.MoveView;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class FingerPaint extends FragmentActivity implements ColorPickerDialog.OnColorChangedListener, Callback {

  private static final String TAG = FingerPaint.class.toString();

  private final Stack<Undo> undo = new Stack<Undo>();
  private final Stack<Undo> redo = new Stack<Undo>();

  private ViewGroup layout;
  private DrawingView view;
  private MoveView moveView;

  private State state = State.DrawPath;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    layout = (ViewGroup) findViewById(R.id.main);
    ViewGroup drawableArea = (ViewGroup) findViewById(R.id.drawable_area);
    view = new DrawingView(getApplicationContext(), this);

    drawableArea.addView(view);
    TouchView touchView = new TouchView(getApplicationContext());
    drawableArea.addView(touchView);

    setupActionBar();

    /*TODO test code
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
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
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
              startMove(moveComponent);
            }
          }
          return true;
        }
        case WriteText:
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isEditorVisibile()) {
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


  private boolean isEditorVisibile() {
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
  }

  @Override
  public void textEditDone() {

    hideEditor();
  }

  private void setupActionBar() {
    if (Compatibility.get().supportActionBar()) {
      getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
      getActionBar().setCustomView(R.layout.action_bar_layout);

      final View actionBar = getActionBar().getCustomView().findViewById(R.id.action_bar_toggle_group);
      final View editButton = actionBar.findViewById(R.id.action_bar_edit);
      final View moveButton = actionBar.findViewById(R.id.action_bar_move);
      final View deleteButton = actionBar.findViewById(R.id.action_bar_delete);
      final View textButton = actionBar.findViewById(R.id.action_bar_text);

      editButton.setSelected(true);
      editButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          editButton.setSelected(true);
          moveButton.setSelected(false);
          deleteButton.setSelected(false);
          textButton.setSelected(false);

          state = State.DrawPath;
        }
      });

      moveButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          editButton.setSelected(false);
          moveButton.setSelected(true);
          deleteButton.setSelected(false);
          textButton.setSelected(false);

          state = State.Move;
        }
      });

      deleteButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          editButton.setSelected(false);
          moveButton.setSelected(false);
          deleteButton.setSelected(true);
          textButton.setSelected(false);

          state = State.Delete;
        }
      });

      textButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          editButton.setSelected(false);
          moveButton.setSelected(false);
          deleteButton.setSelected(false);
          textButton.setSelected(true);

          state = State.WriteText;
        }
      });
    }
  }

  @Override
  protected void onPause() {
    super.onPause();

    Fragment fragment = getSupportFragmentManager().findFragmentByTag(Tags.FRAGMENT_SLIDER);
    if (fragment != null) {
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(fragment);
      transaction.commit();
    }
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

  public void colorChanged(int color) {
    view.setColor(color);
  }

  @Override
  public void move(Component component, float dx, float dy) {
    layout.removeView(moveView);
    moveView.destroy();
    moveView = null;
    component.setVisible(true);
    view.move(component, dx, dy);
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);

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

    MenuItem menuWidth = menu.findItem(R.id.menu_width);
    if (menuWidth != null) {
      menuWidth.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          Fragment fragment = getSupportFragmentManager().findFragmentByTag(Tags.FRAGMENT_SLIDER);

          FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
          if (fragment == null) {
            SliderFragment sliderFragment = new SliderFragment(FingerPaint.this, view.getStrokeWidth());
            transaction.replace(R.id.configuration_top, sliderFragment, Tags.FRAGMENT_SLIDER);
          } else {
            transaction.remove(fragment);
          }

          transaction.commit();
          return true;
        }
      });
    }

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

    MenuItem menuShare = menu.findItem(R.id.menu_share);
    if (menuShare != null) {
      menuShare.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          try {
            File file = Storage.getStorage(getApplicationContext()).writeToFile(view.getBitmap());
            final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/png");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Some test");
            sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(file));
            startActivity(Intent.createChooser(sharingIntent, "Share image using"));
          } catch (IOException e) {
            Log.e(TAG, "IOException", e);
          }
          return true;
        }
      });
    }

    MenuItem menuSave = menu.findItem(R.id.menu_save);
    if (menuSave != null) {
      menuSave.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          try {
            //writeToFile(getApplicationContext(), view.getBitmap());
            Storage.getStorage(getApplicationContext()).writeToFile(view, "file.note");
          } catch (IOException e) {
            Log.e(TAG, "IOException", e);
          }
          return true;
        }
      });
    }

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
            Log.e(TAG, "IOException", e);
          } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException", e);
          }
          return true;
        }
      });
    }

    MenuItem menuColor = menu.findItem(R.id.menu_color);
    if (menuColor != null) {
      menuColor.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          new ColorPickerDialog(FingerPaint.this, FingerPaint.this, view.getColor()).show();
          return true;
        }
      });
    }

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
            Log.e(TAG, "IOException", e);
          } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException", e);
          }
          return true;
        }
      });
    }


    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return super.onOptionsItemSelected(item);
  }


  @Override
  public State getState() {
    return state;
  }

  @Override
  public void startMove(Component moveComponent) {
    moveView = new MoveView(getApplicationContext(), moveComponent, FingerPaint.this);
    layout.addView(moveView);
    view.redraw();
  }

  @Override
  public void setStrokeWidth(int width) {
    view.setStrokeWidth(width);
  }

  @Override
  public void add(Undo undo) {
    this.undo.add(undo);
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

}
