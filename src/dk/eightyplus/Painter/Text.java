package dk.eightyplus.Painter;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.Serializable;

/**
 */
public class Text implements Graphic, Serializable {

  String text = "This is a test text!";

  private float x = 0;
  private float y = 0;
  private int color = 0xFFFFFF00;
  private float width = 1.0f;;

  @Override
  public void onDraw(Canvas canvas, Paint paint) {
    paint.setColor(color);
    paint.setStrokeWidth(width);
    canvas.drawText(text, x, y, paint);
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
    x += dx;
    y += dy;
  }
}
