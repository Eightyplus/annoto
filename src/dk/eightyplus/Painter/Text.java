package dk.eightyplus.Painter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.io.Serializable;

/**
 */
public class Text extends Graphic {

  String text = "This is a test text!";

  private float x = 10;
  private float y = 30;

  @Override
  public void onDraw(Canvas canvas, Paint paint) {
    super.onDraw(canvas, paint);
    paint.setColor(color);
    paint.setStrokeWidth(1.0f);

    paint.setTextAlign(Paint.Align.LEFT);
    paint.setTypeface(Typeface.create("HelveticaNeue", Typeface.NORMAL));
    paint.setTextSize(40);
    paint.setAntiAlias(true);

    canvas.drawText(text, x, y, paint);
  }

  @Override
  public void move(float dx, float dy) {
    x += dx;
    y += dy;
  }
}
