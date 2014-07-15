package dk.eightyplus.Painter.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import dk.eightyplus.Painter.Callback;
import dk.eightyplus.Painter.R;
import dk.eightyplus.Painter.utilities.Storage;

import java.lang.ref.SoftReference;

/**
 * User: fries
 * Date: 29/03/14
 * Time: 18.56
 */
public class NoteListFragment extends DialogFragment {

  private final SoftReference<Callback> callbackSoftReference;
  private Context context;
  private NoteListAdapter adapter;

  public NoteListFragment(Callback callback) {
    this.callbackSoftReference = new SoftReference<Callback>(callback);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (adapter != null) {
      adapter.onResume();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (adapter != null) {
      adapter.onPause();
    }
  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    if (adapter != null) {
      adapter.onDestroy();
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.note_list_layout, container, false);

    context = getActivity().getApplicationContext();
    ListView listView = (ListView) view.findViewById(android.R.id.list);
    String[] list = Storage.getStorage(context).getNotes();
    adapter = new NoteListAdapter(context, listView, R.layout.note_list_item, list);

    view.findViewById(R.id.new_note).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(context, "Add new", Toast.LENGTH_SHORT).show();
      }
    });

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final String fileName = adapter.getItem(position);
        if (id == R.id.button_delete) {
          LayoutInflater factory = LayoutInflater.from(context);
          View message = factory.inflate(R.layout.title_image_view, null);

          TextView title = (TextView) message.findViewById(R.id.message_title);
          ImageView image = (ImageView) message.findViewById(R.id.message_image);

          title.setText(context.getString(R.string.delete_file, fileName));
          image.setImageDrawable(adapter.getImage(position));

          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
              .setTitle(R.string.delete_list_item)
              .setView(message)
              .setCancelable(true)
              .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  Storage.getStorage(context).deleteNoteAndThumb(fileName);
                  NoteListFragment.this.dismiss();
                }
              })
              .setNegativeButton(R.string.cancel, null);
          builder.show();
          return;
        }

        Callback callback = callbackSoftReference.get();
        if (callback != null) {
          callback.load(fileName);
        }

      }
    });

    listView.setAdapter(adapter);

    return view;
  }
}
