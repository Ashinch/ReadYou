@file:Suppress("SpellCheckingInspection")

package me.ash.reader.ui.ext

import me.ash.reader.BuildConfig

const val GITHUB = "github"
const val FDROID = "fdroid"

const val isFdroid = BuildConfig.FLAVOR == FDROID
const val notFdroid = !isFdroid