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

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FingerPaint extends GraphicsActivity implements ColorPickerDialog.OnColorChangedListener {

  private final List<Component> components = new ArrayList<Component>();
  private final List<Component> undoComponents = new ArrayList<Component>();
  // TODO crete user interaction -> private final List<State> undos = new ArrayList<State>();

  private static final String TAG = FingerPaint.class.toString();
  private FingerPaint.MyView view;

  private MoveView moveView;
  private RelativeLayout layout;

  enum State {
    DrawPath,
    Move,
    Delete,
    WriteText
  }

  private State state = State.DrawPath;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    layout = new RelativeLayout(getApplicationContext());
    view = new MyView(getApplicationContext());
    setContentView(layout);
    layout.addView(view);

    boolean show = savedInstanceState == null || !savedInstanceState.getBoolean("TESTCODE", false);

    /*TODO test code */
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

    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setDither(true);
    mPaint.setColor(0xFFFF0000);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);

    mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
        0.4f, 6, 3.5f);

    mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    int i = 0;
    Component component;
    while ((component = (Component) savedInstanceState.get("COMPONTENT" + i++)) != null) {
      components.add(component);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putBoolean("TESTCODE", true);

    for (int i = 0; i < components.size(); i++) {
      Component component = components.get(i);
      outState.putSerializable("COMPONTENT" + i , component);
    }
  }

  private Paint       mPaint;
  private MaskFilter  mEmboss;
  private MaskFilter  mBlur;

  public void colorChanged(int color) {
    view.color = color;
    mPaint.setColor(color);
  }

  public class MyView extends View {

    private static final float MINP = 0.25f;
    private static final float MAXP = 0.75f;

    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Polygon    mPath;
    private Paint   mBitmapPaint;
    private int color = 0;

    public MyView(Context c) {
      super(c);

      mPath = new Polygon();
      mPath.setStrokeWidth(12);
      mBitmapPaint = new Paint(Paint.DITHER_FLAG);

      setLayerType(LAYER_TYPE_HARDWARE, mPaint); // TODO API level 11
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      super.onSizeChanged(w, h, oldw, oldh);
      mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
      mCanvas = new Canvas(mBitmap);
      redraw();
    }

    @Override
    protected void onDraw(Canvas canvas) {
      canvas.drawColor(0xFFAAAAAA);
      canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

      mPath.onDraw(canvas, mPaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
      int color = this.color != 0 ? this.color : (int) (0xFFFFFF00 * Math.random());
      mPaint.setColor(color);

      mPath.setColor(color);
      mPath.getPath().reset();
      mPath.getPath().moveTo(x, y);
      mX = x;
      mY = y;
    }
    private void touch_move(float x, float y) {
      float dx = Math.abs(x - mX);
      float dy = Math.abs(y - mY);
      if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
        mPath.getPath().quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
        mX = x;
        mY = y;
      }
    }
    private void touch_up() {
      mPath.getPath().lineTo(mX, mY);
      // commit the path to our offscreen
      mPath.onDraw(mCanvas, mPaint);
      // kill this so we don't double draw

      components.add(new Polygon(mPath));

      mPath.getPath().reset();
    }

    private Path getPath(MotionEvent event) {
      Path path = new Path();
      int historySize = event.getHistorySize();
      for (int i = 0; i < historySize; i++) {
        float historicalX = event.getHistoricalX(i);
        float historicalY = event.getHistoricalY(i);
        path.lineTo(historicalX, historicalY);
      }
      return path;
    }

    void clear() {
      int w = mBitmap.getWidth();
      int h = mBitmap.getHeight();

      mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
      mCanvas = new Canvas(mBitmap);
    }

    void drawComponent(int color, Component path) {
      mPaint.setColor(color);
      path.onDraw(mCanvas, mPaint);
      invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      float x = event.getX();
      float y = event.getY();

      switch (state) {
        case Delete: {
          Component deleteComponent = null;
          float minimumDistance = Float.MAX_VALUE;
          for (Component component : components) {
            float distance = component.centerDist(x, y);
            if (distance < minimumDistance) {
              deleteComponent = component;
              minimumDistance = distance;
            }
          }

          if (deleteComponent != null) {
            components.remove(deleteComponent);
            undoComponents.add(deleteComponent);
            redraw();
          }
        }

        break;
        case Move: {
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Component moveComponent = null;
            float minimumDistance = Float.MAX_VALUE;
            for (Component component : components) {
              float distance = component.centerDist(x, y);
              if (distance < minimumDistance) {
                moveComponent = component;
                minimumDistance = distance;
              }
            }

            if (moveComponent != null) {
              components.remove(moveComponent);
              redraw();

              //Component path = getLastPath();
              moveView = new MoveView(getApplicationContext(), moveComponent);
              layout.addView(moveView);
              moveView.onTouchEvent(event);
            }
          }
        }
        break;
        case DrawPath:
        default:

          switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
              touch_start(x, y);
              invalidate();
              break;
            case MotionEvent.ACTION_MOVE:
              touch_move(x, y);
              invalidate();
              break;
            case MotionEvent.ACTION_UP:
              touch_up();
              invalidate();
              break;
          }
      }
      return true;
    }
  }

  private static final int COLOR_MENU_ID = Menu.FIRST;
  private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
  private static final int BLUR_MENU_ID = Menu.FIRST + 2;
  private static final int ERASE_MENU_ID = Menu.FIRST + 3;
  private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;
  private static final int SHARE_MENU_ID = Menu.FIRST + 5;
  private static final int REDRAW_MENU_ID = Menu.FIRST + 6;
  private static final int MOVE_MENU_ID = Menu.FIRST + 7;

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
          redraw(50, true);
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
            File file = writeToFile(getApplicationContext(), view.mBitmap);

            final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/png");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Some test");
            //sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, file.toURI());

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
            writeToFile(getApplicationContext(), view.mBitmap);
          } catch (IOException e) {
            Log.e(TAG, "IOException", e);
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
          new ColorPickerDialog(FingerPaint.this, FingerPaint.this, mPaint.getColor()).show();
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

    /*
    menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
    menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
    menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
    menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
    menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');

    menu.add(0, SHARE_MENU_ID, 0, "Share");
    menu.add(0, REDRAW_MENU_ID, 0, "Redraw");
    menu.add(0, MOVE_MENU_ID, 0, "Move last");
    */

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    mPaint.setXfermode(null);
    mPaint.setAlpha(0xFF);

    switch (item.getItemId()) {
      case COLOR_MENU_ID:
        new ColorPickerDialog(this, this, mPaint.getColor()).show();
        return true;
      case EMBOSS_MENU_ID:
        if (mPaint.getMaskFilter() != mEmboss) {
          mPaint.setMaskFilter(mEmboss);
        } else {
          mPaint.setMaskFilter(null);
        }
        return true;
      case BLUR_MENU_ID:
        if (mPaint.getMaskFilter() != mBlur) {
          mPaint.setMaskFilter(mBlur);
        } else {
          mPaint.setMaskFilter(null);
        }
        return true;
      case ERASE_MENU_ID:
        mPaint.setXfermode(new PorterDuffXfermode(
            PorterDuff.Mode.CLEAR));
        return true;
      case SRCATOP_MENU_ID:
        mPaint.setXfermode(new PorterDuffXfermode(
            PorterDuff.Mode.SRC_ATOP));
        mPaint.setAlpha(0x80);
        return true;

      case SHARE_MENU_ID:

        return true;

      case REDRAW_MENU_ID:


        return true;

      case MOVE_MENU_ID:
        Component path = getLastPath();
        moveView = new MoveView(getApplicationContext(), path);
        layout.addView(moveView);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private Component getLastPath() {
    Component path = components.get(components.size() - 1 );
    components.remove(path);
    redraw();
    return path;
  }

  private void redraw() {
    redraw(0, false);
  }

  private void redraw(final int delay, final boolean randomColor) {
    view.clear();

    new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < components.size(); i++) {
          final Component component = components.get(i);

          if (delay > 0) {
            try {
              Thread.sleep(delay);
            } catch (InterruptedException e) {

            }
          }
          final int color = randomColor ? (int) (0xFFAAAAAA * Math.random()) : component.getColor();
          component.setColor(color);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              view.drawComponent(color, component);
            }
          });
        }
      }
    }).start();

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        view.invalidate();
      }
    });
  }

  @Override
  public void onBackPressed() {
    if (undoComponents.size() > 0) {
      Component component = undoComponents.remove(0);
      components.add(component);
      redraw();
    } else {
      super.onBackPressed();
    }
  }


  private void undo() {
    if (undoComponents.size() > 0) {
      Component component = undoComponents.remove(undoComponents.size() - 1);
      //State undo = undos.remove(0);
      components.add(component);
      redraw();
    }
  }


  private class MoveView extends View {



    private int _xDelta;
    private int _yDelta;
    private Bitmap mBitmap;
    private Component component;
    private Paint   mBitmapPaint;
    private Paint mPaint;

    public MoveView(Context context, Component component) {
      super(context);
      this.component = component;

      mPaint = new Paint();
      mPaint.setAntiAlias(true);
      mPaint.setDither(true);
      mPaint.setStyle(Paint.Style.STROKE);
      mPaint.setStrokeJoin(Paint.Join.ROUND);
      mPaint.setStrokeCap(Paint.Cap.ROUND);
      mPaint.setStrokeWidth(12);
      setLayerType(LAYER_TYPE_HARDWARE, mPaint); // TODO API level 11
      mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      super.onSizeChanged(w, h, oldw, oldh);
      mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); // TODO create smaller bitmap (from bounds)
    }

    @Override
    protected void onDraw(Canvas canvas) {
      canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
      component.onDraw(canvas, mPaint);

      RectF bounds = component.getBounds();

      mPaint.setColor(0xFF000000);
      mPaint.setStrokeWidth(1.0f);
      mPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
      canvas.drawRoundRect(bounds, 5.0f, 5.0f, mPaint);
      mPaint.setPathEffect(null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      final int X = (int) event.getRawX();
      final int Y = (int) event.getRawY();
      switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
          RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) moveView.getLayoutParams();
          _xDelta = X - lParams.leftMargin;
          _yDelta = Y - lParams.topMargin;
          break;
        case MotionEvent.ACTION_UP:
          moveView.component.move(X - _xDelta, Y - _yDelta);

          components.add(moveView.component);
          layout.removeView(moveView);
          moveView = null;
          redraw();
          break;
        case MotionEvent.ACTION_POINTER_DOWN:
          break;
        case MotionEvent.ACTION_POINTER_UP:
          break;
        case MotionEvent.ACTION_MOVE:
          RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) moveView.getLayoutParams();
          layoutParams.leftMargin = X - _xDelta;
          layoutParams.topMargin = Y - _yDelta;
          layoutParams.rightMargin = -250;
          layoutParams.bottomMargin = -250;
          moveView.setLayoutParams(layoutParams);
          break;
      }
      return true;
    }
  };

  private static File writeToFile(final Context context, final Bitmap bitmap) throws IOException {
    File file
        = getFilename(context, "image.png");
    //  = File.createTempFile("image", ".png", context.getCacheDir());
    DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
    out.close();
    return file;
  }

  public static File getFilename(final Context context, final String filename) {
    File applicationPath = context.getExternalFilesDir(null);
    if (filename == null) {
      return new File(applicationPath,  File.separator);
    }
    return new File(applicationPath, File.separator + filename);
  }
}
