package dk.eightyplus.annoto

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView

/**
 * User: fries
 * Date: 15/07/14
 * Time: 13.03
 */
class WebPageActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.web_page_layout)

        intent?.getStringExtra(Keys.LINK).let {
            (findViewById<View>(R.id.web_view) as WebView).loadUrl(it)
        }
    }

    companion object {

        fun startWebPageActivity(activity: Activity, url: String) {
            val webPageIntent = createIntent(activity, url)
            activity.startActivity(webPageIntent)
        }

        private fun createIntent(activity: Activity, url: String): Intent {
            return Intent(activity, WebPageActivity::class.java).run {
                putExtra(Keys.LINK, url)
            }
        }
    }
}
