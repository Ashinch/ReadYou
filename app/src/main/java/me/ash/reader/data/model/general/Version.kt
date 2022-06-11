package me.ash.reader.data.model.general

class Version(identifiers: List<String>) {

    private var major: Int = 0
    private var minor: Int = 0
    private var point: Int = 0

    init {
        major = identifiers.getOrNull(0)?.toIntOrNull() ?: 0
        minor = identifiers.getOrNull(1)?.toIntOrNull() ?: 0
        point = identifiers.getOrNull(2)?.toIntOrNull() ?: 0
    }

    constructor() : this(listOf())
    constructor(string: String?) : this(string?.split(".") ?: listOf())

    fun whetherNeedUpdate(current: Version, skip: Version): Boolean = this > current && this > skip

    operator fun compareTo(target: Version): Int = when {
        major > target.major -> 1
        major < target.major -> -1
        minor > target.minor -> 1
        minor < target.minor -> -1
        point > target.point -> 1
        point < target.point -> -1
        else -> 0
    }

    override fun toString() = "$major.$minor.$point"
}

fun String?.toVersion(): Version = Version(this)
