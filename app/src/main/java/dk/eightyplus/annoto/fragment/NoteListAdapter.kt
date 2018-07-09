package dk.eightyplus.annoto.fragment

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import dk.eightyplus.annoto.R
import dk.eightyplus.annoto.utilities.Compatibility

import java.lang.ref.SoftReference
import java.util.HashMap

/**
 * User: fries
 * Date: 29/03/14
 * Time: 19.34
 */
class NoteListAdapter(context: Context, listView: ListView, resource: Int, objects: Array<String>) : ArrayAdapter<String>(context, resource, objects), ThumbLoader.Notify {
    private val cachedImages = HashMap<String, SoftReference<Drawable>>()
    private val loading = HashMap<String, Boolean>()

    private val buttonOnClickListener: ButtonOnClickListener
    private val initialThumbLoadSize = 3

    init {
        var i = 0
        while (i < initialThumbLoadSize && i < objects.size) {
            startImageLoad(objects[i])
            i++
        }

        buttonOnClickListener = ButtonOnClickListener(listView)
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.note_list_item, parent, false)
        } else {
            convertView
        }

        val textView = rowView.findViewById<View>(R.id.note_title) as TextView
        val imageView = rowView.findViewById<View>(R.id.note_thumb) as ImageView
        val item = getItem(position)
        textView.text = item

        val buttonDelete = rowView.findViewById<View>(R.id.button_delete)
        buttonDelete.tag = R.id.button_delete
        buttonDelete.setOnClickListener(buttonOnClickListener)

        if (!setImage(imageView, item)) {
            imageView.setImageResource(android.R.drawable.stat_notify_error)
            startImageLoad(item)
        }

        return rowView
    }

    fun onPause() {}

    fun onResume() {}

    fun onDestroy() {}

    private fun setImage(imageView: ImageView, path: String?): Boolean {
        if (cachedImages.containsKey(path)) {
            cachedImages[path]?.get().let {
                imageView.setImageDrawable(it)
                return true
            }
        }
        return false
    }

    fun getImage(position: Int): Drawable? {
        val path = getItem(position)
        return if (cachedImages.containsKey(path)) {
            cachedImages[path]?.get()
        } else null
    }

    private fun startImageLoad(s: String) {
        if (doStartLoad(s)) {
            setLoadingStarted(s)
            Compatibility.get().startTask(ThumbLoader(context, this), s)
        }
    }

    private fun setLoadingStarted(key: String) {
        loading[key] = true
    }

    private fun setLoadingDone(key: String) {
        loading.remove(key)
    }

    private fun doStartLoad(s: String?): Boolean {
        return !loading.containsKey(s) || loading[s] == false
    }

    override fun done(path: String, drawable: Drawable) {
        cachedImages[path] = SoftReference(drawable)
        setLoadingDone(path)
        notifyDataSetChanged()
    }

    /**
     * Small class to put on buttons inside list view item, passed click with id of button
     */
    private class ButtonOnClickListener(listView: ListView) : View.OnClickListener {
        private val listViewSoftReference: SoftReference<ListView> = SoftReference(listView)

        override fun onClick(v: View) {
            listViewSoftReference.get()?.let {
                val position = it.getPositionForView(v.parent as View)
            it.performItemClick(v, position, (v.tag as Int).toLong())
            }
        }
    }
}
