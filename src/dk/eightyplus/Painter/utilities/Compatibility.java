package dk.eightyplus.Painter.utilities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.view.View;

/**
 *
 */
public abstract class Compatibility {
  private static final String TAG = Compatibility.class.toString();

  private static final Compatibility INSTANCE;

  static {
    int version = getApiVersion();

    if (version >= Build.VERSION_CODES.HONEYCOMB) {
      INSTANCE = new APIVersion11();
    } else {
      INSTANCE = new UnsupportedVersion();
    }
    Log.d(TAG, "Instance " + INSTANCE);
  }


  /**
   * @return API version
   */
  public static int getApiVersion() {
    int version = 3; // SDK_INT was introduced in API Level 4
    try {
      version = Build.VERSION.class.getDeclaredField("SDK_INT").getInt(null);
    } catch (Throwable ignore) {
    }
    return version;
  }

  /**
   * @param context context
   * @return version code the app was built with
   */
  public static int getVersionCode(final Context context) {
    int versionCode = 0;
    try {
      versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Error package manager", e);
    }
    return versionCode;
  }

  /**
   * Private constructor. Don't want/need any other instances of this class around.
   */
  private Compatibility() { }

  /**
   * @return A singleton instance of this class.
   */
  public static Compatibility get() {
    return INSTANCE;
  }

  public abstract boolean supportActionBar();

  public abstract void setHardwareAccelerated(final View view, final Paint paint);

  /* Implementations */

  /**
   * Unsupported version.
   *
   * @author Henrik Kirk <mailto:hbk@visiolink.com/>
   * @version 1.0
   * @since 07-30-2012
   */
  private static class UnsupportedVersion extends Compatibility {
    private static final String UNSUPPORTED_ERROR = "This Android version isn't supported";

    @Override
    public boolean supportActionBar() {
      return false;
    }

    @Override
    public void setHardwareAccelerated(View view, Paint paint) { }
  }

  /**
   * API version 11 (Build.VERSION_CODES.HONEYCOMB).
   *
   * @author Henrik Kirk <mailto:hbk@visiolink.com>
   * @version 1.0
   * @since Jul 30, 2012
   */
  @SuppressWarnings("deprecation")
  private static class APIVersion11 extends UnsupportedVersion {

    @Override
    public boolean supportActionBar() {
      return true;
    }

    @Override
    public void setHardwareAccelerated(View view, Paint paint) {
      view.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }


  }


}
