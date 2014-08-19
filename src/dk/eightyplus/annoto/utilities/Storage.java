package dk.eightyplus.annoto.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import dk.eightyplus.annoto.R;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * User: fries
 * Date: 18/03/14
 * Time: 16.49
 */
public class Storage {
  private static final String TAG = Storage.class.toString();

  private static Storage storage;
  /** The application context. */
  protected Context context;

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

  public void writeToFile(SaveLoad save, String fileName) throws IOException {
    File file = getFilename(fileName);

    GZIPOutputStream outputStream = new GZIPOutputStream(new FileOutputStream(file));
    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
    save.save(context, dataOutputStream);
    outputStream.flush();
    dataOutputStream.close();
    outputStream.close();
  }

  public boolean deleteFile(String fileName) {
    File file = getFilename(fileName);
    if (file.exists()) {
      return file.delete();
    }
    return false;
  }

  public void deleteNoteAndThumb(String fileName) {
    File file = getFilename(fileName);
    if (file.exists()) {
      deleteFromFile(file);
      file.delete();
    }
    File thumb = getThumb2Notes(fileName);
    if (thumb.exists()) {
      thumb.delete();
    }
  }

  public void loadFromFile(SaveLoad load, String fileName) throws IOException, ClassNotFoundException {
    loadFromFile(load, fileName, false);
  }

  public void loadFromFile(SaveLoad load, String fileName, boolean isAsset) throws IOException, ClassNotFoundException {
    GZIPInputStream inputStream;
    if (isAsset) {
      inputStream = new GZIPInputStream(context.getAssets().open(fileName));
    } else {
      File file = getFilename(fileName);
      inputStream = new GZIPInputStream(new FileInputStream(file));
    }
    DataInputStream dataInputStream = new DataInputStream(inputStream);
    load.load(context, dataInputStream);
    dataInputStream.close();
    inputStream.close();
  }

  private void deleteFromFile(File file) {
    try {
      GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(file));
      DataInputStream dataInputStream = new DataInputStream(inputStream);

      NoteStorage.fromJsonDelete(context, dataInputStream);
    } catch (IOException e) {

    } catch (JSONException e) {

    }
  }

  public File writeToFile(final Bitmap bitmap) throws IOException {
    return writeToFile(bitmap, "image.png", 90);
  }

  public File writeToFile(final Bitmap bitmap, final String fileName, int quality) throws IOException {
    File file
        = getFilename(fileName);
    //  = File.createTempFile("image", ".png", context.getCacheDir());
    DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
    bitmap.compress(Bitmap.CompressFormat.PNG, quality, out);
    out.close();
    return file;
  }

  public Bitmap loadFromFile() throws IOException {
    return loadFromFile("image.png");
  }

  public Bitmap loadFromFile(String fileName) {
    File file = getFilename(fileName);
    Bitmap bitmap = null;
    if (file.exists()) {
      bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
    }
    return bitmap;
  }

  public void addImageToGallery(final ContextWrapper context, final String filePath) {
    // TODO check preference?
    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    //values.put(MediaStore.Images.Media.TITLE, title);
    //values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
    //values.put(MediaStore.Images.Media.DESCRIPTION, description);
    values.put(MediaStore.MediaColumns.DATA, filePath);
    context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
  }

  public Bitmap loadBitmapFromIntent(ContextWrapper context, Intent data, int sampleSize) throws IOException {
    Uri contentURI = Uri.parse(data.getDataString());
    InputStream inputStream = context.getContentResolver().openInputStream(contentURI);
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = sampleSize;
    return BitmapFactory.decodeStream(inputStream, null, options);
  }

  public String[] getNotes() {
    File root = getFilename(null);

    return root.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String filename) {
        return filename.endsWith(context.getString(R.string.note_file_format, context.getString(R.string.empty)));
      }
    });
  }

  public File getThumb2Notes(String fileName) {
    String prefix = fileName.substring(0, fileName.lastIndexOf('.'));
    return getFilename(String.format("%s.png", prefix));
  }


  public void saveList(List<? extends Object> list, String fileName) {
    File file = getFilename(fileName);

    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(file));
      out.writeInt(list.size());
      for (Object object : list) {
        out.writeObject(object);
      }
      out.flush();
    } catch (IOException e) {
      Log.d(TAG, context.getString(R.string.log_error_exception), e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {

        }
      }
    }
  }

  public List<Object> loadDemo(String fileName) {

    List<Object> list = new ArrayList<Object>();
    File file = getFilename(fileName);
    ObjectInputStream in = null;
    try {
      if (file.exists()) {
        //in = new ObjectInputStream(context.getAssets().open(fileName));
        in = new ObjectInputStream(new FileInputStream(file));
        int length = in.readInt();
        for (int i=0; i< length; i++) {
          list.add(in.readObject());
        }

      }
    } catch (FileNotFoundException e) {
      Log.d(TAG, context.getString(R.string.log_error_exception), e);
    } catch (StreamCorruptedException e) {
      Log.d(TAG, context.getString(R.string.log_error_exception), e);
    } catch (IOException e) {
      Log.d(TAG, context.getString(R.string.log_error_exception), e);
    } catch (ClassNotFoundException e) {
      Log.d(TAG, context.getString(R.string.log_error_exception), e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          Log.d(TAG, context.getString(R.string.log_error_exception), e);
        }
      }
    }

    return list;
  }


  public File getFilename(final String filename) {
    File applicationPath = context.getExternalFilesDir(null);
    if (filename == null) {
      return new File(applicationPath,  File.separator);
    }
    return new File(applicationPath, File.separator + filename);
  }

  public static void writeData(final Context context, byte[] data, final OutputStream out) throws IOException {
    final int buffer = 8192;
    try {
      int offset = 0;
      while (true) {
        int length = data.length - offset;

        if (length <= 0) {
          break;
        } else if (length > buffer) {
          length = buffer;
        }

        out.write(data, offset, length);
        offset += length;
      }
    } catch (final IOException e) {
      Log.d(TAG, context.getString(R.string.log_error_exception), e);
      throw e;
    }
  }

  public static String readData(final InputStream in) throws IOException {
    if (in == null) {
      return null;
    }
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int byteValue;
      while ((byteValue = in.read()) != -1) {
        baos.write(byteValue);
      }
      return new String(baos.toByteArray());
    } finally {
      in.close();
    }
  }

  public String getPath(Uri uri) {
    if( uri == null ) {
      // ERROR ?
      return null;
    }

    String[] projection = { MediaStore.Images.Media.DATA };
    CursorLoader loader = new CursorLoader(context, uri, projection, null, null, null);
    Cursor cursor = loader.loadInBackground();
    if (cursor.moveToFirst()) {
      int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
      return cursor.getString(columnIndex);
    }
    return uri.getPath();
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

      if (RDrawable != null) {
        Field[] drawables = RDrawable.getFields();
        for(Field dr : drawables) {
          list.add(dr.getInt(null));
        }
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
