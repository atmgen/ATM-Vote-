package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.GoldStar
import com.example.ui.theme.MintGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.VotePackage
import com.example.ui.viewmodel.NotificationItem

@Composable
fun VoterDashboard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allActiveEvents by viewModel.allActiveEvents.collectAsState()
    val allBlogs by viewModel.allBlogs.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedContestant by viewModel.selectedContestant.collectAsState()

    val eventCategories by viewModel.eventCategories.collectAsState()
    val categoryContestants by viewModel.categoryContestants.collectAsState()

    var activeSubPage by remember { mutableStateOf("home") } // "home", "about", "pricing", "results", "blog", "faq", "account", "legal"
    val eventSearchText by viewModel.eventSearchQuery.collectAsState()
    val contestantSearchText by viewModel.contestantSearchQuery.collectAsState()

    // Private Passcode Modal State
    var passcodeToUnlockEvent by remember { mutableStateOf<EventEntity?>(null) }
    var enteredPasscode by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf("") }

    // Checkout Modal State
    var checkoutContestant by remember { mutableStateOf<ContestantEntity?>(null) }
    var selectedPackage by remember { mutableStateOf<VotePackage?>(null) }
    var showCaptchaDialog by remember { mutableStateOf(false) }
    var showPaymentModal by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("MTN_MOMO") }
    var finalReceiptTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Horizontal navigation strip for web-styled pages in-app
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScrollableRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WebNavButton(label = "Home", active = activeSubPage == "home", onClick = { activeSubPage = "home" })
                WebNavButton(label = "Pricing", active = activeSubPage == "pricing", onClick = { activeSubPage = "pricing" })
                WebNavButton(label = "Live Results", active = activeSubPage == "results", onClick = { activeSubPage = "results" })
                WebNavButton(label = "News", active = activeSubPage == "blog", onClick = { activeSubPage = "blog" })
                WebNavButton(label = "About", active = activeSubPage == "about", onClick = { activeSubPage = "about" })
                WebNavButton(label = "FAQ", active = activeSubPage == "faq", onClick = { activeSubPage = "faq" })
                WebNavButton(label = "My Account", active = activeSubPage == "account", onClick = { activeSubPage = "account" })
                WebNavButton(label = "Terms", active = activeSubPage == "legal", onClick = { activeSubPage = "legal" })
            }
        }

        // Selected Context Header or List
        if (selectedEvent != null) {
            EventDetailsHeading(
                event = selectedEvent!!,
                onBack = { viewModel.selectEvent(null) }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                selectedContestant != null -> {
                    ContestantProfileScreen(
                        contestant = selectedContestant!!,
                        isFavorite = favorites.contains(selectedContestant!!.id),
                        onToggleFavorite = { viewModel.toggleFavorite(selectedContestant!!.id) },
                        onBack = { viewModel.selectContestant(null) },
                        onVoteClick = { checkoutContestant = selectedContestant }
                    )
                }

                selectedCategory != null -> {
                    CategoryContestantsScreen(
                        category = selectedCategory!!,
                        contestants = categoryContestants,
                        favorites = favorites,
                        searchText = contestantSearchText,
                        onSearchChange = { viewModel.contestantSearchQuery.value = it },
                        onBack = { viewModel.selectCategory(null) },
                        onSelectContestant = { viewModel.selectContestant(it) },
                        onVoteQuick = { checkoutContestant = it }
                    )
                }

                selectedEvent != null -> {
                    EventCategoriesList(
                        categories = eventCategories,
                        onSelectCategory = { viewModel.selectCategory(it) }
                    )
                }

                else -> {
                    // Standard Website Marketing Pages
                    when (activeSubPage) {
                        "home" -> HomeScreen(
                            events = allActiveEvents,
                            searchText = eventSearchText,
                            onSearchChange = { viewModel.eventSearchQuery.value = it },
                            onSelectEvent = { ev ->
                                if (ev.type == "PRIVATE") {
                                    passcodeToUnlockEvent = ev
                                    enteredPasscode = ""
                                    passcodeError = ""
                                } else {
                                    viewModel.selectEvent(ev)
                                }
                            }
                        )
                        "pricing" -> PricingScreen(packages = viewModel.votePackages)
                        "results" -> LiveResultsScreen(events = allActiveEvents, viewModel = viewModel)
                        "blog" -> BlogListingScreen(blogs = allBlogs)
                        "about" -> AboutScreen()
                        "faq" -> FAQScreen()
                        "account" -> AccountScreen(
                            transactions = allTransactions,
                            notifications = notifications,
                            favoritesCount = favorites.size,
                            viewModel = viewModel
                        )
                        "legal" -> LegalScreen()
                    }
                }
            }
        }
    }

    // Passcode Validation for Private Events
    if (passcodeToUnlockEvent != null) {
        AlertDialog(
            onDismissRequest = { passcodeToUnlockEvent = null },
            title = { Text("Private Passcode Required", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        "This voting event is private and password-protected to ensure secure tenant compliance. Please enter the passcode to enter.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextField(
                        value = enteredPasscode,
                        onValueChange = { enteredPasscode = it },
                        label = { Text("Enter Passcode (Hint: 'ATM2026' or 'BVA2026')") },
                        modifier = Modifier.fillMaxWidth().testTag("passcode_field")
                    )
                    if (passcodeError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(passcodeError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val expected = passcodeToUnlockEvent!!.passcode.ifEmpty { "ATM2026" }
                        val expectedAlternative = "BVA2026"
                        if (enteredPasscode == expected || enteredPasscode == expectedAlternative) {
                            val target = passcodeToUnlockEvent!!
                            passcodeToUnlockEvent = null
                            viewModel.selectEvent(target)
                        } else {
                            passcodeError = "Incorrect passcode credentials. Please try again."
                        }
                    },
                    modifier = Modifier.testTag("submit_passcode_button")
                ) {
                    Text("Unlock & Open")
                }
            },
            dismissButton = {
                TextButton(onClick = { passcodeToUnlockEvent = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Checkout Panel Steps
    if (checkoutContestant != null) {
        VoteCheckoutDialog(
            contestant = checkoutContestant!!,
            packages = viewModel.votePackages,
            onDismiss = { checkoutContestant = null },
            onPackageSelect = { pack ->
                selectedPackage = pack
                showCaptchaDialog = true
            }
        )
    }

    if (showCaptchaDialog && selectedPackage != null) {
        VerificationCaptchaDialog(
            packageItem = selectedPackage!!,
            onDismiss = { showCaptchaDialog = false },
            onVerified = {
                showCaptchaDialog = false
                if (selectedPackage!!.priceGHS == 0.0) {
                    // Free Vote -> cast instantly
                    viewModel.castVote(
                        voterName = "Voter " + (10..99).random(),
                        voterEmail = "voter_atm@gmail.com",
                        voterPhone = "+233 24 000 0000",
                        promoCode = "",
                        packageItem = selectedPackage!!,
                        paymentMethod = "FREE_VERIFIED_OTP",
                        onResult = { success, msg, transaction ->
                            if (success) {
                                finalReceiptTransaction = transaction
                            }
                            checkoutContestant = null
                            selectedPackage = null
                        }
                    )
                } else {
                    // Paid vote -> proceed to Payment selection
                    showPaymentModal = true
                }
            }
        )
    }

    if (showPaymentModal && selectedPackage != null) {
        AlertDialog(
            onDismissRequest = { showPaymentModal = false },
            title = { Text("Select Payment Method", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Choose your preferred network or card gateway integrated on ATM VOTE:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val methods = listOf("MTN_MOMO", "TELECEL_CASH", "PAYSTACK", "STRIPE", "PAYPAL", "FLUTTERWAVE")
                    for (m in methods) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPaymentMethod = m }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(selected = selectedPaymentMethod == m, onClick = { selectedPaymentMethod = m })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(m.replace("_", " "), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentModal = false
                        // Launch Simulator
                    },
                    modifier = Modifier.testTag("payment_method_select_button")
                ) {
                    Text("Select & billing")
                }
            }
        )
        
        // This acts as step transition
        if (!showPaymentModal) {
            // Trigger actual simulated modal
            LaunchedEffect(Unit) {
                showPaymentModal = false
            }
        }
    }

    // Payment Simulator
    if (!showPaymentModal && selectedPackage != null && !showCaptchaDialog && checkoutContestant != null) {
        PaymentSimulationModal(
            packageItem = selectedPackage!!,
            paymentMethod = selectedPaymentMethod,
            viewModel = viewModel,
            onDismiss = {
                selectedPackage = null
                checkoutContestant = null
            },
            onPaymentSuccess = { promo ->
                viewModel.castVote(
                    voterName = viewModel.currentUser.value?.name ?: "Voter Guest",
                    voterEmail = viewModel.currentUser.value?.email ?: "voter@atmvote.com",
                    voterPhone = viewModel.currentUser.value?.phone ?: "+233 546 541 560",
                    promoCode = promo,
                    packageItem = selectedPackage!!,
                    paymentMethod = selectedPaymentMethod,
                    onResult = { success, _, transaction ->
                        if (success) {
                            finalReceiptTransaction = transaction
                        }
                        selectedPackage = null
                        checkoutContestant = null
                    }
                )
            }
        )
    }

    // Receipt viewer
    if (finalReceiptTransaction != null) {
        InteractiveReceiptCard(
            transaction = finalReceiptTransaction!!,
            votesCast = selectedPackage?.votes ?: 1,
            contestantNumber = checkoutContestant?.contestantNumber ?: "ATM001",
            onDismiss = { finalReceiptTransaction = null }
        )
    }
}

// Helpers
@Composable
fun ScrollableRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 4.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
fun WebNavButton(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EventDetailsHeading(event: EventEntity, onBack: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_events_voter_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Bono Region • Hosted by ATM Generations", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

// subpages
@Composable
fun HomeScreen(
    events: List<EventEntity>,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onSelectEvent: (EventEntity) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Visual Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                        contentDescription = "Hero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Vote Securely & Instantly",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Ghana's Multi-Tenant SaaS platform with real-time audit ledger.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Live Event Search Box
        item {
            TextField(
                value = searchText,
                onValueChange = onSearchChange,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                placeholder = { Text("Search active voting contests...") },
                modifier = Modifier.fillMaxWidth().testTag("event_search_field")
            )
        }

        item {
            Text("Active Voting Events", fontWeight = FontWeight.Black, fontSize = 15.sp)
        }

        val filtered = events.filter { it.title.contains(searchText, ignoreCase = true) }

        if (filtered.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No events match your criteria. Contact ATM Generations support at +233 546 541 560.",
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(filtered) { ev ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectEvent(ev) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        AsyncImage(
                            model = ev.bannerUrl.ifEmpty { "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800" },
                            contentDescription = "Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        )
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ev.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (ev.type == "PRIVATE") {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Lock, contentDescription = "Private", tint = GoldStar, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(ev.description, fontSize = 11.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            CountdownTimer(targetTimestamp = ev.countdownDate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PricingScreen(packages: List<VotePackage>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("ATM VOTE Flexible Pricing Plans", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Text("Complete transaction encryption. Pay via MTN Momo, Card, or PayPal.", fontSize = 11.sp, color = Color.Gray)
        }

        items(packages) { pack ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pack.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(pack.description, fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("+${pack.votes} Guaranteed Votes", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        text = if (pack.priceGHS == 0.0) "FREE" else "GHS ${String.format("%.2f", pack.priceGHS)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MintGreen
                    )
                }
            }
        }
    }
}

@Composable
fun LiveResultsScreen(events: List<EventEntity>, viewModel: MainViewModel) {
    var chosenEvent by remember { mutableStateOf<EventEntity?>(null) }
    var catsList by remember { mutableStateOf<List<CategoryEntity>>(emptyList()) }
    var focusedCat by remember { mutableStateOf<CategoryEntity?>(null) }
    var consList by remember { mutableStateOf<List<ContestantEntity>>(emptyList()) }

    // Coroutine triggers
    LaunchedEffect(chosenEvent) {
        if (chosenEvent != null) {
            catsList = viewModel.getCategoriesForEventSync(chosenEvent!!.id)
            focusedCat = null
            consList = emptyList()
        }
    }

    LaunchedEffect(focusedCat) {
        if (focusedCat != null) {
            consList = viewModel.getContestantsForCategorySync(focusedCat!!.id)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Live audit Rankings results", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Text("Real-time SQLite synchronized contestant charts.", fontSize = 11.sp, color = Color.Gray)
        }

        if (chosenEvent == null) {
            item { Text("Select Event to load logs:", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            items(events) { ev ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { chosenEvent = ev },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(ev.title, modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        } else {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.clickable { chosenEvent = null }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(chosenEvent!!.title, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }

            if (focusedCat == null) {
                item { Text("Select Category:", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                items(catsList) { cat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { focusedCat = cat },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(cat.name, modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            } else {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.clickable { focusedCat = null }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(focusedCat!!.name, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }

                if (consList.isEmpty()) {
                    item { Text("No results found.", fontSize = 11.sp, color = Color.Gray) }
                } else {
                    items(consList) { con ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(con.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Rank Position: #${con.rank}", fontSize = 10.sp, color = Color.Gray)
                                }
                                Text("${con.votesCount} votes", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlogListingScreen(blogs: List<BlogEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("ATM Newsroom & Tech Blog", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Text("Read updates on electronic voting safety, fintech, and Ghana regional updates.", fontSize = 11.sp, color = Color.Gray)
        }

        if (blogs.isEmpty()) {
            item {
                Text("No blog articles published yet.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            items(blogs) { blog ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        AsyncImage(
                            model = blog.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600" },
                            contentDescription = "Blog Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(blog.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row {
                                Text("By ${blog.author}", fontSize = 9.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(blog.content, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen() {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("About ATM VOTE", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Text("Enterprise-Grade Multi-Tenant SaaS voting platform", fontSize = 11.sp, color = Color.Gray)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Our Mission", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "ATM VOTE is engineered by ATM Generations Group to support fully secure, immutable online casting across organizations in West Africa. We provide clean dynamic branding, encrypted wallets for Mobile Money, and platform fee metrics for complete transparency.",
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Corporate Contact Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ContactRow(icon = Icons.Default.Email, label = "Email", value = "atmgenerations@gmail.com", onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:atmgenerations@gmail.com")
                        }
                        context.startActivity(intent)
                    })
                    ContactRow(icon = Icons.Default.Phone, label = "Phone", value = "+233 546 541 560", onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:+233546541560")
                        }
                        context.startActivity(intent)
                    })
                    ContactRow(icon = Icons.Default.LocationOn, label = "Location", value = "Sunyani, Bono Region, Ghana", onClick = {})
                }
            }
        }
    }
}

@Composable
fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("$label: ", fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
    }
}

@Composable
fun FAQScreen() {
    val faqs = listOf(
        Pair("How do I cast a paid vote?", "Click on any active event, browse categories, select your contestant, and click 'Cast Vote'. Choose GHS packages, complete Momo details, and submit verification codes. A formal printable receipt will generate on success."),
        Pair("What mobile networks are integrated?", "ATM VOTE integrates with MTN Mobile Money (MoMo), Telecel Cash, and AirtelTigo Money networks across Ghana, alongside Paystack and card processing."),
        Pair("What is the Platform Commission Rate?", "Platform commission rate is controlled globally by the Super Admin (default 10%). On every paid voting transaction, the percentage is automatically deducted and recorded inside transaction ledger.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Frequently Asked Questions", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary) }
        items(faqs) { faq ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(faq.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(faq.second, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AccountScreen(
    transactions: List<TransactionEntity>,
    notifications: List<NotificationItem>,
    favoritesCount: Int,
    viewModel: MainViewModel
) {
    val user by viewModel.currentUser.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("User Profile Panel", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(user?.name ?: "Voter Guest", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("Role: ${user?.role ?: "GUEST"} | Email: ${user?.email ?: "guest@atmvote.com"}", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Log Out Session")
                    }
                }
            }
        }

        item { Text("In-App Notification Center (${notifications.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        items(notifications) { notif ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(notif.message, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun LegalScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Terms of Service & Privacy Policy", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary) }
        item {
            Card {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("1. Data Privacy Policy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("At ATM VOTE, we encrypt voter transactional payloads and enforce rigorous multi-tenant database protection. No external scraper, duplicate Sybil accounts, or unauthorized audit logs can alter database logs.", fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("2. Refund Regulations", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("All ticket payments and package purchases on MTN Mobile Money or Credit Card are final. Transactions immediately deduct Super Admin platform commissions and distribute net profits to tenant organizations in real time.", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun EventCategoriesList(categories: List<CategoryEntity>, onSelectCategory: (CategoryEntity) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Available Voting Categories", fontWeight = FontWeight.Black, fontSize = 14.sp) }
        if (categories.isEmpty()) {
            item { Text("No categories uploaded for this event.", fontSize = 11.sp, color = Color.Gray) }
        } else {
            items(categories) { cat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCategory(cat) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(cat.description.ifEmpty { "Category details" }, fontSize = 11.sp, color = Color.Gray)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Open")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryContestantsScreen(
    category: CategoryEntity,
    contestants: List<ContestantEntity>,
    favorites: Set<String>,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    onSelectContestant: (ContestantEntity) -> Unit,
    onVoteQuick: (ContestantEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_category_voter_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(category.name, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }

        TextField(
            value = searchText,
            onValueChange = onSearchChange,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            placeholder = { Text("Search candidates...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).testTag("voter_candidate_search")
        )

        val filtered = contestants.filter { it.name.contains(searchText, ignoreCase = true) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { con ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectContestant(con) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = con.photoUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300" },
                            contentDescription = "Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(con.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                if (con.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = MintGreen, modifier = Modifier.size(13.dp))
                                }
                            }
                            Text("No: ${con.contestantNumber} | Rank: #${con.rank}", fontSize = 10.sp, color = Color.Gray)
                            Text("${con.votesCount} votes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Button(
                            onClick = { onVoteQuick(con) },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("vote_quick_button")
                        ) {
                            Text("VOTE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContestantProfileScreen(
    contestant: ContestantEntity,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    onVoteClick: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_profile_voter_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        AsyncImage(
                            model = contestant.photoUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300" },
                            contentDescription = "Candidate Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient Overlay for professional depth
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.75f)
                                        )
                                    )
                                )
                        )
                        
                        // Floating Badge
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 12.dp, topEnd = 0.dp, bottomStart = 0.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = " NO. ${contestant.contestantNumber} ",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(contestant.name, fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            if (contestant.isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = MintGreen, modifier = Modifier.size(18.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Current Ranking: #${contestant.rank} | ${contestant.votesCount} Votes",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onToggleFavorite,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("favorite_button")
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isFavorite) "My Favorite" else "Favorite", fontSize = 12.sp)
                            }

                            if (contestant.videoUrl.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contestant.videoUrl))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Video", tint = MaterialTheme.colorScheme.onSecondary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Watch Bio", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Biographical background", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(contestant.bio.ifEmpty { "No biography details supplied by organization admin." }, fontSize = 12.sp)
                }
            }
        }

        item {
            Button(
                onClick = onVoteClick,
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("vote_now_profile_button")
            ) {
                Icon(Icons.Default.HowToVote, contentDescription = "Vote")
                Spacer(modifier = Modifier.width(8.dp))
                Text("CAST VOTE SECURELY", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun VoteCheckoutDialog(
    contestant: ContestantEntity,
    packages: List<VotePackage>,
    onDismiss: () -> Unit,
    onPackageSelect: (VotePackage) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select secure Vote Package", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(packages) { pack ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPackageSelect(pack) }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pack.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(pack.description, fontSize = 10.sp, color = Color.Gray)
                                Text("+${pack.votes} Votes added", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                            }
                            Text(
                                if (pack.priceGHS == 0.0) "FREE" else "GHS ${String.format("%.2f", pack.priceGHS)}",
                                fontWeight = FontWeight.Bold,
                                color = MintGreen,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
