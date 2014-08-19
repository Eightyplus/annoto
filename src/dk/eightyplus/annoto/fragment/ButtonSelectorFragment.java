package dk.eightyplus.annoto.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import dk.eightyplus.annoto.Callback;
import dk.eightyplus.annoto.Keys;
import dk.eightyplus.annoto.R;
import dk.eightyplus.annoto.action.State;
import dk.eightyplus.annoto.view.ClickMoveView;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * User: fries
 * Date: 17/07/14
 * Time: 07.46
 */
public class ButtonSelectorFragment extends DialogFragment {

  private Callback callback;
  private boolean unfold = false;

  private int[] icons;
  private String[] tags;

  @SuppressWarnings("unused")
  public ButtonSelectorFragment() { }

  public ButtonSelectorFragment(int[] icons, String[] tags) {
    this.icons = icons;
    this.tags = tags;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putInt(Keys.LENGTH, icons.length);
    for (int i = 0; i < icons.length; i++) {
      outState.putInt(Keys.ICON + i, icons[i]);
      outState.putString(Keys.TAG + i, tags[i]);
    }
    outState.putBoolean(Keys.STATE, unfold);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    callback = (Callback) getActivity();

    if (savedInstanceState != null) {
      int length = savedInstanceState.getInt(Keys.LENGTH);
      if (length > 0) {
        icons = new int[length];
        tags = new String[length];
        for (int i = 0; i < length; i++) {
          icons[i] = savedInstanceState.getInt(Keys.ICON + i);
          tags[i] = savedInstanceState.getString(Keys.TAG + i);
        }
      }

      unfold = savedInstanceState.getBoolean(Keys.STATE, false);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.button_menu_layout, container, false);
    final ViewGroup dynamicButtons = (ViewGroup) view.findViewById(R.id.dynamic_buttons);

    List<View> buttons = new ArrayList<View>();

    for (int i = 0; i < icons.length; i++) {
      int icon = icons[i];
      String tag = tags[i];

      View buttonsView = inflater.inflate(R.layout.button_item, dynamicButtons, false);
      buttonsView.findViewById(R.id.icon).setBackgroundResource(icon);

      View button = buttonsView.findViewById(R.id.button);
      button.setTag(tag);
      buttons.add(button);
      dynamicButtons.addView(buttonsView);

      if (i == 0) {
        button.setSelected(true);
      }
    }

    for (View button : buttons) {
      button.setOnClickListener(new ToggleButtonClickListener(callback, buttons, (String) button.getTag()));
    }

    final ClickMoveView buttonFold = (ClickMoveView) view.findViewById(R.id.button_fold);
    final ClickMoveView buttonUnfold = (ClickMoveView) view.findViewById(R.id.button_unfold);
    buttonFold.setParent(view);
    buttonUnfold.setParent(view);

    buttonUnfold.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        buttonFold.setVisibility(View.VISIBLE);
        buttonUnfold.setVisibility(View.GONE);
        dynamicButtons.setVisibility(View.VISIBLE);
        unfold = true;
      }
    });

    buttonFold.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        buttonFold.setVisibility(View.GONE);
        buttonUnfold.setVisibility(View.VISIBLE);
        dynamicButtons.setVisibility(View.GONE);
        unfold = false;
      }
    });

    return view;
  }

  /*
  @Override
  public void onResume() {
    super.onResume();
    //unfold(unfold);
    if (unfold) {
      buttonFold.setVisibility(View.VISIBLE);
      buttonUnfold.setVisibility(View.GONE);
      dynamicButtons.setVisibility(View.VISIBLE);
    }
  }

  private void unfold(boolean unfold) {
      buttonFold.setVisibility(unfold ? View.GONE : View.VISIBLE);
      buttonUnfold.setVisibility(unfold ? View.VISIBLE : View.GONE);
      dynamicButtons.setVisibility(unfold ? View.GONE : View.VISIBLE);
    this.unfold = unfold;
  }*/

  private static class ToggleButtonClickListener implements View.OnClickListener {

    private final String tag;
    private SoftReference<Callback> callback;
    private List<View> deselect;

    public ToggleButtonClickListener(Callback callback, List<View> deselect, String tag) {
      this.callback = new SoftReference<Callback>(callback);
      this.deselect = deselect;
      this.tag = tag;
    }

    @Override
    public void onClick(View v) {
      Callback callback = this.callback.get();
      if (callback != null) {
        for (View view : deselect) {
          view.setSelected(false);
        }
        v.setSelected(true);
        callback.setState(State.state(tag));
      }
    }
  }
}
