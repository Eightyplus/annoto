package dk.eightyplus.annoto.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.EditText

/**
 * User: fries
 * Date: 20/03/14
 * Time: 22.44
 */
class EditTextCustom : EditText {

        private var keyListener: View.OnKeyListener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    fun setOnKeyBoardDownListener(keyListener: View.OnKeyListener) {
        this.keyListener = keyListener
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            if (keyListener?.onKey(this, keyCode, event) == true) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
