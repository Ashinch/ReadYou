package me.ash.reader.ui.component.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.webkit.JavascriptInterface
import android.webkit.WebView

object WebViewLayout {

    @SuppressLint("SetJavaScriptEnabled")
    fun get(
        context: Context,
        webViewClient: WebViewClient,
        onImageClick: ((imgUrl: String, altText: String) -> Unit)? = null,
    ) = WebView(context).apply {
        this.webViewClient = webViewClient
        scrollBarSize = 0
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = true
        setBackgroundColor(Color.TRANSPARENT)
        with(this.settings) {
            domStorageEnabled = true
            javaScriptEnabled = true
            addJavascriptInterface(object : JavaScriptInterface {
                @JavascriptInterface
                override fun onImgTagClick(imgUrl: String?, alt: String?) {
                    if (onImageClick != null && imgUrl != null) {
                        onImageClick.invoke(imgUrl, alt ?: "")
                    }
                }
            }, JavaScriptInterface.NAME)
            setSupportZoom(false)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                isAlgorithmicDarkeningAllowed = true
            }
        }
    }
}
