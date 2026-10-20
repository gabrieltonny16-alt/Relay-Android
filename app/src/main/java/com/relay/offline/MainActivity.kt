package com.omniblast.os

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST = 4102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) return false
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    // Keep external links out of the local app WebView.
                    return true
                }
                return false
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.fileChooserCallback?.onReceiveValue(null)
                this@MainActivity.fileChooserCallback = filePathCallback

                return try {
                    val intent = fileChooserParams?.createIntent()
                        ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "text/csv"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST)
                    true
                } catch (e: Exception) {
                    this@MainActivity.fileChooserCallback?.onReceiveValue(null)
                    this@MainActivity.fileChooserCallback = null
                    Toast.makeText(
                        this@MainActivity,
                        "Unable to open the file picker on this device.",
                        Toast.LENGTH_LONG
                    ).show()
                    false
                }
            }
        }
        webView.addJavascriptInterface(OmniBlastBridge(), "RelayAndroid")
        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != FILE_CHOOSER_REQUEST) return

        val callback = fileChooserCallback ?: return
        fileChooserCallback = null

        val results = if (resultCode == RESULT_OK) {
            val selected = data?.data
            when {
                data?.clipData != null -> {
                    val clip = data.clipData!!
                    Array(clip.itemCount) { index -> clip.getItemAt(index).uri }
                }
                selected != null -> arrayOf(selected)
                else -> null
            }
        } else {
            null
        }
        callback.onReceiveValue(results)
    }

    inner class OmniBlastBridge {
        @JavascriptInterface
        fun openSmsComposer(number: String, message: String) {
            runOnUiThread {
                try {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.fromParts("smsto", number, null)
                        putExtra("sms_body", message)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "No SMS app is available on this device.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
