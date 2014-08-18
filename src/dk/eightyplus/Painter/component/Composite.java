package dk.eightyplus.Painter.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.renderscript.Script;
import dk.eightyplus.Painter.utilities.FileId;
import dk.eightyplus.Painter.utilities.NoteStorage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite class to server as drawing interface/component collection for the Composite pattern
 */
@SuppressWarnings("unused")
public class Composite extends Component {

  protected List<Component> componentList;

  private boolean hasComponents() {
    return componentList != null;
  }

  public void onDraw(Canvas canvas, Paint paint) {
    if (isVisible() && hasComponents()) {
      canvas.save();

      RectF bounds = getBounds();
      canvas.scale(scale, scale, bounds.left, bounds.top);
      canvas.translate(x, y);
      for (Component component : componentList) {
        component.onDraw(canvas, paint);
      }
      canvas.restore();
    }

  }

  public void add(Component component) {
    if (componentList == null) {
      componentList = new ArrayList<Component>();
    }
    componentList.add(component);
  }

  public void remove(Component component) {
    if (hasComponents()) {
      componentList.remove(component);
    }
  }

  public Component removeLast() {
    if (hasComponents()) {
      return componentList.remove(componentList.size() - 1);
    }
    return null;
  }

  public Component getChild(int location) {
    if (hasComponents()) {
      return componentList.get(location);
    }
    return null;
  }
/*
  @Override
  public void setScale(float scale) {
    super.setScale(scale);
    if (hasComponents()) {
      for (Component component : componentList) {
        component.setScale(scale);
      }
    }
  }

  @Override
  public void move(float dx, float dy) {
    if (hasComponents()) {
      for (Component component : componentList) {
        component.move(dx, dy);
      }
    }
  }
  */

  @Override
  public float centerDist(float x, float y) {
    float minimumDistance = Float.MAX_VALUE;
    if (hasComponents()) {
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
    if (hasComponents()) {
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

      bounds.offset(x, y);
      bounds.right += bounds.width() * (scale - 1);
      bounds.bottom += bounds.height() * (scale - 1);
    }
    return bounds;
  }

  @Override
  public boolean delete() {
    boolean result = super.delete();
    for (Component component : componentList) {
      result &= component.delete();
    }
    return result;
  }

  @Override
  public ComponentType getType() {
    return ComponentType.CompositeType;
  }

  public static Composite fromJson(final Context context, JSONObject object) throws JSONException {
    Composite composite = new Composite();
    composite.fromJsonPrimary(object);

    int size = object.getInt(FileId.SIZE);
    if (size > 0) {
      composite.componentList = new ArrayList<Component>();
      NoteStorage.fromJson(context, object, composite.componentList);
    }

    return composite;
  }

  @Override
  public JSONObject toJson() throws JSONException {
    JSONObject object = super.toJson();

    int size = componentList != null ? componentList.size() : 0;
    object.put(FileId.SIZE, size);
    if (size > 0) {
      NoteStorage.toJson(object, componentList);
    }
    return object;
  }
}
