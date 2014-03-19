package dk.eightyplus.Painter.component;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * Text class to give drawing capabilities
 */
public class Text extends Component {

  private static final long serialVersionUID = 4256622835983660086L;

  private String text;
  private float x = 10;
  private float y = 30;
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

      canvas.drawText(text, x, y, paint);
    }
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  @Override
  public void move(float dx, float dy) {
    x += dx;
    y += dy;
  }

  @Override
  public float centerDist(float x, float y) {
    return calculateCenterDistance(x, y, getBounds());
  }

  @Override
  public RectF getBounds() {
    final Paint textPaint = new Paint() {
      {
        setTextAlign(Paint.Align.LEFT);
        setTypeface(typeFace);
        setTextSize(fontSize);
        setAntiAlias(true);
      }
    };

    Rect bounds = new Rect();
    textPaint.getTextBounds(text, 0, text.length(), bounds);
    bounds.offset((int)this.x, (int)this.y);
    return new RectF(bounds);
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
