package me.ash.reader.infrastructure.html

import android.util.Log
import net.dankito.readability4j.extended.Readability4JExtended
import org.jsoup.nodes.Element

object Readability {

    fun parseToText(htmlContent: String?, uri: String?): String {
        htmlContent ?: return ""
        return try {
            Readability4JExtended(uri ?: "", htmlContent).parse().textContent?.trim() ?: ""
        } catch (e: Exception) {
            Log.e("RLog", "Readability.parseToText '$uri' is error: ", e)
            ""
        }
    }

    fun parseToElement(htmlContent: String?, uri: String?): Element? {
        htmlContent ?: return null
        return try {
            Readability4JExtended(uri ?: "", htmlContent).parse().articleContent
        } catch (e: Exception) {
            Log.e("RLog", "Readability.parseToElement '$uri' is error: ", e)
            null
        }
    }
}
