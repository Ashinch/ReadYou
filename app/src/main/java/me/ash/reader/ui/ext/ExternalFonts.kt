package me.ash.reader.ui.ext

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import me.ash.reader.ui.theme.SystemTypography
import java.io.File

class ExternalFonts(
    private val ctx: Context,
    private val uri: Uri,
    private val type: FontType,
) {

    enum class FontType(val value: String) {
        BasicFont("basic_font.ttf"),
        ReadingFont("reading_font.ttf"),
        ;

        fun toPath(ctx: Context): String = ctx.filesDir.absolutePath + File.separator + value
    }

    private lateinit var fontByteArray: ByteArray

    init {
        ctx.contentResolver.openInputStream(uri)?.use { inputStream ->
            fontByteArray = inputStream.readBytes()
            // File(inputStream.readString()).let {
            //     if (!it.exists()) throw IllegalArgumentException("Invalid path")
            //     if (!it.isFile) throw IllegalArgumentException("Invalid path")
            //     if (it.extension.lowercase() != "ttf") throw IllegalArgumentException("Only *.ttf fonts are supported")
            //     fontByteArray = it
            // }
        }
    }

    fun copyToInternalStorage() {
        File(type.toPath(ctx)).let {
            if (it.exists()) it.delete()
            if (it.createNewFile()) it.writeBytes(fontByteArray)
        }
    }

    companion object {

        fun loadBasicTypography(ctx: Context): Typography = loadTypography(ctx, FontType.BasicFont)

        fun loadReadingTypography(ctx: Context): Typography = loadTypography(ctx, FontType.ReadingFont)

        private var basicTypography: Typography? = null
        private var readingTypography: Typography? = null

        private fun createFontFamily(ctx: Context, type: FontType): FontFamily =
            File(type.toPath(ctx)).takeIf { it.exists() }
                ?.run { FontFamily(Typeface.createFromFile(this)) } ?: FontFamily.Default

        private fun createTypography(fontFamily: FontFamily): Typography =
            Typography(
                displayLarge = SystemTypography.displayLarge.copy(
                    fontFamily = fontFamily,
                ),
                displayMedium = SystemTypography.displayMedium.copy(
                    fontFamily = fontFamily,
                ),
                displaySmall = SystemTypography.displaySmall.copy(
                    fontFamily = fontFamily,
                ),
                headlineLarge = SystemTypography.headlineLarge.copy(
                    fontFamily = fontFamily
                ),
                headlineMedium = SystemTypography.headlineMedium.copy(
                    fontFamily = fontFamily
                ),
                headlineSmall = SystemTypography.headlineSmall.copy(
                    fontFamily = fontFamily
                ),
                titleLarge = SystemTypography.titleLarge.copy(
                    fontFamily = fontFamily
                ),
                titleMedium = SystemTypography.titleMedium.copy(
                    fontFamily = fontFamily
                ),
                titleSmall = SystemTypography.titleSmall.copy(
                    fontFamily = fontFamily
                ),
                labelLarge = SystemTypography.labelLarge.copy(
                    fontFamily = fontFamily
                ),
                bodyLarge = SystemTypography.bodyLarge.copy(
                    fontFamily = fontFamily
                ),
                bodyMedium = SystemTypography.bodyMedium.copy(
                    fontFamily = fontFamily
                ),
                bodySmall = SystemTypography.bodySmall.copy(
                    fontFamily = fontFamily
                ),
                labelMedium = SystemTypography.labelMedium.copy(
                    fontFamily = fontFamily
                ),
                labelSmall = SystemTypography.labelSmall.copy(
                    fontFamily = fontFamily
                ),
            )

        private fun loadTypography(ctx: Context, type: FontType): Typography =
            when (type) {
                FontType.BasicFont -> basicTypography ?: createTypography(createFontFamily(ctx, type))
                    .also { basicTypography = it }

                FontType.ReadingFont -> readingTypography ?: createTypography(createFontFamily(ctx, type))
                    .also { readingTypography = it }
            }
    }
}
