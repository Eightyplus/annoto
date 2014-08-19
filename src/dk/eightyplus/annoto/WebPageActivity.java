package dk.eightyplus.annoto;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

/**
 * User: fries
 * Date: 15/07/14
 * Time: 13.03
 */
public class WebPageActivity extends Activity {


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

    setContentView(R.layout.web_page_layout);

    Intent intent = getIntent();
    if (intent != null && intent.hasExtra(Keys.LINK)) {
      String url = intent.getStringExtra(Keys.LINK);
      ((WebView) findViewById(R.id.web_view)).loadUrl(url);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    //Compatibility.get().resumeWebView(webView);
  }

  @Override
  public void onPause() {
    super.onPause();
    //Compatibility.get().pauseWebView(webView);
  }

  public static void startWebPageActivity(final Activity activity, final String url) {
    Intent webPageIntent = createIntent(activity, url);
    activity.startActivity(webPageIntent);
  }

  private static Intent createIntent(final Activity activity, final String url) {
    final Intent webPageIntent = new Intent(activity, WebPageActivity.class);
    webPageIntent.putExtra(Keys.LINK, url);
    return webPageIntent;
  }
}
