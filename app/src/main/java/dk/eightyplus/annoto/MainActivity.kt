package dk.eightyplus.annoto

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import dk.eightyplus.annoto.action.State
import dk.eightyplus.annoto.action.Undo
import dk.eightyplus.annoto.component.Component
import dk.eightyplus.annoto.component.Picture
import dk.eightyplus.annoto.component.Text
import dk.eightyplus.annoto.dialog.ColorPickerDialog
import dk.eightyplus.annoto.fragment.*
import dk.eightyplus.annoto.utilities.Compatibility
import dk.eightyplus.annoto.utilities.Storage
import dk.eightyplus.annoto.view.DrawingView
import dk.eightyplus.annoto.view.MoveView
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MainActivity : FragmentActivity(), ColorPickerDialog.OnColorChangedListener, Callback {

    private val undoes = Stack<Undo>()
    private val redoes = Stack<Undo>()

    private var layout: ViewGroup? = null
    private lateinit var view: DrawingView
    private var moveView: MoveView? = null

    override var state = State.DrawPath
    private lateinit var visibleLayer: ViewGroup
    private var color = 0
    private var cameraFileName: String? = null
    private var saveFileNamePrefix: String? = null


    private val isEditorVisible: Boolean
        get() = supportFragmentManager.findFragmentByTag(Tags.FRAGMENT_EDITOR) != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        layout = (findViewById<View>(R.id.main) as ViewGroup).apply {
            val touchLayer = findViewById<View>(R.id.touch_layer) as ViewGroup
            val touchView = TouchView(applicationContext)
            touchLayer.addView(touchView)
        }
        visibleLayer = findViewById<View>(R.id.visible_layer) as ViewGroup
        view = DrawingView(applicationContext, this)
        visibleLayer.addView(view)

        setupActionBar()

        showButtonSelector()
    }

    /**
     * TouchView delegates user interaction to correct views from user toggles
     */
    inner class TouchView(context: Context) : View(context) {

        override fun onTouchEvent(event: MotionEvent): Boolean {

            when (state) {
                State.Delete -> {
                    if (event.action == MotionEvent.ACTION_MOVE) {
                        val deleteComponent = view.findComponent(event.x, event.y)

                        if (deleteComponent != null) {
                            view.remove(deleteComponent)
                            add(Undo(deleteComponent, State.Delete))
                            view.redraw()
                        }
                    }
                    return true
                }
                State.Move -> {
                    if (moveView == null && event.action == MotionEvent.ACTION_DOWN) {
                        val moveComponent = view.findComponent(event.x, event.y)

                        if (moveComponent != null) {
                            moveComponent.isVisible = false
                            this@MainActivity.moveView = MoveView(applicationContext, moveComponent, this@MainActivity).apply {
                                visibleLayer.addView(this)
                            }
                            view.redraw()
                        }
                    }

                    if (moveView != null) {
                        when (event.action) {
                            MotionEvent.ACTION_DOWN,
                            MotionEvent.ACTION_MOVE,
                            MotionEvent.ACTION_UP ->
                                moveView?.onTouchEvent(event)
                        }
                    }
                    return true
                }
                State.Text -> {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        if (isEditorVisible) {
                            hideEditor()
                        } else {
                            val editComponent = view.findComponent(event.x, event.y)
                            showEditor(editComponent, event.x, event.y)
                        }
                    }
                    return true
                }
                State.DrawPath -> return view.onTouchEvent(event)
                else -> return view.onTouchEvent(event)
            }
        }
    }

    private fun showEditor(component: Component?, x: Float, y: Float) {
        val fragment = supportFragmentManager.findFragmentByTag(Tags.FRAGMENT_EDITOR)

        val transaction = supportFragmentManager.beginTransaction()
        if (fragment == null) {
            val editorFragment = EditorFragment(this@MainActivity, component as? Text, x, y)
            transaction.replace(R.id.configuration_top, editorFragment, Tags.FRAGMENT_EDITOR)
        } else {
            transaction.remove(fragment)
        }

        transaction.commit()
    }

    private fun hideEditor() {
        val fragment = supportFragmentManager.findFragmentByTag(Tags.FRAGMENT_EDITOR)

        if (fragment != null) {
            val editorFragment = fragment as EditorFragment
            editorFragment.textChanges?.let {
                if (color != 0) {
                    it.component.color = color
                }
                view.add(it.component)
                view.redraw()
                add(it.undo)
            }

            supportFragmentManager.beginTransaction().run {
                remove(fragment)
            }.commit()
        }

        layout?.windowToken.let { windowToken ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }

    }

    private fun showWidthSlider() {
        val fragment = supportFragmentManager.findFragmentByTag(Tags.FRAGMENT_SLIDER)
        val transaction = supportFragmentManager.beginTransaction()
        if (fragment == null) {
            val sliderFragment = SliderFragment(this, view.strokeWidth)
            transaction.replace(R.id.configuration_top, sliderFragment, Tags.FRAGMENT_SLIDER)
        } else {
            transaction.remove(fragment)
        }
        transaction.commit()
    }

    private fun showColorPalette() {
        val fragment = supportFragmentManager.findFragmentByTag(Tags.FRAGMENT_COLOR)
        val transaction = supportFragmentManager.beginTransaction()
        if (fragment == null) {
            val colorPaletteFragment = ColorPaletteFragment()
            transaction.replace(R.id.configuration_top, colorPaletteFragment, Tags.FRAGMENT_COLOR)
        } else {
            transaction.remove(fragment)
        }
        transaction.commit()
    }

    private fun showNotesList() {
        val transaction = supportFragmentManager.beginTransaction()
        val noteListFragment = NoteListFragment()
        noteListFragment.show(transaction, Tags.FRAGMENT_LIST)
    }

    private fun removeNotesList() {
        val fragment = supportFragmentManager.findFragmentByTag(Tags.FRAGMENT_LIST)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().run {
                remove(fragment)
            }.commit()
        }
    }

    private fun showButtonSelector() {
        val fragment = supportFragmentManager.findFragmentByTag(Tags.FRAGMENT_SLIDER)
        val transaction = supportFragmentManager.beginTransaction()
        if (fragment == null) {
            val icons = intArrayOf(android.R.drawable.ic_menu_edit, R.drawable.ic_menu_copy, android.R.drawable.ic_menu_delete, R.drawable.ic_action_keyboard)

            val tags = arrayOf(State.DrawPath.name, State.Move.name, State.Delete.name, State.Text.name)

            val buttonSelectorFragment = ButtonSelectorFragment(icons, tags)
            transaction.replace(R.id.button_selector, buttonSelectorFragment, Tags.FRAGMENT_SELECTOR)
        } else {
            transaction.remove(fragment)
        }
        transaction.commit()
    }


    private fun showSpinner(show: Boolean) {
        val pb = findViewById<View>(R.id.progress) as ProgressBar
        runOnUiThread { pb.visibility = if (show) View.VISIBLE else View.GONE }
    }

    override fun textEditDone() {

        hideEditor()
    }

    private fun setupActionBar() {
        if (Compatibility.get().supportActionBar()) {
            actionBar?.let {
                it.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
                it.setCustomView(R.layout.action_bar_layout)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideEditor()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        view.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        view.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            val context = applicationContext
            val storage = Storage.getStorage(context)
            val uri = data.dataString ?: return

            when (requestCode) {
                Tags.SELECT_PICTURE -> try {
                    val bitmap = storage.loadBitmapFromIntent(this, data, 1)
                    val fileIndex = uri.lastIndexOf("/")
                    val uriFileName = uri.substring(fileIndex + 1)
                    val galleryFileName = determineGalleryFileName(storage, uriFileName)

                    thread {
                        try {
                            storage.writeToFile(bitmap, galleryFileName, 90)
                        } catch (e: IOException) {
                            Log.d(TAG, getString(R.string.log_error_exception), e)
                        }
                    }

                    val picture = Picture(context, bitmap, galleryFileName)
                    view.add(picture)
                    add(Undo(picture, State.Add))
                } catch (e: IOException) {
                    Log.d(TAG, getString(R.string.log_error_exception), e)
                    Toast.makeText(context, getString(R.string.error_loading_image), Toast.LENGTH_LONG).show()
                }

                Tags.CAMERA_REQUEST -> {
                    cameraFileName?.let { filename ->
                        storage.loadFromFile(filename)?.let {bitmap ->
                            val picture = Picture(context, bitmap, filename)
                            view.add(picture)
                            add(Undo(picture, State.Add))
                        }
                    }

                }
            }
            view.redraw()
        }
    }

    private fun determineGalleryFileName(storage: Storage, filename: String): String {
        var name: String
        var count = 1
        while (true) {
            name = getString(R.string.gallery_file_format, count, filename)
            if (!storage.getFilename(name).exists()) {
                break
            }
            count++
        }
        return name
    }

    override fun colorChanged(color: Int) {
        this.color = color
        view.color = color
    }

    override fun hexColorForResourceId(color: Int): Int {
        return ContextCompat.getColor(applicationContext, color)
    }

    override fun move(component: Component, dx: Float, dy: Float, scale: Float) {
        component.isVisible = true
        view.move(component, dx, dy, scale)
        visibleLayer.removeView(moveView)
        moveView = null
    }

    override fun setStrokeWidth(width: Int) {
        view.strokeWidth = width
    }

    override fun add(undo: Undo) {
        this.undoes.add(undo)
    }

    override fun load(fileName: String) {

        thread {
            removeNotesList()
            showSpinner(true)
            try {
                cleanup(true, false)
                saveFileNamePrefix = fileName.substring(0, fileName.lastIndexOf("."))
                Storage.getStorage(applicationContext).loadFromFile(view, fileName)
                view.redraw()
            } catch (e: IOException) {
                Log.e(TAG, getString(R.string.log_error_exception), e)
            } catch (e: ClassNotFoundException) {
                Log.e(TAG, getString(R.string.log_error_exception), e)
            }

            showSpinner(false)
        }
    }

    private fun save() {
        thread {
            showSpinner(true)
            try {
                //writeToFile(getApplicationContext(), view.getBitmap());
                if (saveFileNamePrefix == null) {
                    val formatter = SimpleDateFormat(getString(R.string.date_format), Locale.getDefault())
                    val date = formatter.format(Calendar.getInstance().time)
                    saveFileNamePrefix = String.format("file-%s", date)
                }

                val storage = Storage.getStorage(applicationContext)
                storage.writeToFile(view, getString(R.string.note_file_format, saveFileNamePrefix))
                storage.writeToFile(view.bitmap, String.format("%s.png", saveFileNamePrefix), 30)
            } catch (e: IOException) {
                Log.e(TAG, getString(R.string.log_error_exception), e)
            }

            showSpinner(false)
        }
    }

    private fun cleanup(reinitialise: Boolean, invalidate: Boolean) {
        val delete = saveFileNamePrefix == null
        clearUndos(delete)
        saveFileNamePrefix = null
        if (reinitialise) {
            view.reinitialise(delete, invalidate)
        } else {
            view.delete(delete)
        }
    }

    fun undo(): Boolean {
        if (undoes.size > 0) {
            val undo = this.undoes.pop()
            if (undo.undo(view)) {
                redoes.add(undo)
                view.redraw()
                return true
            }
        }
        return false
    }

    fun redo(): Boolean {
        if (redoes.size > 0) {
            val redo = this.redoes.pop()
            if (redo.redo(view)) {
                undoes.add(redo)
                view.redraw()
                return true
            }
        }
        return false
    }

    private fun clearUndos(delete: Boolean) {
        if (delete) {
            deleteReUnDos(undoes)
        }
        undoes.clear()
        clearRedos(delete)
    }

    private fun clearRedos(delete: Boolean) {
        if (delete) {
            deleteReUnDos(redoes)
        }
        redoes.clear()
    }

    private fun deleteReUnDos(reUnDos: Stack<Undo>) {
        for (reUnDo in reUnDos) {
            reUnDo.cleanup()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        setupDrawPathMenuButton(menu)
        setupWidthMenuButton(menu)
        setupMoveMenuButton(menu)
        setupRedrawMenuButton(menu)
        setupShareMenuButton(menu)
        setupCameraMenuButton(menu)
        setupGalleryMenuButton(menu)
        setupSaveMenuButton(menu)
        setupColorMenuButton(menu)
        setupDeleteMenuButton(menu)
        setupUndoMenuButton(menu)
        setupRedoMenuButton(menu)
        setupReplayMenuButton(menu)
        setupNewMenuButton(menu)
        setupMenuListButton(menu)
        setupMenuFeedbackButton(menu)

        return true
    }

    private fun setupDrawPathMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_path)?.setOnMenuItemClickListener {
            state = State.DrawPath
            true
        }
    }

    private fun setupWidthMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_width)?.setOnMenuItemClickListener {
            showWidthSlider()
            true
        }
    }

    private fun setupMoveMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_move)?.setOnMenuItemClickListener {
            state = State.Move
            true
        }
    }

    private fun setupRedrawMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_redraw)?.setOnMenuItemClickListener {
            view.redraw(50, true)
            true
        }
    }

    private fun setupShareMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_share)?.setOnMenuItemClickListener {
            try {
                val file = Storage.getStorage(applicationContext).writeToFile(view.bitmap)
                val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                    type = getString(R.string.image_format)
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
                    putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                }
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_information)))
            } catch (e: IOException) {
                Log.e(TAG, applicationContext.getString(R.string.log_error_exception), e)
            }

            true
        }
    }

    private fun setupCameraMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_camera)?.setOnMenuItemClickListener {
            val formatter = SimpleDateFormat(getString(R.string.date_format), Locale.getDefault())
            val date = formatter.format(Calendar.getInstance().time)
            cameraFileName = getString(R.string.camera_file_format, date)
            val cameraFile = Storage.getStorage(applicationContext).getFilename(cameraFileName)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile))
            startActivityForResult(cameraIntent, Tags.CAMERA_REQUEST)
            true
        }
    }

    private fun setupGalleryMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_gallery)?.setOnMenuItemClickListener {
            val intent = Intent()
            intent.type = getString(R.string.image_format_wildcard)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), Tags.SELECT_PICTURE)
            true
        }
    }

    private fun setupSaveMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_save)?.setOnMenuItemClickListener {
            save()
            true
        }
    }

    private fun setupColorMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_color)?.setOnMenuItemClickListener {
            //new ColorPickerDialog(MainActivity.this, MainActivity.this, view.getColor()).show();
            showColorPalette()
            true
        }
    }

    private fun setupDeleteMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_delete)?.setOnMenuItemClickListener {
            state = State.Delete
            true
        }
    }

    private fun setupUndoMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_undo)?.setOnMenuItemClickListener {
            undo()
            true
        }
    }

    private fun setupRedoMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_redo)?.setOnMenuItemClickListener {
            redo()
            true
        }
    }

    private fun setupReplayMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_replay)?.setOnMenuItemClickListener {
            try {
                Storage.getStorage(applicationContext).loadFromFile(view, "file.note", true)
                runOnUiThread { view.redraw(100, false) }
            } catch (e: IOException) {
                Log.e(TAG, getString(R.string.log_error_exception), e)
            } catch (e: ClassNotFoundException) {
                Log.e(TAG, getString(R.string.log_error_exception), e)
            }

            true
        }
    }

    private fun setupNewMenuButton(menu: Menu) {
        menu.findItem(R.id.menu_new)?.setOnMenuItemClickListener {
            cleanup(true, true)
            true
        }
    }

    private fun setupMenuListButton(menu: Menu) {
        menu.findItem(R.id.menu_list).setOnMenuItemClickListener {
            showNotesList()
            true
        }
    }

    private fun setupMenuFeedbackButton(menu: Menu) {
        menu.findItem(R.id.menu_feedback).setOnMenuItemClickListener {
            val url = applicationContext.getString(R.string.url_feedback)
            WebPageActivity.startWebPageActivity(this@MainActivity, url)
            true
        }

    }

    companion object {
        private val TAG = MainActivity::class.java.toString()
    }
}
