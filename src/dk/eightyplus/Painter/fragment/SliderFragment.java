package dk.eightyplus.Painter.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import dk.eightyplus.Painter.Callback;
import dk.eightyplus.Painter.R;

/**
 * 
 */
public class SliderFragment extends DialogFragment {

  private Callback callback;

  private int width = 1;

  public SliderFragment(final Callback callback) {
    this.callback = callback;
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.slider, container, false);

    SeekBar slider = (SeekBar) view.findViewById(R.id.slider);
    slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        width = progress;
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        callback.setStrokeWidth(width);
      }
    });

    Log.d(getTag(), "on create");

    return view;
  }
}
