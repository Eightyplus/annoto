package dk.eightyplus.annoto.action;

import android.graphics.RectF;
import dk.eightyplus.annoto.component.Component;
import dk.eightyplus.annoto.component.Text;
import dk.eightyplus.annoto.view.ComponentList;

/**
 * Undo (Redo) class contains the reverse action of an user interaction, so these can be reversed.
 */
public class Undo {

  private Component component;

  private float x;
  private float y;
  private float scale;
  private String text;

  private State undoAction;

  /**
   * Undo for components being moved
   * @param component component being moved
   * @param state action to be undo
   */
  public Undo(final Component component, State state) {
    this(component, 0, 0, 1, state);
  }

  /**
   * Undo for components being moved
   * @param component component being moved
   * @param x previous x-coordinate
   * @param y previous y-coordinate
   * @param state action to be undo
   */
  public Undo(final Component component, float x, float y, float scale, State state) {
    this(component, null, state);
    this.x = x;
    this.y = y;
    this.scale = scale;
    this.undoAction = state;
  }

  public Undo(final Component component, String text, State state) {
    this.component = component;
    this.undoAction = state;
    this.text = text;
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
        return move();
      case Text:
        return changeText();
      case Add:
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
        return move();
      case Text:
        return changeText();
      case Add:
      case DrawPath:
        components.add(component);
        return true;
    }
  }

  private boolean move() {
    RectF bounds = component.getBounds();
    float x = bounds.left;
    float y = bounds.top;
    float scale = component.getScale();
    component.setScale(this.scale);
    component.move(this.x - x, this.y - y);
    this.x = x;
    this.y = y;
    this.scale = scale;
    return true;
  }


  private boolean changeText() {
    if (component instanceof Text) {
      String tmp = ((Text)component).getText();
      ((Text)component).setText(text);
      text = tmp;
      return true;
    }
    return false;
  }

  public void cleanup() {
    if (undoAction == State.Delete) {
      component.delete();
    }
  }
}
