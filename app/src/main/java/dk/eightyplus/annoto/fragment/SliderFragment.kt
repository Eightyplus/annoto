package dk.eightyplus.annoto.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import dk.eightyplus.annoto.Callback
import dk.eightyplus.annoto.Keys
import dk.eightyplus.annoto.R

/**
 * Fragment for width slider
 */
class SliderFragment : DialogFragment {

    private var callback: Callback? = null
    private var width: Int = 0

    constructor() {}

    @SuppressLint("ValidFragment")
    constructor(callback: Callback, width: Int) {
        this.callback = callback
        this.width = width
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            width = savedInstanceState.getInt(Keys.WIDTH)
        }

        callback = activity as Callback?
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Keys.WIDTH, width)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.slider, container, false)

        val slider = view.findViewById<View>(R.id.slider) as SeekBar
        slider.progress = width
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                callback!!.setStrokeWidth(seekBar.progress)
            }
        })

        return view
    }
}
