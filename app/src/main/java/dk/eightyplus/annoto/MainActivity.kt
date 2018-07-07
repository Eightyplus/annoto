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

        layout = findViewById<View>(R.id.main) as ViewGroup
        visibleLayer = findViewById<View>(R.id.visible_layer) as ViewGroup
        view = DrawingView(applicationContext, this)
        visibleLayer.addView(view)

        val touchLayer = layout!!.findViewById<View>(R.id.touch_layer) as ViewGroup
        val touchView = TouchView(applicationContext)
        touchLayer.addView(touchView)

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
                    var moveView = moveView

                    if (moveView == null && event.action == MotionEvent.ACTION_DOWN) {
                        val moveComponent = view.findComponent(event.x, event.y)

                        if (moveComponent != null) {
                            moveComponent.isVisible = false
                            moveView = MoveView(applicationContext, moveComponent, this@MainActivity)
                            this@MainActivity.moveView = moveView
                            visibleLayer.addView(moveView)
                            view.redraw()
                        }
                    }

                    if (moveView != null) {
                        when (event.action) {
                            MotionEvent.ACTION_DOWN,
                            MotionEvent.ACTION_MOVE,
                            MotionEvent.ACTION_UP ->
                                moveView.onTouchEvent(event)
                            else -> {}
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
            val textChanges = editorFragment.textChanges
            if (textChanges != null) {
                if (color != 0) {
                    textChanges.component.color = color
                }
                view.add(textChanges.component)
                view.redraw()
                add(textChanges.undo)
            }
        }
        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.remove(fragment)
            transaction.commit()
        }

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(layout!!.windowToken, 0)
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
            val transaction = supportFragmentManager.beginTransaction()
            transaction.remove(fragment)
            transaction.commit()
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
            actionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar!!.setCustomView(R.layout.action_bar_layout)
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
            when (requestCode) {
                Tags.SELECT_PICTURE -> try {
                    val bitmap = storage.loadBitmapFromIntent(this, data, 1)
                    val fileIndex = data.dataString!!.lastIndexOf("/")
                    val uriFileName = data.dataString!!.substring(fileIndex + 1)
                    val galleryFileName = determineGalleryFileName(storage, uriFileName)

                    Thread(Runnable {
                        try {
                            storage.writeToFile(bitmap, galleryFileName, 90)
                        } catch (e: IOException) {
                            Log.d(TAG, getString(R.string.log_error_exception), e)
                        }
                    }).start()

                    val picture = Picture(context, bitmap, galleryFileName)
                    view.add(picture)
                    add(Undo(picture, State.Add))
                } catch (e: IOException) {
                    Log.d(TAG, getString(R.string.log_error_exception), e)
                    Toast.makeText(context, getString(R.string.error_loading_image), Toast.LENGTH_LONG).show()
                }

                Tags.CAMERA_REQUEST -> {
                    val cameraFileName = cameraFileName
                    if (cameraFileName != null) {
                        val bitmap = storage.loadFromFile(cameraFileName)
                        if (bitmap != null) {
                            val picture = Picture(context, bitmap, cameraFileName)
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

        Thread(Runnable {
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
        }).start()
    }

    private fun save() {
        Thread(Runnable {
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
        }).start()
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
        val path = menu.findItem(R.id.menu_path)
        path?.setOnMenuItemClickListener {
            state = State.DrawPath
            true
        }
    }

    private fun setupWidthMenuButton(menu: Menu) {
        val menuWidth = menu.findItem(R.id.menu_width)
        menuWidth?.setOnMenuItemClickListener {
            showWidthSlider()
            true
        }
    }

    private fun setupMoveMenuButton(menu: Menu) {
        val menuMove = menu.findItem(R.id.menu_move)
        menuMove?.setOnMenuItemClickListener {
            state = State.Move
            true
        }
    }

    private fun setupRedrawMenuButton(menu: Menu) {
        val menuRedraw = menu.findItem(R.id.menu_redraw)
        menuRedraw?.setOnMenuItemClickListener {
            view.redraw(50, true)
            true
        }
    }

    private fun setupShareMenuButton(menu: Menu) {
        val menuShare = menu.findItem(R.id.menu_share)
        menuShare?.setOnMenuItemClickListener {
            try {
                val file = Storage.getStorage(applicationContext).writeToFile(view.bitmap)
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = getString(R.string.image_format)
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
                sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_information)))
            } catch (e: IOException) {
                Log.e(TAG, applicationContext.getString(R.string.log_error_exception), e)
            }

            true
        }
    }

    private fun setupCameraMenuButton(menu: Menu) {
        val menuCamera = menu.findItem(R.id.menu_camera)
        menuCamera?.setOnMenuItemClickListener {
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
        val menuGallery = menu.findItem(R.id.menu_gallery)
        menuGallery?.setOnMenuItemClickListener {
            val intent = Intent()
            intent.type = getString(R.string.image_format_wildcard)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), Tags.SELECT_PICTURE)
            true
        }
    }

    private fun setupSaveMenuButton(menu: Menu) {
        val menuSave = menu.findItem(R.id.menu_save)
        menuSave?.setOnMenuItemClickListener {
            save()
            true
        }
    }

    private fun setupColorMenuButton(menu: Menu) {
        val menuColor = menu.findItem(R.id.menu_color)
        menuColor?.setOnMenuItemClickListener {
            //new ColorPickerDialog(MainActivity.this, MainActivity.this, view.getColor()).show();
            showColorPalette()
            true
        }
    }

    private fun setupDeleteMenuButton(menu: Menu) {
        val menuDelete = menu.findItem(R.id.menu_delete)
        menuDelete?.setOnMenuItemClickListener {
            state = State.Delete
            true
        }
    }

    private fun setupUndoMenuButton(menu: Menu) {
        val menuUndo = menu.findItem(R.id.menu_undo)
        menuUndo?.setOnMenuItemClickListener {
            undo()
            true
        }
    }

    private fun setupRedoMenuButton(menu: Menu) {
        val menuRedo = menu.findItem(R.id.menu_redo)
        menuRedo?.setOnMenuItemClickListener {
            redo()
            true
        }
    }

    private fun setupReplayMenuButton(menu: Menu) {
        val menuReplay = menu.findItem(R.id.menu_replay)
        menuReplay?.setOnMenuItemClickListener {
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
        val menuNew = menu.findItem(R.id.menu_new)
        menuNew?.setOnMenuItemClickListener {
            cleanup(true, true)
            true
        }
    }

    private fun setupMenuListButton(menu: Menu) {
        val menuItem = menu.findItem(R.id.menu_list)
        menuItem.setOnMenuItemClickListener {
            showNotesList()
            true
        }
    }

    private fun setupMenuFeedbackButton(menu: Menu) {
        val menuItem = menu.findItem(R.id.menu_feedback)
        menuItem.setOnMenuItemClickListener {
            val url = applicationContext.getString(R.string.url_feedback)
            WebPageActivity.startWebPageActivity(this@MainActivity, url)
            true
        }

    }

    companion object {
        private val TAG = MainActivity::class.java.toString()
    }
}
