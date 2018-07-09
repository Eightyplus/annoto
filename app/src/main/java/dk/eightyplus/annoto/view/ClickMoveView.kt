package dk.eightyplus.annoto.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import dk.eightyplus.annoto.utilities.Compatibility

import java.lang.ref.SoftReference

/**
 * Special button to move parent view or call click listener on self
 *
 * User: fries
 * Date: 17/07/14
 * Time: 12.42
 */
class ClickMoveView : View {

    private var parentSoftReference: SoftReference<View>? = null
    private var _xDelta: Int = 0
    private var _yDelta: Int = 0
    private var touchDownTime: Long = 0

    private var onClickListener: View.OnClickListener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun setOnClickListener(l: View.OnClickListener?) {
        super.setOnClickListener(l)
        onClickListener = l
    }

    fun setParent(parent: View) {
        this.parentSoftReference = SoftReference(parent)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val parent = parentSoftReference?.get()
        if (parent != null) {
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownTime = System.currentTimeMillis()

                    val lParams = parent.layoutParams as FrameLayout.LayoutParams
                    _xDelta = x - lParams.leftMargin
                    _yDelta = y - lParams.topMargin
                }
                MotionEvent.ACTION_UP -> {
                    val touchUpTime = System.currentTimeMillis()

                    onClickListener?.let {
                        if (touchUpTime - touchDownTime < 100) {
                            Compatibility.get().callOnClick(this, it)
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val layoutParams = parent.layoutParams as FrameLayout.LayoutParams
                    layoutParams.leftMargin = x - _xDelta
                    layoutParams.topMargin = y - _yDelta
                    if (layoutParams.leftMargin < 0) {
                        layoutParams.leftMargin = 0
                    }
                    if (layoutParams.topMargin < 0) {
                        layoutParams.topMargin = 0
                    }

                    parent.layoutParams = layoutParams
                }
            }
            return true
        } else {
            return super.onTouchEvent(event)
        }
    }

    companion object {

        private val TAG = ClickMoveView::class.java.toString()
    }
}
