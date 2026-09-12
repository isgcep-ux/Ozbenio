package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CastSessionEntity::class,
        CustomStreamEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        RecentDeviceEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CastDatabase : RoomDatabase() {
    abstract fun castDao(): CastDao

    companion object {
        @Volatile
        private var INSTANCE: CastDatabase? = null

        fun getDatabase(context: Context): CastDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CastDatabase::class.java,
                    "smart_cast_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
