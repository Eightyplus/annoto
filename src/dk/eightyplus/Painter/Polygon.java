package dk.eightyplus.Painter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.io.Serializable;

/**
 *
 */
public class Polygon extends Path implements Graphic, Serializable {

  private int color = 0xFFFFFF00;
  private float width = 1.0f;

  public Polygon() {
    super();
  }

  public Polygon(Polygon src) {
    super(src);
    color = src.color;
    width = src.width;
  }

  @Override
  public void onDraw(Canvas canvas, Paint paint) {
    paint.setColor(color);
    paint.setStrokeWidth(width);
    canvas.drawPath(this, paint);
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
    offset(dx, dy);
  }
}
