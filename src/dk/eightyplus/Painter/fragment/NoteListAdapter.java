package dk.eightyplus.Painter.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
  private Map<String, SoftReference<Drawable>> cachedImages = new HashMap<String, SoftReference<Drawable>>();
  private Map<String, Boolean> loading = new HashMap<String, Boolean>();

  private ButtonOnClickListener buttonOnClickListener;

  public NoteListAdapter(Context context, ListView listView, int resource, String[] objects) {
    super(context, resource, objects);
    for (int i = 0; i < initialThumbLoadSize && i < objects.length; i++) {
      startImageLoad(objects[i]);
    }

    buttonOnClickListener = new ButtonOnClickListener(listView);
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.note_list_item, parent, false);
    }
    View rowView = convertView;

    TextView textView = (TextView) rowView.findViewById(R.id.note_title);
    final ImageView imageView = (ImageView) rowView.findViewById(R.id.note_thumb);
    String item = getItem(position);
    textView.setText(item);

    final View buttonDelete = rowView.findViewById(R.id.button_delete);
    buttonDelete.setTag(R.id.button_delete);
    buttonDelete.setOnClickListener(buttonOnClickListener);

    if (!setImage(imageView, item)) {
      imageView.setImageResource(android.R.drawable.stat_notify_error);
      startImageLoad(item);
    }

    return rowView;
  }

  public void onPause() { }

  public void onResume() { }

  public void onDestroy() { }

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

  public Drawable getImage(int position) {
    String path = getItem(position);
    if (cachedImages.containsKey(path)) {
      return cachedImages.get(path).get();
    }
    return null;
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

  /**
   * Small class to put on buttons inside list view item, passed click with id of button
   */
  private static class ButtonOnClickListener implements View.OnClickListener {
    private final SoftReference<ListView> listViewSoftReference;

    public ButtonOnClickListener(ListView listView) {
      this.listViewSoftReference = new SoftReference<ListView>(listView);
    }

    @Override
    public void onClick(View v) {
      ListView listView = listViewSoftReference.get();
      if (listView != null) {
        int position = listView.getPositionForView((View) v.getParent());
        listView.performItemClick(v, position, (Integer) v.getTag());
      }
    }
  };
}
