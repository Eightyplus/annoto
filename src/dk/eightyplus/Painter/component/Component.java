package dk.eightyplus.Painter.component;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import dk.eightyplus.Painter.utilities.FileId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Component drawing class to server as drawing interface for the Composite pattern
 */
public abstract class Component implements Serializable {
  protected int color = 0xFFFFFFFF;
  protected float width = 6.0f;
  protected float scale = 1.0f;
  protected transient boolean visible = true;
  protected float x;
  protected float y;

  public abstract void onDraw(Canvas canvas, Paint paint);
  public abstract float centerDist(float x, float y);
  public abstract RectF getBounds();

  public void move(float dx, float dy) {
    x += dx;
    y += dy;
  }

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
  }

  public void setStrokeWidth(float width) {
    this.width = width;
  }

  public float getWidth() {
    return width;
  }

  public void setWidth(float width) {
    this.width = width;
  }

  public float getScale() {
    return scale;
  }

  public void setScale(float scale) {
    this.scale = scale;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  protected float calculateCenterDistance(float x, float y, RectF bounds) {
    if (bounds.contains(x, y)) {
      float cx = bounds.centerX() - x;
      float cy = bounds.centerY() - y;
      return /* <ignore> Math.sqrt */ cx * cx + cy * cy;
    }

    return Float.MAX_VALUE;
  }

  /**
   * Delete component
   * @return true if successful
   */
  public boolean delete() {
    return true;
  };

  /**
   * @return component type
   */
  public abstract ComponentType getType();

  /**
   * @return json object containing primitives for all drawable components
   * @throws JSONException
   */
  public JSONObject toJson() throws JSONException {
    JSONObject object = new JSONObject();
    object.put(FileId.TYPE, getType().name());
    object.put(FileId.X, x);
    object.put(FileId.Y, y);
    object.put(FileId.WIDTH, width);
    object.put(FileId.SCALE, scale);
    object.put(FileId.COLOR, color);
    return object;
  }

  /**
   * General function to initialise component
   * @param object object containing data to initialise from
   * @throws JSONException
   */
  protected void fromJsonPrimary(JSONObject object) throws JSONException {
    this.x = (float) object.getDouble(FileId.X);
    this.y = (float) object.getDouble(FileId.Y);
    this.width = (float) object.getDouble(FileId.WIDTH);
    this.scale = (float) object.getDouble(FileId.SCALE);
    this.color = object.getInt(FileId.COLOR);
  }
}
