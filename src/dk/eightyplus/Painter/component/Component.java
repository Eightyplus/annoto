package dk.eightyplus.Painter.component;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.io.Serializable;

/**
 * Component drawing class to server as drawing interface for the Composite pattern
 */
public abstract class Component implements Serializable {
  private static final long serialVersionUID = -362013088797954232L;
  protected int color = 0xFFFFFFFF;
  protected float width = 6.0f;
  protected float scale = 1.0f;
  protected boolean visible = true;
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
}
