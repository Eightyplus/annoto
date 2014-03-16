package dk.eightyplus.Painter;

/**
 *
 */
public interface Callback {

  public void move(Component component, float dx, float dy);

  public State getState();

  public void startMove(Component moveComponent);
}
