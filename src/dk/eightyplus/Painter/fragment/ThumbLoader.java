package dk.eightyplus.Painter.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Pair;
import dk.eightyplus.Painter.utilities.Storage;

import java.io.File;

/**
 * User: fries
 * Date: 30/03/14
 * Time: 11.37
 */
public class ThumbLoader extends AsyncTask<String, Void, Pair<String, Drawable>> {


  private final Context context;
  private final Notify notify;

  public ThumbLoader(final Context context, Notify notify) {
    this.context = context;
    this.notify = notify;
  }

  @Override
  protected Pair<String, Drawable> doInBackground(String ... paths) {
    String path = paths[0];

    final File file = Storage.getStorage(context).getThumb2Notes(path);
    Drawable drawable = null;
    if (file.exists()) {
      drawable = Drawable.createFromPath(file.getAbsolutePath());
    }

    return new Pair<String, Drawable>(path, drawable);
  }

  @Override
  protected void onPostExecute(Pair<String, Drawable> drawablePair) {
    notify.done(drawablePair.first, drawablePair.second);
  }


  public interface Notify {
    public void done(String path, Drawable drawable);
  }
}
