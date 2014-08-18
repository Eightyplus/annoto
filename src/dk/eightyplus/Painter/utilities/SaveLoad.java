package dk.eightyplus.Painter.utilities;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * User: fries
 * Date: 18/03/14
 * Time: 16.59
 */
public interface SaveLoad {
  public void save(final Context context, final DataOutputStream outputStream) throws IOException;
  public void load(final Context context, final DataInputStream inputStream) throws IOException, ClassNotFoundException;
}
