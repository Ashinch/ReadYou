package me.ash.reader.ui.component.base

import android.content.Context
import android.graphics.Color
import android.net.http.SslError
import android.os.Build
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import me.ash.reader.infrastructure.preference.LocalOpenLink
import me.ash.reader.infrastructure.preference.LocalOpenLinkSpecificBrowser
import me.ash.reader.infrastructure.preference.LocalReadingImageHorizontalPadding
import me.ash.reader.infrastructure.preference.LocalReadingImageRoundedCorners
import me.ash.reader.infrastructure.preference.LocalReadingPageTonalElevation
import me.ash.reader.infrastructure.preference.LocalReadingSubheadAlign
import me.ash.reader.infrastructure.preference.LocalReadingSubheadBold
import me.ash.reader.infrastructure.preference.LocalReadingTextAlign
import me.ash.reader.infrastructure.preference.LocalReadingTextBold
import me.ash.reader.infrastructure.preference.LocalReadingTextFontSize
import me.ash.reader.infrastructure.preference.LocalReadingTextHorizontalPadding
import me.ash.reader.infrastructure.preference.LocalReadingTextLetterSpacing
import me.ash.reader.ui.ext.openURL
import me.ash.reader.ui.ext.surfaceColorAtElevation
import kotlin.math.absoluteValue

const val INJECTION_TOKEN = "/android_asset_font/"

