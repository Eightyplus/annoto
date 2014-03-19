package dk.eightyplus.Painter.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import dk.eightyplus.Painter.Callback;
import dk.eightyplus.Painter.utilities.Compatibility;
import dk.eightyplus.Painter.component.Component;

/**
 * MoveView to put a single component into and show moves by user
 */
public class MoveView extends View {
  private int _xDelta;
  private int _yDelta;
  private Bitmap mBitmap;
  private Component component;
  private Paint mBitmapPaint;
  private Paint mPaint;

  private Callback callBack;

  private float xOffset;
  private float yOffset;
  private int margin = 10;

  public MoveView(final Context context, final Component component, final Callback callBack) {
    super(context);
    this.component = component;
    this.callBack = callBack;

    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setDither(true);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth(12);
    Compatibility.get().setHardwareAccelerated(this, mPaint);
    mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    createBitmap();
  }

  public void destroy() {
    callBack = null;
    mBitmap.recycle();
    mBitmap = null;
    mPaint = null;
    component = null;
  }

  private void createBitmap() {
    RectF bounds = component.getBounds();
    xOffset = bounds.left - margin;
    yOffset = bounds.top - margin;

    int width = (int) (bounds.width() + 2 * margin);
    int height = (int) (bounds.height() + 2 * margin);
    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
    layoutParams.leftMargin = (int) xOffset;
    layoutParams.topMargin = (int) yOffset;

    setLayoutParams(layoutParams);
    mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
  }

  /*
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
  }*/

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

    RectF bounds = component.getBounds();
    component.move(-xOffset, -yOffset);
    component.setVisible(true);

    component.onDraw(canvas, mPaint);

    component.move(xOffset, yOffset);
    component.setVisible(false);

    RectF rect = new RectF(0, 0, bounds.width() + 2 * margin - 1, bounds.height() + 2 * margin - 1);

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
        callBack.move(component, dx - xOffset, dy - yOffset);
        break;
      case MotionEvent.ACTION_POINTER_DOWN:
        break;
      case MotionEvent.ACTION_POINTER_UP:
        break;
      case MotionEvent.ACTION_MOVE:
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        layoutParams.leftMargin = X - _xDelta;
        layoutParams.topMargin = Y - _yDelta;
        layoutParams.rightMargin = -250;
        layoutParams.bottomMargin = -250;
        setLayoutParams(layoutParams);
        break;
    }
    return true;
  }
};