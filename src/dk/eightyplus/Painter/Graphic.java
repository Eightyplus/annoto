package dk.eightyplus.Painter;

import android.graphics.Canvas;
import android.graphics.Paint;

public interface Graphic {
  void onDraw(Canvas canvas, Paint paint);

  void setColor(int color);
  void setStrokeWidth(float width);

  void move(float dx, float dy);
}
