package me.ash.reader.data.source

import android.content.Context
import androidx.room.*
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.data.dao.ArticleDao
import me.ash.reader.data.dao.FeedDao
import me.ash.reader.data.dao.GroupDao
import me.ash.reader.data.entity.Account
import me.ash.reader.data.entity.Article
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.entity.Group
import java.util.*

@Database(
    entities = [Account::class, Feed::class, Article::class, Group::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(ReaderDatabase.Converters::class)
abstract class ReaderDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun feedDao(): FeedDao
    abstract fun articleDao(): ArticleDao
    abstract fun groupDao(): GroupDao

    companion object {
        private var instance: ReaderDatabase? = null

        fun getInstance(context: Context): ReaderDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ReaderDatabase::class.java,
                    "Reader"
                ).build().also {
                    instance = it
                }
            }
        }
    }

    class Converters {

        @TypeConverter
        fun toDate(dateLong: Long?): Date? {
            return dateLong?.let { Date(it) }
        }

        @TypeConverter
        fun fromDate(date: Date?): Long? {
            return date?.time
        }
    }
}