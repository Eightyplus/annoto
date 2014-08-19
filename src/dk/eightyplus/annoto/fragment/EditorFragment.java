package dk.eightyplus.annoto.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import dk.eightyplus.annoto.Callback;
import dk.eightyplus.annoto.R;
import dk.eightyplus.annoto.action.State;
import dk.eightyplus.annoto.action.Undo;
import dk.eightyplus.annoto.component.Component;
import dk.eightyplus.annoto.component.Text;
import dk.eightyplus.annoto.view.EditTextCustom;

/**
 * EditorFragment for presenting edit text
 */
public class EditorFragment extends DialogFragment {
  @SuppressWarnings("unused")
  private static final String TAG = EditorFragment.class.toString();

  private final float x;
  private final float y;
  private Text component;
  private Callback callback;
  private EditTextCustom editText;

  public EditorFragment(final Callback callback, Component component, float x, float y) {
    this.callback = callback;
    this.component = (component instanceof Text) ? (Text) component : null;
    this.x = x;
    this.y = y;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.editor, container, false);

    editText = (EditTextCustom) view.findViewById(R.id.edit);
    editText.setOnKeyBoardDownListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        callback.textEditDone();
        return true;
      }
    });

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

    showKeyboard();
    return view;
  }

  private void showKeyboard() {
    new Handler().post(new Runnable() {
      @Override
      public void run() {
        if (editText.requestFocus()) {
          InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
      }
    });
  }

  public Pair<Text, Undo> getTextChanges() {
    Undo undo = null;
    String text = editText.getText().toString();
    if (text.length() > 0) {
      if (component == null) {
        component = new Text();
        component.move(x, y);
        undo = new Undo(component, State.Add);
      } else {
        undo = new Undo(component, component.getText(), State.Text);
      }
      component.setText(text);
    }
    return new Pair<Text, Undo>(component, undo);
  }
}
