package dk.eightyplus.Painter.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import dk.eightyplus.Painter.R;
import dk.eightyplus.Painter.utilities.Compatibility;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * User: fries
 * Date: 29/03/14
 * Time: 19.34
 */
public class NoteListAdapter extends ArrayAdapter<String> implements ThumbLoader.Notify {

  @SuppressWarnings("unused")
  private static final String TAG = NoteListAdapter.class.toString();

  private static final int initialThumbLoadSize = 3;
  Map<String, SoftReference<Drawable>> cachedImages = new HashMap<String, SoftReference<Drawable>>();
  Map<String, Boolean> loading = new HashMap<String, Boolean>();

  public NoteListAdapter(Context context, int resource, String[] objects) {
    super(context, resource, objects);
    for (int i = 0; i < initialThumbLoadSize; i++) {
      startImageLoad(objects[i]);
    }
  }

  @Override
  public String getItem(int position) {
    return super.getItem(position);
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.note_list_item, parent, false);

    TextView textView = (TextView) rowView.findViewById(R.id.note_title);
    final ImageView imageView = (ImageView) rowView.findViewById(R.id.note_thumb);
    String item = getItem(position);
    textView.setText(item);

    String s = item;

    if (!setImage(imageView, s)) {
      imageView.setImageResource(android.R.drawable.stat_notify_error);
      startImageLoad(s);
    }

    return rowView;
  }

  private boolean setImage(ImageView imageView, String path) {
    if (cachedImages.containsKey(path)) {
      Drawable drawable = cachedImages.get(path).get();
      if (drawable != null) {
        imageView.setImageDrawable(drawable);
        return true;
      }
    }
    return false;
  }

  private void startImageLoad(String s) {
    if (doStartLoad(s)) {
      setLoadingStarted(s);
      Compatibility.get().startTask(new ThumbLoader(getContext(), this), s);
    }
  }

  private void setLoadingStarted(String key) {
    loading.put(key, true);
  }

  private void setLoadingDone(String key) {
    loading.remove(key);
  }

  private boolean doStartLoad(String s) {
    return !loading.containsKey(s) || loading.get(s) == false;
  }

  @Override
  public void done(String path, Drawable drawable) {
    cachedImages.put(path, new SoftReference<Drawable>(drawable));
    setLoadingDone(path);
    notifyDataSetChanged();
  }
}
