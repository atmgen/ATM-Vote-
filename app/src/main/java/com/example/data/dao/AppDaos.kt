package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE organizationId = :orgId")
    fun getUsersByOrg(orgId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)
}

@Dao
interface OrganizationDao {
    @Query("SELECT * FROM organizations WHERE id = :id LIMIT 1")
    fun getOrganizationById(id: String): Flow<OrganizationEntity?>

    @Query("SELECT * FROM organizations WHERE id = :id LIMIT 1")
    suspend fun getOrganizationByIdSync(id: String): OrganizationEntity?

    @Query("SELECT * FROM organizations")
    fun getAllOrganizations(): Flow<List<OrganizationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(org: OrganizationEntity)

    @Delete
    suspend fun delete(org: OrganizationEntity)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE organizationId = :orgId ORDER BY startDate DESC")
    fun getEventsByOrg(orgId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE status = 'ACTIVE' ORDER BY startDate DESC")
    fun getAllActiveEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun getEventById(id: String): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventByIdSync(id: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE eventId = :eventId")
    fun getCategoriesForEvent(eventId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE eventId = :eventId")
    suspend fun getCategoriesForEventSync(eventId: String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)
}

@Dao
interface ContestantDao {
    @Query("SELECT * FROM contestants WHERE categoryId = :categoryId ORDER BY votesCount DESC, name ASC")
    fun getContestantsForCategory(categoryId: String): Flow<List<ContestantEntity>>

    @Query("SELECT * FROM contestants WHERE categoryId = :categoryId ORDER BY votesCount DESC, name ASC")
    suspend fun getContestantsForCategorySync(categoryId: String): List<ContestantEntity>

    @Query("SELECT * FROM contestants WHERE id = :id LIMIT 1")
    suspend fun getContestantById(id: String): ContestantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contestant: ContestantEntity)

    @Delete
    suspend fun delete(contestant: ContestantEntity)

    @Query("DELETE FROM contestants WHERE categoryId = :categoryId")
    suspend fun deleteContestantsByCategoryId(categoryId: String)

    @Query("UPDATE contestants SET votesCount = votesCount + :count WHERE id = :id")
    suspend fun incrementContestantVotes(id: String, count: Int)
}

@Dao
interface VoterDao {
    @Query("SELECT * FROM voters WHERE organizationId = :orgId")
    fun getVotersByOrg(orgId: String): Flow<List<VoterEntity>>

    @Query("SELECT * FROM voters WHERE id = :id LIMIT 1")
    suspend fun getVoterById(id: String): VoterEntity?

    @Query("SELECT * FROM voters WHERE organizationId = :orgId AND email = :email LIMIT 1")
    suspend fun getVoterByEmail(orgId: String, email: String): VoterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voter: VoterEntity)

    @Delete
    suspend fun delete(voter: VoterEntity)
}

@Dao
interface VoteDao {
    @Query("SELECT * FROM votes WHERE contestantId = :contestantId")
    fun getVotesByContestant(contestantId: String): Flow<List<VoteEntity>>

    @Query("SELECT * FROM votes WHERE voterId = :voterId")
    fun getVotesByVoter(voterId: String): Flow<List<VoteEntity>>

    @Query("SELECT * FROM votes WHERE eventId = :eventId")
    fun getVotesByEvent(eventId: String): Flow<List<VoteEntity>>

    @Query("SELECT COUNT(*) FROM votes")
    fun getAllVotesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vote: VoteEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE organizationId = :orgId ORDER BY timestamp DESC")
    fun getTransactionsByOrg(orgId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: TransactionEntity)
}

@Dao
interface BlogDao {
    @Query("SELECT * FROM blogs WHERE organizationId = :orgId ORDER BY timestamp DESC")
    fun getBlogsByOrg(orgId: String): Flow<List<BlogEntity>>

    @Query("SELECT * FROM blogs ORDER BY timestamp DESC")
    fun getAllBlogs(): Flow<List<BlogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blog: BlogEntity)

    @Delete
    suspend fun delete(blog: BlogEntity)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements WHERE organizationId = :orgId ORDER BY timestamp DESC")
    fun getAnnouncementsByOrg(orgId: String): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(announcement: AnnouncementEntity)

    @Delete
    suspend fun delete(announcement: AnnouncementEntity)
}

@Dao
interface SystemConfigDao {
    @Query("SELECT * FROM system_configs WHERE `key` = :key LIMIT 1")
    suspend fun getConfig(key: String): SystemConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: SystemConfigEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLogEntity)
}
