package dk.eightyplus.Painter.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import dk.eightyplus.Painter.Callback;
import dk.eightyplus.Painter.R;

/**
 * User: fries
 * Date: 15/07/14
 * Time: 14.00
 */
public class ColorPaletteFragment extends DialogFragment {

  private Callback callback;

  public ColorPaletteFragment(final Callback callback) {
    this.callback = callback;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.color_palette, container, false);

    LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.color);

    int[] colors = new int[]{R.color.red, R.color.Orange, R.color.yellow, R.color.green, R.color.blue, R.color.purple};
    for (int color : colors) {
      ImageButton button = (ImageButton) inflater.inflate(R.layout.color_button, linearLayout, false);
      //Drawable drawable = new ColorDrawable(color);
          //new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
      //button.setImageDrawable(drawable);
      button.setBackgroundColor(color);
      linearLayout.addView(button);
    }

    return view;
  }
}
