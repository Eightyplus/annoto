package dk.eightyplus.annoto.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eightyplus.annoto.Callback
import dk.eightyplus.annoto.Keys
import dk.eightyplus.annoto.R
import dk.eightyplus.annoto.action.State
import dk.eightyplus.annoto.view.ClickMoveView

import java.lang.ref.SoftReference
import java.util.ArrayList

/**
 * User: fries
 * Date: 17/07/14
 * Time: 07.46
 */
class ButtonSelectorFragment : DialogFragment {

    private var callbackSoftReference: SoftReference<Callback>? = null
    private var unfold = false

    private var icons: IntArray? = null
    private var tags: Array<String>? = null

    constructor() {}

    @SuppressLint("ValidFragment")
    constructor(icons: IntArray, tags: Array<String>) {
        this.icons = icons
        this.tags = tags
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(Keys.LENGTH, icons!!.size)
        for (i in icons!!.indices) {
            outState.putInt(Keys.ICON + i, icons!![i])
            outState.putString(Keys.TAG + i, tags!![i])
        }
        outState.putBoolean(Keys.STATE, unfold)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackSoftReference = SoftReference(activity as Callback)

        if (savedInstanceState != null) {
            val length = savedInstanceState.getInt(Keys.LENGTH)
            if (length > 0) {
                icons = IntArray(length) { savedInstanceState.getInt(Keys.ICON + it) }
                tags = Array(length) { savedInstanceState.getString(Keys.TAG + it) }
            }
            unfold = savedInstanceState.getBoolean(Keys.STATE, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.button_menu_layout, container, false)
        val dynamicButtons = view.findViewById<View>(R.id.dynamic_buttons) as ViewGroup

        val buttons = ArrayList<View>()

        for (i in icons!!.indices) {
            val icon = icons!![i]
            val tag = tags!![i]

            val buttonsView = inflater.inflate(R.layout.button_item, dynamicButtons, false)
            buttonsView.findViewById<View>(R.id.icon).setBackgroundResource(icon)

            val button = buttonsView.findViewById<View>(R.id.button)
            button.tag = tag
            buttons.add(button)
            dynamicButtons.addView(buttonsView)

            if (i == 0) {
                button.isSelected = true
            }
        }

        for (button in buttons) {
            button.setOnClickListener(ToggleButtonClickListener(callbackSoftReference, buttons, button.tag as String))
        }

        val buttonFold = view.findViewById<View>(R.id.button_fold) as ClickMoveView
        val buttonUnfold = view.findViewById<View>(R.id.button_unfold) as ClickMoveView
        buttonFold.setParent(view)
        buttonUnfold.setParent(view)

        buttonUnfold.setOnClickListener {
            buttonFold.visibility = View.VISIBLE
            buttonUnfold.visibility = View.GONE
            dynamicButtons.visibility = View.VISIBLE
            unfold = true
        }

        buttonFold.setOnClickListener {
            buttonFold.visibility = View.GONE
            buttonUnfold.visibility = View.VISIBLE
            dynamicButtons.visibility = View.GONE
            unfold = false
        }

        return view
    }

    private class ToggleButtonClickListener(private val callback: SoftReference<Callback>?,
                                            private val deselect: List<View>,
                                            private val tag: String) : View.OnClickListener {

        override fun onClick(v: View) {
            val callback = this.callback?.get() ?: return
            for (view in deselect) {
                view.isSelected = false
            }
            v.isSelected = true
            callback.state = State.state(tag)
        }
    }
}
