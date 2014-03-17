package dk.eightyplus.Painter;

import dk.eightyplus.Painter.action.State;
import dk.eightyplus.Painter.action.Undo;
import dk.eightyplus.Painter.component.Component;

/**
 *
 */
public interface Callback {

  public void move(Component component, float dx, float dy);

  public State getState();

  public void startMove(Component moveComponent);

  public void setStrokeWidth(int width);

  public void add(Undo undo);
}
