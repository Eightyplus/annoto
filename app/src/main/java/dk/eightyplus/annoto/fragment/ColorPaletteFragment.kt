package dk.eightyplus.annoto.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import dk.eightyplus.annoto.Callback
import dk.eightyplus.annoto.Keys
import dk.eightyplus.annoto.R
import dk.eightyplus.annoto.dialog.ColorPickerDialog
import dk.eightyplus.annoto.dialog.ColorPickerView
import java.lang.ref.SoftReference

/**
 * User: fries
 * Date: 15/07/14
 * Time: 14.00
 */
class ColorPaletteFragment : DialogFragment() {

    private var callbackSoftReference: SoftReference<Callback>? = null
    private var softPreferences: SoftReference<SharedPreferences>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.callbackSoftReference = SoftReference<Callback>(activity as Callback?)
        this.softPreferences = SoftReference<SharedPreferences>(activity?.getSharedPreferences(Keys.PREFERENCES, Context.MODE_PRIVATE))
    }

    fun preference(key: String, defaultValue: Int = -1): Int {
        val preferences = softPreferences?.get()
        return preferences?.getInt(key, defaultValue) ?: defaultValue
    }

    fun saveColorPreference(color: Int) {
        val preferences = softPreferences?.get() ?: return
        var lastColorSlot = preference(Keys.COLOR_LAST, -1)

        lastColorSlot++
        if (lastColorSlot > 5) {
            lastColorSlot = 0
        }

        val edit = preferences.edit()
        edit.putInt(Keys.COLOR + lastColorSlot, color)
        edit.putInt(Keys.COLOR_LAST, lastColorSlot)
        edit.apply()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.color_palette, container, false)

        val callback = callbackSoftReference?.get() ?: return view

        val linearLayoutTop = view.findViewById<View>(R.id.color_top) as LinearLayout
        val blackWhite = view.findViewById<View>(R.id.color_static1) as LinearLayout
        val grayScale = view.findViewById<View>(R.id.color_static2) as LinearLayout
        val rainbow = view.findViewById<View>(R.id.color_rainbow) as LinearLayout
        val linearLayoutDynamic = view.findViewById<View>(R.id.color_dynamic) as LinearLayout

        val bwColors = intArrayOf(R.color.black, R.color.white)
        for (color in bwColors) {
            val hexColor = callback.hexColorForResourceId(color)

            val button = inflater.inflate(R.layout.color_button, blackWhite, false) as ImageButton
            button.setBackgroundColor(hexColor)
            blackWhite.addView(button)

            button.setOnClickListener(ButtonColorClickListener(callback, hexColor))
        }
        val randomButton = inflater.inflate(R.layout.color_button, blackWhite, false) as ImageButton
        randomButton.setBackgroundResource(android.R.drawable.ic_menu_help)
        randomButton.setOnClickListener(ButtonColorClickListener(callback, 0))
        blackWhite.addView(randomButton)

        val grayColors = intArrayOf(R.color.dark_grey, R.color.gray, R.color.light_grey)
        for (color in grayColors) {
            val hexColor = callback.hexColorForResourceId(color)

            val button = inflater.inflate(R.layout.color_button, grayScale, false) as ImageButton
            button.setBackgroundColor(hexColor)
            grayScale.addView(button)

            button.setOnClickListener(ButtonColorClickListener(callback, hexColor))
        }

        val colorPickerView = ColorPickerView(inflater.context, object : ColorPickerDialog.OnColorChangedListener {
            private var lastColor = 0

            override fun colorChanged(color: Int) {
                callback.colorChanged(color)

                if (lastColor != color) {
                    lastColor = color
                    saveColorPreference(color)
                    updatePickedColors(linearLayoutDynamic, inflater, callback)
                    linearLayoutDynamic.invalidate()
                }
            }
        }, 0)
        linearLayoutTop.addView(colorPickerView)

        val colors = intArrayOf(R.color.red, R.color.Orange, R.color.yellow, R.color.green, R.color.blue, R.color.purple)
        for (color in colors) {
            val hexColor = callback.hexColorForResourceId(color)

            val button = inflater.inflate(R.layout.color_button, rainbow, false) as ImageButton
            button.setBackgroundColor(hexColor)
            rainbow.addView(button)

            button.setOnClickListener(ButtonColorClickListener(callback, hexColor))
        }

        updatePickedColors(linearLayoutDynamic, inflater, callback)

        return view
    }

    private fun updatePickedColors(linearLayoutDynamic: LinearLayout, inflater: LayoutInflater, callback: Callback) {
        linearLayoutDynamic.removeAllViews()
        val lastColorSlot = preference(Keys.COLOR_LAST, -1)
        if (lastColorSlot >= 0) {
            for (i in 0..5) {
                val hexWhiteColor = callback.hexColorForResourceId(R.color.white)
                val hexColor = preference(Keys.COLOR + i, hexWhiteColor)

                val button = inflater.inflate(R.layout.color_button, linearLayoutDynamic, false) as ImageButton
                button.setBackgroundColor(hexColor)
                linearLayoutDynamic.addView(button)

                button.setOnClickListener(ButtonColorClickListener(callback, hexColor))
            }
        }
    }


    private class ButtonColorClickListener(callback: Callback, private val color: Int) : View.OnClickListener {

        private val callbackSoftReference: SoftReference<Callback> = SoftReference(callback)
        private var animation: Animation? = null

        override fun onClick(v: View) {
            val callback = callbackSoftReference.get() ?: return
            callback.colorChanged(color)
            v.startAnimation(getAnimation())
        }

        private fun getAnimation(): Animation {
            var animation = animation
            if (animation == null) {
                animation = AlphaAnimation(1f, 0f)
                animation.duration = 50
                animation.interpolator = LinearInterpolator()
                this.animation = animation
            }
            return animation
        }
    }
}
