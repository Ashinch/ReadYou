package me.ash.reader.ui.widget

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.webkit.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.page.home.read.ReadViewAction
import me.ash.reader.ui.page.home.read.ReadViewModel

@Composable
fun WebView(
    modifier: Modifier = Modifier,
    content: String,
    viewModel: ReadViewModel = hiltViewModel(),
    onProgressChange: (progress: Int) -> Unit = {},
    onReceivedError: (error: WebResourceError?) -> Unit = {}
) {
    val context = LocalContext.current
    val color = MaterialTheme.colorScheme.secondary.toArgb()
    val backgroundColor = MaterialTheme.colorScheme.surface.toArgb()
    val viewState = viewModel.viewState.collectAsStateValue()
    val webViewClient = object : WebViewClient() {

        override fun onPageStarted(
            view: WebView?,
            url: String?,
            favicon: Bitmap?
        ) {
            super.onPageStarted(view, url, favicon)
//            _isLoading = true
            onProgressChange(-1)
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
            viewModel.dispatch(ReadViewAction.ChangeLoading(false))
            onProgressChange(100)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (null == request?.url) return false
            val url = request.url.toString()
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
            return true
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            onReceivedError(error)
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            handler?.cancel()
        }
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
        modifier = modifier,
        factory = {
            WebView(it).apply {
                this.webViewClient = webViewClient
                setBackgroundColor(backgroundColor)
                isHorizontalScrollBarEnabled = false
                isVerticalScrollBarEnabled = false
            }
        },
        update = {
            it.apply {
                Log.i("RLog", "CustomWebView: ${content}")
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

fun argbToCssColor(argb: Int): String = String.format("#%06X", 0xFFFFFF and argb)

fun getStyle(argb: Int): String = """
<head><style>
*{
    padding: 0;
    margin: 0;
    color: ${argbToCssColor(argb)}
}

.page {
    padding: 0 20px;
}

img {
    margin: 0 -20px 20px;
    width: calc(100% + 40px);
    height: auto;
}

p,span,a,ol,ul,blockquote,article,section {
    text-align: justify;
    font-size: 18px;
    line-height: 32px;
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
    margin-bottom: 20px;
}

.element::-webkit-scrollbar { width: 0 !important }
</style></head>
"""