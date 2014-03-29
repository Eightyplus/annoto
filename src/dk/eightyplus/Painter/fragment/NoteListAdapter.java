package dk.eightyplus.Painter.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import dk.eightyplus.Painter.R;
import dk.eightyplus.Painter.utilities.Storage;

import java.io.File;

/**
 * User: fries
 * Date: 29/03/14
 * Time: 19.34
 */
public class NoteListAdapter extends ArrayAdapter<String> {
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

    final File file = Storage.getStorage(rowView.getContext()).getThumb2Notes(item);

    if (file.exists()) {
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          Drawable drawable = Drawable.createFromPath(file.getAbsolutePath());
          imageView.setImageDrawable(drawable);
        }
      };

      runnable.run();
      //new Handler().post(runnable);
    } else {
      imageView.setImageResource(android.R.drawable.stat_notify_error);
    }

    return rowView;
  }
}
