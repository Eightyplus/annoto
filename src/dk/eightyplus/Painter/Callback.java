package dk.eightyplus.Painter;

import dk.eightyplus.Painter.action.State;
import dk.eightyplus.Painter.action.Undo;
import dk.eightyplus.Painter.component.Component;
import dk.eightyplus.Painter.component.Text;

/**
 *
 */
public interface Callback {

  public void textEditDone();

  public void move(Component component, float dx, float dy);

  public State getState();

  public void setState(State state);

  public void setStrokeWidth(int width);

  public void add(Undo undo);
}
