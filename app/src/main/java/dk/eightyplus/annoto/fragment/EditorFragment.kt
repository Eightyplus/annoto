package dk.eightyplus.annoto.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import dk.eightyplus.annoto.Callback
import dk.eightyplus.annoto.R
import dk.eightyplus.annoto.action.State
import dk.eightyplus.annoto.action.Undo
import dk.eightyplus.annoto.component.Component
import dk.eightyplus.annoto.component.Text
import dk.eightyplus.annoto.view.EditTextCustom

/**
 * Changes returned from fragment
 */
class TextChanges(val component: Component, val undo: Undo)

/**
 * EditorFragment for presenting edit text
 */
@SuppressLint("ValidFragment")
class EditorFragment(private val callback: Callback, private var component: Text?,
                     private val x: Float, private val y: Float) : DialogFragment() {
    private lateinit var editText: EditTextCustom

    val textChanges: TextChanges?
        get() = editText.text.run {
            editText.clearFocus()
            if(isNotEmpty()) {
                val text = this.toString()
                val undoText = component?.text
                val state = if (component == null) State.Add else State.Text
                val component = component ?: Text().apply {
                    move(x, y)
                    component = this
                 }
                component.text = text
                return TextChanges(component, Undo(component, state, undoText))
             }
             null
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.editor, container, false)

        editText = (view.findViewById<View>(R.id.edit) as EditTextCustom).apply {
            val keyListener: View.OnKeyListener = View.OnKeyListener { _, _, _ ->
                callback.textEditDone()
                true
            }
            setOnKeyBoardDownListener(keyListener)

            component?.text?.let {
                setText(it)
            }

            setOnEditorActionListener { _, _, _ ->
                callback.textEditDone()
                false
            }
        }

        showKeyboard()
        return view
    }

    private fun showKeyboard() {
        Handler().post {
            if (editText.requestFocus()) {
                (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).run {
                    showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
    }
}
