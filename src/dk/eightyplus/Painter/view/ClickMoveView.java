package dk.eightyplus.Painter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.lang.ref.SoftReference;

/**
 * Special button to move parent view or call click listener on self
 *
 * User: fries
 * Date: 17/07/14
 * Time: 12.42
 */
public class ClickMoveView extends View {

  @SuppressWarnings("unused")
  private static String TAG = ClickMoveView.class.toString();

  private SoftReference<View> parentSoftReference;
  private int _xDelta;
  private int _yDelta;
  private long touchDownTime;

  @SuppressWarnings("unused")
  public ClickMoveView(Context context) {
    super(context);
  }

  @SuppressWarnings("unused")
  public ClickMoveView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @SuppressWarnings("unused")
  public ClickMoveView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setParent(View parent) {
    this.parentSoftReference = new SoftReference<View>(parent);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    View parent = parentSoftReference.get();
    if (parent != null) {
      final int x = (int) event.getRawX();
      final int y = (int) event.getRawY();
      switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
          touchDownTime = System.currentTimeMillis();

          FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) parent.getLayoutParams();
          _xDelta = x - lParams.leftMargin;
          _yDelta = y - lParams.topMargin;
          break;
        case MotionEvent.ACTION_UP:
          long touchUpTime = System.currentTimeMillis();
          if (touchUpTime - touchDownTime < 100) {
            callOnClick();
          }
          break;
        case MotionEvent.ACTION_MOVE:
          FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) parent.getLayoutParams();
          layoutParams.leftMargin = x - _xDelta;
          layoutParams.topMargin = y - _yDelta;
          parent.setLayoutParams(layoutParams);
        default:
          break;
      }
      return true;
    } else {
      return super.onTouchEvent(event);
    }
  }
}
