package me.ash.reader.data.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.ash.reader.data.Converters
import me.ash.reader.data.account.Account
import me.ash.reader.data.account.AccountDao
import me.ash.reader.data.article.Article
import me.ash.reader.data.article.ArticleDao
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.feed.FeedDao
import me.ash.reader.data.group.Group
import me.ash.reader.data.group.GroupDao

@Database(
    entities = [Account::class, Feed::class, Article::class, Group::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
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
}