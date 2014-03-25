package dk.eightyplus.Painter.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import dk.eightyplus.Painter.Callback;
import dk.eightyplus.Painter.component.Component;
import dk.eightyplus.Painter.utilities.Compatibility;

/**
 * MoveView to put a single component into and show moves by user
 */
public class MoveView extends View {
  @SuppressWarnings("unused")
  private static final String TAG = MoveView.class.toString();

  private int _xDelta;
  private int _yDelta;
  private Component component;
  private Paint mPaint;

  private Callback callBack;

  private float xOffsetComponent;
  private float yOffsetComponent;
  private final int margin = 20;

  private final float initialScale;
  private float scaleFactor = 1.0f;
  private float width;
  private float height;
  private float oldDist;
  private int points = 1;

  public MoveView(final Context context, final Component component, final Callback callBack) {
    super(context);
    this.component = component;
    this.initialScale = component.getScale();
    this.callBack = callBack;

    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setDither(true);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth(12);
    Compatibility.get().setHardwareAccelerated(this, mPaint);

    setupSize();
  }

  public void destroy() {
    callBack = null;
    mPaint = null;
    component = null;
  }

  private void setupSize() {
    RectF bounds = component.getBounds();
    xOffsetComponent = bounds.left - margin;
    yOffsetComponent = bounds.top - margin;

    width = bounds.width() + 2 * margin;
    height = bounds.height() + 2 * margin;
    setViewBounds();
  }

  private void moveComponentFromMoveView(float dx, float dy) {
    callBack.move(component, dx - xOffsetComponent, dy - yOffsetComponent, initialScale * scaleFactor);
  }

  private void setViewBounds() {
    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)width, (int)height);
    layoutParams.leftMargin = (int) xOffsetComponent;
    layoutParams.topMargin = (int) yOffsetComponent;
    setLayoutParams(layoutParams);
  }

  /*
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
  }*/

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.drawColor(0x66FF00FF);

    canvas.save();
    canvas.scale(scaleFactor, scaleFactor);
    canvas.translate( -xOffsetComponent, -yOffsetComponent);
    component.setVisible(true);
    component.onDraw(canvas, mPaint);
    component.setVisible(false);
    canvas.restore();

    drawDashBounds(canvas);
  }

  private void drawDashBounds(Canvas canvas) {
    RectF rect = new RectF(0, 0, getWidth() - 1, getHeight() - 1);

    mPaint.setColor(0xFF000000);
    mPaint.setStrokeWidth(1.0f);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
    canvas.drawRoundRect(rect, 5.0f, 5.0f, mPaint);
    mPaint.setPathEffect(null);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    final int X = (int) event.getRawX();
    final int Y = (int) event.getRawY();
    switch (event.getAction() & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:
        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) getLayoutParams();
        _xDelta = X - lParams.leftMargin;
        _yDelta = Y - lParams.topMargin;
        break;
      case MotionEvent.ACTION_UP:
        float dx = X - _xDelta;
        float dy = Y - _yDelta;
        moveComponentFromMoveView(dx, dy);
        break;
      case MotionEvent.ACTION_POINTER_DOWN:
        break;
      case MotionEvent.ACTION_POINTER_UP:
        break;
      case MotionEvent.ACTION_MOVE:

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();

        if (points != event.getPointerCount()) {
          points = event.getPointerCount();
          Log.d(TAG, "MotionEvent.ACTION_MOVE :" + points);

          if (points == 1) {
            _xDelta = X - layoutParams.leftMargin;
            _yDelta = Y - layoutParams.topMargin;
          } else {
            oldDist = calculateDistance(event);
          }
        }

        layoutParams.leftMargin = X - _xDelta;
        layoutParams.topMargin = Y - _yDelta;

        if (points > 1) {
          scaleFactor = calculateDistance(event) / oldDist;
          scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
          layoutParams.width = (int) (width * scaleFactor);
          layoutParams.height = (int) (height * scaleFactor);
        }

        setLayoutParams(layoutParams);
        break;
    }
    return true;
  }

  private float calculateDistance(MotionEvent event) {
    final float x = event.getX(0) - event.getX(1);
    final float y = event.getY(0) - event.getY(1);
    return FloatMath.sqrt(x * x + y * y);
  }
}

