package com.example.data.repository

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class AppRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)

    val userDao = db.userDao()
    val organizationDao = db.organizationDao()
    val eventDao = db.eventDao()
    val categoryDao = db.categoryDao()
    val contestantDao = db.contestantDao()
    val voterDao = db.voterDao()
    val voteDao = db.voteDao()
    val transactionDao = db.transactionDao()
    val blogDao = db.blogDao()
    val announcementDao = db.announcementDao()
    val systemConfigDao = db.systemConfigDao()
    val auditLogDao = db.auditLogDao()

    // Users
    fun getPlayersByOrg(orgId: String): Flow<List<UserEntity>> = userDao.getUsersByOrg(orgId)
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }
    suspend fun insertUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insert(user)
    }

    // Organizations
    fun getOrganizationById(id: String): Flow<OrganizationEntity?> = organizationDao.getOrganizationById(id)
    suspend fun getOrganizationByIdSync(id: String): OrganizationEntity? = withContext(Dispatchers.IO) {
        organizationDao.getOrganizationByIdSync(id)
    }
    fun getAllOrganizations(): Flow<List<OrganizationEntity>> = organizationDao.getAllOrganizations()
    suspend fun insertOrganization(org: OrganizationEntity) = withContext(Dispatchers.IO) {
        organizationDao.insert(org)
    }

    // Events
    fun getEventsByOrg(orgId: String): Flow<List<EventEntity>> = eventDao.getEventsByOrg(orgId)
    fun getAllActiveEvents(): Flow<List<EventEntity>> = eventDao.getAllActiveEvents()
    fun getEventById(id: String): Flow<EventEntity?> = eventDao.getEventById(id)
    suspend fun getEventByIdSync(id: String): EventEntity? = withContext(Dispatchers.IO) {
        eventDao.getEventByIdSync(id)
    }
    suspend fun insertEvent(event: EventEntity) = withContext(Dispatchers.IO) {
        eventDao.insert(event)
    }
    suspend fun deleteEvent(event: EventEntity) = withContext(Dispatchers.IO) {
        eventDao.delete(event)
    }

    // Categories
    fun getCategoriesForEvent(eventId: String): Flow<List<CategoryEntity>> = categoryDao.getCategoriesForEvent(eventId)
    suspend fun getCategoriesForEventSync(eventId: String): List<CategoryEntity> = withContext(Dispatchers.IO) {
        categoryDao.getCategoriesForEventSync(eventId)
    }
    suspend fun insertCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.insert(category)
    }
    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.delete(category)
    }
    suspend fun deleteCategoryById(id: String) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategoryById(id)
    }

    // Contestants
    fun getContestantsForCategory(categoryId: String): Flow<List<ContestantEntity>> = contestantDao.getContestantsForCategory(categoryId)
    suspend fun getContestantsForCategorySync(categoryId: String): List<ContestantEntity> = withContext(Dispatchers.IO) {
        contestantDao.getContestantsForCategorySync(categoryId)
    }
    suspend fun insertContestant(contestant: ContestantEntity) = withContext(Dispatchers.IO) {
        contestantDao.insert(contestant)
    }
    suspend fun deleteContestant(contestant: ContestantEntity) = withContext(Dispatchers.IO) {
        contestantDao.delete(contestant)
    }
    suspend fun getContestantById(id: String): ContestantEntity? = withContext(Dispatchers.IO) {
        contestantDao.getContestantById(id)
    }
    suspend fun incrementContestantVotes(id: String, count: Int) = withContext(Dispatchers.IO) {
        contestantDao.incrementContestantVotes(id, count)
    }

    // Voters
    fun getVotersByOrg(orgId: String): Flow<List<VoterEntity>> = voterDao.getVotersByOrg(orgId)
    suspend fun getVoterById(id: String): VoterEntity? = withContext(Dispatchers.IO) {
        voterDao.getVoterById(id)
    }
    suspend fun getVoterByEmail(orgId: String, email: String): VoterEntity? = withContext(Dispatchers.IO) {
        voterDao.getVoterByEmail(orgId, email)
    }
    suspend fun insertVoter(voter: VoterEntity) = withContext(Dispatchers.IO) {
        voterDao.insert(voter)
    }
    suspend fun deleteVoter(voter: VoterEntity) = withContext(Dispatchers.IO) {
        voterDao.delete(voter)
    }

    // Votes
    fun getVotesByContestant(contestantId: String): Flow<List<VoteEntity>> = voteDao.getVotesByContestant(contestantId)
    fun getVotesByVoter(voterId: String): Flow<List<VoteEntity>> = voteDao.getVotesByVoter(voterId)
    fun getVotesByEvent(eventId: String): Flow<List<VoteEntity>> = voteDao.getVotesByEvent(eventId)
    fun getAllVotesCount(): Flow<Int> = voteDao.getAllVotesCount()
    suspend fun recordVote(vote: VoteEntity) = withContext(Dispatchers.IO) {
        voteDao.insert(vote)
    }

    // Transactions
    fun getTransactionsByOrg(orgId: String): Flow<List<TransactionEntity>> = transactionDao.getTransactionsByOrg(orgId)
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    suspend fun recordTransaction(tx: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.insert(tx)
    }

    // Blogs
    fun getBlogsByOrg(orgId: String): Flow<List<BlogEntity>> = blogDao.getBlogsByOrg(orgId)
    fun getAllBlogs(): Flow<List<BlogEntity>> = blogDao.getAllBlogs()
    suspend fun insertBlog(blog: BlogEntity) = withContext(Dispatchers.IO) {
        blogDao.insert(blog)
    }
    suspend fun deleteBlog(blog: BlogEntity) = withContext(Dispatchers.IO) {
        blogDao.delete(blog)
    }

    // Announcements
    fun getAnnouncementsByOrg(orgId: String): Flow<List<AnnouncementEntity>> = announcementDao.getAnnouncementsByOrg(orgId)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity) = withContext(Dispatchers.IO) {
        announcementDao.insert(announcement)
    }
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity) = withContext(Dispatchers.IO) {
        announcementDao.delete(announcement)
    }

    // System Configs
    suspend fun getConfig(key: String): String? = withContext(Dispatchers.IO) {
        systemConfigDao.getConfig(key)?.value
    }
    suspend fun setConfig(key: String, value: String) = withContext(Dispatchers.IO) {
        systemConfigDao.insert(SystemConfigEntity(key, value))
    }

    // Audit Logs
    fun getAuditLogs(): Flow<List<AuditLogEntity>> = auditLogDao.getAuditLogs()
    suspend fun insertAuditLog(userEmail: String, action: String, details: String) = withContext(Dispatchers.IO) {
        auditLogDao.insert(AuditLogEntity(userEmail = userEmail, action = action, details = details))
    }

    // Seed Database
    suspend fun seedDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        val count = userDao.getUserByEmail("atmgenerations@gmail.com")
        if (count == null) {
            // Seed global configs
            systemConfigDao.insert(SystemConfigEntity("global_commission_rate", "10.0"))

            // Seed Super Admin
            val superAdmin = UserEntity(
                email = "atmgenerations@gmail.com",
                name = "Super Admin (ATM)",
                passwordHash = "password123",
                role = "SUPER_ADMIN"
            )
            userDao.insert(superAdmin)

            // Seed Organizations
            val orgId1 = "atm_org_1"
            val organization1 = OrganizationEntity(
                id = orgId1,
                name = "ATM Generations Group",
                logoUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=300",
                primaryColor = "#D4AF37", // Gold
                secondaryColor = "#121212", // Black
                inviteCode = "ATM2026",
                commissionRate = 12.0
            )
            organizationDao.insert(organization1)

            val orgId2 = "berekum_voters_assoc"
            val organization2 = OrganizationEntity(
                id = orgId2,
                name = "Berekum Voters Association",
                logoUrl = "https://images.unsplash.com/photo-1511578314322-379afb476865?w=300",
                primaryColor = "#D4AF37", // Gold
                secondaryColor = "#222222", // Charcoal Dark
                inviteCode = "BVA2026",
                commissionRate = 8.5
            )
            organizationDao.insert(organization2)

            // Seed Organization Admins
            val orgAdmin = UserEntity(
                email = "org@atmvote.com",
                name = "ATM Org Admin",
                passwordHash = "password123",
                role = "ORG_ADMIN",
                organizationId = orgId1
            )
            userDao.insert(orgAdmin)

            val staffUser = UserEntity(
                email = "staff@atmvote.com",
                name = "Kojo Mensah (Staff)",
                passwordHash = "password123",
                role = "STAFF",
                organizationId = orgId1
            )
            userDao.insert(staffUser)

            val voterUser = UserEntity(
                email = "voter@atmvote.com",
                name = "Ama Osei",
                passwordHash = "password123",
                role = "VOTER",
                organizationId = orgId1
            )
            userDao.insert(voterUser)

            // Seed Events
            val eventId1 = "bono_awards_2026"
            val event1 = EventEntity(
                id = eventId1,
                organizationId = orgId1,
                title = "Bono Region Choice Awards 2026",
                description = "ATM VOTE proudly hosts the annual Bono Region Choice Awards 2026. Celebrating excellence in leadership, music, entrepreneurship, and community development across the Bono Region. Join us in making history by casting your secure votes.",
                bannerUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800",
                type = "PUBLIC",
                status = "ACTIVE",
                countdownDate = System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000L, // 15 days from now
                startDate = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L, // Started 2 days ago
                endDate = System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000L,
                location = "Eusbett Hotel Conference Hall, Sunyani, Bono Region",
                contact = "+233 546 541 560",
                email = "atmgenerations@gmail.com"
            )
            eventDao.insert(event1)

            val eventId2 = "berekum_election"
            val event2 = EventEntity(
                id = eventId2,
                organizationId = orgId2,
                title = "BVA Executive Elections 2026",
                description = "Elections for the Executive Committee of the Berekum Voters Association. Private event for registered BVA members. Access requires an invitation code.",
                bannerUrl = "https://images.unsplash.com/photo-1540910419892-4a36d2c3266c?w=800",
                type = "PRIVATE",
                status = "ACTIVE",
                countdownDate = System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L,
                startDate = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000L,
                endDate = System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L,
                location = "Municipal Assembly Hall, Berekum, Bono Region",
                contact = "+233 546 541 560",
                email = "berekumvoters@gmail.com"
            )
            eventDao.insert(event2)

            // Seed Categories
            val catId1 = "cat_best_musician"
            val category1 = CategoryEntity(
                id = catId1,
                eventId = eventId1,
                name = "Best Artist of the Year",
                description = "Recognizing the musician who has made the most impact with original releases and stage presence in Bono Region.",
                maxVotesPerPerson = 9999
            )
            categoryDao.insert(category1)

            val catId2 = "cat_tech_innovator"
            val category2 = CategoryEntity(
                id = catId2,
                eventId = eventId1,
                name = "Enterprising Tech Innovator",
                description = "Celebrating outstanding local software creators, hardware engineers, and technology instructors.",
                maxVotesPerPerson = 5
            )
            categoryDao.insert(category2)

            val catId3 = "cat_bva_president"
            val category3 = CategoryEntity(
                id = catId3,
                eventId = eventId2,
                name = "Association President",
                description = "Select the executive leader of the BVA for the 2026/2027 calendar term.",
                maxVotesPerPerson = 1
            )
            categoryDao.insert(category3)

            // Seed Contestants
            val contestants = listOf(
                ContestantEntity(
                    id = "con_mus_1",
                    categoryId = catId1,
                    name = "Kofi Kinaata Jnr (Bono Star)",
                    bio = "Kofi Kinaata Jnr is an award-winning lyricist and singer originating from Sunyani. Combining highlife rhythms with conscious street poetry.",
                    photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                    videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    contestantNumber = "ATM001",
                    isVerified = true,
                    votesCount = 1450,
                    rank = 1
                ),
                ContestantEntity(
                    id = "con_mus_2",
                    categoryId = catId1,
                    name = "Gifty Sunyani (Sankofa)",
                    bio = "Gifty is a traditional highlife songstress who has performed globally, bringing Bono's rich musical cultural heritage to the world stage.",
                    photoUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300",
                    contestantNumber = "ATM002",
                    isVerified = true,
                    votesCount = 1220,
                    rank = 2
                ),
                ContestantEntity(
                    id = "con_mus_3",
                    categoryId = catId1,
                    name = "Bono Rhymer",
                    bio = "Rising Afro-fusion artist known for energetic flows and engaging live youth performances.",
                    photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
                    contestantNumber = "ATM003",
                    isVerified = false,
                    votesCount = 680,
                    rank = 3
                ),
                ContestantEntity(
                    id = "con_tech_1",
                    categoryId = catId2,
                    name = "Emmanuel Boateng (ATM Codes)",
                    bio = "Emmanuel is an independent web developer and digital skills coach based in Sunyani, training over 200 youths in modern coding languages.",
                    photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
                    contestantNumber = "ATM501",
                    isVerified = true,
                    votesCount = 42,
                    rank = 1
                ),
                ContestantEntity(
                    id = "con_tech_2",
                    categoryId = catId2,
                    name = "Yaa codingQueen",
                    bio = "Creator of local agritech platform linking Bono vegetable farmers with distributors in Kumasi and Accra.",
                    photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
                    contestantNumber = "ATM502",
                    isVerified = true,
                    votesCount = 38,
                    rank = 2
                ),
                ContestantEntity(
                    id = "con_bva_1",
                    categoryId = catId3,
                    name = "Charles Mensah",
                    bio = "Charles Mensah has worked as a civic organizer in Berekum for over a decade. Dedicated to transparent leadership and community representation.",
                    photoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300",
                    contestantNumber = "BVA01",
                    isVerified = true,
                    votesCount = 150,
                    rank = 1
                ),
                ContestantEntity(
                    id = "con_bva_2",
                    categoryId = catId3,
                    name = "Patricia Owusu",
                    bio = "Patricia Owusu is an educational director, promoting women's civic participation and local business support hubs.",
                    photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=300",
                    contestantNumber = "BVA02",
                    isVerified = true,
                    votesCount = 148,
                    rank = 2
                )
            )

            for (con in contestants) {
                contestantDao.insert(con)
            }

            // Seed Voters
            val votersList = listOf(
                VoterEntity(id = "vot_1", organizationId = orgId1, name = "Akosua Addae", email = "akosua@gmail.com", phone = "+233 241 123 456", inviteCode = "ATM2026", status = "ACTIVE"),
                VoterEntity(id = "vot_2", organizationId = orgId1, name = "Kwame Mensah", email = "kwame@gmail.com", phone = "+233 205 987 654", inviteCode = "ATM2026", status = "ACTIVE"),
                VoterEntity(id = "vot_3", organizationId = orgId1, name = "George Osei", email = "george@gmail.com", phone = "+233 543 456 789", inviteCode = "ATM2026", status = "RESTRICTED")
            )
            for (v in votersList) {
                voterDao.insert(v)
            }

            // Seed Votes
            val votesList = listOf(
                VoteEntity(id = "v_seed_1", eventId = eventId1, categoryId = catId1, contestantId = "con_mus_1", voterId = "vot_1", isPaid = true, amountPaid = 20.0, transactionId = "TXN_MOMO_9941a", timestamp = System.currentTimeMillis() - 3600000L),
                VoteEntity(id = "v_seed_2", eventId = eventId1, categoryId = catId1, contestantId = "con_mus_2", voterId = "vot_2", isPaid = true, amountPaid = 50.0, transactionId = "TXN_PAYSTACK_8820c", timestamp = System.currentTimeMillis() - 1800000L)
            )
            for (vt in votesList) {
                voteDao.insert(vt)
            }

            // Seed Transactions
            val transactionsList = listOf(
                TransactionEntity(
                    id = "txn_1",
                    organizationId = orgId1,
                    amount = 20.0,
                    commissionAmount = 2.4, // 12% commission
                    netAmount = 17.6,
                    voterName = "Akosua Addae",
                    paymentMethod = "MTN_MOMO",
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis() - 3600000L,
                    contestantName = "Kofi Kinaata Jnr (Bono Star)",
                    eventTitle = "Bono Region Choice Awards 2026"
                ),
                TransactionEntity(
                    id = "txn_2",
                    organizationId = orgId1,
                    amount = 50.0,
                    commissionAmount = 6.0, // 12% commission
                    netAmount = 44.0,
                    voterName = "Kwame Mensah",
                    paymentMethod = "PAYSTACK",
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis() - 1800000L,
                    contestantName = "Gifty Sunyani (Sankofa)",
                    eventTitle = "Bono Region Choice Awards 2026"
                )
            )
            for (txn in transactionsList) {
                transactionDao.insert(txn)
            }

            // Seed Announcements
            announcementDao.insert(AnnouncementEntity(
                organizationId = orgId1,
                title = "Live Voting Commenced!",
                content = "ATM VOTE has officially enabled MoMo and Card voting transactions for the Bono Region Choice Awards 2026! Every single paid transaction will be logged with instantaneous receipt generation and automatic commission payout.",
                timestamp = System.currentTimeMillis() - 86400000L
            ))

            // Seed Blogs
            blogDao.insert(BlogEntity(
                organizationId = orgId1,
                title = "Behind the Scenes of Bono Region Choice Awards",
                content = "This year's event represents a giant technological milestone in West Africa's voting systems. Implementing automated SMS confirmations, Mobile Money callbacks, and complete multi-tenant database protection on ATM VOTE ensures that transparency is kept at an all-time high.",
                author = "SaaS Editorial",
                imageUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600",
                timestamp = System.currentTimeMillis() - 2 * 86400000L
            ))

            // Seed Audit Log
            auditLogDao.insert(AuditLogEntity(
                userEmail = "system",
                action = "SYSTEM_INITIALIZATION",
                details = "Initialized database with sample multi-tenant SaaS organizations, active events, categories, and payment rates.",
                timestamp = System.currentTimeMillis()
            ))
        }
    }
}
