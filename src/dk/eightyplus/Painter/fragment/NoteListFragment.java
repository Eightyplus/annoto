package dk.eightyplus.Painter.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.note_list_layout, container, false);

    context = getActivity().getApplicationContext();
    String[] list = Storage.getStorage(context).getNotes();
    adapter = new NoteListAdapter(context, R.layout.note_list_item, list);

    view.findViewById(R.id.new_note).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(context, "Add new", Toast.LENGTH_SHORT).show();
      }
    });

    ListView listView = (ListView) view.findViewById(android.R.id.list);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item = adapter.getItem(position);
        Toast.makeText(context, "Opening: " + item, Toast.LENGTH_SHORT).show();

        Callback callback = callbackSoftReference.get();
        if (callback != null) {
          callback.load(item);
        }

      }
    });

    listView.setAdapter(adapter);

    return view;
  }
}
