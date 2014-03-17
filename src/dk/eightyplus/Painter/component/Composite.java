package dk.eightyplus.Painter.component;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite class to server as drawing interface/component collection for the Composite pattern
 */
public class Composite extends Component {
  protected List<Component> componentList;

  public void onDraw(Canvas canvas, Paint paint) {
    if (componentList != null) {
      for (Component component : componentList) {
        component.onDraw(canvas, paint);
      }
    }
  }

  public void add(Component component) {
    if (componentList == null) {
      componentList = new ArrayList<Component>();
    }
    componentList.add(component);
  }

  public void remove(Component component) {
    if (componentList != null) {
      componentList.remove(component);
    }
  }

  public Component removeLast() {
    if (componentList != null) {
      return componentList.remove(componentList.size() - 1);
    }
    return null;
  }

  public Component getChild(int location) {
    if (componentList != null) {
      return componentList.get(location);
    }
    return null;
  }

  @Override
  public void move(float dx, float dy) {
    if (componentList != null) {
      for (Component component : componentList) {
        component.move(dx, dy); // TODO move ?
      }
    }
  }

  @Override
  public float centerDist(float x, float y) {
    float minimumDistance = Float.MAX_VALUE;
    if (componentList != null) {
      minimumDistance = calculateCenterDistance(x, y, getBounds());
      for (Component component : componentList) {
        float distance = component.centerDist(x, y);
        if (distance < minimumDistance) {
          minimumDistance = distance;
        }
      }
    }

    return minimumDistance;
  }

  @Override
  public RectF getBounds() {
    RectF bounds = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
    if (componentList != null) {
      for (Component component : componentList) {
        RectF componentBounds = component.getBounds();

        if (componentBounds.left < bounds.left) {
          bounds.left = componentBounds.left;
        }

        if (componentBounds.top < bounds.top) {
          bounds.top = componentBounds.top;
        }

        if (componentBounds.right > bounds.right) {
          bounds.right = componentBounds.right;
        }

        if (componentBounds.bottom > bounds.bottom) {
          bounds.bottom = componentBounds.bottom;
        }

      }
    }
    return bounds;
  }
}
