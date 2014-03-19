package dk.eightyplus.Painter.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import dk.eightyplus.Painter.Callback;
import dk.eightyplus.Painter.R;
import dk.eightyplus.Painter.component.Component;
import dk.eightyplus.Painter.component.Text;

/**
 * 
 */
public class EditorFragment extends DialogFragment {

  private static final String TAG = EditorFragment.class.toString();
  private final float x;
  private final float y;
  private Text component;
  private Callback callback;
  private EditText editText;

  public EditorFragment(final Callback callback, Component component, float x, float y) {
    this.callback = callback;
    this.component = (component instanceof Text) ? (Text) component : null;
    this.x = x;
    this.y = y;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.editor, container, false);

    editText = (EditText) view.findViewById(R.id.edit);

    if (component != null) {
      editText.setText(component.getText());
    }

    editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        callback.textEditDone();
        return false;
      }
    });

    return view;
  }

  public Text getText() {
    String text = editText.getText().toString();
    if (text.length() > 0) {
      if (component == null) {
        component = new Text();
        component.move(x, y);
      }
      component.setText(text);
    }
    return component;
  }
}
