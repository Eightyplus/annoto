package dk.eightyplus.Painter.view;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import dk.eightyplus.Painter.Callback;
import dk.eightyplus.Painter.action.State;
import dk.eightyplus.Painter.component.Component;
import dk.eightyplus.Painter.component.Polygon;
import dk.eightyplus.Painter.action.Undo;

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
  private Polygon mPath;
  private Paint   mBitmapPaint;
  private int color = 0;

  private float mX, mY;
  private static final float TOUCH_TOLERANCE = 4;
  private int strokeWidth = 12;

  public DrawingView(final Context context, Callback callback) {
    super(context);

    this.callback = callback;

    mPath = new Polygon();
    mPath.setStrokeWidth(strokeWidth);
    mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setDither(true);
    mPaint.setColor(0xFFFF0000);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);

    setLayerType(LAYER_TYPE_HARDWARE, mPaint); // TODO API level 11
  }

  public void onRestoreInstanceState(Bundle savedInstanceState) {
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

  private void touch_start(float x, float y) {
    int color = this.color != 0 ? this.color : (int) (0xFF000000 + 0x00FFFFFF * Math.random());
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

    Component component = new Polygon(mPath);
    components.add(component);
    component.setStrokeWidth(strokeWidth);
    RectF bounds = component.getBounds();
    undo.add(new Undo(component, State.DrawPath));

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
    mPaint.setStrokeWidth(strokeWidth);
    this.strokeWidth = strokeWidth;
  }
}
