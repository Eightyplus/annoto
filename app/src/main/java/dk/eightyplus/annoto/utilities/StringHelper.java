package dk.eightyplus.annoto.utilities;

import android.content.Context;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User: fries
 * Date: 14/07/14
 * Time: 14.06
 */
public class StringHelper {

  private static final String TAG = StringHelper.class.toString();

  private StringHelper() { }

  /**
   * @param context the context
   * @return the device id
   */
  @SuppressWarnings("unused")
  public static String deviceId(final Context context) {
    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    return StringHelper.md5(android_id).toUpperCase();
  }

  /**
   * @param input text
   * @return md5 of text
   */
  @SuppressWarnings("unused")
  public static String md5(final String input) {
    final int hash = 0xFF;

    try {
      // Create MD5 Hash
      MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
      digest.update(input.getBytes());
      byte[] messageDigest = digest.digest();

      // Create Hex String
      StringBuilder hexString = new StringBuilder();
      for (byte aMessageDigest : messageDigest) {
        String h = Integer.toHexString(hash & aMessageDigest);
        while (h.length() < 2) {
          h = "0" + h;
        }
        hexString.append(h);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      Log.d(TAG, "MD5 not available");
    }
    return null;
  }

  /**
   * Concatenates two string arrays
   * @param a first
   * @param b second
   * @return concatenated string array
   */
  @SuppressWarnings("unused")
  public static String[] concatenate(String[] a, String[] b) {
    if (a == null || a.length == 0) {
      return b;
    }
    if (b == null || b.length == 0) {
      return a;
    }

    int aLen = a.length;
    int bLen = b.length;
    String[] c = new String[aLen+bLen];
    System.arraycopy(a, 0, c, 0, aLen);
    System.arraycopy(b, 0, c, aLen, bLen);
    return c;
  }

  /**
   * Removes html formatting from text
   * @param html text containing html
   * @return stripped html or empty string if html is null
   */
  @SuppressWarnings("unused")
  public static String stripHtml(final String html) {
    return html == null ? "" : Html.fromHtml(html).toString();
  }

  /**
   * Removes all characters from text string except A-Z & a-z & 0-9 & . (dot)
   * @param text text to be stripped
   * @return stripped text
   */
  @SuppressWarnings("unused")
  public static String stripNonAlphaNumeric(final String text) {
    return text.replaceAll("[^A-Za-z0-9.]", "");
  }
}
