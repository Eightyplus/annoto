package dk.eightyplus.annoto.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import dk.eightyplus.annoto.R
import dk.eightyplus.annoto.utilities.FileId
import dk.eightyplus.annoto.utilities.Storage
import org.json.JSONException
import org.json.JSONObject

import java.io.IOException

/**
 * Picture component in the Composite pattern
 */
class Picture private constructor(@field:Transient private val context: Context, private val filename: String) : Component() {

    @Transient
    private var bitmap: Bitmap? = null

    private val matrix: Matrix
        get() {
            val values = FloatArray(Matrix.MPERSP_2 + 1).apply {
                this[Matrix.MSCALE_Y] = scale
                this[Matrix.MSCALE_X] = scale
                this[Matrix.MTRANS_X] = x
                this[Matrix.MTRANS_Y] = y
                this[Matrix.MPERSP_2] = 1f
            }
            return Matrix().apply {
                reset()
                setValues(values)
            }
        }

    override val bounds: RectF
        get() = RectF(x, y, x + (bitmap?.width ?: 0) * scale, y + (bitmap?.height ?: 0) * scale)

    override val type: ComponentType
        get() = ComponentType.PictureType

    constructor(context: Context, bitmap: Bitmap, filename: String) : this(context, filename) {
        this.bitmap = bitmap
    }

    override fun onDraw(canvas: Canvas, paint: Paint) {
        if (isVisible) {
            bitmap?.let {
                canvas.drawBitmap(it, matrix, paint)
            }
        }
    }

    override fun centerDist(x: Float, y: Float): Float {
        return calculateCenterDistance(x, y, bounds)
    }

    override fun delete(): Boolean {
        super.delete()
        return Storage.getStorage(context).deleteFile(filename)
    }

    /**
     * Initialises picture when loaded from storage
     * @throws IOException
     */
    @Throws(IOException::class)
    fun initialise(): Picture {
        bitmap = Storage.getStorage(context).loadFromFile(filename) ?: throw IOException(context.getString(R.string.log_error_file_missing))
        return this
    }

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        return super.toJson().apply {
            put(FileId.FILE_NAME, filename)
        }
    }

    companion object {

        @Throws(JSONException::class)
        fun fromJson(context: Context, jsonObject: JSONObject): Picture {
            return Picture(context, jsonObject.getString(FileId.FILE_NAME)).apply {
                fromJsonPrimary(jsonObject)
            }
        }
    }
}
