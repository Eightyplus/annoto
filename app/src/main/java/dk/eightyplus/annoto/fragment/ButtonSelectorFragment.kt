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

/**
 * User: fries
 * Date: 17/07/14
 * Time: 07.46
 */
@SuppressLint("ValidFragment")
class ButtonSelectorFragment(private var icons: IntArray = IntArray(0),
                             private var tags: Array<String> = emptyArray()) : DialogFragment() {
    private var unfold = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        with(outState) {
            putInt(Keys.LENGTH, icons.size)
            for (i in icons.indices) {
                putInt(Keys.ICON + i, icons[i])
                putString(Keys.TAG + i, tags[i])
            }
            putBoolean(Keys.STATE, unfold)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            with (savedInstanceState) {
                getInt(Keys.LENGTH).let {
                    icons = IntArray(it) { getInt(Keys.ICON + it) }
                    tags = Array(it) { getString(Keys.TAG + it) }
                }
                unfold = getBoolean(Keys.STATE, false)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.button_menu_layout, container, false)
        val dynamicButtons = view.findViewById<View>(R.id.dynamic_buttons) as ViewGroup

        val buttons = icons.indices.map {
            val buttonsView = inflater.inflate(R.layout.button_item, dynamicButtons, false).apply {
                findViewById<View>(R.id.icon).setBackgroundResource(icons[it])
            }
            dynamicButtons.addView(buttonsView)
            buttonsView.findViewById<View>(R.id.button).apply {
                tag = tags[it]
                isSelected = it == 0
            }
        }

        val callback: SoftReference<Callback> = SoftReference(activity as Callback)
        for (button in buttons) {
            button.setOnClickListener(ToggleButtonClickListener(callback, buttons, button.tag as String))
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

    private class ToggleButtonClickListener(private val callback: SoftReference<Callback>,
                                            private val deselect: List<View>,
                                            private val tag: String) : View.OnClickListener {

        override fun onClick(v: View) {
            val callback = this.callback.get() ?: return
            for (view in deselect) {
                view.isSelected = false
            }
            v.isSelected = true
            callback.state = State.state(tag)
        }
    }
}
