package me.ash.reader.ui.ext

import android.text.Html
import android.util.Base64
import java.math.BigInteger
import java.security.MessageDigest
import java.text.Bidi

object MimeType {

    const val ANY = "*/*"
    const val FONT = "font/ttf"
    const val OPML = "text/x-opml"  // Not supported yet
    const val JSON = "application/json"
}

fun String.formatUrl(): String {
    if (this.startsWith("//")) {
        return "https:$this"
    }
    val regex = Regex("^(https?|ftp|file).*")
    return if (!regex.matches(this)) {
        "https://$this"
    } else {
        this
    }
}

fun String.isUrl(): Boolean {
    val regex = Regex("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]")
    return regex.matches(this)
}

fun String.mask(): String = run {
    "\u2022".repeat(length)
}

fun String.encodeBase64(): String = Base64.encodeToString(toByteArray(), Base64.DEFAULT)

fun String.decodeBase64(): String = String(Base64.decode(this, Base64.DEFAULT))

fun String.md5(): String =
    BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
        .toString(16).padStart(32, '0')

fun String?.decodeHTML(): String? = this?.run { Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString() }

fun String?.orNotEmpty(l: (value: String) -> String): String =
    if (this.isNullOrBlank()) "" else l(this)


fun String.requiresBidi(): Boolean = Bidi.requiresBidi(this.toCharArray(), 0, this.length)

fun String?.extractDomain(): String? {
    if (this.isNullOrBlank()) return null
    val urlMatchResult = Regex("(?<=://)([\\w\\d.-]+)").find(this)
    if (urlMatchResult != null) {
        return urlMatchResult.value
    }
    val domainRegex = Regex("[\\w\\d.-]+\\.[\\w\\d.-]+")
    val domainMatchResult = domainRegex.find(this)
    return domainMatchResult?.value
}
