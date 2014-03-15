package dk.eightyplus.Painter;

import android.graphics.RectF;
import java.util.List;

/**
 * Undo (Redo) class contains the reverse action of an user interaction, so these can be reversed.
 */
public class Undo {

  private Component component;

  private float x;
  private float y;

  private State undoAction;

  public Undo(final Component component, float x, float y, State state) {
    this.component = component;
    this.x = x;
    this.y = y;
    this.undoAction = state;
  }

  public Undo(final Component component, State state) {
    this(component, 0, 0, state);
  }

  public boolean undo(final List<Component> components) {

    switch (undoAction) {
      default:
      case Delete:
        components.add(component);
        return true;
      case Move:
        RectF bounds = component.getBounds();
        float x = bounds.left;
        float y = bounds.top;
        component.move(this.x - x, this.y - y);
        return true;

      case WriteText:
      case DrawPath:
        components.remove(component);
        return true;
    }
  }

  public boolean redo(final List<Component> components) {

    switch (undoAction) {
      default:
      case Delete:
        components.remove(component);
        return true;
      case Move:
        RectF bounds = component.getBounds();
        float x = bounds.left;
        float y = bounds.top;
        component.move(x - this.x, y - this.y);
        return true;

      case WriteText:
      case DrawPath:
        components.add(component);
        return true;
    }
  }
}
