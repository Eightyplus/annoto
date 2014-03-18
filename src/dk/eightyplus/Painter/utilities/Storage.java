package dk.eightyplus.Painter.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * User: fries
 * Date: 18/03/14
 * Time: 16.49
 */
public class Storage {
  private static final String TAG = Storage.class.toString();

  private static Storage storage;
  /** The application context. */
  protected static Context context;

  private Storage(final Context context) {
    this.context = context.getApplicationContext();
  }

  public static Storage getStorage(final Context context) {
    if (storage == null) {
      createStorage(context);
    }
    return storage;
  }

  private synchronized static void createStorage(final Context context) {
    if (storage == null) {
      storage = new Storage(context);
    }
  }

  public void writeToFile(SaveLoad save) throws IOException {
    File file = getFilename(context, "file.note");

    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
    save.save(out);
    out.flush();
    out.close();
  }

  public void loadFromFile(SaveLoad load) throws IOException, ClassNotFoundException {
    File file = getFilename(context, "file.note");

    ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));

    load.load(in);
    in.close();
  }

  public File writeToFile(final Context context, final Bitmap bitmap) throws IOException {
    File file
        = getFilename(context, "image.png");
    //  = File.createTempFile("image", ".png", context.getCacheDir());
    DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
    out.close();
    return file;
  }

  public File getFilename(final Context context, final String filename) {
    File applicationPath = context.getExternalFilesDir(null);
    if (filename == null) {
      return new File(applicationPath,  File.separator);
    }
    return new File(applicationPath, File.separator + filename);
  }

  public List<Integer> getDrawableResources() {
    List<Integer> list = new ArrayList<Integer>();

    try {
      Class RClass = Class.forName("android.R");

      Class[] subclasses = RClass.getDeclaredClasses();

      Class RDrawable = null;

      for(Class subclass : subclasses) {
        if("android.R.drawable".equals(subclass.getCanonicalName())) {
          RDrawable = subclass;
          break;
        }
      }

      Field[] drawables = RDrawable.getFields();
      for(Field dr : drawables) {
        list.add(dr.getInt(null));
      }


/*
      List<Map<String, Object>> drinfo = new ArrayList<Map<String, Object>>();

      Field[] drawables = RDrawable.getFields();
      for(Field dr : drawables) {
        Map<String, Object> map = new HashMap<String, Object>();
        Drawable img = getResources().getDrawable(dr.getInt(null));

        map.put("drimg", dr.getInt(null));
        map.put("drname", dr.getName());

        drinfo.add(map);
      }

      setListAdapter(new SimpleAdapter(this,
          drinfo,
          R.layout.listitem,
          new String[] { "drimg", "drname" },
          new int[] { R.id.drimg, R.id.drname }));
*/
    } catch(IllegalAccessException iae) {
      Log.e(TAG, iae.toString());
    } catch(ClassNotFoundException cnfe) {
      Log.e(TAG, cnfe.toString());
    }

    return list;
  }
}
