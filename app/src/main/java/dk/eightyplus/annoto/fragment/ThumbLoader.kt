package dk.eightyplus.annoto.fragment

import android.R
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.support.v4.content.ContextCompat
import android.util.Pair
import dk.eightyplus.annoto.utilities.Storage

import java.io.File

/**
 * User: fries
 * Date: 30/03/14
 * Time: 11.37
 */
class ThumbLoader(context: Context, private val notify: Notify) : AsyncTask<String, Void, Pair<String, Drawable>>() {

    private val storage: Storage = Storage.getStorage(context)
    private val fallback: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_dialog_alert) ?: ColorDrawable()

    override fun doInBackground(vararg paths: String): Pair<String, Drawable> {
        val path = paths[0]

        val file = storage.getThumb2Notes(path)
        return Pair(path, if (file.exists()) Drawable.createFromPath(file.absolutePath) else fallback)
    }

    override fun onPostExecute(drawablePair: Pair<String, Drawable>) {
        notify.done(drawablePair.first, drawablePair.second)
    }

    interface Notify {
        fun done(path: String, drawable: Drawable)
    }
}
