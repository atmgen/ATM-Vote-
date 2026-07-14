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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLogEntity
import com.example.data.model.OrganizationEntity
import com.example.ui.theme.GoldStar
import com.example.ui.theme.MintGreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SuperAdminDashboard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val globalCommission by viewModel.globalCommissionRate.collectAsState()
    val allOrganizations by viewModel.allOrganizations.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var showCreateOrgDialog by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(globalCommission.toFloat()) }

    // Synchronize slider state when VM value changes
    LaunchedEffect(globalCommission) {
        sliderValue = globalCommission.toFloat()
    }

    // Computing platform-wide metrics
    val totalPlatformRevenue = allTransactions.sumOf { it.commissionAmount }
    val totalGrossVolume = allTransactions.sumOf { it.amount }
    val totalVotesCount = allTransactions.size // 1 trans per vote package

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Platform Heading
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = "Admin", tint = GoldStar, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ATM VOTE - SaaS Control Room",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Configure platform rules, manage multi-tenant configurations, and monitor financial transactions.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Section 1: Financial and Activity Analytics
        item {
            Text("SaaS Global Metrics", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Platform Earnings",
                    value = "GHS ${String.format("%.2f", totalPlatformRevenue)}",
                    desc = "Accumulated Commissions",
                    icon = Icons.Default.MonetizationOn,
                    color = MintGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Gross Volume",
                    value = "GHS ${String.format("%.2f", totalGrossVolume)}",
                    desc = "All paid Transactions",
                    icon = Icons.Default.TrendingUp,
                    color = MaterialTheme.colorScheme.secondary,
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
                    title = "Total Active Tenants",
                    value = "${allOrganizations.size} Tenants",
                    desc = "Registered Companies",
                    icon = Icons.Default.Business,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "System Voting Batches",
                    value = "$totalVotesCount Batches",
                    desc = "Realtime cast records",
                    icon = Icons.Default.Ballot,
                    color = GoldStar,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section 2: Commission Rate Configuration (Requested CORE Feature)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Platform commission Fee rate", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Automatically deducted from every paid voting transaction", fontSize = 11.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${String.format("%.1f", sliderValue)}%",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..50f,
                        steps = 99,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("commission_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.updateGlobalCommissionRate(sliderValue.toDouble())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_commission_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply Commission Rate Change")
                    }
                }
            }
        }

        // Section 3: Registered SaaS Tenants List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tenant Organizations", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Button(
                    onClick = { showCreateOrgDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_tenant_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Tenant", fontSize = 11.sp)
                }
            }
        }

        if (allOrganizations.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No tenant organizations registered yet.",
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(allOrganizations) { org ->
                val orgTransactions = allTransactions.filter { it.organizationId == org.id }
                val orgVotes = orgTransactions.size
                val orgNetSales = orgTransactions.sumOf { it.netAmount }

                TenantCard(
                    organization = org,
                    votesCount = orgVotes,
                    netSales = orgNetSales,
                    onClick = {
                        // Switch session to this tenant to explore their admin layout
                        viewModel.login(
                            email = "org@atmvote.com",
                            orgInviteCode = org.inviteCode,
                            onResult = { _, _ -> }
                        )
                    }
                )
            }
        }

        // Section 4: System Audit Logs
        item {
            Text("System Audit trail", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Real-Time Security & Operation Logs", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(auditLogs) { log ->
                            AuditLogRow(log = log)
                        }
                    }
                }
            }
        }
    }

    if (showCreateOrgDialog) {
        CreateTenantDialog(
            onDismiss = { showCreateOrgDialog = false },
            onSave = { name, code, pri, sec, email, adminName ->
                viewModel.registerOrganization(name, code, pri, sec, email, adminName) {
                    showCreateOrgDialog = false
                }
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, maxLines = 1)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
        }
    }
}

@Composable
fun TenantCard(
    organization: OrganizationEntity,
    votesCount: Int,
    netSales: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(android.graphics.Color.parseColor(organization.primaryColor)))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = organization.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text("Code: ", fontSize = 11.sp, color = Color.Gray)
                    Text(organization.inviteCode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Commission: ", fontSize = 11.sp, color = Color.Gray)
                    Text("${organization.commissionRate}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("GHS ${String.format("%.2f", netSales)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                Text("$votesCount vote batches", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AuditLogRow(log: AuditLogEntity) {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.action,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = sdf.format(Date(log.timestamp)),
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "By: ${log.userEmail}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = log.details,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CreateTenantDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var primaryColor by remember { mutableStateOf("#D4AF37") }
    var secondaryColor by remember { mutableStateOf("#121212") }
    var adminEmail by remember { mutableStateOf("") }
    var adminName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Tenant Organization", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Organization Name (e.g., Sunyani Poly)") },
                        modifier = Modifier.fillMaxWidth().testTag("tenant_name_input")
                    )
                }
                item {
                    TextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Admin Invite/Registration Code") },
                        modifier = Modifier.fillMaxWidth().testTag("tenant_code_input")
                    )
                }
                item {
                    TextField(
                        value = primaryColor,
                        onValueChange = { primaryColor = it },
                        label = { Text("Primary Brand Hex (e.g. #D4AF37)") },
                        modifier = Modifier.fillMaxWidth().testTag("tenant_pri_input")
                    )
                }
                item {
                    TextField(
                        value = secondaryColor,
                        onValueChange = { secondaryColor = it },
                        label = { Text("Secondary Brand Hex (e.g. #121212)") },
                        modifier = Modifier.fillMaxWidth().testTag("tenant_sec_input")
                    )
                }
                item {
                    TextField(
                        value = adminName,
                        onValueChange = { adminName = it },
                        label = { Text("Admin Contact Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("tenant_adminname_input")
                    )
                }
                item {
                    TextField(
                        value = adminEmail,
                        onValueChange = { adminEmail = it },
                        label = { Text("Admin Login Email") },
                        modifier = Modifier.fillMaxWidth().testTag("tenant_adminemail_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, code, primaryColor, secondaryColor, adminEmail, adminName) },
                modifier = Modifier.testTag("tenant_dialog_submit")
            ) {
                Text("Register & Deploy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
