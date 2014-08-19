package dk.eightyplus.Painter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * User: fries
 * Date: 19/08/14
 * Time: 08.49
 */
public class MovableLinearLayout extends LinearLayout {
  private int _xDelta;
  private int _yDelta;

  @SuppressWarnings("unused")
  public MovableLinearLayout(Context context) {
    super(context);
  }

  @SuppressWarnings("unused")
  public MovableLinearLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @SuppressWarnings("unused")
  public MovableLinearLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    final int x = (int) event.getRawX();
    final int y = (int) event.getRawY();
    switch (event.getAction() & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:

        FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) getLayoutParams();
        _xDelta = x - lParams.leftMargin;
        _yDelta = y - lParams.topMargin;
        break;
      case MotionEvent.ACTION_UP:
        break;
      case MotionEvent.ACTION_MOVE:
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.leftMargin = x - _xDelta;
        layoutParams.topMargin = y - _yDelta;

        if (layoutParams.leftMargin < 0) {
          layoutParams.leftMargin = 0;
        }
        if (layoutParams.topMargin < 0) {
          layoutParams.topMargin = 0;
        }
        setLayoutParams(layoutParams);
      default:
        break;
    }
    return true;
  }
}
