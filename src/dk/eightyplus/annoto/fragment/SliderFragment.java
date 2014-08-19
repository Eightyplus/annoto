package dk.eightyplus.annoto.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import dk.eightyplus.annoto.Callback;
import dk.eightyplus.annoto.Keys;
import dk.eightyplus.annoto.R;

/**
 * Fragment for width slider
 */
public class SliderFragment extends DialogFragment {

  private Callback callback;
  private int width;

  @SuppressWarnings("unused")
  public SliderFragment() { }

  public SliderFragment(final Callback callback, int width) {
    this.callback = callback;
    this.width = width;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      width = savedInstanceState.getInt(Keys.WIDTH);
    }

    callback = (Callback) getActivity();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(Keys.WIDTH, width);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.slider, container, false);

    SeekBar slider = (SeekBar) view.findViewById(R.id.slider);
    slider.setProgress(width);
    slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        callback.setStrokeWidth(seekBar.getProgress());
      }
    });

    return view;
  }
}
