package dk.eightyplus.Painter.utilities;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import dk.eightyplus.Painter.R;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Initial implementation of ads.
 *
 * User: fries
 * Date: 15/07/14
 * Time: 11.36
 */
public class Ad {

  private static final int MAPPING = 3;

  private Map<Integer, SoftReference<AdView>> adViews = new HashMap<Integer, SoftReference<AdView>>();

  /**
   * @param context the context
   * @param position the position in list
   * @return view to be injected into list
   */
  public View injectView(final Context context, int position) {

    SoftReference<AdView> adViewSoftReference = adViews.get(position);
    AdView adView;
    if (adViewSoftReference == null || adViewSoftReference.get() == null) {
      String deviceId = StringHelper.deviceId(context);
      AdRequest adRequest = new AdRequest.Builder()
          .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
          .addTestDevice(deviceId)
          .build();
      adView = getAd(context);

      adView.loadAd(adRequest);
      adView.setLayoutParams(new ViewGroup.LayoutParams(context.getResources().getDimensionPixelSize(R.dimen.ad_width),
          context.getResources().getDimensionPixelSize(R.dimen.ad_height)));
      adViews.put(position, new SoftReference<AdView>(adView));
    } else {
      adView = adViewSoftReference.get();
    }

    return adView;
  }

  private AdView getAd(final Context context) {
    AdView adView = new AdView(context);
    adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
    adView.setAdUnitId(context.getString(R.string.ad_unit_id));
    return adView;
  }

  /**
   * Map count if ads are present
   * @param count the count
   * @return mapped count
   */
  public int mapCount(int count) {
    if ( hasAds() ) {
      count += count / MAPPING;
    }
    return count;
  }

  /**
   * Map position without ads
   * @param position the position
   * @return mapped position
   */
  public int mapPosition(int position) {
    if ( hasAds() ) {
      position -= position / MAPPING;
    }
    return position;
  }

  /**
   * @param position position
   * @return true if ad should be injected
   */
  public boolean inject(int position) {
    return hasAds() && position % MAPPING == 0;
  }

  /**
   * @return true if app has ads
   */
  public boolean hasAds() {
    return true;
  }

  public void onPause() {
    for (SoftReference<AdView> adViewSoftReference : adViews.values()) {
      AdView adView = adViewSoftReference.get();
      if (adView != null){
        adView.pause();
      }
    }
  }

  public void onResume() {
    for (SoftReference<AdView> adViewSoftReference : adViews.values()) {
      AdView adView = adViewSoftReference.get();
      if (adView != null){
        adView.resume();
      }
    }
  }

  public void onDestroy() {
    for (SoftReference<AdView> adViewSoftReference : adViews.values()) {
      AdView adView = adViewSoftReference.get();
      if (adView != null){
        adView.destroy();
      }
    }

    adViews.clear();
  }
}
