package dk.eightyplus.annoto.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import dk.eightyplus.annoto.R;
import dk.eightyplus.annoto.utilities.FileId;
import dk.eightyplus.annoto.utilities.Storage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Picture component in the Composite pattern
 */
@SuppressWarnings("unused")
public class Picture extends Component {

  private transient Bitmap bitmap;
  private final String filename;
  private final transient Context context;

  private Picture(final Context context, final String filename) {
    this.context = context;
    this.filename = filename;
  }

  public Picture(final Context context, final Bitmap bitmap, final String filename) {
    this(context, filename);
    if (bitmap == null) {
      throw new NullPointerException(context.getString(R.string.log_error_bitmap_null));
    }
    this.bitmap = bitmap;
  }

  @Override
  public void onDraw(Canvas canvas, Paint paint) {
    if (isVisible()) {
      canvas.drawBitmap(bitmap, getMatrix(), paint);
    }
  }

  private Matrix getMatrix() {
    float[] values = new float[Matrix.MPERSP_2 + 1];
    values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = getScale();
    values[Matrix.MTRANS_X] = x;
    values[Matrix.MTRANS_Y] = y;
    values[Matrix.MPERSP_2] = 1;
    Matrix matrix = new Matrix();
    matrix.reset();
    matrix.setValues(values);
    return matrix;
  }

  @Override
  public float centerDist(float x, float y) {
    return calculateCenterDistance(x, y, getBounds());
  }

  @Override
  public RectF getBounds() {
    return new RectF(x, y, x + bitmap.getWidth() * scale, y + bitmap.getHeight() * scale);
  }

  @Override
  public boolean delete() {
    super.delete();
    return Storage.getStorage(context).deleteFile(filename);
  }

  @Override
  public ComponentType getType() {
    return ComponentType.PictureType;
  }

  public static Picture fromJson(final Context context, JSONObject object) throws JSONException {
    Picture picture = new Picture(context, object.getString(FileId.FILE_NAME));
    picture.fromJsonPrimary(object);
    return picture;
  }

  /**
   * Initialises picture when loaded from storage
   * @throws IOException
   */
  public Picture initialise() throws IOException {
    bitmap = Storage.getStorage(context).loadFromFile(filename);
    if (bitmap == null) {
      throw new IOException(context.getString(R.string.log_error_file_missing));
    }
    return this;
  }

  @Override
  public JSONObject toJson() throws JSONException {
    JSONObject object = super.toJson();
    object.put(FileId.FILE_NAME, filename);
    return object;
  }
}
