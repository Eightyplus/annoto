package dk.eightyplus.annoto.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import dk.eightyplus.annoto.utilities.FileId
import dk.eightyplus.annoto.utilities.NoteStorage
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList

/**
 * Composite class to server as drawing interface/component collection for the Composite pattern
 */
class Composite : Component() {

    protected var componentList = mutableListOf<Component>()

    override val bounds: RectF
        get() {
            val bounds = RectF(java.lang.Float.MAX_VALUE, java.lang.Float.MAX_VALUE, java.lang.Float.MIN_VALUE, java.lang.Float.MIN_VALUE)
            if (hasComponents()) {
                for (component in componentList) {
                    val componentBounds = component.bounds

                    if (componentBounds.left < bounds.left) {
                        bounds.left = componentBounds.left
                    }

                    if (componentBounds.top < bounds.top) {
                        bounds.top = componentBounds.top
                    }

                    if (componentBounds.right > bounds.right) {
                        bounds.right = componentBounds.right
                    }

                    if (componentBounds.bottom > bounds.bottom) {
                        bounds.bottom = componentBounds.bottom
                    }

                }

                bounds.offset(x, y)
                bounds.right += bounds.width() * (scale - 1)
                bounds.bottom += bounds.height() * (scale - 1)
            }
            return bounds
        }

    override val type: ComponentType
        get() = ComponentType.CompositeType

    private fun hasComponents(): Boolean {
        return componentList.size > 0
    }

    override fun onDraw(canvas: Canvas, paint: Paint) {
        if (isVisible && hasComponents()) {
            canvas.save()

            val bounds = bounds
            canvas.scale(scale, scale, bounds.left, bounds.top)
            canvas.translate(x, y)
            for (component in componentList) {
                component.onDraw(canvas, paint)
            }
            canvas.restore()
        }

    }

    fun add(component: Component) {
        componentList.add(component)
    }

    fun remove(component: Component) {
        componentList.remove(component)
    }

    fun removeLast(): Component? {
        return if (hasComponents()) {
            componentList.removeAt(componentList.size - 1)
        } else null
    }

    fun getChild(location: Int): Component? {
        return if (location < componentList.size) {
            componentList[location]
        } else null
    }

    override fun centerDist(x: Float, y: Float): Float {
        var minimumDistance = java.lang.Float.MAX_VALUE
        if (hasComponents()) {
            minimumDistance = calculateCenterDistance(x, y, bounds)
            for (component in componentList) {
                val distance = component.centerDist(x, y)
                if (distance < minimumDistance) {
                    minimumDistance = distance
                }
            }
        }

        return minimumDistance
    }

    override fun delete(): Boolean {
        var result = super.delete()
        if (hasComponents()) {
            for (component in componentList) {
                result = result and component.delete()
            }
        }
        return result
    }

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        val jsonObject = super.toJson()

        jsonObject.put(FileId.SIZE, componentList.size)
        if (componentList.size > 0) {
            NoteStorage.toJson(jsonObject, componentList)
        }
        return jsonObject
    }

    companion object {

        @Throws(JSONException::class)
        fun fromJson(context: Context, jsonObject: JSONObject): Composite {
            val composite = Composite()
            composite.fromJsonPrimary(jsonObject)

            val size = jsonObject.getInt(FileId.SIZE)
            if (size > 0) {
                composite.componentList = ArrayList()
                NoteStorage.fromJson(context, jsonObject, composite.componentList)
            }

            return composite
        }
    }
}
