package dk.eightyplus.annoto.component

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import dk.eightyplus.annoto.utilities.FileId
import org.json.JSONException
import org.json.JSONObject

/**
 * Polygon class to be used drawing polygon objects: paths, lines, etc.
 */
class Polygon() : Component() {

    var path = CustomPath()

    override val bounds: RectF
        get() {
            val bounds = RectF()
            path.computeBounds(bounds, false)
            bounds.offset(x, y)
            bounds.right += bounds.width() * (scale - 1)
            bounds.bottom += bounds.height() * (scale - 1)
            return bounds
        }

    override val type: ComponentType
        get() = ComponentType.PolygonType

    fun getPath(): Path {
        return path
    }

    override fun onDraw(canvas: Canvas, paint: Paint) {
        if (isVisible) {
            paint.color = color
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = width

            canvas.save()
            canvas.scale(scale, scale, bounds.left, bounds.top)
            canvas.translate(x, y)
            canvas.drawPath(path, paint)
            canvas.restore()
        }
    }

    override fun centerDist(x: Float, y: Float): Float {
        return calculateCenterDistance(x, y, bounds)
    }

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        val jsonObject = super.toJson()
        jsonObject.put(FileId.PATH, path.toJson())
        return jsonObject
    }

    companion object {

        @Throws(JSONException::class)
        fun fromJson(jsonObject: JSONObject): Polygon {
            val polygon = Polygon()
            polygon.fromJsonPrimary(jsonObject)
            polygon.path.fromJson(jsonObject.getJSONObject(FileId.PATH))
            return polygon
        }
    }
}
