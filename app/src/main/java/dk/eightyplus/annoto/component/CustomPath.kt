package dk.eightyplus.annoto.component

import android.graphics.Path
import dk.eightyplus.annoto.utilities.FileId
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable
import java.util.ArrayList


enum class PathActionType {
    LINE_TO, MOVE_TO, OFFSET, QUAD_TO
}

/**
 * CustomPath extends Path the only purpose of being Serializable, so it can be save and loaded from file
 */
class CustomPath() : Path(), Serializable {

    private var actions = ArrayList<CustomPath.PathAction>()

    override fun moveTo(x: Float, y: Float) {
        actions.add(ActionMove(x, y))
        super.moveTo(x, y)
    }

    override fun lineTo(x: Float, y: Float) {
        actions.add(ActionLine(x, y))
        super.lineTo(x, y)
    }

    override fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        actions.add(ActionQuadTo(x1, y1, x2, y2))
        super.quadTo(x1, y1, x2, y2)
    }

    override fun offset(dx: Float, dy: Float) {
        actions.add(ActionOffset(dx, dy))
        super.offset(dx, dy)
    }

    private fun drawThisPath() {
        for (p in actions) {
            when (p.type) {
                PathActionType.MOVE_TO -> super.moveTo(p.x, p.y)
                PathActionType.LINE_TO -> super.lineTo(p.x, p.y)
                PathActionType.QUAD_TO -> super.quadTo(p.x, p.y, p.x2, p.y2)
                PathActionType.OFFSET -> super.offset(p.x, p.x)
            }
        }
    }

    interface PathAction : Serializable {
        val type: PathActionType
        val x: Float
        val y: Float
        val x2: Float
        val y2: Float

        @Throws(JSONException::class)
        fun toJson(): JSONObject
    }

    class ActionMove(x: Float, y: Float) : ActionLine(x, y) {

        override val type = PathActionType.MOVE_TO

        companion object {

            @Throws(JSONException::class)
            fun fromJson(jsonObject: JSONObject): ActionMove {
                return ActionMove(jsonObject.getDouble(FileId.X).toFloat(), jsonObject.getDouble(FileId.Y).toFloat())
            }
        }
    }

    open class ActionLine(override val x: Float, override val y: Float) : PathAction {

        override val type = PathActionType.LINE_TO

        override val x2: Float
            get() = 0f

        override val y2: Float
            get() = 0f

        @Throws(JSONException::class)
        override fun toJson(): JSONObject {
            return JSONObject().apply {
                put(FileId.TYPE, type.name)
                put(FileId.X, x.toDouble())
                put(FileId.Y, y.toDouble())
            }
        }

        companion object {

            @Throws(JSONException::class)
            fun fromJson(jsonObject: JSONObject): ActionLine {
                return ActionLine(jsonObject.getDouble(FileId.X).toFloat(), jsonObject.getDouble(FileId.Y).toFloat())
            }
        }
    }

    class ActionQuadTo(x: Float, y: Float, override val x2: Float, override val y2: Float) : ActionLine(x, y) {

        override val type = PathActionType.QUAD_TO

        @Throws(JSONException::class)
        override fun toJson(): JSONObject {
            return super.toJson().apply {
                put(FileId.X2, x2.toDouble())
                put(FileId.Y2, y2.toDouble())
            }
        }

        companion object {

            @Throws(JSONException::class)
            fun fromJson(jsonObject: JSONObject): ActionQuadTo {
                return ActionQuadTo(jsonObject.getDouble(FileId.X).toFloat(), jsonObject.getDouble(FileId.Y).toFloat(),
                        jsonObject.getDouble(FileId.X2).toFloat(), jsonObject.getDouble(FileId.Y2).toFloat())
            }
        }
    }

    class ActionOffset(x: Float, y: Float) : ActionLine(x, y) {

        override val type = PathActionType.OFFSET

        companion object {

            @Throws(JSONException::class)
            fun fromJson(jsonObject: JSONObject): ActionOffset {
                return ActionOffset(jsonObject.getDouble(FileId.X).toFloat(), jsonObject.getDouble(FileId.Y).toFloat())
            }
        }
    }

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put(FileId.ACTIONS, JSONArray().apply {
                actions.forEach { put(it.toJson()) }
            })
        }
    }

    /**
     * Initialises CustomPath from json object
     * @param object object containing custom path data
     * @throws JSONException
     */
    @Throws(JSONException::class)
    internal fun fromJson(jsonObject: JSONObject) {
        val actions = jsonObject.getJSONArray(FileId.ACTIONS)
        loop@ for (i in 0 until actions.length()) {
            val obj = actions.getJSONObject(i)

            val type = obj.getString(FileId.TYPE)
            val action: PathAction = when (PathActionType.valueOf(type)) {
                PathActionType.MOVE_TO -> ActionMove.fromJson(obj)
                PathActionType.LINE_TO -> ActionLine.fromJson(obj)
                PathActionType.OFFSET -> ActionOffset.fromJson(obj)
                PathActionType.QUAD_TO -> ActionQuadTo.fromJson(obj)
            }
            this.actions.add(action)
        }
        drawThisPath()
    }
}