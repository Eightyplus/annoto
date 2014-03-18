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
public class Picture extends Component {

  private float x = 0.0f;
  private float y = 0.0f;
  private float scale = 1.0f;
  private transient Bitmap bitmap;

  public Picture(Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  public void setScale(float scale) {
    this.scale = scale;
  }

  @Override
  public void onDraw(Canvas canvas, Paint paint) {
    if (visible) {
      if (scale == 1.0f) {
        canvas.drawBitmap(bitmap, x, y, paint);
      } else {
        canvas.drawBitmap(bitmap, getMatrix(), paint);
      }
    }
  }

  private Matrix getMatrix() {
    float[] values = new float[Matrix.MPERSP_2 + 1];
    values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = scale;
    values[Matrix.MTRANS_X] = x;
    values[Matrix.MTRANS_Y] = y;
    values[Matrix.MPERSP_2] = 1;
    Matrix matrix = new Matrix();
    matrix.reset();
    matrix.setValues(values);
    return matrix;
  }

  @Override
  public void move(float dx, float dy) {
    x += dx;
    y += dy;
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
