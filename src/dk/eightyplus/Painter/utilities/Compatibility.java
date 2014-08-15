package dk.eightyplus.Painter.utilities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;

/**
 * Compatibility implementation to support a wider selection of android version
 */
public abstract class Compatibility {
  private static final String TAG = Compatibility.class.toString();

  private static final Compatibility INSTANCE;

  static {
    int version = getApiVersion();

    if (version >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
      INSTANCE = new APIVersion15();
    } else if (version >= Build.VERSION_CODES.HONEYCOMB) {
      INSTANCE = new APIVersion11();
    } else if (version >= Build.VERSION_CODES.ECLAIR_MR1) {
      INSTANCE = new APIVersion7();
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
  @SuppressWarnings("unused")
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

  public abstract <Params, Progress, Result> void startTask(final AsyncTask<Params, Progress, Result> task,
                                                            final Params... params);

  public abstract boolean callOnClick(final View view, final View.OnClickListener onClickListener);

  /**
   * Unsupported version.
   */
  private static class UnsupportedVersion extends Compatibility {
    @SuppressWarnings("unused")
    private static final String UNSUPPORTED_ERROR = "This Android version isn't supported";

    @Override
    public boolean supportActionBar() {
      return false;
    }

    @Override
    public void setHardwareAccelerated(View view, Paint paint) { }

    @Override
    public <Params, Progress, Result> void startTask(AsyncTask<Params, Progress, Result> task, Params... params) { }

    @Override
    public boolean callOnClick(final View view, final View.OnClickListener onClickListener) {
      return false;
    }
  }

  /**
   * API version 11 (Build.VERSION_CODES.ECLAIR_MR1).
   */
  private static class APIVersion7 extends UnsupportedVersion {
    @Override
    public <Params, Progress, Result> void startTask(AsyncTask<Params, Progress, Result> task, Params... params) {
      task.execute(params);
    }
  }

  /**
   * API version 11 (Build.VERSION_CODES.HONEYCOMB).
   */
  @SuppressWarnings("deprecation")
  private static class APIVersion11 extends APIVersion7 {

    @Override
    public <Params, Progress, Result> void startTask(AsyncTask<Params, Progress, Result> task, Params... params) {
      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);;
    }

    @Override
    public boolean supportActionBar() {
      return true;
    }

    @Override
    public void setHardwareAccelerated(View view, Paint paint) {
      view.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

    @Override
    public boolean callOnClick(final View view, final View.OnClickListener onClickListener) {
      if (onClickListener != null) {
        onClickListener.onClick(view);
      } else if (view != null) {
        return view.performClick();
      }
      return true;
    }
  }


  /**
   * API version 15 (Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1).
   */
  @SuppressWarnings("deprecation")
  private static class APIVersion15 extends APIVersion11 {

    @Override
    public boolean callOnClick(final View view, final View.OnClickListener onClickListener) {
      return view != null && view.callOnClick();
    }
  }
}
