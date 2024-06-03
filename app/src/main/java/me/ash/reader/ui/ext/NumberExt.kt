package me.ash.reader.ui.ext

infix fun Int.spacerDollar(str: Any): String = "$this$$str"

fun String.dollarLast(): String = split("$").last()

fun Int.getDefaultGroupId() = this.spacerDollar("read_you_app_default_group")

fun Int.toBoolean(): Boolean = this != 0
