package me.ash.reader.data.entity

import androidx.room.*

@Entity(
    tableName = "feed",
    foreignKeys = [ForeignKey(
        entity = Group::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
    )],
)
data class Feed(
    @PrimaryKey
    val id: String,
    @ColumnInfo
    val name: String,
    @ColumnInfo
    var icon: ByteArray? = null,
    @ColumnInfo
    val url: String,
    @ColumnInfo(index = true)
    var groupId: String,
    @ColumnInfo(index = true)
    val accountId: Int,
    @ColumnInfo(defaultValue = "false")
    var isNotification: Boolean = false,
    @ColumnInfo(defaultValue = "false")
    var isFullContent: Boolean = false,
) {
    @Ignore
    var important: Int? = 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Feed

        if (id != other.id) return false
        if (name != other.name) return false
        if (icon != null) {
            if (other.icon == null) return false
            if (!icon.contentEquals(other.icon)) return false
        } else if (other.icon != null) return false
        if (url != other.url) return false
        if (groupId != other.groupId) return false
        if (accountId != other.accountId) return false
        if (isNotification != other.isNotification) return false
        if (isFullContent != other.isFullContent) return false
        if (important != other.important) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (icon?.contentHashCode() ?: 0)
        result = 31 * result + url.hashCode()
        result = 31 * result + groupId.hashCode()
        result = 31 * result + accountId
        result = 31 * result + isNotification.hashCode()
        result = 31 * result + isFullContent.hashCode()
        result = 31 * result + (important ?: 0)
        return result
    }
}
