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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FingerPaint extends GraphicsActivity implements ColorPickerDialog.OnColorChangedListener {

  private final List<Graphic> pathList = new ArrayList<Graphic>();
  private final List<Integer> colorList = new ArrayList<Integer>();


  private static final String TAG = FingerPaint.class.toString();
  private FingerPaint.MyView view;

  private MoveView moveView;
  private RelativeLayout layout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    layout = new RelativeLayout(getApplicationContext());
    view = new MyView(getApplicationContext());
    setContentView(layout);
    layout.addView(view);

    pathList.add(new Text());
    colorList.add(0xFFFFFF00);

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


  private Paint       mPaint;
  private MaskFilter  mEmboss;
  private MaskFilter  mBlur;

  private Bitmap createImageFromText(final String text, final Rect bounds, final float fontSize) {
    final Paint textPaint = new Paint() {
      {
        setColor(0xFF00FF00);
        setTextAlign(Paint.Align.LEFT);
        setTypeface(Typeface.create("HelveticaNeue", Typeface.BOLD));
        setTextSize(fontSize);
        setAntiAlias(true);
      }
    };

    //use ALPHA_8 (instead of ARGB_8888) to get text mask
    final Bitmap bmp = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
    final Canvas canvas = new Canvas(bmp);
    canvas.drawText(text, 0, bounds.height(), textPaint);
    return bmp;
  }

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
      int color = this.color != 0 ? this.color : (int) (0xFFAAAAAA * Math.random());
      mPaint.setColor(color);
      colorList.add(color);

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

      pathList.add(new Polygon(mPath));

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

    void drawPath(int color, Graphic path) {
      mPaint.setColor(color);
      path.onDraw(mCanvas, mPaint);
      invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      float x = event.getX();
      float y = event.getY();

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
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
    //menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
    //menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
    //menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
    //menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');

    menu.add(0, SHARE_MENU_ID, 0, "Share");
    menu.add(0, REDRAW_MENU_ID, 0, "Redraw");
    menu.add(0, MOVE_MENU_ID, 0, "Move last");
    /****   Is this the mechanism to extend with filter effects?
     Intent intent = new Intent(null, getIntent().getData());
     intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
     menu.addIntentOptions(
     Menu.ALTERNATIVE, 0,
     new ComponentName(this, NotesList.class),
     null, intent, 0, null);
     *****/
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
        try {
          File file = writeToFile(getApplicationContext(), view.mBitmap );

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

      case REDRAW_MENU_ID:
        redraw(50, true);

        return true;

      case MOVE_MENU_ID:
        Graphic path = getLastPath();
        int lastIndex = colorList.size() - 1;
        int color = colorList.get(lastIndex);
        colorList.remove(lastIndex);

        moveView = new MoveView(getApplicationContext(), path, color);
        layout.addView(moveView);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private Graphic getLastPath() {
    Graphic path = pathList.get(pathList.size() - 1 );
    pathList.remove(path);
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
        for (int i = 0; i < pathList.size(); i++) {
          final Graphic path = pathList.get(i);
          int pathColor = colorList.get(i);

          if (delay > 0) {
            try {
              Thread.sleep(delay);
            } catch (InterruptedException e) {

            }
          }
          final int color = randomColor ? (int) (0xFFAAAAAA * Math.random()) : pathColor;
          if (randomColor) colorList.set(i, color);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              view.drawPath(color, path);
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

    /*new Thread(new Runnable() {
      @Override
      public void run() {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            for (int i = 0; i < pathList.size(); i++) {
              final Path path = pathList.get(i);
              final int color = colorList.get(i);

              RectF bounds = new RectF();
              path.computeBounds(bounds, false);
              view.drawPath(color, path);
            }
          }
        });
      }
    }).start();*/
  }

  @Override
  public void onBackPressed() {
    if (pathList.size() > 0) {
      getLastPath();
    } else {
      super.onBackPressed();
    }
  }



  private class MoveView extends View {



    private int _xDelta;
    private int _yDelta;
    private Bitmap mBitmap;
    private Graphic graphic;
    private Paint   mBitmapPaint;
    private Paint mPaint;
    private int color;

    public MoveView(Context context, Graphic graphic, int color) {
      super(context);
      this.graphic = graphic;
      this.color = color;


      mPaint = new Paint();
      mPaint.setAntiAlias(true);
      mPaint.setDither(true);
      mPaint.setColor(color);
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
      mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
      graphic.onDraw(canvas, mPaint);
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
          moveView.graphic.move(X - _xDelta, Y - _yDelta);

          pathList.add(moveView.graphic);
          colorList.add(moveView.color);
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
