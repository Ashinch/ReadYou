package me.ash.reader.domain.model.general

/**
 * Application version number, consisting of three fields.
 *
 * - [major]: The major version number, such as 1
 * - [minor]: The major version number, such as 2
 * - [point]: The major version number, such as 3 (if converted to a string,
 * the value is: "1.2.3")
 */
class Version(numbers: List<String>) {

    private var major: Int = 0
    private var minor: Int = 0
    private var point: Int = 0

    init {
        major = numbers.getOrNull(0)?.toIntOrNull() ?: 0
        minor = numbers.getOrNull(1)?.toIntOrNull() ?: 0
        point = numbers.getOrNull(2)?.toIntOrNull() ?: 0
    }

    constructor() : this(listOf())
    constructor(string: String?) : this(string?.split(".") ?: listOf())

    override fun toString() = "$major.$minor.$point"

    /**
     * Use [major], [minor], [point] for comparison.
     *
     * 1. [major] <=> [other.major]
     * 2. [minor] <=> [other.minor]
     * 3. [point] <=> [other.point]
     */
    operator fun compareTo(other: Version): Int = when {
        major > other.major -> 1
        major < other.major -> -1
        minor > other.minor -> 1
        minor < other.minor -> -1
        point > other.point -> 1
        point < other.point -> -1
        else -> 0
    }

    /**
     * Returns whether this version is larger [current] version and [skip] version.
     */
    fun whetherNeedUpdate(current: Version, skip: Version): Boolean = this > current && this > skip
}

fun String?.toVersion(): Version = Version(this)
