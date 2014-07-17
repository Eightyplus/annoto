package dk.eightyplus.Painter.action;

/**
 *
 */
public enum State {
  Add,
  Delete,
  DrawPath,
  Move,
  Text;


  public static State state(String state) {
    return State.valueOf(state);
  }
}

