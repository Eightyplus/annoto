package dk.eightyplus.Painter.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import dk.eightyplus.Painter.Callback;
import dk.eightyplus.Painter.Keys;
import dk.eightyplus.Painter.R;
import dk.eightyplus.Painter.dialog.ColorPickerDialog;
import dk.eightyplus.Painter.dialog.ColorPickerView;

import java.lang.ref.SoftReference;

/**
 * User: fries
 * Date: 15/07/14
 * Time: 14.00
 */
public class ColorPaletteFragment extends DialogFragment {

  private SoftReference<Callback> callbackSoftReference;
  private LinearLayout linearLayoutDynamic;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.callbackSoftReference = new SoftReference<Callback>((Callback) getActivity());
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.color_palette, container, false);

    Callback callback = callbackSoftReference.get();

    LinearLayout linearLayoutTop = (LinearLayout) view.findViewById(R.id.color_top);
    LinearLayout blackWhite = (LinearLayout) view.findViewById(R.id.color_static1);
    LinearLayout grayScale = (LinearLayout) view.findViewById(R.id.color_static2);
    LinearLayout rainbow = (LinearLayout) view.findViewById(R.id.color_rainbow);
    linearLayoutDynamic = (LinearLayout) view.findViewById(R.id.color_dynamic);

    int[] bwColors = new int[]{R.color.black, R.color.white};
    for (int color : bwColors) {
      final int hexColor = getActivity().getResources().getColor(color);

      ImageButton button = (ImageButton) inflater.inflate(R.layout.color_button, blackWhite, false);
      button.setBackgroundColor(hexColor);
      blackWhite.addView(button);

      button.setOnClickListener(new ButtonColorClickListener(callback, hexColor));
    }
    ImageButton randomButton = (ImageButton) inflater.inflate(R.layout.color_button, blackWhite, false);
    randomButton.setBackgroundResource(android.R.drawable.ic_menu_help);
    randomButton.setOnClickListener(new ButtonColorClickListener(callback, 0));
    blackWhite.addView(randomButton);

    int[] grayColors = new int[]{R.color.dark_grey, R.color.gray, R.color.light_grey};
    for (int color : grayColors) {
      final int hexColor = getActivity().getResources().getColor(color);

      ImageButton button = (ImageButton) inflater.inflate(R.layout.color_button, grayScale, false);
      button.setBackgroundColor(hexColor);
      grayScale.addView(button);

      button.setOnClickListener(new ButtonColorClickListener(callback, hexColor));
    }

    ColorPickerView colorPickerView = new ColorPickerView(inflater.getContext(), new ColorPickerDialog.OnColorChangedListener() {
      private int lastColor = 0;

      @Override
      public void colorChanged(int color) {
        Callback callback = callbackSoftReference.get();
        if (callback != null) {
          callback.colorChanged(color);
        }

        if (lastColor != color) {
          lastColor = color;

          final SharedPreferences preferences = getActivity().getSharedPreferences(Keys.PREFERENCES, Context.MODE_PRIVATE);
          int lastColorSlot = preferences.getInt(Keys.COLOR_LAST, -1);

          lastColorSlot++;
          if (lastColorSlot > 5) {
            lastColorSlot = 0;
          }

          SharedPreferences.Editor edit = preferences.edit();
          edit.putInt(Keys.COLOR + lastColorSlot, color);
          edit.putInt(Keys.COLOR_LAST, lastColorSlot);
          edit.commit();

          updatePickedColors(inflater, callback);
          linearLayoutDynamic.invalidate();
        }
      }
    }, 0);
    linearLayoutTop.addView(colorPickerView);

    int[] colors = new int[]{R.color.red, R.color.Orange, R.color.yellow, R.color.green, R.color.blue, R.color.purple};
    for (int color : colors) {
      final int hexColor = getActivity().getResources().getColor(color);

      ImageButton button = (ImageButton) inflater.inflate(R.layout.color_button, rainbow, false);
      button.setBackgroundColor(hexColor);
      rainbow.addView(button);

      button.setOnClickListener(new ButtonColorClickListener(callback, hexColor));
    }

    updatePickedColors(inflater, callback);

    return view;
  }

  private void updatePickedColors(LayoutInflater inflater, Callback callback) {
    linearLayoutDynamic.removeAllViews();
    final SharedPreferences preferences = getActivity().getSharedPreferences(Keys.PREFERENCES, Context.MODE_PRIVATE);
    //clearSavedColors(preferences);
    int lastColorSlot = preferences.getInt(Keys.COLOR_LAST, -1);
    if (lastColorSlot >= 0) {
      for (int i = 0 ; i < 6; i++) {
        final int hexWhiteColor = getActivity().getResources().getColor(R.color.white);
        int hexColor = preferences.getInt(Keys.COLOR + i, hexWhiteColor);

        ImageButton button = (ImageButton) inflater.inflate(R.layout.color_button, linearLayoutDynamic, false);
        button.setBackgroundColor(hexColor);
        linearLayoutDynamic.addView(button);

        button.setOnClickListener(new ButtonColorClickListener(callback, hexColor));
      }
    }
  }

  private void clearSavedColors(SharedPreferences preferences) {
    SharedPreferences.Editor editor = preferences.edit();
    editor.remove(Keys.COLOR_LAST);

    for (int i = 0 ; i < 6; i++) {
      editor.remove(Keys.COLOR + i);
    }
    editor.commit();
  }

  private static class ButtonColorClickListener implements View.OnClickListener {

    private SoftReference<Callback> callbackSoftReference;
    private int color;
    private Animation animation;

    public ButtonColorClickListener(Callback callback, int color) {
      this.callbackSoftReference = new SoftReference<Callback>(callback);
      this.color = color;
    }

    @Override
    public void onClick(View v) {
      Callback callback = callbackSoftReference.get();
      if (callback != null) {
        callback.colorChanged(color);
      }
      v.startAnimation(getAnimation());
    }

    private Animation getAnimation() {
      if (animation == null) {
        animation = new AlphaAnimation(1, 0);
        animation.setDuration(50);
        animation.setInterpolator(new LinearInterpolator());
      }
      return animation;
    }
  }

  /*@Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ColorPickerView colorPickerView = (ColorPickerView) view.findViewById(R.id.color_picker);
    colorPickerView.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
      @Override
      public void colorChanged(int color) {
        Toast.makeText(getActivity(), "Clicked color", Toast.LENGTH_LONG).show();
      }
    });

  }*/
}
