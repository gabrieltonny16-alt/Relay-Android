package com.relay.offline

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private var pendingNumber = ""
    private var pendingMessage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(RelayBridge(), "RelayAndroid")
        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")
    }

    inner class RelayBridge {
        @JavascriptInterface fun sendSms(number: String, message: String) {
            runOnUiThread {
                pendingNumber = number
                pendingMessage = message
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.SEND_SMS), 701)
                } else sendPendingSms()
            }
        }
    }

    private fun sendPendingSms() {
        try {
            @Suppress("DEPRECATION")
            val sms = SmsManager.getDefault()
            sms.sendTextMessage(pendingNumber, null, pendingMessage, null, null)
            Toast.makeText(this, "SMS submitted to your mobile network", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "SMS could not be sent: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 701 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) sendPendingSms()
        else if (requestCode == 701) Toast.makeText(this, "SMS permission was not granted", Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
