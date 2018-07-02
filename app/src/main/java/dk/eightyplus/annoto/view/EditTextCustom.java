package dk.eightyplus.annoto.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * User: fries
 * Date: 20/03/14
 * Time: 22.44
 */
public class EditTextCustom extends EditText {

  private OnKeyListener keyListener;

  @SuppressWarnings("unused")
  public EditTextCustom(Context context) {
    super(context);
  }

  @SuppressWarnings("unused")
  public EditTextCustom(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @SuppressWarnings("unused")
  public EditTextCustom(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setOnKeyBoardDownListener(OnKeyListener keyListener) {
    this.keyListener = keyListener;
  }

  @Override
  public boolean onKeyPreIme(int keyCode, KeyEvent event) {
    if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK) {
      if (keyListener != null && keyListener.onKey(this, keyCode, event)) {
        return true;
      }
    }
    return super.dispatchKeyEvent(event);
  }
}
