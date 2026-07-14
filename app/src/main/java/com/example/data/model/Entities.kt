package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val email: String,
    val name: String,
    val passwordHash: String,
    val role: String, // "SUPER_ADMIN", "ORG_ADMIN", "STAFF", "VOTER", "GUEST"
    val organizationId: String = "",
    val isTwoFactorEnabled: Boolean = false,
    val phone: String = "",
    val mfaSecret: String = ""
)

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val logoUrl: String = "",
    val primaryColor: String = "#0D6EFD", // Hex code
    val secondaryColor: String = "#198754", // Hex code
    val inviteCode: String = "",
    val commissionRate: Double = 10.0 // Default 10%
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val title: String,
    val description: String,
    val bannerUrl: String = "",
    val type: String = "PUBLIC", // "PUBLIC", "PRIVATE", "PASSWORD_PROTECTED", "INVITE_ONLY"
    val status: String = "ACTIVE", // "ACTIVE", "ARCHIVED"
    val passcode: String = "",
    val countdownDate: Long = 0L,
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val location: String = "Sunyani, Ghana",
    val contact: String = "+233 546 541 560",
    val email: String = "atmgenerations@gmail.com"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val name: String,
    val description: String = "",
    val maxVotesPerPerson: Int = 1
)

@Entity(tableName = "contestants")
data class ContestantEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val name: String,
    val bio: String = "",
    val photoUrl: String = "",
    val videoUrl: String = "",
    val contestantNumber: String,
    val isVerified: Boolean = false,
    val votesCount: Int = 0,
    val rank: Int = 1
)

@Entity(tableName = "voters")
data class VoterEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val inviteCode: String = "",
    val status: String = "ACTIVE" // "ACTIVE", "RESTRICTED"
)

@Entity(tableName = "votes")
data class VoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val categoryId: String,
    val contestantId: String,
    val voterId: String,
    val isPaid: Boolean = false,
    val amountPaid: Double = 0.0,
    val transactionId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val ipAddress: String = "127.0.0.1"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val amount: Double,
    val commissionAmount: Double,
    val netAmount: Double,
    val voterName: String,
    val paymentMethod: String, // "MTN_MOMO", "TELECEL_CASH", "AIRTELTIGO_MONEY", "PAYSTACK", "FLUTTERWAVE", "STRIPE", "PAYPAL"
    val status: String = "SUCCESS", // "SUCCESS", "PENDING", "FAILED"
    val timestamp: Long = System.currentTimeMillis(),
    val contestantName: String = "",
    val eventTitle: String = ""
)

@Entity(tableName = "blogs")
data class BlogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val title: String,
    val content: String,
    val author: String,
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_configs")
data class SystemConfigEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userEmail: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
