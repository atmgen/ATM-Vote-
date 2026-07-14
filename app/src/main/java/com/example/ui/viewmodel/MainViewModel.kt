package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.data.service.PaymentIntegrationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class VotePackage(
    val name: String,
    val votes: Int,
    val priceGHS: Double,
    val description: String
)

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "INFO" // "INFO", "SUCCESS", "ALERT"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)
    val paymentService = PaymentIntegrationService(repository)

    // Current Session State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentOrganization = MutableStateFlow<OrganizationEntity?>(null)
    val currentOrganization: StateFlow<OrganizationEntity?> = _currentOrganization.asStateFlow()

    // Global App Configuration
    private val _globalCommissionRate = MutableStateFlow(10.0)
    val globalCommissionRate: StateFlow<Double> = _globalCommissionRate.asStateFlow()

    // Dynamic UI Colors for multi-tenancy
    private val _primaryColorHex = MutableStateFlow("#D4AF37") // Default Gold
    val primaryColorHex: StateFlow<String> = _primaryColorHex.asStateFlow()

    private val _secondaryColorHex = MutableStateFlow("#121212") // Default Black
    val secondaryColorHex: StateFlow<String> = _secondaryColorHex.asStateFlow()

    // UI Active Elements
    private val _selectedEvent = MutableStateFlow<EventEntity?>(null)
    val selectedEvent: StateFlow<EventEntity?> = _selectedEvent.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryEntity?>(null)
    val selectedCategory: StateFlow<CategoryEntity?> = _selectedCategory.asStateFlow()

    private val _selectedContestant = MutableStateFlow<ContestantEntity?>(null)
    val selectedContestant: StateFlow<ContestantEntity?> = _selectedContestant.asStateFlow()

    // Local lists fed dynamically by Flows from the Repository
    val allOrganizations: StateFlow<List<OrganizationEntity>> = repository.getAllOrganizations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActiveEvents: StateFlow<List<EventEntity>> = repository.getAllActiveEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _orgEvents = MutableStateFlow<List<EventEntity>>(emptyList())
    val orgEvents: StateFlow<List<EventEntity>> = _orgEvents.asStateFlow()

    private val _orgVoters = MutableStateFlow<List<VoterEntity>>(emptyList())
    val orgVoters: StateFlow<List<VoterEntity>> = _orgVoters.asStateFlow()

    private val _orgTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val orgTransactions: StateFlow<List<TransactionEntity>> = _orgTransactions.asStateFlow()

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _eventCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val eventCategories: StateFlow<List<CategoryEntity>> = _eventCategories.asStateFlow()

    private val _categoryContestants = MutableStateFlow<List<ContestantEntity>>(emptyList())
    val categoryContestants: StateFlow<List<ContestantEntity>> = _categoryContestants.asStateFlow()

    val allBlogs: StateFlow<List<BlogEntity>> = repository.getAllBlogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _orgAnnouncements = MutableStateFlow<List<AnnouncementEntity>>(emptyList())
    val orgAnnouncements: StateFlow<List<AnnouncementEntity>> = _orgAnnouncements.asStateFlow()

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated Notifications Center
    private val _notifications = MutableStateFlow<List<NotificationItem>>(
        listOf(
            NotificationItem(title = "Welcome to ATM VOTE", message = "Ghana's premier secure multi-tenant SaaS voting platform is live. Toggle roles to test full capabilities.", type = "SUCCESS")
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Favorites
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    // Search & Filter
    val voterSearchQuery = MutableStateFlow("")
    val eventSearchQuery = MutableStateFlow("")
    val contestantSearchQuery = MutableStateFlow("")

    // Preset Voting Packages
    val votePackages = listOf(
        VotePackage("Free Trial", 1, 0.0, "Verify with OTP to submit 1 free vote"),
        VotePackage("Starter Pack", 10, 10.0, "Get 10 secure votes for GHS 1.00 per vote"),
        VotePackage("Bronze Bundle", 50, 45.0, "Get 50 votes (10% Discount) at GHS 0.90 per vote"),
        VotePackage("Silver Shield", 100, 80.0, "Get 100 votes (20% Discount) at GHS 0.80 per vote"),
        VotePackage("Gold Heavyweight", 500, 350.0, "Get 500 votes (30% Bulk Discount) at GHS 0.70 per vote"),
        VotePackage("Mega Custom Package", 1000, 600.0, "Get 1000 votes (40% Mega Discount) at GHS 0.60 per vote")
    )

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
            loadGlobalCommission()
            
            // Default Session: set to GUEST initially so they can browse freely!
            logout()
        }
    }

    private suspend fun loadGlobalCommission() {
        val commission = repository.getConfig("global_commission_rate")
        _globalCommissionRate.value = commission?.toDoubleOrNull() ?: 10.0
    }

    fun updateGlobalCommissionRate(newRate: Double) {
        viewModelScope.launch {
            repository.setConfig("global_commission_rate", newRate.toString())
            _globalCommissionRate.value = newRate
            repository.insertAuditLog(
                _currentUser.value?.email ?: "system",
                "UPDATE_COMMISSION",
                "Changed platform-wide commission rate to $newRate%"
            )
            addNotification(
                "System Settings Updated",
                "Global platform commission rate has been adjusted to $newRate%. Future transactions will automatically deduct this rate.",
                "SUCCESS"
            )
        }
    }

    // Auth Actions
    fun login(email: String, orgInviteCode: String = "", onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank()) {
                onResult(false, "Please enter an email address")
                return@launch
            }

            val existingUser = repository.getUserByEmail(email)
            if (existingUser != null) {
                // Login existing
                setSession(existingUser)
                onResult(true, "Successfully logged in as ${existingUser.name}")
            } else {
                // Register a new user based on context
                if (orgInviteCode.isNotBlank()) {
                    // Registering as an Organization Admin or Voter matching an invitation code
                    val matchOrg = allOrganizations.value.firstOrNull { it.inviteCode.equals(orgInviteCode, ignoreCase = true) }
                    if (matchOrg != null) {
                        val newUser = UserEntity(
                            email = email,
                            name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                            passwordHash = "password123",
                            role = "ORG_ADMIN",
                            organizationId = matchOrg.id
                        )
                        repository.insertUser(newUser)
                        setSession(newUser)
                        onResult(true, "Created Organization Admin account for ${matchOrg.name}")
                    } else {
                        onResult(false, "Invalid Organization invitation code.")
                    }
                } else {
                    // Registering as a standard Voter/Guest
                    val newUser = UserEntity(
                        email = email,
                        name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        passwordHash = "password123",
                        role = "VOTER"
                    )
                    repository.insertUser(newUser)
                    setSession(newUser)
                    onResult(true, "Created Voter account. Welcome to ATM VOTE!")
                }
            }
        }
    }

    fun loginAsDemoRole(role: String) {
        viewModelScope.launch {
            val email = when (role) {
                "SUPER_ADMIN" -> "atmgenerations@gmail.com"
                "ORG_ADMIN" -> "org@atmvote.com"
                "STAFF" -> "staff@atmvote.com"
                "VOTER" -> "voter@atmvote.com"
                else -> ""
            }

            if (email.isNotEmpty()) {
                val user = repository.getUserByEmail(email)
                if (user != null) {
                    setSession(user)
                }
            } else {
                logout() // GUEST role
            }
        }
    }

    fun setSession(user: UserEntity) {
        viewModelScope.launch {
            _currentUser.value = user
            repository.insertAuditLog(user.email, "USER_LOGIN", "Logged in with role: ${user.role}")
            
            if (user.organizationId.isNotEmpty()) {
                val org = repository.getOrganizationByIdSync(user.organizationId)
                _currentOrganization.value = org
                _primaryColorHex.value = org?.primaryColor ?: "#D4AF37"
                _secondaryColorHex.value = org?.secondaryColor ?: "#121212"
                
                // Load organization specific tables
                loadOrganizationSpecifics(user.organizationId)
            } else {
                _currentOrganization.value = null
                _primaryColorHex.value = "#D4AF37"
                _secondaryColorHex.value = "#121212"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                repository.insertAuditLog(user.email, "USER_LOGOUT", "User logged out.")
            }
            _currentUser.value = null
            _currentOrganization.value = null
            _primaryColorHex.value = "#D4AF37"
            _secondaryColorHex.value = "#121212"
            _orgEvents.value = emptyList()
            _orgVoters.value = emptyList()
            _orgTransactions.value = emptyList()
            _orgAnnouncements.value = emptyList()
        }
    }

    private fun loadOrganizationSpecifics(orgId: String) {
        viewModelScope.launch {
            repository.getEventsByOrg(orgId).collectLatest { _orgEvents.value = it }
        }
        viewModelScope.launch {
            repository.getVotersByOrg(orgId).collectLatest { _orgVoters.value = it }
        }
        viewModelScope.launch {
            repository.getTransactionsByOrg(orgId).collectLatest { _orgTransactions.value = it }
        }
        viewModelScope.launch {
            repository.getAnnouncementsByOrg(orgId).collectLatest { _orgAnnouncements.value = it }
        }
    }

    // Selection Methods
    fun selectEvent(event: EventEntity?) {
        _selectedEvent.value = event
        _selectedCategory.value = null
        _selectedContestant.value = null
        _eventCategories.value = emptyList()
        _categoryContestants.value = emptyList()

        if (event != null) {
            viewModelScope.launch {
                repository.getCategoriesForEvent(event.id).collectLatest {
                    _eventCategories.value = it
                }
            }
        }
    }

    fun selectCategory(category: CategoryEntity?) {
        _selectedCategory.value = category
        _selectedContestant.value = null
        _categoryContestants.value = emptyList()

        if (category != null) {
            viewModelScope.launch {
                repository.getContestantsForCategory(category.id).collectLatest {
                    _categoryContestants.value = it
                }
            }
        }
    }

    fun selectContestant(contestant: ContestantEntity?) {
        _selectedContestant.value = contestant
    }

    // SaaS Organization Creator
    fun registerOrganization(
        name: String,
        inviteCode: String,
        primaryColor: String,
        secondaryColor: String,
        adminEmail: String,
        adminName: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (name.isBlank() || inviteCode.isBlank() || adminEmail.isBlank() || adminName.isBlank()) {
                onResult(false)
                return@launch
            }

            // Create Organization
            val orgId = "org_${UUID.randomUUID().toString().take(6)}"
            val newOrg = OrganizationEntity(
                id = orgId,
                name = name,
                inviteCode = inviteCode,
                primaryColor = if (primaryColor.startsWith("#")) primaryColor else "#D4AF37",
                secondaryColor = if (secondaryColor.startsWith("#")) secondaryColor else "#121212",
                commissionRate = 10.0 // Default 10%
            )
            repository.insertOrganization(newOrg)

            // Create admin user
            val newAdmin = UserEntity(
                email = adminEmail,
                name = adminName,
                passwordHash = "password123",
                role = "ORG_ADMIN",
                organizationId = orgId
            )
            repository.insertUser(newAdmin)

            repository.insertAuditLog(adminEmail, "REGISTER_ORG", "Created tenant organization: $name ($inviteCode)")
            addNotification(
                "New Organization Registered",
                "Organization '$name' is now active with unique ID: $orgId. Invite Staff with code '$inviteCode'!",
                "SUCCESS"
            )
            
            // Set session automatically to test the newly created org!
            setSession(newAdmin)
            onResult(true)
        }
    }

    // Event Management
    fun saveEvent(
        title: String,
        description: String,
        bannerUrl: String,
        type: String,
        passcode: String,
        countdownDays: Int,
        location: String,
        contact: String,
        email: String
    ) {
        val user = _currentUser.value ?: return
        val orgId = user.organizationId.ifEmpty { "atm_org_1" }
        viewModelScope.launch {
            val eventId = _selectedEvent.value?.id ?: UUID.randomUUID().toString()
            val finalBanner = bannerUrl.ifBlank { "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800" }
            val newEvent = EventEntity(
                id = eventId,
                organizationId = orgId,
                title = title,
                description = description,
                bannerUrl = finalBanner,
                type = type,
                passcode = passcode,
                countdownDate = System.currentTimeMillis() + countdownDays * 24 * 60 * 60 * 1000L,
                startDate = System.currentTimeMillis(),
                endDate = System.currentTimeMillis() + countdownDays * 24 * 60 * 60 * 1000L,
                location = location.ifBlank { "Sunyani, Ghana" },
                contact = contact.ifBlank { "+233 546 541 560" },
                email = email.ifBlank { "atmgenerations@gmail.com" }
            )
            repository.insertEvent(newEvent)
            repository.insertAuditLog(user.email, "SAVE_EVENT", "Saved voting event: $title")
            addNotification("Event Configured", "Event '$title' is active and ready for contestants.", "SUCCESS")
            selectEvent(newEvent)
        }
    }

    fun archiveEvent(event: EventEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = event.copy(status = "ARCHIVED")
            repository.insertEvent(updated)
            repository.insertAuditLog(user.email, "ARCHIVE_EVENT", "Archived event: ${event.title}")
            addNotification("Event Archived", "'${event.title}' is archived and removed from public listings.", "INFO")
            if (_selectedEvent.value?.id == event.id) {
                selectEvent(updated)
            }
        }
    }

    fun restoreEvent(event: EventEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = event.copy(status = "ACTIVE")
            repository.insertEvent(updated)
            repository.insertAuditLog(user.email, "RESTORE_EVENT", "Restored event: ${event.title}")
            addNotification("Event Restored", "'${event.title}' is active once again.", "SUCCESS")
            if (_selectedEvent.value?.id == event.id) {
                selectEvent(updated)
            }
        }
    }

    fun deleteEventPermanently(event: EventEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteEvent(event)
            repository.insertAuditLog(user.email, "DELETE_EVENT", "Permanently deleted event: ${event.title}")
            addNotification("Event Deleted Permanently", "'${event.title}' was deleted from ATM VOTE database.", "ALERT")
            if (_selectedEvent.value?.id == event.id) {
                selectEvent(null)
            }
        }
    }

    // Category Management
    fun saveCategory(name: String, description: String, maxVotes: Int) {
        val event = _selectedEvent.value ?: return
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val catId = _selectedCategory.value?.id ?: UUID.randomUUID().toString()
            val newCat = CategoryEntity(
                id = catId,
                eventId = event.id,
                name = name,
                description = description,
                maxVotesPerPerson = maxVotes
            )
            repository.insertCategory(newCat)
            repository.insertAuditLog(user.email, "SAVE_CATEGORY", "Saved category '$name' for event '${event.title}'")
            addNotification("Category Saved", "Category '$name' is configured successfully.", "SUCCESS")
            
            // Refresh categories list
            selectEvent(event)
            selectCategory(newCat)
        }
    }

    fun deleteCategoryPermanently(category: CategoryEntity) {
        val user = _currentUser.value ?: return
        val event = _selectedEvent.value ?: return
        viewModelScope.launch {
            repository.deleteCategory(category)
            repository.insertAuditLog(user.email, "DELETE_CATEGORY", "Deleted category: ${category.name}")
            addNotification("Category Deleted", "'${category.name}' has been deleted.", "ALERT")
            selectEvent(event)
            selectCategory(null)
        }
    }

    // Contestant Management
    fun saveContestant(name: String, bio: String, photoUrl: String, videoUrl: String, number: String, isVerified: Boolean) {
        val category = _selectedCategory.value ?: return
        val user = _currentUser.value ?: return
        val event = _selectedEvent.value ?: return
        viewModelScope.launch {
            val conId = _selectedContestant.value?.id ?: UUID.randomUUID().toString()
            val finalPhoto = photoUrl.ifBlank { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300" }
            val newContestant = ContestantEntity(
                id = conId,
                categoryId = category.id,
                name = name,
                bio = bio,
                photoUrl = finalPhoto,
                videoUrl = videoUrl,
                contestantNumber = number.ifBlank { "ATM${(100..999).random()}" },
                isVerified = isVerified,
                votesCount = _selectedContestant.value?.votesCount ?: 0,
                rank = _selectedContestant.value?.rank ?: 1
            )
            repository.insertContestant(newContestant)
            repository.insertAuditLog(user.email, "SAVE_CONTESTANT", "Saved contestant: $name in category ${category.name}")
            addNotification("Contestant Configured", "Contestant '$name' is registered under number: ${newContestant.contestantNumber}", "SUCCESS")
            
            // Refresh contestants list
            selectCategory(category)
            selectContestant(newContestant)
        }
    }

    fun deleteContestantPermanently(contestant: ContestantEntity) {
        val user = _currentUser.value ?: return
        val category = _selectedCategory.value ?: return
        viewModelScope.launch {
            repository.deleteContestant(contestant)
            repository.insertAuditLog(user.email, "DELETE_CONTESTANT", "Permanently deleted contestant: ${contestant.name}")
            addNotification("Contestant Deleted", "'${contestant.name}' removed from category.", "ALERT")
            selectCategory(category)
            selectContestant(null)
        }
    }

    // Voter / Guest Actions
    fun toggleFavorite(contestantId: String) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(contestantId)) {
            current.remove(contestantId)
        } else {
            current.add(contestantId)
        }
        _favorites.value = current
    }

    // Voter Management (Admin panel)
    fun inviteVoter(name: String, email: String, phone: String, code: String) {
        val user = _currentUser.value ?: return
        val orgId = user.organizationId.ifEmpty { "atm_org_1" }
        viewModelScope.launch {
            val newVoter = VoterEntity(
                organizationId = orgId,
                name = name,
                email = email,
                phone = phone,
                inviteCode = code,
                status = "ACTIVE"
            )
            repository.insertVoter(newVoter)
            repository.insertAuditLog(user.email, "INVITE_VOTER", "Invited voter: $name ($email)")
            addNotification("Voter Added", "Voter $name has been registered for organization's events.", "SUCCESS")
            loadOrganizationSpecifics(orgId)
        }
    }

    fun toggleVoterStatus(voter: VoterEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val newStatus = if (voter.status == "ACTIVE") "RESTRICTED" else "ACTIVE"
            val updated = voter.copy(status = newStatus)
            repository.insertVoter(updated)
            repository.insertAuditLog(user.email, "TOGGLE_VOTER_STATUS", "Changed voter ${voter.name} status to $newStatus")
            addNotification("Voter Restricted", "Voter status updated successfully to $newStatus.", "ALERT")
            loadOrganizationSpecifics(voter.organizationId)
        }
    }

    // Cast Secure Vote (Free/Paid with commission dynamic deduction)
    fun castVote(
        voterName: String,
        voterEmail: String,
        voterPhone: String,
        promoCode: String,
        packageItem: VotePackage,
        paymentMethod: String, // MOMO, Paystack, etc.
        onResult: (Boolean, String, TransactionEntity?) -> Unit
    ) {
        val contestant = _selectedContestant.value
        val category = _selectedCategory.value
        val event = _selectedEvent.value
        
        if (contestant == null || category == null || event == null) {
            onResult(false, "Voter context is incomplete. Please select an event and contestant first.", null)
            return
        }

        if (voterName.isBlank() || voterEmail.isBlank()) {
            onResult(false, "Please complete your Name and Email address.", null)
            return
        }

        viewModelScope.launch {
            // Check if voter is restricted inside the database for this organization
            val databaseVoter = repository.getVoterByEmail(event.organizationId, voterEmail)
            if (databaseVoter != null && databaseVoter.status == "RESTRICTED") {
                onResult(false, "Your email address has been flagged and restricted by the organization.", null)
                return@launch
            }

            // Apply Promo Code
            var price = packageItem.priceGHS
            if (promoCode.isNotBlank() && promoCode.uppercase() == "ATM10") {
                price *= 0.90 // 10% off
            }

            // 1. Double check organization settings for Commission Deduction
            val org = repository.getOrganizationByIdSync(event.organizationId)
            val orgCommissionPercentage = org?.commissionRate ?: _globalCommissionRate.value

            val commission = (price * orgCommissionPercentage) / 100.0
            val netEarnings = price - commission

            // 2. Log Vote transaction in DB
            val txId = "TXN_${paymentMethod}_" + UUID.randomUUID().toString().take(8).uppercase()
            val voteVoterId = databaseVoter?.id ?: "guest_voter_" + UUID.randomUUID().toString().take(6)
            
            // Record Vote rows (one per count)
            val isPaid = price > 0.0
            val voteRecord = VoteEntity(
                eventId = event.id,
                categoryId = category.id,
                contestantId = contestant.id,
                voterId = voteVoterId,
                isPaid = isPaid,
                amountPaid = price,
                transactionId = txId,
                timestamp = System.currentTimeMillis()
            )
            repository.recordVote(voteRecord)

            // Update contestant tally
            repository.incrementContestantVotes(contestant.id, packageItem.votes)

            // Record Financial transaction
            val transaction = TransactionEntity(
                id = UUID.randomUUID().toString(),
                organizationId = event.organizationId,
                amount = price,
                commissionAmount = commission,
                netAmount = netEarnings,
                voterName = voterName,
                paymentMethod = paymentMethod,
                status = "SUCCESS",
                timestamp = System.currentTimeMillis(),
                contestantName = contestant.name,
                eventTitle = event.title
            )
            repository.recordTransaction(transaction)

            // Re-fetch category contestants to refresh rankings
            repository.getContestantsForCategorySync(category.id).forEachIndexed { index, item ->
                repository.insertContestant(item.copy(rank = index + 1))
            }

            // Record Audit Log
            repository.insertAuditLog(voterEmail, "CAST_VOTE", "Voted ${packageItem.votes} times for ${contestant.name} via $paymentMethod. Amount: GHS $price. Platform Commission: GHS $commission")

            addNotification(
                "Vote Cast Confirmed",
                "Successfully added ${packageItem.votes} votes to '${contestant.name}'. Secure Receipt generated under TX ID: $txId.",
                "SUCCESS"
            )

            // Trigger actual update
            selectCategory(category)
            selectContestant(repository.getContestantById(contestant.id))

            onResult(true, "Thank you! Your votes have been counted successfully.", transaction)
        }
    }

    // Blog Management
    fun saveBlog(title: String, content: String, author: String, imageUrl: String) {
        val user = _currentUser.value ?: return
        val orgId = user.organizationId.ifEmpty { "atm_org_1" }
        viewModelScope.launch {
            val blog = BlogEntity(
                organizationId = orgId,
                title = title,
                content = content,
                author = author.ifBlank { "ATM Editorial" },
                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600" },
                timestamp = System.currentTimeMillis()
            )
            repository.insertBlog(blog)
            repository.insertAuditLog(user.email, "SAVE_BLOG", "Published new blog post: $title")
            addNotification("Blog Published", "Post '$title' is published on active events web board.", "SUCCESS")
        }
    }

    // Announcement Management
    fun saveAnnouncement(title: String, content: String) {
        val user = _currentUser.value ?: return
        val orgId = user.organizationId.ifEmpty { "atm_org_1" }
        viewModelScope.launch {
            val announcement = AnnouncementEntity(
                organizationId = orgId,
                title = title,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            repository.insertAnnouncement(announcement)
            repository.insertAuditLog(user.email, "SAVE_ANNOUNCEMENT", "Published announcement: $title")
            addNotification("Announcement Broadcast", "Alert '$title' broadcast to all logged in Voters.", "SUCCESS")
            loadOrganizationSpecifics(orgId)
        }
    }

    fun deleteAnnouncement(announcement: AnnouncementEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteAnnouncement(announcement)
            repository.insertAuditLog(user.email, "DELETE_ANNOUNCEMENT", "Deleted announcement: ${announcement.title}")
            addNotification("Announcement Removed", "Broadcast removed.", "INFO")
            loadOrganizationSpecifics(announcement.organizationId)
        }
    }

    fun addNotification(title: String, message: String, type: String = "INFO") {
        val newItem = NotificationItem(title = title, message = message, type = type)
        _notifications.value = listOf(newItem) + _notifications.value
    }

    suspend fun getCategoriesForEventSync(eventId: String): List<CategoryEntity> {
        return repository.getCategoriesForEventSync(eventId)
    }

    suspend fun getContestantsForCategorySync(categoryId: String): List<ContestantEntity> {
        return repository.getContestantsForCategorySync(categoryId)
    }

    // Payment split calculation helpers
    suspend fun calculateSplit(amount: Double, organizationId: String): PaymentIntegrationService.PaymentSplitResult {
        return paymentService.calculateSplit(amount, organizationId)
    }

    suspend fun initializePaystackTransaction(
        amount: Double,
        email: String,
        organizationId: String
    ): PaymentIntegrationService.PaystackResponse {
        return paymentService.initializePaystackTransaction(amount, email, organizationId)
    }

    suspend fun initializeFlutterwaveTransaction(
        amount: Double,
        email: String,
        name: String,
        phone: String,
        organizationId: String
    ): PaymentIntegrationService.FlutterwaveResponse {
        return paymentService.initializeFlutterwaveTransaction(amount, email, name, phone, organizationId)
    }
}
