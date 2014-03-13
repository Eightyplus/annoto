package dk.eightyplus.Painter;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Composite extends Component {
  protected List<Component> componentList;

  void onDraw(Canvas canvas, Paint paint) {
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
  void move(float dx, float dy) {
    if (componentList != null) {
      for (Component component : componentList) {
        component.move(dx, dy); // TODO move ?
      }
    }
  }
}
