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
            return RectF().apply {
                path.computeBounds(this, false)
                offset(x, y)
                right += width() * (scale - 1)
                bottom += height() * (scale - 1)
            }
        }

    override val type: ComponentType
        get() = ComponentType.PolygonType

    fun getPath(): Path {
        return path
    }

    override fun onDraw(canvas: Canvas, paint: Paint) {
        if (isVisible) {
            with(paint) {
                color = this@Polygon.color
                style = Paint.Style.STROKE
                strokeWidth = width
            }

            with(canvas) {
                save()
                scale(scale, scale, bounds.left, bounds.top)
                translate(x, y)
                drawPath(path, paint)
                restore()
            }
        }
    }

    override fun centerDist(x: Float, y: Float): Float {
        return calculateCenterDistance(x, y, bounds)
    }

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        return super.toJson().apply {
            put(FileId.PATH, path.toJson())
        }
    }

    companion object {

        @Throws(JSONException::class)
        fun fromJson(jsonObject: JSONObject): Polygon {
            return Polygon().apply {
                fromJsonPrimary(jsonObject)
                path.fromJson(jsonObject.getJSONObject(FileId.PATH))
            }
        }
    }
}
