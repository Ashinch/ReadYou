package me.ash.reader.ui.ext

fun Int.spacerDollar(str: Any): String = "$this$$str"

fun Int.getDefaultGroupId() = this.spacerDollar("read_you_app_default_group")