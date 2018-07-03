package dk.eightyplus.annoto.component

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import dk.eightyplus.annoto.utilities.FileId
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable

/**
 * Component drawing class to server as drawing interface for the Composite pattern
 */
abstract class Component : Serializable {
    var color = -0x1
    var width = 6.0f
    var scale = 1.0f
    @Transient
    var isVisible = true
    protected var x: Float = 0f
    protected var y: Float = 0f
    abstract val bounds: RectF

    abstract val type: ComponentType

    abstract fun onDraw(canvas: Canvas, paint: Paint)
    abstract fun centerDist(x: Float, y: Float): Float

    fun move(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    fun setStrokeWidth(width: Float) {
        this.width = width
    }

    protected fun calculateCenterDistance(x: Float, y: Float, bounds: RectF): Float {
        if (bounds.contains(x, y)) {
            val cx = bounds.centerX() - x
            val cy = bounds.centerY() - y
            return /* <ignore> Math.sqrt */ cx * cx + cy * cy
        }

        return java.lang.Float.MAX_VALUE
    }

    /**
     * Delete component
     * @return true if successful
     */
    open fun delete(): Boolean {
        return true
    }

    /**
     * @return json object containing primitives for all drawable components
     * @throws JSONException
     */
    @Throws(JSONException::class)
    open fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put(FileId.TYPE, type.name)
        jsonObject.put(FileId.X, x.toDouble())
        jsonObject.put(FileId.Y, y.toDouble())
        jsonObject.put(FileId.WIDTH, width.toDouble())
        jsonObject.put(FileId.SCALE, scale.toDouble())
        jsonObject.put(FileId.COLOR, color)
        return jsonObject
    }

    /**
     * General function to initialise component
     * @param object object containing data to initialise from
     * @throws JSONException
     */
    @Throws(JSONException::class)
    protected fun fromJsonPrimary(jsonObject: JSONObject) {
        this.x = jsonObject.getDouble(FileId.X).toFloat()
        this.y = jsonObject.getDouble(FileId.Y).toFloat()
        this.width = jsonObject.getDouble(FileId.WIDTH).toFloat()
        this.scale = jsonObject.getDouble(FileId.SCALE).toFloat()
        this.color = jsonObject.getInt(FileId.COLOR)
    }
}
