package dk.eightyplus.annoto.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.util.Pair
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import dk.eightyplus.annoto.Callback
import dk.eightyplus.annoto.R
import dk.eightyplus.annoto.action.State
import dk.eightyplus.annoto.action.Undo
import dk.eightyplus.annoto.component.Component
import dk.eightyplus.annoto.component.Text
import dk.eightyplus.annoto.view.EditTextCustom

/**
 * EditorFragment for presenting edit text
 */
@SuppressLint("ValidFragment")
class EditorFragment(private val callback: Callback, private var component: Text?,
                     private val x: Float, private val y: Float) : DialogFragment() {
    private var editText: EditTextCustom? = null

    val textChanges: Pair<Text, Undo>
        get() {
            var undo: Undo? = null
            val text = editText!!.text.toString()
            if (text.isNotEmpty()) {
                var component = component
                if (component == null) {
                    component = Text()
                    this.component = component
                    component.move(x, y)
                    undo = Undo(component, State.Add)
                } else {
                    undo = Undo(component, component.text, State.Text)
                }
                component.text = text
            }
            return Pair<Text, Undo>(component, undo)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.editor, container, false)

        val editText = view.findViewById<View>(R.id.edit) as EditTextCustom

        val keyListener: View.OnKeyListener = View.OnKeyListener { _, _, _ ->
            callback.textEditDone()
            true
        }
        editText.setOnKeyBoardDownListener(keyListener)

        val textComponent = component
        if (textComponent != null) {
            editText.setText(textComponent.text)
        }

        editText.setOnEditorActionListener { v, actionId, event ->
            callback.textEditDone()
            false
        }
        this.editText = editText

        showKeyboard()
        return view
    }

    private fun showKeyboard() {
        Handler().post {
            if (editText!!.requestFocus()) {
                val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    companion object {
        private val TAG = EditorFragment::class.java.toString()
    }
}
