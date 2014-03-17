package dk.eightyplus.Painter.view;

import android.content.Context;
import android.graphics.*;
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

  public MoveView(final Context context, final Component component, final Callback callBack) {
    super(context);
    this.component = component;
    this.callBack = callBack;

    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setDither(true);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth(12);
    Compatibility.get().setHardwareAccelerated(this, mPaint);
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
        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) getLayoutParams();
        _xDelta = X - lParams.leftMargin;
        _yDelta = Y - lParams.topMargin;
        break;
      case MotionEvent.ACTION_UP:
        float dx = X - _xDelta;
        float dy = Y - _yDelta;
        callBack.move(component, dx, dy);
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