package dk.eightyplus.Painter.component;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Polygon class to be used drawing polygon objects: paths, lines, etc.
 */
public class Polygon extends Component {

  private CustomPath path = new CustomPath();

  public Polygon() {
    super();
  }

  @SuppressWarnings("unused")
  public Polygon(Polygon src) {
    path = new CustomPath(src.path);
    color = src.color;
    width = src.width;
  }

  public Path getPath() {
    return path;
  }

  @Override
  public void onDraw(Canvas canvas, Paint paint) {
    if (visible) {
      paint.setColor(color);
      paint.setStrokeWidth(width);
      canvas.drawPath(path, paint);
    }
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
