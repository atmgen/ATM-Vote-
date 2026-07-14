package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.GoldStar
import com.example.ui.theme.MintGreen
import com.example.ui.viewmodel.MainViewModel
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun OrganizationDashboard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentOrg by viewModel.currentOrganization.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val orgEvents by viewModel.orgEvents.collectAsState()
    val orgVoters by viewModel.orgVoters.collectAsState()
    val orgTransactions by viewModel.orgTransactions.collectAsState()
    val orgAnnouncements by viewModel.orgAnnouncements.collectAsState()

    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedContestant by viewModel.selectedContestant.collectAsState()

    val eventCategories by viewModel.eventCategories.collectAsState()
    val categoryContestants by viewModel.categoryContestants.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Brand Config, 2: Events & Contestants, 3: Voters, 4: Announcements & Blogs

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Headers
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            edgePadding = 8.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().testTag("org_tabs")
        ) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                Text("Overview", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                Text("Brand Config", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                Text("Events & Candidates", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 3, onClick = { activeTab = 3 }) {
                Text("Voters List", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 4, onClick = { activeTab = 4 }) {
                Text("News Feed", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Main Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                0 -> OrgOverviewTab(
                    organization = currentOrg,
                    transactions = orgTransactions,
                    eventsCount = orgEvents.size,
                    votersCount = orgVoters.size
                )
                1 -> BrandConfigTab(
                    organization = currentOrg,
                    onSave = { name, code, pri, sec, logo ->
                        viewModel.registerOrganization(name, code, pri, sec, currentUser?.email ?: "", currentUser?.name ?: "") {
                            // Reloads
                        }
                    }
                )
                2 -> EventsContestantsTab(
                    events = orgEvents,
                    selectedEvent = selectedEvent,
                    selectedCategory = selectedCategory,
                    selectedContestant = selectedContestant,
                    categories = eventCategories,
                    contestants = categoryContestants,
                    onSelectEvent = { viewModel.selectEvent(it) },
                    onSelectCategory = { viewModel.selectCategory(it) },
                    onSelectContestant = { viewModel.selectContestant(it) },
                    onSaveEvent = { title, desc, banner, type, pass, days, loc, con, email ->
                        viewModel.saveEvent(title, desc, banner, type, pass, days, loc, con, email)
                    },
                    onArchiveEvent = { viewModel.archiveEvent(it) },
                    onRestoreEvent = { viewModel.restoreEvent(it) },
                    onDeleteEvent = { viewModel.deleteEventPermanently(it) },
                    onSaveCategory = { name, desc, max -> viewModel.saveCategory(name, desc, max) },
                    onDeleteCategory = { viewModel.deleteCategoryPermanently(it) },
                    onSaveContestant = { name, bio, photo, video, number, verified ->
                        viewModel.saveContestant(name, bio, photo, video, number, verified)
                    },
                    onDeleteContestant = { viewModel.deleteContestantPermanently(it) }
                )
                3 -> VotersTab(
                    voters = orgVoters,
                    inviteCode = currentOrg?.inviteCode ?: "ATM2026",
                    onInviteVoter = { name, email, phone, code -> viewModel.inviteVoter(name, email, phone, code) },
                    onToggleStatus = { viewModel.toggleVoterStatus(it) }
                )
                4 -> NewsFeedTab(
                    announcements = orgAnnouncements,
                    onSaveAnnouncement = { title, content -> viewModel.saveAnnouncement(title, content) },
                    onDeleteAnnouncement = { viewModel.deleteAnnouncement(it) },
                    onSaveBlog = { title, content, author, url -> viewModel.saveBlog(title, content, author, url) }
                )
            }
        }
    }
}

// Sub-Tab 0: Overview & Exportable Analytics
@Composable
fun OrgOverviewTab(
    organization: OrganizationEntity?,
    transactions: List<TransactionEntity>,
    eventsCount: Int,
    votersCount: Int
) {
    val netRevenue = transactions.sumOf { it.netAmount }
    val commissionPaid = transactions.sumOf { it.commissionAmount }
    val grossSales = transactions.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "${organization?.name ?: "Organization"} Analytics Hub",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Track live financial ledgers, audit commission rate deductions, and download custom logs.",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Your Net Earnings",
                    value = "GHS ${String.format("%.2f", netRevenue)}",
                    desc = "Commission Excluded",
                    icon = Icons.Default.Wallet,
                    color = MintGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Commission Paid",
                    value = "GHS ${String.format("%.2f", commissionPaid)}",
                    desc = "${organization?.commissionRate ?: 10.0}% Platform share",
                    icon = Icons.Default.Percent,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Gross Ticket Revenue",
                    value = "GHS ${String.format("%.2f", grossSales)}",
                    desc = "Gross voter checkout value",
                    icon = Icons.Default.TrendingUp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Active Context",
                    value = "$eventsCount Events | $votersCount Voters",
                    desc = "Total tenant deployment",
                    icon = Icons.Default.Business,
                    color = GoldStar,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Interactive Export Module
        item {
            val columns = listOf("TransactionID", "VoterName", "PaidMethod", "GrossGHS", "CommissionGHS", "NetEarningsGHS", "Candidate", "Event")
            val rows = transactions.map { txn ->
                listOf(
                    txn.id.take(8).uppercase(),
                    txn.voterName,
                    txn.paymentMethod,
                    String.format("%.2f", txn.amount),
                    String.format("%.2f", txn.commissionAmount),
                    String.format("%.2f", txn.netAmount),
                    txn.contestantName,
                    txn.eventTitle
                )
            }

            MockFileExporter(
                title = "Financial ledger for ${organization?.name ?: "Company"}",
                columns = columns,
                rows = rows
            )
        }

        // Live Transactions ledger List
        item {
            Text("Recent Receipts log", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (transactions.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No voting transactions registered yet. Try voting in Voter section!",
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(transactions) { txn ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(txn.voterName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                "GHS ${String.format("%.2f", txn.netAmount)} Net",
                                fontWeight = FontWeight.Bold,
                                color = MintGreen,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${txn.paymentMethod} | to: ${txn.contestantName}", fontSize = 10.sp, color = Color.Gray)
                            Text("Fee rate: -GHS ${String.format("%.2f", txn.commissionAmount)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// Sub-Tab 1: Brand Configuration (SaaS Branding)
@Composable
fun BrandConfigTab(
    organization: OrganizationEntity?,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(organization?.name ?: "") }
    var code by remember { mutableStateOf(organization?.inviteCode ?: "") }
    var primaryColor by remember { mutableStateOf(organization?.primaryColor ?: "#D4AF37") }
    var secondaryColor by remember { mutableStateOf(organization?.secondaryColor ?: "#121212") }
    var logoUrl by remember { mutableStateOf(organization?.logoUrl ?: "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Customize SaaS Tenant Branding", fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text("Modify colors and logos dynamically. Brand styling applies instantly across the platform.", fontSize = 11.sp, color = Color.Gray)
        }

        item {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Organization Public Title") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            TextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Invite Code / Registration Key") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            TextField(
                value = primaryColor,
                onValueChange = { primaryColor = it },
                label = { Text("Primary Brand Color Hex") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            TextField(
                value = secondaryColor,
                onValueChange = { secondaryColor = it },
                label = { Text("Secondary Accent Color Hex") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            TextField(
                value = logoUrl,
                onValueChange = { logoUrl = it },
                label = { Text("Company Logo Web URL") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = { onSave(name, code, primaryColor, secondaryColor, logoUrl) },
                modifier = Modifier.fillMaxWidth().testTag("apply_branding_button")
            ) {
                Icon(Icons.Default.Palette, contentDescription = "Palette")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply Custom Brand Style")
            }
        }
    }
}

// Sub-Tab 2: Events & Candidates Configurator
@Composable
fun EventsContestantsTab(
    events: List<EventEntity>,
    selectedEvent: EventEntity?,
    selectedCategory: CategoryEntity?,
    selectedContestant: ContestantEntity?,
    categories: List<CategoryEntity>,
    contestants: List<ContestantEntity>,
    onSelectEvent: (EventEntity?) -> Unit,
    onSelectCategory: (CategoryEntity?) -> Unit,
    onSelectContestant: (ContestantEntity?) -> Unit,
    onSaveEvent: (String, String, String, String, String, Int, String, String, String) -> Unit,
    onArchiveEvent: (EventEntity) -> Unit,
    onRestoreEvent: (EventEntity) -> Unit,
    onDeleteEvent: (EventEntity) -> Unit,
    onSaveCategory: (String, String, Int) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onSaveContestant: (String, String, String, String, String, Boolean) -> Unit,
    onDeleteContestant: (ContestantEntity) -> Unit
) {
    // Dialog Triggers
    var showEventForm by remember { mutableStateOf(false) }
    var showCategoryForm by remember { mutableStateOf(false) }
    var showContestantForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dropdowns / Context Row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("1. Select Active Focus Event", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedEvent?.title ?: "No event focused",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Row {
                            Button(
                                onClick = { showEventForm = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("new_event_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                                Text("New Event", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        if (selectedEvent == null) {
            // List all tenant events
            items(events) { ev ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectEvent(ev) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (ev.status == "ARCHIVED") Color.Gray.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ev.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (ev.status == "ACTIVE") MintGreen else Color.Gray)
                                        .size(6.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(ev.status, fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.LockOpen, contentDescription = "Type", modifier = Modifier.size(10.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(ev.type, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Select")
                    }
                }
            }
        } else {
            // Event Details & CRUD Controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSelectEvent(null) },
                        modifier = Modifier.weight(1f).testTag("back_events_list_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        Text("List Events")
                    }

                    if (selectedEvent.status == "ACTIVE") {
                        Button(
                            onClick = { onArchiveEvent(selectedEvent) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f).testTag("archive_event_button")
                        ) {
                            Text("Archive")
                        }
                    } else {
                        Button(
                            onClick = { onRestoreEvent(selectedEvent) },
                            colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                            modifier = Modifier.weight(1f).testTag("restore_event_button")
                        ) {
                            Text("Restore")
                        }
                    }

                    Button(
                        onClick = { onDeleteEvent(selectedEvent) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).testTag("delete_event_button")
                    ) {
                        Text("Delete")
                    }
                }
            }

            // Categories Header
            item {
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("2. Categories", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Button(
                        onClick = { showCategoryForm = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("add_category_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                        Text("Add Category", fontSize = 10.sp)
                    }
                }
            }

            if (categories.isEmpty()) {
                item {
                    Text("No categories added yet. Add a category first.", fontSize = 11.sp, color = Color.Gray)
                }
            } else {
                items(categories) { cat ->
                    val isSelected = selectedCategory?.id == cat.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCategory(if (isSelected) null else cat) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { onDeleteCategory(cat) }
                                    )
                                }
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(cat.description.ifEmpty { "No description added." }, fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Max votes limit: ${cat.maxVotesPerPerson}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Contestants Management (Requires focused Category)
            if (selectedCategory != null) {
                item {
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("3. Contestants under '${selectedCategory.name}'", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Button(
                            onClick = { showContestantForm = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_contestant_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                            Text("New Candidate", fontSize = 10.sp)
                        }
                    }
                }

                if (contestants.isEmpty()) {
                    item {
                        Text("No contestants registered for this category.", fontSize = 11.sp, color = Color.Gray)
                    }
                } else {
                    items(contestants) { con ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(con.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        if (con.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = MintGreen, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Text("Number: ${con.contestantNumber} | Rank: #${con.rank}", fontSize = 10.sp, color = Color.Gray)
                                    Text("Live Votes: ${con.votesCount} votes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }

                                Row {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                onSelectContestant(con)
                                                showContestantForm = true
                                            }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { onDeleteContestant(con) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Forms
    if (showEventForm) {
        EventFormDialog(
            onDismiss = { showEventForm = false },
            onSave = { title, desc, banner, type, pass, days, loc, con, email ->
                onSaveEvent(title, desc, banner, type, pass, days, loc, con, email)
                showEventForm = false
            }
        )
    }

    if (showCategoryForm) {
        CategoryFormDialog(
            onDismiss = { showCategoryForm = false },
            onSave = { name, desc, max ->
                onSaveCategory(name, desc, max)
                showCategoryForm = false
            }
        )
    }

    if (showContestantForm) {
        ContestantFormDialog(
            contestant = selectedContestant,
            onDismiss = {
                onSelectContestant(null)
                showContestantForm = false
            },
            onSave = { name, bio, photo, video, number, verified ->
                onSaveContestant(name, bio, photo, video, number, verified)
                onSelectContestant(null)
                showContestantForm = false
            }
        )
    }
}

// Sub-Tab 3: Voters Management
@Composable
fun VotersTab(
    voters: List<VoterEntity>,
    inviteCode: String,
    onInviteVoter: (String, String, String, String) -> Unit,
    onToggleStatus: (VoterEntity) -> Unit
) {
    var showInviteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Secure Tenant Voter Registry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Your Organization Invite Code is '$inviteCode'. Members typing this during enrollment are registered as voters.",
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Voters directory", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Button(
                    onClick = { showInviteDialog = true },
                    modifier = Modifier.testTag("invite_voter_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Invite Voter")
                }
            }
        }

        if (voters.isEmpty()) {
            item {
                Text("No voters registered. Voters will appear here once they sign up or get added.", fontSize = 11.sp, color = Color.Gray)
            }
        } else {
            items(voters) { v ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(v.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(v.email, fontSize = 11.sp, color = Color.Gray)
                            if (v.phone.isNotEmpty()) {
                                Text("Phone: ${v.phone}", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Button(
                            onClick = { onToggleStatus(v) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (v.status == "ACTIVE") MintGreen else MaterialTheme.colorScheme.error
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(v.status, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        InviteVoterDialog(
            inviteCode = inviteCode,
            onDismiss = { showInviteDialog = false },
            onSave = { name, email, phone ->
                onInviteVoter(name, email, phone, inviteCode)
                showInviteDialog = false
            }
        )
    }
}

// Sub-Tab 4: Announcements & Blogs
@Composable
fun NewsFeedTab(
    announcements: List<AnnouncementEntity>,
    onSaveAnnouncement: (String, String) -> Unit,
    onDeleteAnnouncement: (AnnouncementEntity) -> Unit,
    onSaveBlog: (String, String, String, String) -> Unit
) {
    var showAnnDialog by remember { mutableStateOf(false) }
    var showBlogDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { showAnnDialog = true }, modifier = Modifier.weight(1f).padding(end = 4.dp).testTag("new_announcement_button")) {
                    Icon(Icons.Default.Announcement, contentDescription = "Ann")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Broadcast Alert")
                }
                Button(onClick = { showBlogDialog = true }, modifier = Modifier.weight(1f).padding(start = 4.dp).testTag("new_blog_button")) {
                    Icon(Icons.Default.Book, contentDescription = "Blog")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Write Blog")
                }
            }
        }

        item {
            Text("Broadcast alerts active", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (announcements.isEmpty()) {
            item {
                Text("No broadcasts published yet.", fontSize = 11.sp, color = Color.Gray)
            }
        } else {
            items(announcements) { ann ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ann.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onDeleteAnnouncement(ann) }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ann.content, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showAnnDialog) {
        AnnouncementDialog(
            onDismiss = { showAnnDialog = false },
            onSave = { title, content ->
                onSaveAnnouncement(title, content)
                showAnnDialog = false
            }
        )
    }

    if (showBlogDialog) {
        BlogDialog(
            onDismiss = { showBlogDialog = false },
            onSave = { title, content, author, url ->
                onSaveBlog(title, content, author, url)
                showBlogDialog = false
            }
        )
    }
}

// Dialog Forms Implementation
@Composable
fun EventFormDialog(onDismiss: () -> Unit, onSave: (String, String, String, String, String, Int, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var banner by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PUBLIC") } // PUBLIC, PRIVATE, PASSWORD_PROTECTED
    var passcode by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("15") }
    var location by remember { mutableStateOf("Sunyani, Ghana") }
    var contact by remember { mutableStateOf("+233 546 541 560") }
    var email by remember { mutableStateOf("atmgenerations@gmail.com") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Voting Event", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { TextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth().testTag("event_form_title")) }
                item { TextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth()) }
                item { TextField(value = banner, onValueChange = { banner = it }, label = { Text("Banner Image URL") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == "PUBLIC", onClick = { type = "PUBLIC" })
                        Text("Public", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = type == "PRIVATE", onClick = { type = "PRIVATE" })
                        Text("Private Code", fontSize = 12.sp)
                    }
                }
                if (type == "PRIVATE") {
                    item { TextField(value = passcode, onValueChange = { passcode = it }, label = { Text("Passcode Key") }, modifier = Modifier.fillMaxWidth()) }
                }
                item { TextField(value = days, onValueChange = { days = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Active Days Duration") }, modifier = Modifier.fillMaxWidth()) }
                item { TextField(value = location, onValueChange = { location = it }, label = { Text("Location Address") }, modifier = Modifier.fillMaxWidth()) }
                item { TextField(value = contact, onValueChange = { contact = it }, label = { Text("Support Contact Phone") }, modifier = Modifier.fillMaxWidth()) }
                item { TextField(value = email, onValueChange = { email = it }, label = { Text("Support Email") }, modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, desc, banner, type, passcode, days.toIntOrNull() ?: 15, location, contact, email) }, modifier = Modifier.testTag("event_form_submit")) {
                Text("Deploy Event")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CategoryFormDialog(onDismiss: () -> Unit, onSave: (String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var maxVotes by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Voting Category", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Category Name (e.g. Artiste)") }, modifier = Modifier.fillMaxWidth().testTag("cat_form_name"))
                TextField(value = desc, onValueChange = { desc = it }, label = { Text("Short Description") }, modifier = Modifier.fillMaxWidth())
                TextField(
                    value = maxVotes,
                    onValueChange = { maxVotes = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Max votes limit per voter") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, desc, maxVotes.toIntOrNull() ?: 1) }, modifier = Modifier.testTag("cat_form_submit")) {
                Text("Add Category")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ContestantFormDialog(contestant: ContestantEntity?, onDismiss: () -> Unit, onSave: (String, String, String, String, String, Boolean) -> Unit) {
    var name by remember { mutableStateOf(contestant?.name ?: "") }
    var bio by remember { mutableStateOf(contestant?.bio ?: "") }
    var photo by remember { mutableStateOf(contestant?.photoUrl ?: "") }
    var video by remember { mutableStateOf(contestant?.videoUrl ?: "") }
    var number by remember { mutableStateOf(contestant?.contestantNumber ?: "") }
    var verified by remember { mutableStateOf(contestant?.isVerified ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contestant != null) "Edit Contestant" else "Add New Contestant", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photo.isNotEmpty()) {
                            AsyncImage(
                                model = photo,
                                contentDescription = "Contestant Image Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Photo Space Indicator",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "CONTESTANT PHOTO SPACE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Enter a photo web URL below to view candidate preview",
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                item { TextField(value = name, onValueChange = { name = it }, label = { Text("Contestant Name") }, modifier = Modifier.fillMaxWidth().testTag("con_form_name")) }
                item { TextField(value = bio, onValueChange = { bio = it }, label = { Text("Biography") }, modifier = Modifier.fillMaxWidth()) }
                item { TextField(value = number, onValueChange = { number = it }, label = { Text("Contestant # (e.g. ATM001)") }, modifier = Modifier.fillMaxWidth()) }
                item { TextField(value = photo, onValueChange = { photo = it }, label = { Text("Photo Web URL") }, modifier = Modifier.fillMaxWidth()) }
                item { TextField(value = video, onValueChange = { video = it }, label = { Text("Bio Video Link (YouTube)") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = verified, onCheckedChange = { verified = it })
                        Text("Apply System Verified Badge", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, bio, photo, video, number, verified) }, modifier = Modifier.testTag("con_form_submit")) {
                Text("Save Candidate")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun InviteVoterDialog(inviteCode: String, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Voter Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Voter Full Name") }, modifier = Modifier.fillMaxWidth().testTag("invite_form_name"))
                TextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, email, phone) }, modifier = Modifier.testTag("invite_form_submit")) {
                Text("Save Voter")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AnnouncementDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Broadcast In-App Announcement", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = title, onValueChange = { title = it }, label = { Text("Announcement Title") }, modifier = Modifier.fillMaxWidth().testTag("ann_form_title"))
                TextField(value = content, onValueChange = { content = it }, label = { Text("Alert Body Content") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, content) }, modifier = Modifier.testTag("ann_form_submit")) {
                Text("Broadcast Now")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun BlogDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Tech / Event Blog", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { TextField(value = title, onValueChange = { title = it }, label = { Text("Article Title") }, modifier = Modifier.fillMaxWidth().testTag("blog_form_title")) }
                item { TextField(value = content, onValueChange = { content = it }, label = { Text("Content") }, modifier = Modifier.fillMaxWidth()) }
                item { TextField(value = author, onValueChange = { author = it }, label = { Text("Author Name") }, modifier = Modifier.fillMaxWidth()) }
                item { TextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Feature Image URL") }, modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, content, author, imageUrl) }, modifier = Modifier.testTag("blog_form_submit")) {
                Text("Publish Post")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
