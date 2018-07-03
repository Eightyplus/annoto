package dk.eightyplus.annoto.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * User: fries
 * Date: 19/08/14
 * Time: 08.49
 */
class MovableLinearLayout : LinearLayout {
    private var _xDelta: Int = 0
    private var _yDelta: Int = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {

                val lParams = layoutParams as FrameLayout.LayoutParams
                _xDelta = x - lParams.leftMargin
                _yDelta = y - lParams.topMargin
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_MOVE -> {
                val layoutParams = layoutParams as FrameLayout.LayoutParams
                layoutParams.leftMargin = x - _xDelta
                layoutParams.topMargin = y - _yDelta

                if (layoutParams.leftMargin < 0) {
                    layoutParams.leftMargin = 0
                }
                if (layoutParams.topMargin < 0) {
                    layoutParams.topMargin = 0
                }
                setLayoutParams(layoutParams)
            }
        }
        return true
    }
}
