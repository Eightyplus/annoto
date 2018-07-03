package dk.eightyplus.annoto.component


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import dk.eightyplus.annoto.utilities.FileId
import org.json.JSONException
import org.json.JSONObject

/**
 * Text class to give drawing capabilities
 */
class Text(var text: String = "") : Component() {

    private var fontSize = 40
    @Transient
    private val typeFace = Typeface.defaultFromStyle(Typeface.NORMAL) //Typeface.create("HelveticaNeue", Typeface.NORMAL);

    override val bounds: RectF
        get() {
            val bounds = textBounds
            bounds.offset(this.x.toInt(), this.y.toInt())
            bounds.right += (bounds.width() * (scale - 1)).toInt()
            bounds.bottom += (bounds.height() * (scale - 1)).toInt()
            return RectF(bounds)
        }

    private val textBounds: Rect
        get() {
            val textPaint = object : Paint() {
                init {
                    textAlign = Paint.Align.LEFT
                    typeface = typeFace
                    textSize = fontSize.toFloat()
                    isAntiAlias = true
                }
            }
            val bounds = Rect()

            val lines = text.split(FileId.NEW_LINE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            textPaint.getTextBounds(lines[0], 0, lines[0].length, bounds)
            for (i in 1 until lines.size) {
                val lineBounds = Rect()
                textPaint.getTextBounds(lines[i], 0, lines[i].length, lineBounds)

                if (bounds.right < lineBounds.right) {
                    bounds.right = lineBounds.right
                }

                if (bounds.left > lineBounds.left) {
                    bounds.left = lineBounds.left
                }

                bounds.bottom += Math.abs(lineBounds.bottom - lineBounds.top)
            }
            return bounds
        }

    override val type: ComponentType
        get() = ComponentType.TextType

    override fun onDraw(canvas: Canvas, paint: Paint) {
        if (isVisible) {
            paint.color = color
            paint.strokeWidth = 1.0f

            paint.textAlign = Paint.Align.LEFT
            //paint.setTypeface(typeFace);
            paint.style = Paint.Style.FILL
            paint.textSize = fontSize.toFloat()
            paint.isAntiAlias = true

            val lines = text.split(FileId.NEW_LINE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val textBounds = textBounds
            val height = (textBounds.height() / lines.size).toFloat()

            canvas.save()
            canvas.scale(scale, scale, x + textBounds.left, y + textBounds.top)
            for (i in lines.indices) {
                canvas.drawText(lines[i], x, y + height * i, paint)
            }

            canvas.restore()
        }
    }

    override fun centerDist(x: Float, y: Float): Float {
        return calculateCenterDistance(x, y, bounds)
    }

    private fun createImageFromText(text: String, bounds: Rect, fontSize: Float): Bitmap {
        val textPaint = object : Paint() {
            init {
                color = -0xff0100
                textAlign = Paint.Align.LEFT
                typeface = typeFace
                textSize = fontSize
                isAntiAlias = true
            }
        }

        //use ALPHA_8 (instead of ARGB_8888) to get text mask
        val bmp = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawText(text, 0f, bounds.height().toFloat(), textPaint)
        return bmp
    }

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        val jsonObject = super.toJson()
        jsonObject.put(FileId.TEXT, text)
        jsonObject.put(FileId.FONT_SIZE, fontSize)
        return jsonObject
    }

    companion object {

        @Throws(JSONException::class)
        fun fromJson(jsonObject: JSONObject): Text {
            val text = Text()
            text.fromJsonPrimary(jsonObject)
            text.text = jsonObject.getString(FileId.TEXT)
            text.fontSize = jsonObject.getInt(FileId.FONT_SIZE)
            return text
        }
    }
}
