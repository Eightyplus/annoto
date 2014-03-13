package dk.eightyplus.Painter;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Component implements Serializable {
  protected int color = 0xFFFFFF00;
  protected float width = 12.0f;

  abstract void onDraw(Canvas canvas, Paint paint);
  abstract void move(float dx, float dy);

  void setColor(int color) {
    this.color = color;
  }

  void setStrokeWidth(float width) {
    this.width = width;
  }

}
