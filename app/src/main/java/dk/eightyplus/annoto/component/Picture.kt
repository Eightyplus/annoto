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
            val values = FloatArray(Matrix.MPERSP_2 + 1)
            values[Matrix.MSCALE_Y] = scale
            values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y]
            values[Matrix.MTRANS_X] = x
            values[Matrix.MTRANS_Y] = y
            values[Matrix.MPERSP_2] = 1f
            val matrix = Matrix()
            matrix.reset()
            matrix.setValues(values)
            return matrix
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
            canvas.drawBitmap(bitmap!!, matrix, paint)
        }
    }

    override fun centerDist(x: Float, y: Float): Float {
        return calculateCenterDistance(x, y, bounds)
    }

    override fun delete(): Boolean {
        super.delete()
        return Storage.getStorage(context)?.deleteFile(filename) ?: false
    }

    /**
     * Initialises picture when loaded from storage
     * @throws IOException
     */
    @Throws(IOException::class)
    fun initialise(): Picture {
        bitmap = Storage.getStorage(context)?.loadFromFile(filename)
        if (bitmap == null) {
            throw IOException(context.getString(R.string.log_error_file_missing))
        }
        return this
    }

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        val jsonObject = super.toJson()
        jsonObject.put(FileId.FILE_NAME, filename)
        return jsonObject
    }

    companion object {

        @Throws(JSONException::class)
        fun fromJson(context: Context, jsonObject: JSONObject): Picture {
            val picture = Picture(context, jsonObject.getString(FileId.FILE_NAME))
            picture.fromJsonPrimary(jsonObject)
            return picture
        }
    }
}
