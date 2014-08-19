package dk.eightyplus.annoto.action;

import android.view.View;
import dk.eightyplus.annoto.Callback;

import java.lang.ref.SoftReference;
import java.util.List;

/**
 * User: fries
 * Date: 23/03/14
 * Time: 22.06
 */
public class ActionBarClickListener implements View.OnClickListener {

  private final List<View> deselect;
  private final State clickAction;
  private final SoftReference<Callback> callback;

  public ActionBarClickListener(List<View> deselect, State clickAction, Callback callback) {
    this.deselect = deselect;
    this.clickAction = clickAction;
    this.callback = new SoftReference<Callback>(callback);
  }

  @Override
  public void onClick(View v) {
    Callback callback = this.callback.get();
    if (callback != null) {
      for (View view : deselect) {
        view.setSelected(false);
      }
      v.setSelected(true);
      callback.setState(clickAction);
    }
  }
}
