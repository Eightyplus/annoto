package dk.eightyplus.Painter.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import dk.eightyplus.Painter.R;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * User: fries
 * Date: 29/03/14
 * Time: 19.34
 */
public class NoteListAdapter extends ArrayAdapter<String> implements ThumbLoader.Notify {

  Map<String, SoftReference<Drawable>> cachedImages = new HashMap<String, SoftReference<Drawable>>();

  public NoteListAdapter(Context context, int resource, String[] objects) {
    super(context, resource, objects);
  }

  @Override
  public String getItem(int position) {
    return super.getItem(position);
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
      new ThumbLoader(getContext(), this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s);
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


  @Override
  public void done(String path, Drawable drawable) {
    cachedImages.put(path, new SoftReference<Drawable>(drawable));
    notifyDataSetChanged();
  }
}
