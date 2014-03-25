package dk.eightyplus.Painter.component;

import android.graphics.Path;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomPath extends Path the only purpose of being Serializable, so it can be save and loaded from file
 */
public class CustomPath extends Path implements Serializable {

  private static final long serialVersionUID = -5974912367682897467L;

  private List<PathAction> actions = new ArrayList<CustomPath.PathAction>();

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
    in.defaultReadObject();
    drawThisPath();
  }


  public CustomPath() { }

  public CustomPath(CustomPath src) {
    super(src);
    this.actions = new ArrayList<PathAction>(src.actions);
  }

  @Override
  public void moveTo(float x, float y) {
    actions.add(new ActionMove(x, y));
    super.moveTo(x, y);
  }

  @Override
  public void lineTo(float x, float y){
    actions.add(new ActionLine(x, y));
    super.lineTo(x, y);
  }

  @Override
  public void quadTo(float x1, float y1, float x2, float y2) {
    actions.add(new ActionQuadTo(x1, y1, x2, y2));
    super.quadTo(x1, y1, x2, y2);
  }

  @Override
  public void offset(float dx, float dy) {
    actions.add(new ActionOffset(dx, dy));
    super.offset(dx, dy);
  }

  private void drawThisPath(){
    for(PathAction p : actions){
      switch (p.getType()) {
        case MOVE_TO:
          super.moveTo(p.getX(), p.getY());
          break;
        case LINE_TO:
          super.lineTo(p.getX(), p.getY());
          break;
        case QUAD_TO:
          super.quadTo(p.getX(), p.getY(), p.getX2(), p.getY2());
          break;
        case OFFSET:
          super.offset(p.getX(), p.getX());
          break;
      }
    }
  }

  public static interface PathAction extends  Serializable{
    public enum PathActionType {LINE_TO, MOVE_TO, OFFSET, QUAD_TO};
    public PathActionType getType();
    public float getX();
    public float getY();
    public float getX2();
    public float getY2();
  }

  public static class ActionMove extends ActionLine {
    private static final long serialVersionUID = -7198142191254133295L;

    public ActionMove(float x, float y){
      super(x, y);
    }

    @Override
    public PathActionType getType() {
      return PathActionType.MOVE_TO;
    }
  }

  public static class ActionLine implements PathAction {
    private static final long serialVersionUID = 8307137961494172589L;

    private float x,y;

    public ActionLine(float x, float y){
      this.x = x;
      this.y = y;
    }

    @Override
    public PathActionType getType() {
      return PathActionType.LINE_TO;
    }

    @Override
    public float getX() {
      return x;
    }

    @Override
    public float getY() {
      return y;
    }

    @Override
    public float getX2() {
      return 0;
    }

    @Override
    public float getY2() {
      return 0;
    }
  }

  public static class ActionQuadTo extends ActionLine {
    private static final long serialVersionUID = -2922409288151860775L;

    private float x2, y2;
    public ActionQuadTo(float x, float y, float x2, float y2) {
      super(x, y);
      this.x2 = x2;
      this.y2 = y2;
    }

    @Override
    public PathActionType getType() {
      return PathActionType.QUAD_TO;
    }

    @Override
    public float getX2() {
      return x2;
    }

    @Override
    public float getY2() {
      return y2;
    }
  }

  public static class ActionOffset extends ActionLine {
    //private static final long serialVersionUID = -7198142191254133295L;

    public ActionOffset(float x, float y){
      super(x, y);
    }

    @Override
    public PathActionType getType() {
      return PathActionType.OFFSET;
    }
  }
}