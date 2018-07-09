package dk.eightyplus.annoto.utilities

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.view.View

/**
 * Compatibility implementation to support a wider selection of android version
 */
abstract class Compatibility
/**
 * Private constructor. Don't want/need any other instances of this class around.
 */
private constructor() {

    abstract fun supportActionBar(): Boolean

    abstract fun setHardwareAccelerated(view: View, paint: Paint)

    abstract fun <Params, Progress, Result> startTask(task: AsyncTask<Params, Progress, Result>,
                                                      vararg params: Params)

    abstract fun callOnClick(view: View, onClickListener: View.OnClickListener): Boolean

    /**
     * Unsupported version.
     */
    private open class UnsupportedVersion : Compatibility() {

        override fun supportActionBar(): Boolean {
            return false
        }

        override fun setHardwareAccelerated(view: View, paint: Paint) {}

        override fun <Params, Progress, Result> startTask(task: AsyncTask<Params, Progress, Result>, vararg params: Params) {}

        override fun callOnClick(view: View, onClickListener: View.OnClickListener): Boolean {
            return false
        }

        companion object {
            private val UNSUPPORTED_ERROR = "This Android version isn't supported"
        }
    }

    /**
     * API version 11 (Build.VERSION_CODES.ECLAIR_MR1).
     */
    private open class APIVersion7 : UnsupportedVersion() {
        override fun <Params, Progress, Result> startTask(task: AsyncTask<Params, Progress, Result>, vararg params: Params) {
            task.execute(*params)
        }
    }

    /**
     * API version 11 (Build.VERSION_CODES.HONEYCOMB).
     */
    private open class APIVersion11 : APIVersion7() {

        override fun <Params, Progress, Result> startTask(task: AsyncTask<Params, Progress, Result>, vararg params: Params) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *params)
        }

        override fun supportActionBar(): Boolean {
            return true
        }

        override fun setHardwareAccelerated(view: View, paint: Paint) {
            view.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        }

        override fun callOnClick(view: View, onClickListener: View.OnClickListener): Boolean {
            onClickListener.onClick(view)
            return true
        }
    }


    /**
     * API version 15 (Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1).
     */
    private class APIVersion15 : APIVersion11() {

        override fun callOnClick(view: View, onClickListener: View.OnClickListener): Boolean {
            return view.callOnClick()
        }
    }

    companion object {
        private val TAG = Compatibility::class.java.toString()

        private val INSTANCE: Compatibility

        init {
            INSTANCE = when {
                apiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 -> APIVersion15()
                apiVersion >= Build.VERSION_CODES.HONEYCOMB -> APIVersion11()
                apiVersion >= Build.VERSION_CODES.ECLAIR_MR1 -> APIVersion7()
                else -> UnsupportedVersion()
            }
            Log.d(TAG, "Instance $INSTANCE")
        }

        /**
         * @return API version
         */
        private val apiVersion
            get() = try {
                Build.VERSION::class.java.getDeclaredField("SDK_INT").getInt(null)
            } catch (ignore: Throwable) {
                3 // SDK_INT was introduced in API Level 4
            }

        /**
         * @param context context
         * @return version code the app was built with
         */
        fun getVersionCode(context: Context): Int {
            return try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "Error package manager", e)
                0
            }
        }

        /**
         * @return A singleton instance of this class.
         */
        fun get(): Compatibility {
            return INSTANCE
        }
    }
}
