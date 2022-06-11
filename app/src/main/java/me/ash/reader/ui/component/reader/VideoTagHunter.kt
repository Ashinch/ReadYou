/*
 * Feeder: Android RSS reader app
 * https://gitlab.com/spacecowboy/Feeder
 *
 * Copyright (C) 2022  Jonas Kalderstam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ash.reader.ui.component.reader

// Example strings
// www.youtube.com/embed/cjxnVO9RpaQ
// www.youtube.com/embed/cjxnVO9RpaQ?feature=oembed
// www.youtube.com/embed/cjxnVO9RpaQ/theoretical_crap
// www.youtube.com/embed/cjxnVO9RpaQ/crap?feature=oembed
internal val YoutubeIdPattern = "youtube.com/embed/([^?/]*)".toRegex()

fun getVideo(src: String?): Video? {
    return src?.let {
        YoutubeIdPattern.find(src)?.let { match ->
            val videoId = match.groupValues[1]
            Video(
                src = src,
                imageUrl = "http://img.youtube.com/vi/$videoId/hqdefault.jpg",
                link = "https://www.youtube.com/watch?v=$videoId"
            )
        }
    }
}

data class Video(
    val src: String,
    val imageUrl: String,
    // Youtube needs a different link than embed links
    val link: String,
) {

    val width: Int
        get() = 480

    val height: Int
        get() = 360
}
