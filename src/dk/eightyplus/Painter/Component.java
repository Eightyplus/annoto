package dk.eightyplus.Painter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Component implements Serializable {
  protected int color = 0xFFFFFF00;
  protected float width = 12.0f;

  public abstract void onDraw(Canvas canvas, Paint paint);
  public abstract void move(float dx, float dy);
  public abstract float centerDist(float x, float y);
  public abstract RectF getBounds();

  void setColor(int color) {
    this.color = color;
  }

  void setStrokeWidth(float width) {
    this.width = width;
  }

  public int getColor() {
    return color;
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
