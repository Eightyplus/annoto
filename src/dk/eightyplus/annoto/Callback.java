package dk.eightyplus.annoto;

import dk.eightyplus.annoto.action.State;
import dk.eightyplus.annoto.action.Undo;
import dk.eightyplus.annoto.component.Component;

/**
 *
 */
public interface Callback {

  public void textEditDone();

  public void move(Component component, float dx, float dy, float scale);

  public State getState();

  public void setState(State state);

  public void setStrokeWidth(int width);

  public void add(Undo undo);

  public void load(String fileName);

  public void colorChanged(int color);
}
