package com.example.androidwebviewappwithmax

import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.example.androidwebviewappwithmax.ui.theme.AndroidWebViewAppWithMaxTheme
import kotlin.math.log

class MainActivity : ComponentActivity(), MaxRewardedAdListener {
    private lateinit var rewardedAd: MaxRewardedAd
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initConfig = AppLovinSdkInitializationConfiguration.builder("YOUR_SDK_KEY", this)
            .setMediationProvider(AppLovinMediationProvider.MAX)
            .build()
        AppLovinSdk.getInstance(this).initialize(initConfig) { sdkConfig ->
            rewardedAd = MaxRewardedAd.getInstance( "«ad-unit-ID»", applicationContext )
            rewardedAd.setListener( this )
        }

        setContent {
            AndroidWebViewAppWithMaxTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WebViewScreen("""
                        <html>
                            <head>
                                <title>sample</title>
                                <script type="text/javascript">
                                    function enableShowButton() {
                                        document.getElementById("showButton").disabled = false;
                                    }
                                    function disableShowButton() {
                                        document.getElementById("showButton").disabled = true;
                                    }
                                </script>
                            </head>
                            <body>
                                <h1>sample</h1>
                                <button id="loadButton" type="button" onclick="app.loadAd()">load</button>
                                <button id="showButton" type="button" onclick="app.showAd()" disabled>show</button>
                            </body>
                        </html>
                    """)
                }
            }
        }
    }

    @Composable
    fun WebViewScreen(htmlContent: String) {
        AndroidView(factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                addJavascriptInterface(WebAppInterface(this@MainActivity), "app")
                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                webView = this
            }
        }, modifier = Modifier.fillMaxSize())
    }

    fun load() {
        Log.d("MainActivity", "click load button")
        rewardedAd.loadAd()
    }

    fun show() {
        Log.d("MainActivity", "click show button")
        if ( rewardedAd.isReady ) {
            rewardedAd.showAd(this);
        }
    }

    // MAX Ad Listener
    override fun onAdLoaded(maxAd: MaxAd) {
        Log.d("MainActivity", "ad loaded")
        runOnUiThread {
            webView.evaluateJavascript("enableShowButton();", null)
        }
    }

    override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
        Log.d("MainActivity", "ad load failed")
    }

    override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
        Log.d("MainActivity", "ad display failed")
    }

    override fun onAdDisplayed(maxAd: MaxAd) {
        Log.d("MainActivity", "ad displayed")
    }

    override fun onAdClicked(maxAd: MaxAd) {
        Log.d("MainActivity", "ad clicked")
    }

    override fun onAdHidden(maxAd: MaxAd) {
        Log.d("MainActivity", "ad hidden")
        runOnUiThread {
            webView.evaluateJavascript("disableShowButton();", null)
        }
    }

    override fun onUserRewarded(maxAd: MaxAd, maxReward: MaxReward) {
        Log.d("MainActivity", "user rewarded")
    }
}

class WebAppInterface(private val activity: MainActivity) {
    @JavascriptInterface
    fun loadAd() {
        activity.load()
    }

    @JavascriptInterface
    fun showAd() {
        activity.show()
    }
}