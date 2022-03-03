package me.ash.reader.data.constant

class Filter(
    var index: Int,
    var title: String,
    var description: String,
    var important: Int,
) {
    companion object {
        val Starred = Filter(
            index = 0,
            title = "Starred",
            description = " Starred Items",
            important = 13
        )
        val Unread = Filter(
            index = 1,
            title = "Unread",
            description = " Unread Items",
            important = 666
        )
        val All = Filter(
            index = 2,
            title = "All",
            description = " Unread Items",
            important = 666
        )
    }
}