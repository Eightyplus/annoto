package dk.eightyplus.Painter.component;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 *
 */
public class Picture extends Component {

  private float x = 0.0f;
  private float y = 0.0f;
  private Bitmap bitmap;
  private float scale = 1.0f;

  public Picture(Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  public void setScale(float scale) {
    this.scale = scale;
  }

  @Override
  public void onDraw(Canvas canvas, Paint paint) {
    if (scale == 1.0f) {
      canvas.drawBitmap(bitmap, x, y, paint);
    } else {
      canvas.drawBitmap(bitmap, getMatrix(), paint);
    }
  }

  // ERROR?
  private Matrix getMatrix() {
    float[] values = new float[Matrix.MPERSP_2 + 1];
    values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = scale;
    values[Matrix.MTRANS_X] = x;
    values[Matrix.MTRANS_Y] = y;
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
    return new RectF(x, y, x + bitmap.getWidth(), y + bitmap.getHeight());
  }
}
