package dk.eightyplus.Painter;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Graphic implements Serializable {
  protected int color = 0xFFFFFF00;
  protected float width = 12.0f;

  protected List<Graphic> graphicList;

  abstract void move(float dx, float dy);

  void onDraw(Canvas canvas, Paint paint) {
    if (graphicList != null)
    for (Graphic graphic : graphicList) {
      graphic.onDraw(canvas, paint);
    }
  }

  public void add(Graphic graphic) {
    if (graphicList == null) {
      graphicList = new ArrayList<Graphic>();
    }
    graphicList.add(graphic);
  }

  public void remove(Graphic graphic) {
    if (graphicList != null) {
      graphicList.remove(graphic);
    }
  }

  public Graphic removeLast() {
    if (graphicList != null) {
      return graphicList.remove(graphicList.size() - 1);
    }
    return null;
  }

  public Graphic getChild(int location) {
    if (graphicList != null) {
      return graphicList.get(location);
    }
    return null;
  }

  void setColor(int color) {
    this.color = color;
  }

  void setStrokeWidth(float width) {
    this.width = width;
  }

}
