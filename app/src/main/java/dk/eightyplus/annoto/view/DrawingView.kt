package dk.eightyplus.annoto.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.View
import dk.eightyplus.annoto.Callback
import dk.eightyplus.annoto.Tags
import dk.eightyplus.annoto.action.State
import dk.eightyplus.annoto.action.Undo
import dk.eightyplus.annoto.component.Component
import dk.eightyplus.annoto.component.Composite
import dk.eightyplus.annoto.component.Polygon
import dk.eightyplus.annoto.utilities.Compatibility
import dk.eightyplus.annoto.utilities.NoteStorage
import dk.eightyplus.annoto.utilities.SaveLoad
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.ArrayList

const val COMPONENT = "COMPONENT_"
const val TOUCH_TOLERANCE = 4f
const val STROKE_DELTA = 0.001f
const val STROKE_INCREMENT = 0.1f

/**
 *
 */
class DrawingView(context: Context, private val callback: Callback) : View(context), ComponentList, SaveLoad {

    private val components = ArrayList<Component>()
    private val mPaint: Paint

    lateinit var bitmap: Bitmap
    private lateinit var mCanvas: Canvas

    private lateinit var drawingComponent: Component
    private lateinit var composite: Composite
    private lateinit var polygon: Polygon

    private val mBitmapPaint: Paint

    private var drawingColor = -0x1000000
    var color = -0x1000000

    private var mX: Float = 0.0f
    private var mY: Float = 0.0f
    private var currentStrokeModify = 1.0f
    var strokeWidth = 6
        set(value) {
            field = value
            saveStrokeWidth(value)
        }

    private var variableWidth = true
    private var redrawing = false

    val numComponents: Int
        get() = this.components.size

    init {

        getSavedStrokeWidth()

        touch_reset()
        mBitmapPaint = Paint(Paint.DITHER_FLAG)

        mPaint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        Compatibility.get().setHardwareAccelerated(this, mPaint)
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        components.clear()
        var i = 0
        var component: Any?

        loop@ while (true) {
            component = savedInstanceState.get(COMPONENT + i++)
            when (component) {
                is Component -> components.add(component)
                null -> break@loop
            }
        }
    }

    fun onSaveInstanceState(bundle: Bundle) {
        components.forEachIndexed { i, component ->
            bundle.putSerializable(COMPONENT + i, component)
        }
    }

    @Throws(IOException::class)
    override fun save(context: Context, outputStream: DataOutputStream) {
        NoteStorage.save(context, ArrayList(this.components), outputStream)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun load(context: Context, inputStream: DataInputStream) {
        val components = NoteStorage.load(context, inputStream)

        this.components.clear()
        this.components.addAll(components)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(bitmap)
        redraw()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(-0x555556)
        canvas.drawBitmap(bitmap, 0f, 0f, mBitmapPaint)

        drawingComponent.onDraw(canvas, mPaint)
    }

    private fun touch_start(x: Float, y: Float, pressure: Float) {
        setDrawingColor()

        polygon.color = drawingColor
        currentStrokeModify = pressure
        polygon.path.reset()
        polygon.setStrokeWidth(getStrokeWidth(pressure))
        polygon.path.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touch_move(x: Float, y: Float, pressure: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            polygon.setStrokeWidth(getStrokeWidth(pressure))

            val xEndPoint = (x + mX) / 2
            val yEndPoint = (y + mY) / 2
            polygon.path.quadTo(mX, mY, xEndPoint, yEndPoint)

            if (variableWidth) {
                composite.add(polygon)
                polygon = Polygon()
                polygon.path.moveTo(xEndPoint, yEndPoint)
                polygon.color = drawingColor
            }
            mX = x
            mY = y
        }
    }

    private fun touch_up() {
        polygon.path.lineTo(mX, mY)
        drawingComponent.onDraw(mCanvas, mPaint)

        components.add(drawingComponent)
        callback.add(Undo(drawingComponent, State.DrawPath))

        touch_reset()
    }

    private fun touch_reset() {
        currentStrokeModify = 0.5f

        polygon = Polygon()
        polygon.setStrokeWidth(strokeWidth.toFloat())

        if (variableWidth) {
            composite = Composite()
            drawingComponent = composite
        } else {
            drawingComponent = polygon
        }
    }

    private fun clear() {
        val w = bitmap.width
        val h = bitmap.height
        bitmap.recycle()

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(bitmap)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val p = event.pressure

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touch_start(x, y, p)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touch_move(x, y, p)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touch_up()
                invalidate()
            }
        }

        return true
    }

    override fun add(component: Component) {
        if (!components.contains(component)) {
            components.add(component)
        }
    }

    override fun remove(component: Component) {
        components.remove(component)
    }

    fun findComponent(x: Float, y: Float): Component? {
        var moveComponent: Component? = null
        var minimumDistance = java.lang.Float.MAX_VALUE
        for (component in components) {
            val distance = component.centerDist(x, y)
            if (distance < minimumDistance) {
                moveComponent = component
                minimumDistance = distance
            }
        }
        return moveComponent
    }

    private fun setDrawingColor() {
        drawingColor = if (this.color != 0) this.color else (-0x1000000 + 0x00FFFFFF * Math.random()).toInt()
    }

    fun reinitialise(delete: Boolean, invalidate: Boolean) {
        clear()
        delete(delete)
        if (invalidate) {
            invalidate()
        }
    }

    fun delete(delete: Boolean) {
        if (delete) {
            for (component in components) {
                component.delete()
            }
        }
        components.clear()
    }

    fun redraw() {
        if (redrawing) {
            return
        }
        redrawing = true
        clear()
        handler.post {
            for (component in components) {
                component.onDraw(mCanvas, mPaint)
            }

            redrawing = false
            invalidate()
        }
    }

    fun redraw(delay: Int, randomColor: Boolean) {
        clear()

        Thread(Runnable {
            for (i in components.indices) {
                val component = components[i]

                if (delay > 0) {
                    try {
                        Thread.sleep(delay.toLong())
                    } catch (e: InterruptedException) {

                    }

                }
                val color = if (randomColor) (-0x1000000 + 0x00FFFFFF * Math.random()).toInt() else component.color
                component.color = color
                handler.post {
                    component.onDraw(mCanvas, mPaint)
                    invalidate()
                }
            }
        }).start()
    }

    fun move(component: Component, dx: Float, dy: Float, scale: Float) {
        val bounds = component.bounds
        callback.add(Undo(component, State.Move, x = bounds.left, y = bounds.top, scale = component.scale))
        component.scale = scale
        component.move(dx, dy)

        if (!components.contains(component)) {
            components.add(component)
        }
        redraw()
    }

    private fun getStrokeWidth(eventPressure: Float): Float {
        if (variableWidth) {
            if (Math.abs(eventPressure - currentStrokeModify) > STROKE_DELTA) {
                currentStrokeModify = if (eventPressure > currentStrokeModify) {
                    Math.min(eventPressure, currentStrokeModify + STROKE_INCREMENT)
                } else {
                    Math.max(eventPressure, currentStrokeModify - STROKE_INCREMENT)
                }
            }
        }
        return strokeWidth * currentStrokeModify
    }

    private fun saveStrokeWidth(strokeWidth: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().run {
            putInt(Tags.STROKE_WIDTH, strokeWidth)
        }.apply()
    }

    private fun getSavedStrokeWidth() {
        strokeWidth = PreferenceManager.getDefaultSharedPreferences(context).run {
            getInt(Tags.STROKE_WIDTH, strokeWidth)
        }
    }

    companion object {
        private val TAG = DrawingView::class.java.toString()
    }
}
