package dk.eightyplus.annoto.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import dk.eightyplus.annoto.Callback
import dk.eightyplus.annoto.component.Component
import dk.eightyplus.annoto.utilities.Compatibility
import java.lang.ref.SoftReference

/**
 * MoveView to put a single component into and show moves by user
 */
@SuppressLint("ViewConstructor")
class MoveView(context: Context, private var component: Component, callBack: Callback) : View(context) {

    private val callbackSoftReference = SoftReference(callBack)

    private var _xDelta: Int = 0
    private var _yDelta: Int = 0
    private val mPaint: Paint = Paint()

    private var xOffsetComponent: Float = 0.toFloat()
    private var yOffsetComponent: Float = 0.toFloat()
    private val margin = 20

    private val initialScale: Float = component.scale
    private var scaleFactor = 1.0f
    private var width: Float = 0.toFloat()
    private var height: Float = 0.toFloat()
    private var oldDist: Float = 0.toFloat()
    private var points = 1

    init {
        with (mPaint) {
            isAntiAlias = true
            isDither = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 12f
        }
        Compatibility.get().setHardwareAccelerated(this, mPaint)

        setupSize()
    }

    private fun setupSize() {
        with(component.bounds) {
            xOffsetComponent = this.left - margin
            yOffsetComponent = this.top - margin

            width = this.width() + 2 * margin
            height = this.height() + 2 * margin
        }
        setViewBounds()
    }

    private fun moveComponentFromMoveView(dx: Float, dy: Float) {
        callbackSoftReference.get()?.move(component, dx - xOffsetComponent, dy - yOffsetComponent, initialScale * scaleFactor)
    }

    private fun setViewBounds() {
        val layoutParams = RelativeLayout.LayoutParams(width.toInt(), height.toInt())
        layoutParams.leftMargin = xOffsetComponent.toInt()
        layoutParams.topMargin = yOffsetComponent.toInt()
        setLayoutParams(layoutParams)
    }

    override fun onDraw(canvas: Canvas) {
        with (canvas) {
            save()
            scale(scaleFactor, scaleFactor)
            translate(-xOffsetComponent, -yOffsetComponent)
            component.isVisible = true
            component.onDraw(this, mPaint)
            component.isVisible = false
            restore()
            drawDashBounds(this)
        }
    }

    private fun drawDashBounds(canvas: Canvas) {
        val rect = RectF(0f, 0f, (getWidth() - 1).toFloat(), (getHeight() - 1).toFloat())

        with (mPaint) {
            color = -0x1000000
            strokeWidth = 1.0f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)
            canvas.drawRoundRect(rect, 5.0f, 5.0f, this)
            pathEffect = null
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val X = event.rawX.toInt()
        val Y = event.rawY.toInt()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val lParams = layoutParams as RelativeLayout.LayoutParams
                _xDelta = X - lParams.leftMargin
                _yDelta = Y - lParams.topMargin
            }
            MotionEvent.ACTION_UP -> {
                val dx = (X - _xDelta).toFloat()
                val dy = (Y - _yDelta).toFloat()
                moveComponentFromMoveView(dx, dy)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
            }
            MotionEvent.ACTION_POINTER_UP -> {
            }
            MotionEvent.ACTION_MOVE -> {

                val layoutParams = layoutParams as RelativeLayout.LayoutParams

                if (points != event.pointerCount) {
                    points = event.pointerCount

                    when (points) {
                        2 -> {
                            oldDist = calculateDistance(event) / scaleFactor
                            _xDelta = X - layoutParams.leftMargin
                            _yDelta = Y - layoutParams.topMargin
                        }
                        1 -> {
                            _xDelta = X - layoutParams.leftMargin
                            _yDelta = Y - layoutParams.topMargin
                        }
                        else -> {
                        }
                    }
                }

                layoutParams.leftMargin = X - _xDelta
                layoutParams.topMargin = Y - _yDelta

                if (points > 1) {
                    scaleFactor = calculateDistance(event) / oldDist
                    val scaleTotal = scaleFactor * initialScale
                    if (scaleTotal < 0.1f) {
                        scaleFactor = 0.1f / initialScale
                    } else if (scaleTotal > 5.0f) {
                        scaleFactor = 5.0f / initialScale
                    }

                    layoutParams.width = (width * scaleFactor).toInt()
                    layoutParams.height = (height * scaleFactor).toInt()
                }

                setLayoutParams(layoutParams)
            }
        }
        return true
    }

    private fun calculateDistance(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }
}

