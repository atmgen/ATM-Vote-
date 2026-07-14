package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.screens.OrganizationDashboard
import com.example.ui.screens.SuperAdminDashboard
import com.example.ui.screens.VoterDashboard
import com.example.ui.theme.GoldStar
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentUser by viewModel.currentUser.collectAsState()
            val currentOrg by viewModel.currentOrganization.collectAsState()
            
            val primaryColorHex by viewModel.primaryColorHex.collectAsState()
            val secondaryColorHex by viewModel.secondaryColorHex.collectAsState()

            var isRoleMenuExpanded by remember { mutableStateOf(false) }

            MyApplicationTheme(
                primaryHex = primaryColorHex,
                secondaryHex = secondaryColorHex
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (currentOrg?.logoUrl?.isNotEmpty() == true) {
                                        AsyncImage(
                                            model = currentOrg?.logoUrl,
                                            contentDescription = "Logo",
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(
                                                            MaterialTheme.colorScheme.primary,
                                                            MaterialTheme.colorScheme.secondary
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "AV",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Column {
                                        Text(
                                            text = currentOrg?.name ?: "ATM VOTE",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = when (currentUser?.role) {
                                                "SUPER_ADMIN" -> "Platform Super Admin"
                                                "ORG_ADMIN" -> "Organization Owner"
                                                "STAFF" -> "Staff Personnel"
                                                "VOTER" -> "Verified Voter"
                                                else -> "Public Guest Account"
                                            },
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            },
                            actions = {
                                // Notification bell indicating live feed updates
                                Box(modifier = Modifier.padding(end = 12.dp)) {
                                    IconButton(onClick = {
                                        viewModel.addNotification(
                                            "System Connected",
                                            "Offline-First database active. Session running in Ghana UTC time.",
                                            "INFO"
                                        )
                                    }) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = "Alerts",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.border(0.5.dp, Color.Gray.copy(alpha = 0.15f))
                        )
                    },
                    floatingActionButton = {
                        // Quick switch FAB menu for reviewing the multi-tenant SaaS features
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                AnimatedVisibility(
                                    visible = isRoleMenuExpanded,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Card(
                                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.width(220.dp).border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                "SELECT DEMO SAAS ROLE",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                                            )
                                            
                                            RoleSelectorItem(
                                                label = "Super Admin Console",
                                                desc = "Global rules & commissions",
                                                icon = Icons.Default.VerifiedUser,
                                                color = GoldStar,
                                                onClick = {
                                                    viewModel.loginAsDemoRole("SUPER_ADMIN")
                                                    isRoleMenuExpanded = false
                                                }
                                            )
                                            
                                            RoleSelectorItem(
                                                label = "Organization Admin",
                                                desc = "Edit events & branding",
                                                icon = Icons.Default.Business,
                                                color = MaterialTheme.colorScheme.primary,
                                                onClick = {
                                                    viewModel.loginAsDemoRole("ORG_ADMIN")
                                                    isRoleMenuExpanded = false
                                                }
                                            )

                                            RoleSelectorItem(
                                                label = "Staff Panel",
                                                desc = "Manage candidates & voters",
                                                icon = Icons.Default.People,
                                                color = MaterialTheme.colorScheme.secondary,
                                                onClick = {
                                                    viewModel.loginAsDemoRole("STAFF")
                                                    isRoleMenuExpanded = false
                                                }
                                            )

                                            RoleSelectorItem(
                                                label = "Verified Voter",
                                                desc = "Ama Osei profile",
                                                icon = Icons.Default.HowToVote,
                                                color = MintGreen,
                                                onClick = {
                                                    viewModel.loginAsDemoRole("VOTER")
                                                    isRoleMenuExpanded = false
                                                }
                                            )

                                            RoleSelectorItem(
                                                label = "Public Guest",
                                                desc = "Browse marketing pages",
                                                icon = Icons.Default.Language,
                                                color = Color.Gray,
                                                onClick = {
                                                    viewModel.loginAsDemoRole("GUEST")
                                                    isRoleMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                FloatingActionButton(
                                    onClick = { isRoleMenuExpanded = !isRoleMenuExpanded },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White,
                                    modifier = Modifier.testTag("demo_fab_switcher")
                                ) {
                                    Icon(
                                        imageVector = if (isRoleMenuExpanded) Icons.Default.Close else Icons.Default.SwapHoriz,
                                        contentDescription = "Switch Roles"
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Routing of workspaces based on dynamic login sessions
                        when (currentUser?.role) {
                            "SUPER_ADMIN" -> {
                                SuperAdminDashboard(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            "ORG_ADMIN", "STAFF" -> {
                                OrganizationDashboard(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                VoterDashboard(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoleSelectorItem(
    label: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(desc, fontSize = 9.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