@Composable
fun RYWebView(
    modifier: Modifier = Modifier,
    content: String,
    onReceivedError: (error: WebResourceError?) -> Unit = {},
) {
    val context = LocalContext.current
    val maxWidth = LocalConfiguration.current.screenWidthDp.dp.value
    val openLink = LocalOpenLink.current
    val openLinkSpecificBrowser = LocalOpenLinkSpecificBrowser.current
    val tonalElevation = LocalReadingPageTonalElevation.current
    val backgroundColor = MaterialTheme.colorScheme
        .surfaceColorAtElevation(tonalElevation.value.dp).toArgb()
    val bodyColor: Int = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val linkColor: Int = MaterialTheme.colorScheme.primary.toArgb()
    val subheadColor: Int = MaterialTheme.colorScheme.onSurface.toArgb()
    val subheadBold: Boolean = LocalReadingSubheadBold.current.value
    val subheadAlign: String = LocalReadingSubheadAlign.current.toTextAlignCSS()
    val textBold: Boolean = LocalReadingTextBold.current.value
    val textAlign: String = LocalReadingTextAlign.current.toTextAlignCSS()
    val textFontSize: Int = LocalReadingTextFontSize.current
    val textLetterSpacing: Float = LocalReadingTextLetterSpacing.current
    val imageHorizontalPadding: Int = LocalReadingImageHorizontalPadding.current
    val textHorizontalPadding: Int = LocalReadingTextHorizontalPadding.current
    val imageShape: Int = LocalReadingImageRoundedCorners.current
    val codeColor: Int = MaterialTheme.colorScheme.primary.toArgb()
    val codeBackgroundColor: Int = MaterialTheme.colorScheme
        .surfaceColorAtElevation((tonalElevation.value + 6).dp).toArgb()
    val webViewClient by remember {
        mutableStateOf(object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?,
            ): WebResourceResponse? {
                val url = request?.url?.toString()
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
                return super.shouldInterceptRequest(view, request);
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

    val webView by remember(backgroundColor) {
        mutableStateOf(WebView(context).run {
            this.webViewClient = webViewClient
            setBackgroundColor(Color.TRANSPARENT)
            scrollBarSize = 0
            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = true
            with(this.settings) {
                domStorageEnabled = true
                javaScriptEnabled = true // Do we need JS?
                setSupportZoom(false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    isAlgorithmicDarkeningAllowed = true
                }
            }
            // this.loadUrl(url)
            this
        })
    }

    AndroidView(
        modifier = modifier,
        factory = { webView },
        update = {
            // if (isRefreshing) {
            //     it.reload()
            //     setRefreshed()
            // }
            it.apply {
                Log.i("RLog", "maxWidth: ${maxWidth}")
                Log.i("RLog", "readingFont: ${context.filesDir.absolutePath}")
                Log.i("RLog", "CustomWebView: ${content}")
                settings.javaScriptEnabled = true
                settings.defaultFontSize = textFontSize
                setBackgroundColor(Color.TRANSPARENT)
                scrollBarSize = 0
                loadDataWithBaseURL(
                    null,
                    getStyle(
                        context = context,
                        maxWidth = maxWidth,
                        bodyColor = bodyColor,
                        linkColor = linkColor,
                        subheadColor = subheadColor,
                        subheadBold = subheadBold,
                        subheadAlign = subheadAlign,
                        textBold = textBold,
                        textAlign = textAlign,
                        textFontSize = textFontSize,
                        textLetterSpacing = textLetterSpacing,
                        imageHorizontalPadding = imageHorizontalPadding,
                        textHorizontalPadding = textHorizontalPadding,
                        imageShape = imageShape,
                        codeColor = codeColor,
                        codeBackgroundColor = codeBackgroundColor,
                    ) + content,
                    "text/HTML",
                    "UTF-8", null
                )
            }
        },
    )
}

@Stable
fun argbToCssColor(argb: Int): String = String.format("#%06X", 0xFFFFFF and argb)

@Stable
fun getStyle(
    context: Context,
    maxWidth: Float,
    bodyColor: Int,
    linkColor: Int,
    subheadColor: Int,
    subheadBold: Boolean,
    subheadAlign: String,
    textBold: Boolean,
    textAlign: String,
    textFontSize: Int,
    textLetterSpacing: Float,
    imageHorizontalPadding: Int,
    textHorizontalPadding: Int,
    imageShape: Int,
    codeColor: Int,
    codeBackgroundColor: Int,
): String = """
<html><head><style>
@font-face {
    font-family: 'readingFont';
    src: url('file://${context.filesDir.absolutePath}/reading_font.ttf') format('truetype');
}
*{
    width: ${maxWidth - textHorizontalPadding * 2}px;
    padding: 0;
    margin: 0;
    color: ${argbToCssColor(bodyColor)};
    font-family: readingFont;
}

html {
    padding: 0 ${textHorizontalPadding}px;
}

img, video, iframe {
    margin: 0 ${imageHorizontalPadding - 24}px 20px;
    width: auto;
    height: auto;
    max-width: 100%;
    border-top: 1px solid ${argbToCssColor(bodyColor)}08;
    border-bottom: 1px solid ${argbToCssColor(bodyColor)}08;
    border-radius: ${imageShape}px;
    /*box-shadow: 0px 1px 3px 0px rgba(0, 0, 0, 0.30), 0px 4px 8px 3px rgba(0, 0, 0, 0.15);*/
}

@media (min-width: 50%) {
    img, video, iframe {
       width: ${(maxWidth - textHorizontalPadding * 2) + ((imageHorizontalPadding - 24).absoluteValue * 2)}px;
       max-width: ${maxWidth}px;
    }
}

figcaption {
    text-size: ${textFontSize}px;
    transform: scale(0.7);
    margin-bottom: 20px;
    color: ${argbToCssColor(bodyColor)}B3;
    text-align: center;
}

p,span,a,ol,ul,blockquote,article,section {
    font-weight: ${if (textBold) "600" else "400"};
    text-align: ${textAlign};
    font-size: ${textFontSize}px;
    letter-spacing: ${textLetterSpacing}px;
    color: ${argbToCssColor(bodyColor)};
    line-height: 24px;
    margin-bottom: 20px;
}

a {
    font-size: ${textFontSize}px;
    color: ${argbToCssColor(linkColor)};
    text-decoration: underline;
}

ol,ul {
    padding-left: 1.5rem;
}

section ul {

}

blockquote {
    max-width: 100%;
    margin-left: 0.5rem;
    padding-left: 0.7rem;
    border-left: 1px solid ${argbToCssColor(bodyColor)}33;
    color: ${argbToCssColor(bodyColor)}cc;
}

blockquote > img, blockquote > video, blockquote > iframe {
    margin: unset;
    max-width: 100%;
    border-radius: ${imageShape}px;
}

code {
    white-space: pre-wrap;
    word-wrap: break-word;
    text-size: 14px;
    font-family: monospace;
    color: ${argbToCssColor(codeColor)};
    background: ${argbToCssColor(codeBackgroundColor)};
    border-radius: 5px;
}

pre {
    width: ${maxWidth - textHorizontalPadding * 2}px;
    
    white-space: pre-wrap;
    background: ${argbToCssColor(bodyColor)}11;
    padding: 10px;
    border-radius: 5px;
    margin-bottom: 20px;
}

pre > code {
    white-space: pre-wrap;
    text-size: 14px;
    font-family: monospace;
    color: ${argbToCssColor(codeColor)};
    background: ${argbToCssColor(codeBackgroundColor)};
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
}

hr {
    height: 1px;
    border: none;
    background: ${argbToCssColor(bodyColor)}33;
    margin-bottom: 20px;
}

h1 {
    font-size: 28px;
    font-weight: ${if (subheadBold) "600" else "400"};
    letter-spacing: 0px;
    color: ${argbToCssColor(subheadColor)};
    text-align: ${subheadAlign};
    margin-bottom: 20px;
}

h2 {
    font-size: 28px;
    font-weight: ${if (subheadBold) "600" else "400"};
    letter-spacing: 0px;
    color: ${argbToCssColor(subheadColor)};
    text-align: ${subheadAlign};
    margin-bottom: 20px;
}

h3 {
    font-size: 19px;
    font-weight: ${if (subheadBold) "600" else "400"};
    letter-spacing: 0px;
    color: ${argbToCssColor(subheadColor)};
    text-align: ${subheadAlign};
    margin-bottom: 20px;
}

h4 {
    font-size: 17px;
    font-weight: ${if (subheadBold) "600" else "400"};
    letter-spacing: 0px;
    color: ${argbToCssColor(subheadColor)};
    text-align: ${subheadAlign};
    margin-bottom: 20px;
}

h5 {
    font-size: 17px;
    font-weight: ${if (subheadBold) "600" else "400"};
    letter-spacing: 0px;
    color: ${argbToCssColor(subheadColor)};
    text-align: ${subheadAlign};
    margin-bottom: 20px;
}

.comment {
    color: ${argbToCssColor(bodyColor)}80;
    font-style: italic;
}

.element::-webkit-scrollbar { width: 0 !important }
</style></head></html>
"""
