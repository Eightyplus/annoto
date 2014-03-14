package dk.eightyplus.Painter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 *
 */
public class Polygon extends Component {

  private transient Path path = new Path();

  public Polygon() {
    super();
  }

  public Polygon(Polygon src) {
    path = new Path(src.path);
    color = src.color;
    width = src.width;
  }

  public Path getPath() {
    return path;
  }

  @Override
  public void onDraw(Canvas canvas, Paint paint) {
    paint.setColor(color);
    paint.setStrokeWidth(width);
    canvas.drawPath(path, paint);
  }

  @Override
  public void setColor(int color) {
    this.color = color;
  }

  @Override
  public void setStrokeWidth(float width) {
    this.width = width;
  }

  @Override
  public void move(float dx, float dy) {
    path.offset(dx, dy);
  }

  @Override
  public float centerDist(float x, float y) {
    return calculateCenterDistance(x, y, getBounds());
  }

  @Override
  public RectF getBounds() {
    RectF bounds = new RectF();
    path.computeBounds(bounds, false);
    return bounds;
  }
}
