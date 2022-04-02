package me.ash.reader.ui.ext

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