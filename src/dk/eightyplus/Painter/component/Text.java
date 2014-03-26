package dk.eightyplus.Painter.component;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

/**
 * Text class to give drawing capabilities
 */
public class Text extends Component {

  private static final long serialVersionUID = 4256622835983660086L;
  private final String NEW_LINE = "\n";

  private String text;
  private int fontSize = 40;
  private transient Typeface typeFace = Typeface.create("HelveticaNeue", Typeface.NORMAL);

  @SuppressWarnings("unused")
  public Text() {}
  public Text(String text) {
    this.text = text;
  }

  @Override
  public void onDraw(Canvas canvas, Paint paint) {
    if (visible) {
      paint.setColor(color);
      paint.setStrokeWidth(1.0f);

      paint.setTextAlign(Paint.Align.LEFT);
      //paint.setTypeface(typeFace);
      paint.setStyle(Paint.Style.FILL);
      paint.setTextSize(fontSize);
      paint.setAntiAlias(true);

      String[] lines = text.split(NEW_LINE);
      Rect textBounds = getTextBounds();
      float height = textBounds.height() / lines.length;

      canvas.save();
      canvas.scale(scale, scale, x + textBounds.left, y + textBounds.top);
      for (int i = 0; i < lines.length; i++) {
        canvas.drawText(lines[i], x, y + height * i, paint);
      }

      canvas.restore();
    }
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  @Override
  public float centerDist(float x, float y) {
    return calculateCenterDistance(x, y, getBounds());
  }

  @Override
  public RectF getBounds() {
    Rect bounds = getTextBounds();
    bounds.offset((int)this.x, (int)this.y);
    bounds.right += bounds.width() * (scale - 1);
    bounds.bottom += bounds.height() * (scale - 1);
    return new RectF(bounds);
  }

  private Rect getTextBounds() {
    final Paint textPaint = new Paint() {
      {
        setTextAlign(Paint.Align.LEFT);
        setTypeface(typeFace);
        setTextSize(fontSize);
        setAntiAlias(true);
      }
    };
    Rect bounds = new Rect();

    String[] lines = text.split(NEW_LINE);
    textPaint.getTextBounds(lines[0], 0, lines[0].length(), bounds);
    for (int i = 1; i < lines.length; i++) {
      Rect lineBounds = new Rect();
      textPaint.getTextBounds(lines[i], 0, lines[i].length(), lineBounds);

      if (bounds.right < lineBounds.right) {
        bounds.right = lineBounds.right;
      }

      if (bounds.left > lineBounds.left) {
        bounds.left = lineBounds.left;
      }

      bounds.bottom += Math.abs(lineBounds.bottom - lineBounds.top);
    }
    return bounds;
  }

  @SuppressWarnings("unused")
  private Bitmap createImageFromText(final String text, final Rect bounds, final float fontSize) {
    final Paint textPaint = new Paint() {
      {
        setColor(0xFF00FF00);
        setTextAlign(Paint.Align.LEFT);
        setTypeface(typeFace);
        setTextSize(fontSize);
        setAntiAlias(true);
      }
    };

    //use ALPHA_8 (instead of ARGB_8888) to get text mask
    final Bitmap bmp = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
    final Canvas canvas = new Canvas(bmp);
    canvas.drawText(text, 0, bounds.height(), textPaint);
    return bmp;
  }
}
