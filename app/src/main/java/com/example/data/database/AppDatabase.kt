package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        OrganizationEntity::class,
        EventEntity::class,
        CategoryEntity::class,
        ContestantEntity::class,
        VoterEntity::class,
        VoteEntity::class,
        TransactionEntity::class,
        BlogEntity::class,
        AnnouncementEntity::class,
        SystemConfigEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun organizationDao(): OrganizationDao
    abstract fun eventDao(): EventDao
    abstract fun categoryDao(): CategoryDao
    abstract fun contestantDao(): ContestantDao
    abstract fun voterDao(): VoterDao
    abstract fun voteDao(): VoteDao
    abstract fun transactionDao(): TransactionDao
    abstract fun blogDao(): BlogDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun systemConfigDao(): SystemConfigDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "atm_vote_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
