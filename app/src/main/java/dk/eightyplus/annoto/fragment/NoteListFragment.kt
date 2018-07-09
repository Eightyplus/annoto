package dk.eightyplus.annoto.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import dk.eightyplus.annoto.Callback
import dk.eightyplus.annoto.R
import dk.eightyplus.annoto.utilities.Storage
import java.lang.ref.SoftReference

/**
 * User: fries
 * Date: 29/03/14
 * Time: 18.56
 */
class NoteListFragment : DialogFragment() {

    private var callbackSoftReference: SoftReference<Callback>? = null
    private var adapter: NoteListAdapter? = null
    private var listView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.callbackSoftReference = SoftReference<Callback>(activity as Callback?)
    }

    override fun onResume() {
        super.onResume()
        adapter?.onResume()
    }

    override fun onPause() {
        super.onPause()
        adapter?.onPause()
    }


    override fun onDestroy() {
        super.onDestroy()
        adapter?.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.note_list_layout, container, false)

        dialog.setTitle(R.string.archive)
        val context = inflater.context

        val listView = view.findViewById<View>(android.R.id.list) as ListView
        val adapter = createAdapter(context, listView)

        val newNoteButton = view.findViewById<View>(R.id.new_note)
        newNoteButton?.setOnClickListener { Toast.makeText(context, "Add new", Toast.LENGTH_SHORT).show() }

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, id ->
            val fileName = adapter.getItem(position)
            if (id == R.id.button_delete.toLong()) {
                val factory = LayoutInflater.from(context)
                val message = factory.inflate(R.layout.title_image_view, container)

                val title = message.findViewById<View>(R.id.message_title) as TextView
                val image = message.findViewById<View>(R.id.message_image) as ImageView

                title.text = context.getString(R.string.delete_file, fileName)
                image.setImageDrawable(adapter.getImage(position))

                val builder = AlertDialog.Builder(activity)
                        .setTitle(R.string.delete_list_item)
                        .setView(message)
                        .setCancelable(true)
                        .setPositiveButton(R.string.delete) { dialog, which ->
                            Storage.getStorage(context).deleteNoteAndThumb(fileName)
                            this@NoteListFragment.updateAdapter(context, listView)
                        }
                        .setNegativeButton(R.string.cancel, null)
                builder.show()
                return@OnItemClickListener
            }

            callbackSoftReference?.get()?.load(fileName)
        }

        listView.adapter = adapter
        this.listView = listView
        this.adapter = adapter

        return view
    }

    private fun createAdapter(context: Context, listView: ListView): NoteListAdapter {
        return NoteListAdapter(context, listView, R.layout.note_list_item, Storage.getStorage(context).notes)
    }

    private fun updateAdapter(context: Context, listView: ListView) {
        val adapter = createAdapter(context, listView)
        listView.adapter = adapter
        adapter.notifyDataSetChanged()

    }
}
