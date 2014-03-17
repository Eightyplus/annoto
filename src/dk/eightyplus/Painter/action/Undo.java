package dk.eightyplus.Painter.action;

import android.graphics.RectF;
import dk.eightyplus.Painter.component.Component;
import dk.eightyplus.Painter.view.ComponentList;

/**
 * Undo (Redo) class contains the reverse action of an user interaction, so these can be reversed.
 */
public class Undo {

  private Component component;

  private float x;
  private float y;

  private State undoAction;

  /**
   * Undo for components being moved
   * @param component component being moved
   * @param state action to be undo
   */
  public Undo(final Component component, State state) {
    this(component, 0, 0, state);
  }

  /**
   * Undo for components being moved
   * @param component component being moved
   * @param x previous x-coordinate
   * @param y previous y-coordinate
   * @param state action to be undo
   */
  public Undo(final Component component, float x, float y, State state) {
    this.component = component;
    this.x = x;
    this.y = y;
    this.undoAction = state;
  }

  /**
   * Undo action taken on component
   * @param components list of components
   * @return true if undo was successful
   */
  public boolean undo(final ComponentList components) {
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
        this.x = x;
        this.y = y;
        return true;

      case WriteText:
      case DrawPath:
        components.remove(component);
        return true;
    }
  }

  /**
   * Redo action taken on component
   * @param components list of components
   * @return true if redo was successful
   */
  public boolean redo(final ComponentList components) {
    switch (undoAction) {
      default:
      case Delete:
        components.remove(component);
        return true;
      case Move:
        RectF bounds = component.getBounds();
        float x = bounds.left;
        float y = bounds.top;
        component.move(this.x - x, this.y - y);
        this.x = x;
        this.y = y;
        return true;

      case WriteText:
      case DrawPath:
        components.add(component);
        return true;
    }
  }
}
