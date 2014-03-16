package dk.eightyplus.Painter;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 */
public class SliderFragment extends DialogFragment {


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.slider, container, false);

    Log.d(getTag(), "on create");

    return view;
  }
}
