package dk.eightyplus.Painter.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import dk.eightyplus.Painter.Callback;
import dk.eightyplus.Painter.Tags;
import dk.eightyplus.Painter.action.State;
import dk.eightyplus.Painter.action.Undo;
import dk.eightyplus.Painter.component.Component;
import dk.eightyplus.Painter.component.Composite;
import dk.eightyplus.Painter.component.Polygon;
import dk.eightyplus.Painter.utilities.Compatibility;
import dk.eightyplus.Painter.utilities.NoteStorage;
import dk.eightyplus.Painter.utilities.SaveLoad;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DrawingView extends View implements ComponentList, SaveLoad {

  @SuppressWarnings("unused")
  private static final String TAG = DrawingView.class.toString();
  private static final String COMPONENT = "COMPONTENT_";

  private final List<Component> components = new ArrayList<Component>();

  private final Callback callback;
  private Paint mPaint;

  private Bitmap mBitmap;
  private Canvas mCanvas;

  private Component drawingComponent;
  private Composite composite;
  private Polygon polygon;

  private Paint   mBitmapPaint;

  private int drawingColor = 0xFF000000;
  private int color = 0xFF000000;

  private float mX, mY;
  private static final float TOUCH_TOLERANCE = 4;

  private static final float STROKE_DELTA = 0.001f;
  private static final float STROKE_INCREMENT = 0.1f;
  private float currentStrokeModify = 1.0f;
  private int strokeWidth = 6;

  boolean variableWidth = true; // TODO make configurable
  boolean redrawing = false;

  public DrawingView(final Context context, Callback callback) {
    super(context);

    this.callback = callback;

    getSavedStrokeWidth();

    touch_reset();
    mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setDither(true);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);

    Compatibility.get().setHardwareAccelerated(this, mPaint);
  }

  public void onRestoreInstanceState(Bundle savedInstanceState) {
    components.clear();
    int i = 0;
    Component component;
    while ((component = (Component) savedInstanceState.get(COMPONENT + i++)) != null) {
      components.add(component);
    }
  }

  public void onSaveInstanceState(Bundle bundle) {
    for (int i = 0; i < components.size(); i++) {
      Component component = components.get(i);
      bundle.putSerializable(COMPONENT + i, component);
    }
  }

  public void save(Context context, final DataOutputStream outputStream) throws IOException {
    final List<Component> components = new ArrayList<Component>(this.components);
    NoteStorage.save(context, components, outputStream);
  }

  public void load(final Context context, final DataInputStream inputStream) throws IOException, ClassNotFoundException {
    List<Component> components = new ArrayList<Component>();
    NoteStorage.load(context, components, inputStream);

    this.components.clear();
    this.components.addAll(components);
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

    drawingComponent.onDraw(canvas, mPaint);
  }

  private void touch_start(float x, float y, float pressure) {
    setDrawingColor();

    polygon.setColor(getDrawingColor());
    currentStrokeModify = pressure;
    polygon.getPath().reset();
    polygon.setStrokeWidth(getStrokeWidth(pressure));
    polygon.getPath().moveTo(x, y);
    mX = x;
    mY = y;
  }

  private void touch_move(float x, float y, float pressure) {
    float dx = Math.abs(x - mX);
    float dy = Math.abs(y - mY);
    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
      polygon.setStrokeWidth(getStrokeWidth(pressure));

      float xEndPoint = (x + mX) / 2;
      float yEndPoint = (y + mY) / 2;
      polygon.getPath().quadTo(mX, mY, xEndPoint, yEndPoint);

      if (variableWidth) {
        composite.add(polygon);
        polygon = new Polygon();
        polygon.getPath().moveTo(xEndPoint, yEndPoint);
        polygon.setColor(getDrawingColor());
      }
      mX = x;
      mY = y;
    }
  }

  private void touch_up() {
    polygon.getPath().lineTo(mX, mY);
    drawingComponent.onDraw(mCanvas, mPaint);

    components.add(drawingComponent);
    callback.add(new Undo(drawingComponent, State.DrawPath));

    touch_reset();
  }

  private void touch_reset() {
    currentStrokeModify = 0.5f;

    polygon = new Polygon();
    polygon.setStrokeWidth(strokeWidth);

    if (variableWidth) {
      composite = new Composite();
      drawingComponent = composite;
    } else {
      drawingComponent = polygon;
    }
  }

  private void clear() {
    mBitmap.recycle();
    int w = mBitmap.getWidth();
    int h = mBitmap.getHeight();

    mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    mCanvas = new Canvas(mBitmap);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();
    float p = event.getPressure();

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        touch_start(x, y, p);
        invalidate();
        break;
      case MotionEvent.ACTION_MOVE:
        touch_move(x, y, p);
        invalidate();
        break;
      case MotionEvent.ACTION_UP:
        touch_up();
        invalidate();
        break;
    }

    return true;
  }

  public void add(Component component) {
    if (component != null && !components.contains(component)) {
      components.add(component);
    }
  }

  public void remove(Component component) {
    components.remove(component);
  }

  public Component findComponent(float x, float y) {
    Component moveComponent = null;
    float minimumDistance = Float.MAX_VALUE;
    for (Component component : components) {
      float distance = component.centerDist(x, y);
      if (distance < minimumDistance) {
        moveComponent = component;
        minimumDistance = distance;
      }
    }
    return moveComponent;
  }

  public int getDrawingColor() {
    return drawingColor;
  }

  private void setDrawingColor() {
    drawingColor = this.color != 0 ? this.color : (int) (0xFF000000 + 0x00FFFFFF * Math.random());
  }

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
  }

  public void reinitialise(boolean delete, boolean invalidate) {
    clear();
    delete(delete);
    if (invalidate) {
        invalidate();
    }
  }

  public void delete(boolean delete) {
    if (delete) {
      for (Component component : components) {
        component.delete();
      }
    }
    components.clear();
  }

  public void redraw() {
    if (redrawing) {
      return;
    }
    redrawing = true;
    clear();
    getHandler().post(new Runnable() {
      @Override
      public void run() {
        for (Component component : components) {
          component.onDraw(mCanvas, mPaint);
        }

        redrawing = false;
        invalidate();
      }
    });
  }

  public void redraw(final int delay, final boolean randomColor) {
    clear();

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
          final int color = randomColor ? (int) ((0xFF000000 + 0x00FFFFFF * Math.random())) : component.getColor();
          component.setColor(color);
          getHandler().post(new Runnable() {
            @Override
            public void run() {
              component.onDraw(mCanvas, mPaint);
              invalidate();
            }
          });
        }
      }
    }).start();
  }

  public void move(Component component, float dx, float dy, float scale) {
    RectF bounds = component.getBounds();
    callback.add(new Undo(component, bounds.left, bounds.top,component.getScale(), State.Move));
    component.setScale(scale);
    component.move(dx, dy);

    if (!components.contains(component)) {
      components.add(component);
    }
    redraw();
  }

  public Bitmap getBitmap() {
    return mBitmap;
  }

  private float getStrokeWidth(float eventPressure) {
    if (variableWidth) {
      if( Math.abs(eventPressure - currentStrokeModify) > STROKE_DELTA ) {
        if(eventPressure > currentStrokeModify) {
          currentStrokeModify = Math.min(eventPressure, currentStrokeModify + STROKE_INCREMENT);
        } else {
          currentStrokeModify = Math.max(eventPressure, currentStrokeModify - STROKE_INCREMENT);
        }
      }
    }
    return strokeWidth * currentStrokeModify;
  }

  public void setStrokeWidth(int strokeWidth) {
    this.strokeWidth = strokeWidth;
    saveStrokeWidth(strokeWidth);
  }

  public int getStrokeWidth() {
    return strokeWidth;
  }

  private void saveStrokeWidth(int strokeWidth) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    SharedPreferences.Editor editor = preferences.edit();
    editor.putInt(Tags.STROKE_WIDTH, strokeWidth);
    editor.commit();
  }

  private void getSavedStrokeWidth() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    strokeWidth = preferences.getInt(Tags.STROKE_WIDTH, strokeWidth);
  }
}
