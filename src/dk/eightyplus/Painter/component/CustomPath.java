package dk.eightyplus.Painter.component;

import android.graphics.Path;
import dk.eightyplus.Painter.utilities.FileId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomPath extends Path the only purpose of being Serializable, so it can be save and loaded from file
 */
public class CustomPath extends Path implements Serializable {

  private List<PathAction> actions = new ArrayList<CustomPath.PathAction>();

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

  public static interface PathAction extends Serializable {
    public enum PathActionType {LINE_TO, MOVE_TO, OFFSET, QUAD_TO};
    public PathActionType getType();
    public float getX();
    public float getY();
    public float getX2();
    public float getY2();
    public JSONObject toJson() throws JSONException;
  }

  public static class ActionMove extends ActionLine {

    public ActionMove(float x, float y){
      super(x, y);
    }

    @Override
    public PathActionType getType() {
      return PathActionType.MOVE_TO;
    }

    public static ActionMove fromJson(JSONObject object) throws JSONException {
      return new ActionMove((float)object.getDouble(FileId.X), (float)object.getDouble(FileId.Y));
    }
  }

  public static class ActionLine implements PathAction {
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

    @Override
    public JSONObject toJson() throws JSONException {
      JSONObject object = new JSONObject();
      object.put(FileId.TYPE, getType().name());
      object.put(FileId.X, getX());
      object.put(FileId.Y, getY());
      return object;
    }

    public static ActionLine fromJson(JSONObject object) throws JSONException {
      return new ActionLine((float)object.getDouble(FileId.X), (float)object.getDouble(FileId.Y));
    }
  }

  public static class ActionQuadTo extends ActionLine {
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

    @Override
    public JSONObject toJson() throws JSONException {
      JSONObject object = super.toJson();
      object.put(FileId.X2, getX2());
      object.put(FileId.Y2, getY2());
      return object;
    }

    public static ActionQuadTo fromJson(JSONObject object) throws JSONException {
      return new ActionQuadTo((float)object.getDouble(FileId.X), (float)object.getDouble(FileId.Y),
          (float)object.getDouble(FileId.X2), (float)object.getDouble(FileId.Y2));
    }
  }

  public static class ActionOffset extends ActionLine {

    public ActionOffset(float x, float y){
      super(x, y);
    }

    @Override
    public PathActionType getType() {
      return PathActionType.OFFSET;
    }

    public static ActionOffset fromJson(JSONObject object) throws JSONException {
      return new ActionOffset((float)object.getDouble(FileId.X), (float)object.getDouble(FileId.Y));
    }
  }

  public JSONObject toJson() throws JSONException {
    JSONObject object = new JSONObject();
    JSONArray actions = new JSONArray();

    for (PathAction action : this.actions) {
      actions.put(action.toJson());
    }
    object.put(FileId.ACTIONS, actions);

    return object;
  }

  /**
   * Initialises CustomPath from json object
   * @param object object containing custom path data
   * @throws JSONException
   */
  void fromJson(JSONObject object) throws JSONException {
    JSONArray actions = object.getJSONArray(FileId.ACTIONS);
    for(int i = 0; i < actions.length(); i++) {
      JSONObject obj = actions.getJSONObject(i);

      String type = obj.getString(FileId.TYPE);

      PathAction action;
      switch (PathAction.PathActionType.valueOf(type)) {
        case MOVE_TO:
          action = ActionMove.fromJson(obj);
          break;
        case LINE_TO:
          action = ActionLine.fromJson(obj);
          break;
        case OFFSET:
          action = ActionOffset.fromJson(obj);
          break;
        case QUAD_TO:
          action = ActionQuadTo.fromJson(obj);
          break;
        default:
          continue;
      }
      this.actions.add(action);
    }
    drawThisPath();
  }
}