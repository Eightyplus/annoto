package dk.eightyplus.annoto.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import dk.eightyplus.annoto.utilities.FileId
import dk.eightyplus.annoto.utilities.NoteStorage
import org.json.JSONException
import org.json.JSONObject

/**
 * Composite class to server as drawing interface/component collection for the Composite pattern
 */
class Composite : Component() {

    protected var components = mutableListOf<Component>()

    override val bounds: RectF
        get() {
            val bounds = RectF(java.lang.Float.MAX_VALUE, java.lang.Float.MAX_VALUE, java.lang.Float.MIN_VALUE, java.lang.Float.MIN_VALUE)
            if (hasComponents()) {
                for (component in components) {
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
        return components.size > 0
    }

    override fun onDraw(canvas: Canvas, paint: Paint) {
        if (isVisible && hasComponents()) {
            with(canvas) {
                save()
                val bounds = bounds
                scale(scale, scale, bounds.left, bounds.top)
                translate(x, y)
                for (component in components) {
                    component.onDraw(this, paint)
                }
                restore()
            }
        }

    }

    fun add(component: Component) {
        components.add(component)
    }

    fun remove(component: Component) {
        components.remove(component)
    }

    fun removeLast(): Component? {
        return if (hasComponents()) {
            components.removeAt(components.size - 1)
        } else null
    }

    fun getChild(location: Int): Component? {
        return if (location < components.size) {
            components[location]
        } else null
    }

    override fun centerDist(x: Float, y: Float): Float {
        return if (hasComponents()) {
            val centerDistance = calculateCenterDistance(x, y, bounds)
            components.map { it.centerDist(x, y) }.fold(centerDistance) { min, dist ->
                if (dist < min) dist else min
            }
        } else Float.MAX_VALUE
    }

    override fun delete(): Boolean {
        return super.delete() && components.map { it.delete() }.reduce { result, del -> result && del }
    }

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        return super.toJson().apply {
            put(FileId.SIZE, components.size)
            if (components.size > 0) {
                NoteStorage.toJson(this, components)
            }
        }
    }

    companion object {

        @Throws(JSONException::class)
        fun fromJson(context: Context, jsonObject: JSONObject): Composite {
            return Composite().apply {
                fromJsonPrimary(jsonObject)
                if (jsonObject.getInt(FileId.SIZE) > 0) {
                    components.addAll(NoteStorage.fromJson(context, jsonObject))
                }
            }
        }
    }
}
