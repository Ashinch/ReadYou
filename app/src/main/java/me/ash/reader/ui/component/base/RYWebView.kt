package me.ash.reader.ui.component.base

import android.net.http.SslError
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import me.ash.reader.infrastructure.preference.LocalOpenLink
import me.ash.reader.infrastructure.preference.LocalOpenLinkSpecificBrowser
import me.ash.reader.ui.ext.openURL

const val INJECTION_TOKEN = "/android_asset_font/"

@Composable
fun RYWebView(
    modifier: Modifier = Modifier,
    content: String,
    onReceivedError: (error: WebResourceError?) -> Unit = {},
) {
    val context = LocalContext.current
    val openLink = LocalOpenLink.current
    val openLinkSpecificBrowser = LocalOpenLinkSpecificBrowser.current
    val color = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val backgroundColor = MaterialTheme.colorScheme.surface.toArgb()
    val webViewClient by remember {
        mutableStateOf(object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView?,
                url: String?,
            ): WebResourceResponse? {
                if (url != null && url.contains(INJECTION_TOKEN)) {
                    try {
                        val assetPath = url.substring(
                            url.indexOf(INJECTION_TOKEN) + INJECTION_TOKEN.length,
                            url.length
                        )
                        return WebResourceResponse(
                            "text/HTML",
                            "UTF-8",
                            context.assets.open(assetPath)
                        )
                    } catch (e: Exception) {
                        Log.e("RLog", "WebView shouldInterceptRequest: $e")
                    }
                }
                return super.shouldInterceptRequest(view, url);
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val jsCode = "javascript:(function(){" +
                        "var imgs=document.getElementsByTagName(\"img\");" +
                        "for(var i=0;i<imgs.length;i++){" +
                        "imgs[i].pos = i;" +
                        "imgs[i].onclick=function(){" +
//                    "window.jsCallJavaObj.openImage(this.src,this.pos);" +
                        "alert('asf');" +
                        "}}})()"
                view!!.loadUrl(jsCode)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                if (null == request?.url) return false
                val url = request.url.toString()
                if (url.isNotEmpty()) context.openURL(url, openLink, openLinkSpecificBrowser)
                return true
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                super.onReceivedError(view, request, error)
                onReceivedError(error)
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?,
            ) {
                handler?.cancel()
            }
        })
    }

    val webView by remember {
        mutableStateOf(WebView(context).apply {
            this.webViewClient = webViewClient
            setBackgroundColor(backgroundColor)
            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
        })
    }

//    Column(
//        modifier = modifier
//            .height(if (viewState.isLoading) 100.dp else 0.dp),
//    ) {
//        Icon(
//            modifier = modifier
//                .size(50.dp),
//            imageVector = Icons.Rounded.HourglassBottom,
//            contentDescription = "Loading",
//            tint = MaterialTheme.colorScheme.primary,
//        )
//        Spacer(modifier = modifier.height(50.dp))
//    }

    AndroidView(
        modifier = modifier,//.padding(horizontal = if (content.contains("class=\"page\"")) 0.dp else 24.dp),
        factory = { webView },
        update = {
            it.apply {
                settings.javaScriptEnabled = true
                loadDataWithBaseURL(
                    null,
                    getStyle(color) + content,
                    "text/HTML",
                    "UTF-8", null
                )
            }
        },
    )
}

@Stable
fun argbToCssColor(argb: Int): String = String.format("#%06X", 0xFFFFFF and argb)

// TODO: Google sans is deprecated
@Stable
fun getStyle(argb: Int): String = """
<html><head><style>
*{
    padding: 0;
    margin: 0;
    color: ${argbToCssColor(argb)};
    // font-family: url('/android_asset_font/font/google_sans_text_regular.TTF'),
    //     url('/android_asset_font/font/google_sans_text_medium_italic.TTF'),
    //     url('/android_asset_font/font/google_sans_text_medium.TTF'),
    //     url('/android_asset_font/font/google_sans_text_italic.TTF'),
    //     url('/android_asset_font/font/google_sans_text_bold_italic.TTF'),
    //     url('/android_asset_font/font/google_sans_text_bold.TTF');
}

html {
    padding: 0 24px;
}

img, video, iframe {
    margin: 0 -24px 20px;
    width: calc(100% + 48px);
    border-top: 1px solid ${argbToCssColor(argb)}08;
    border-bottom: 1px solid ${argbToCssColor(argb)}08;
}

p,span,a,ol,ul,blockquote,article,section {
    text-align: left;
    font-size: 16px;
    line-height: 24px;
    margin-bottom: 20px;
}

ol,ul {
    padding-left: 1.5rem;
}

section ul {

}

blockquote {
    margin-left: 0.5rem;
    padding-left: 0.7rem;
    border-left: 1px solid ${argbToCssColor(argb)}33;
    color: ${argbToCssColor(argb)}cc;
}

pre {
    max-width: 100%;
    background: ${argbToCssColor(argb)}11;
    padding: 10px;
    border-radius: 5px;
    margin-bottom: 20px;
}

code {
    white-space: pre-wrap;
}

hr {
    height: 1px;
    border: none;
    background: ${argbToCssColor(argb)}33;
    margin-bottom: 20px;
}

h1,h2,h3,h4,h5,h6,figure,br {
    font-size: large;
    margin-bottom: 20px;
}

.element::-webkit-scrollbar { width: 0 !important }
</style></head></html>
"""
