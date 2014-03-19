package dk.eightyplus.Painter.component;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.io.Serializable;

/**
 * Component drawing class to server as drawing interface for the Composite pattern
 */
public abstract class Component implements Serializable {
  protected int color = 0xFFFFFFFF;
  protected float width = 6.0f;
  protected boolean visible = true;

  public abstract void onDraw(Canvas canvas, Paint paint);
  public abstract void move(float dx, float dy);
  public abstract float centerDist(float x, float y);
  public abstract RectF getBounds();

  public void setColor(int color) {
    this.color = color;
  }

  public void setStrokeWidth(float width) {
    this.width = width;
  }

  public int getColor() {
    return color;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  protected float calculateCenterDistance(float x, float y, RectF bounds) {
    if (bounds.contains(x, y)) {
      float cx = bounds.left - bounds.right;
      float cy = bounds.top - bounds.bottom;
      return (float) Math.sqrt(cx * cx + cy * cy);
    }

    return Float.MAX_VALUE;
  }
}
