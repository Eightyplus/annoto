package dk.eightyplus.Painter.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import dk.eightyplus.Painter.Callback;
import dk.eightyplus.Painter.utilities.Compatibility;
import dk.eightyplus.Painter.action.State;
import dk.eightyplus.Painter.action.Undo;
import dk.eightyplus.Painter.component.Component;
import dk.eightyplus.Painter.component.Polygon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 */
public class DrawingView extends View {

  private final List<Component> components = new ArrayList<Component>();
  private final Stack<Undo> undo = new Stack<Undo>();
  private final Stack<Undo> redo = new Stack<Undo>();

  private final Callback callback;
  private Paint mPaint;

  private Bitmap mBitmap;
  private Canvas mCanvas;
  private Polygon polygon;
  private Paint   mBitmapPaint;
  private int color = 0;

  private float mX, mY;
  private static final float TOUCH_TOLERANCE = 4;
  private int strokeWidth = 8;

  public DrawingView(final Context context, Callback callback) {
    super(context);

    this.callback = callback;

    getSavedStrokeWidth();

    polygon = new Polygon();
    polygon.setStrokeWidth(strokeWidth);
    mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setDither(true);
    mPaint.setColor(0xFFFF0000);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);

    Compatibility.get().setHardwareAccelerated(this, mPaint);
  }

  public void onRestoreInstanceState(Bundle savedInstanceState) {
    components.clear();
    int i = 0;
    Component component;
    while ((component = (Component) savedInstanceState.get("COMPONTENT_" + i++)) != null) {
      components.add(component);
    }
  }

  public void onSaveInstanceState(Bundle bundle) {
    for (int i = 0; i < components.size(); i++) {
      Component component = components.get(i);
      bundle.putSerializable("COMPONTENT_" + i, component);
    }
  }

  public void save(final ObjectOutputStream outputStream) throws IOException {
    outputStream.writeInt(components.size());

    for (int i = 0; i < components.size(); i++) {
      Component component = components.get(i);
      outputStream.writeObject(component);
    }
  }

  public void load(final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
    components.clear();
    int size = inputStream.readInt();
    for (int i = 0 ; i < size; i++) {
      Component component = (Component) inputStream.readObject();
      components.add(component);
    }
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

    polygon.onDraw(canvas, mPaint);
  }

  private void touch_start(float x, float y) {
    int color = this.color != 0 ? this.color : (int) (0xFF000000 + 0x00FFFFFF * Math.random());
    mPaint.setColor(color);

    polygon.setColor(color);
    polygon.getPath().reset();
    polygon.getPath().moveTo(x, y);
    mX = x;
    mY = y;
  }

  private void touch_move(float x, float y) {
    float dx = Math.abs(x - mX);
    float dy = Math.abs(y - mY);
    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
      polygon.getPath().quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
      mX = x;
      mY = y;
    }
  }

  private void touch_up() {
    polygon.getPath().lineTo(mX, mY);
    polygon.onDraw(mCanvas, mPaint);

    components.add(polygon);
    undo.add(new Undo(polygon, State.DrawPath));

    polygon = new Polygon();
    polygon.setStrokeWidth(strokeWidth);
  }

  private void clear() {
    int w = mBitmap.getWidth();
    int h = mBitmap.getHeight();

    mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    mCanvas = new Canvas(mBitmap);
  }

  private void drawComponent(int color, final Component path) {
    mPaint.setColor(color);
    path.onDraw(mCanvas, mPaint);
    invalidate();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();

    switch (callback.getState()) {
      case Delete: {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          Component deleteComponent = findComponent(x, y);

          if (deleteComponent != null) {
            components.remove(deleteComponent);
            undo.add(new Undo(deleteComponent, State.Delete));
            redraw();
          }
        }
      }

      break;
      case Move: {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          Component moveComponent = findComponent(x, y);

          if (moveComponent != null) {
            components.remove(moveComponent);
            redraw();
            callback.startMove(moveComponent);
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

  private Component findComponent(float x, float y) {
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

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
  }

  public boolean undo() {
    if (undo.size() > 0) {
      Undo undo = this.undo.pop();
      if (undo.undo(components)) {
        redo.add(undo);
        redraw();
        return true;
      }
    }
    return false;
  }

  public boolean redo() {
    if (redo.size() > 0) {
      Undo redo = this.redo.pop();
      if (redo.redo(components)) {
        undo.add(redo);
        redraw();
        return true;
      }
    }
    return false;
  }

  public void redraw() {
    redraw(0, false);
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
              drawComponent(color, component);
            }
          });
        }
      }
    }).start();

    getHandler().post(new Runnable() {
      @Override
      public void run() {
        invalidate();
      }
    });
  }

  public void move(Component component, float dx, float dy) {
    RectF bounds = component.getBounds();
    undo.add(new Undo(component, bounds.left, bounds.top, State.Move));
    component.move(dx, dy);
    components.add(component);
    redraw();
  }

  public Bitmap getBitmap() {
    return mBitmap;
  }

  public void setStrokeWidth(int strokeWidth) {
    this.strokeWidth = strokeWidth;
    polygon.setStrokeWidth(strokeWidth);
    saveStrokeWidth(strokeWidth);
  }

  public int getStrokeWidth() {
    return strokeWidth;
  }

  private void saveStrokeWidth(int strokeWidth) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    SharedPreferences.Editor editor = preferences.edit();
    editor.putInt("STROKE_WIDTH", strokeWidth);
    editor.commit();
  }

  private void getSavedStrokeWidth() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    strokeWidth = preferences.getInt("STROKE_WIDTH", strokeWidth);
  }
}
