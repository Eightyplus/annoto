package dk.eightyplus.Painter.component;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import dk.eightyplus.Painter.utilities.FileId;
import org.json.JSONException;
import org.json.JSONObject;

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
    this();
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
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(width);

      RectF bounds = getBounds();

      canvas.save();
      canvas.scale(scale, scale, bounds.left, bounds.top);
      canvas.translate(x, y);
      canvas.drawPath(path, paint);
      canvas.restore();
    }
  }

  /*
  @Override
  public void move(float dx, float dy) {
    path.offset(dx, dy);
  }

  @Override
  public void setScale(float scale) {
    Matrix scaleMatrix = new Matrix();
    RectF rectF = new RectF();
    path.computeBounds(rectF, true);
    scaleMatrix.setScale(scale, scale, rectF.centerX(),rectF.centerY());
    path.transform(scaleMatrix);
  }*/

  @Override
  public float centerDist(float x, float y) {
    return calculateCenterDistance(x, y, getBounds());
  }

  @Override
  public RectF getBounds() {
    RectF bounds = new RectF();
    path.computeBounds(bounds, false);
    bounds.offset(x, y);
    bounds.right += bounds.width() * (scale - 1);
    bounds.bottom += bounds.height() * (scale - 1);
    return bounds;
  }

  @Override
  public ComponentType getType() {
    return ComponentType.PolygonType;
  }

  public static Polygon fromJson(JSONObject object) throws JSONException {
    Polygon polygon = new Polygon();
    polygon.fromJsonPrimary(object);
    polygon.path.fromJson(object.getJSONObject(FileId.PATH));
    return polygon;
  }

  @Override
  public JSONObject toJson() throws JSONException {
    JSONObject object = super.toJson();
    object.put(FileId.PATH, path.toJson());
    return object;
  }
}
