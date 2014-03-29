package dk.eightyplus.Painter.component;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Picture component in the Composite pattern
 */
@SuppressWarnings("unused")
public class Picture extends Component {
  private static final long serialVersionUID = 9170000361129876541L;

  private transient Bitmap bitmap;

  public Picture(Bitmap bitmap) {
    if (bitmap == null) {
      throw new NullPointerException("Bitmap cannot be null");
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

  private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
    objectOutputStream.defaultWriteObject();
    if(bitmap != null){
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
      if(success){
        objectOutputStream.writeObject(byteStream.toByteArray());
      }
    }
  }

  private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException{
    objectInputStream.defaultReadObject();
    byte[] image = (byte[]) objectInputStream.readObject();
    if(image != null && image.length > 0){
      bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
    }
  }
}
