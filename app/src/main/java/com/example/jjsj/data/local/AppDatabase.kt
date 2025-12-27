package com.example.jjsj.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.jjsj.data.local.dao.FavoriteFundDao
import com.example.jjsj.data.local.dao.FundCacheDao
import com.example.jjsj.data.local.dao.FundDetailCacheDao
import com.example.jjsj.data.local.dao.FundNavCacheDao
import com.example.jjsj.data.local.dao.PositionDao
import com.example.jjsj.data.local.dao.SearchHistoryDao
import com.example.jjsj.data.local.dao.TransactionDao
import com.example.jjsj.data.local.entity.FavoriteFundEntity
import com.example.jjsj.data.local.entity.FundCacheEntity
import com.example.jjsj.data.local.entity.FundDetailCacheEntity
import com.example.jjsj.data.local.entity.FundNavCacheEntity
import com.example.jjsj.data.local.entity.PositionEntity
import com.example.jjsj.data.local.entity.SearchHistoryEntity
import com.example.jjsj.data.local.entity.TransactionEntity
import com.example.jjsj.util.Constants

/**
 * Room数据库
 */
@Database(
    entities = [
        FavoriteFundEntity::class,
        PositionEntity::class,
        FundCacheEntity::class,
        FundDetailCacheEntity::class,
        FundNavCacheEntity::class,
        TransactionEntity::class,
        SearchHistoryEntity::class
    ],
    version = Constants.DATABASE_VERSION + 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteFundDao(): FavoriteFundDao
    abstract fun positionDao(): PositionDao
    abstract fun fundCacheDao(): FundCacheDao
    abstract fun fundDetailCacheDao(): FundDetailCacheDao
    abstract fun fundNavCacheDao(): FundNavCacheDao
    abstract fun transactionDao(): TransactionDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()  // 开发阶段允许破坏性迁移
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

