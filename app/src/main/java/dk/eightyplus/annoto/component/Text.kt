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
            return RectF(textBounds.apply {
                offset(x.toInt(), y.toInt())
                right += (width() * (scale - 1)).toInt()
                bottom += (height() * (scale - 1)).toInt()
            })
        }

    private val textBounds: Rect
        get() {
            val textPaint = Paint().apply {
                textAlign = Paint.Align.LEFT
                typeface = typeFace
                textSize = fontSize.toFloat()
                isAntiAlias = true
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
            with(paint) {
                color = this@Text.color
                strokeWidth = 1.0f
                textAlign = Paint.Align.LEFT
                //setTypeface(typeFace);
                style = Paint.Style.FILL
                textSize = fontSize.toFloat()
                isAntiAlias = true
            }

            val lines = text.split(FileId.NEW_LINE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val textBounds = textBounds
            val height = (textBounds.height() / lines.size).toFloat()

            with(canvas) {
                save()
                scale(scale, scale, x + textBounds.left, y + textBounds.top)
                for (i in lines.indices) {
                    drawText(lines[i], x, y + height * i, paint)
                }
                restore()
            }
        }
    }

    override fun centerDist(x: Float, y: Float): Float {
        return calculateCenterDistance(x, y, bounds)
    }

    private fun createImageFromText(text: String, bounds: Rect, fontSize: Float): Bitmap {
        val textPaint = Paint().apply {
            color = -0xff0100
            textAlign = Paint.Align.LEFT
            typeface = typeFace
            textSize = fontSize
            isAntiAlias = true
        }

        //use ALPHA_8 (instead of ARGB_8888) to get text mask
        return Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888).apply {
            Canvas(this).drawText(text, 0f, bounds.height().toFloat(), textPaint)
        }
    }

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        return super.toJson().apply {
            put(FileId.TEXT, text)
            put(FileId.FONT_SIZE, fontSize)
        }
    }

    companion object {

        @Throws(JSONException::class)
        fun fromJson(jsonObject: JSONObject): Text {
            return Text().apply {
                fromJsonPrimary(jsonObject)
                text = jsonObject.getString(FileId.TEXT)
                fontSize = jsonObject.getInt(FileId.FONT_SIZE)
            }
        }
    }
}
