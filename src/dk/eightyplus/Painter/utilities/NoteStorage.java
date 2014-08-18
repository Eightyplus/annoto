package dk.eightyplus.Painter.utilities;

import android.content.Context;
import android.util.Log;
import dk.eightyplus.Painter.R;
import dk.eightyplus.Painter.component.Component;
import dk.eightyplus.Painter.component.ComponentType;
import dk.eightyplus.Painter.component.Composite;
import dk.eightyplus.Painter.component.Picture;
import dk.eightyplus.Painter.component.Polygon;
import dk.eightyplus.Painter.component.Text;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * NoteStorage is a class to convert components to/from json and make use of this in a serialise context
 */
public class NoteStorage {

  private NoteStorage() { }
  private static final String TAG = NoteStorage.class.toString();

  /**
   * Save list of components into output stream
   * @param context the context
   * @param components list of components
   * @param dataOutputStream output stream to write to
   * @return success
   */
  public static boolean save(final Context context, List<Component> components, DataOutputStream dataOutputStream) {
    boolean result = false;
    try {
      JSONObject object = new JSONObject();
      toJson(object, components);
      dataOutputStream.writeUTF(object.toString());
      result = true;
    } catch (JSONException e) {
      Log.d(TAG, context.getString(R.string.log_error_exception), e);
    } catch (IOException e) {
      Log.d(TAG, context.getString(R.string.log_error_exception), e);
    }

    return result;
  }

  /**
   * Loads components from input stream
   * @param context the context
   * @param components list to store components in
   * @param dataInputStream input stream to read from
   * @return success
   */
  public static boolean load(final Context context, List<Component> components, DataInputStream dataInputStream) {

    boolean result = false;
    try {
      JSONObject object = new JSONObject(dataInputStream.readUTF());
      fromJson(context, object, components);
      result = true;
    } catch (JSONException e) {
      Log.d(TAG, context.getString(R.string.log_error_exception), e);
    } catch (IOException e) {
      Log.d(TAG, context.getString(R.string.log_error_exception), e);
    }

    return result;
  }

  /**
   * Loads list of component from json object
   * @param context the context
   * @param object object containing list
   * @param components list to store components to
   * @throws JSONException
   */
  public static void fromJson(final Context context, JSONObject object, final List<Component> components) throws JSONException {
    JSONArray list = object.getJSONArray(FileId.LIST);
    for (int i = 0; i < list.length(); i++) {
      JSONObject comp = (JSONObject) list.get(i);

      String type = comp.getString(FileId.TYPE);

      Component component;
      switch (ComponentType.valueOf(type)) {
        case CompositeType:
          component = Composite.fromJson(context, comp);
          break;
        case PictureType:
          try {
            component = Picture.fromJson(context, comp).initialise();
          } catch (IOException e) {
            Log.d(TAG, context.getString(R.string.log_error_exception), e);
            continue;
          }
          break;
        case PolygonType:
          component = Polygon.fromJson(comp);
          break;
        case TextType:
          component = Text.fromJson(comp);
          break;
        default:
          continue;
      }

      components.add(component);
    }
  }

  /**
   * Deletes components and their associated files (photos etc.)
   * @param context the context
   * @param dataInputStream input stream to load components from and delete
   * @throws JSONException
   * @throws IOException
   */
  public static void fromJsonDelete(final Context context, DataInputStream dataInputStream) throws JSONException, IOException {
    JSONObject object = new JSONObject(dataInputStream.readUTF());
    JSONArray list = object.getJSONArray(FileId.LIST);
    for (int i = 0; i < list.length(); i++) {
      JSONObject comp = (JSONObject) list.get(i);
      String type = comp.getString(FileId.TYPE);

      switch (ComponentType.valueOf(type)) {
        case CompositeType:
          Composite.fromJson(context, comp).delete();
          break;
        case PictureType:
          Picture.fromJson(context, comp).delete();
          break;
        case PolygonType:
        case TextType:
        default:
          continue;
      }
    }
  }

  /**
   * Stores a list of components into a json object
   * @param object object to save list in
   * @param components list to save
   * @throws JSONException
   */
  public static void toJson(JSONObject object, final List<Component> components) throws JSONException {
    JSONArray list = new JSONArray();
    for (Component component : components) {
      list.put(component.toJson());
    }
    object.put(FileId.SIZE, components.size());
    object.put(FileId.LIST, list);
  }
}
