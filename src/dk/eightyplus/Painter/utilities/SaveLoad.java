package dk.eightyplus.Painter.utilities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * User: fries
 * Date: 18/03/14
 * Time: 16.59
 */
public interface SaveLoad {
  public void save(final ObjectOutputStream outputStream) throws IOException;
  public void load(final ObjectInputStream inputStream) throws IOException, ClassNotFoundException;
}
