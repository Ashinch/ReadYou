package me.ash.reader.data.source

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
)
@TypeConverters(RYDatabase.Converters::class)
abstract class RYDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun feedDao(): FeedDao
    abstract fun articleDao(): ArticleDao
    abstract fun groupDao(): GroupDao

    companion object {
        private var instance: RYDatabase? = null

        fun getInstance(context: Context): RYDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RYDatabase::class.java,
                    "Reader"
                ).addMigrations(*allMigrations).build().also {
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

val allMigrations = arrayOf(
    MIGRATION_1_2,
)

@Suppress("ClassName")
object MIGRATION_1_2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE article ADD COLUMN img TEXT DEFAULT NULL
            """.trimIndent()
        )
    }
}