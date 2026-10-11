package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.StationFuelEntity
import com.example.ui.screens.admin.AdminPanelScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.customer.CustomerHomeScreen
import com.example.ui.screens.customer.EmergencyScreen
import com.example.ui.screens.customer.FuelPurchaseScreen
import com.example.ui.screens.customer.OrderHistoryScreen
import com.example.ui.screens.customer.PaymentReceiptScreen
import com.example.ui.screens.customer.StationDetailsScreen
import com.example.ui.screens.notification.NotificationsScreen
import com.example.ui.screens.owner.OwnerDashboardScreen
import com.example.ui.screens.owner.SalesReportsScreen
import com.example.ui.screens.owner.StockManagementScreen
import com.example.ui.screens.owner.VerifyDispenseScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.theme.FuelAmber
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PetrolTeal
import com.example.ui.viewmodel.OilPumpViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OilPumpApp()
            }
        }
    }
}

sealed class Screen(val route: String) {
    object CustomerHome : Screen("customer_home")
    object StationDetails : Screen("station_details")
    object FuelPurchase : Screen("fuel_purchase")
    object PaymentReceipt : Screen("payment_receipt")
    object OrderHistory : Screen("order_history")
    object Emergency : Screen("emergency")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object OwnerDashboard : Screen("owner_dashboard")
    object OwnerVerify : Screen("owner_verify")
    object OwnerStock : Screen("owner_stock")
    object OwnerReports : Screen("owner_reports")
    object AdminPanel : Screen("admin_panel")
    object Auth : Screen("auth")
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val hasBadge: Boolean = false,
    val badgeCount: Int = 0
)

@Composable
fun OilPumpApp(viewModel: OilPumpViewModel = viewModel()) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val activeReceiptOrder by viewModel.activeReceiptOrder.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.CustomerHome) }
    var selectedFuelForPurchase by remember { mutableStateOf<StationFuelEntity?>(null) }
    var viewHistoryOrderReceipt by remember { mutableStateOf<OrderEntity?>(null) }

    val unreadNotifsCount = remember(notifications) { notifications.count { !it.isRead } }

    // Listen for toast events
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Role synchronization
    LaunchedEffect(currentUser?.role) {
        when (currentUser?.role?.lowercase()) {
            "owner" -> if (currentScreen == Screen.CustomerHome) currentScreen = Screen.OwnerDashboard
            "admin" -> if (currentScreen == Screen.CustomerHome) currentScreen = Screen.AdminPanel
            else -> if (currentScreen == Screen.OwnerDashboard || currentScreen == Screen.AdminPanel) currentScreen = Screen.CustomerHome
        }
    }

    // Back button handling
    BackHandler(enabled = currentScreen != Screen.CustomerHome && currentScreen != Screen.OwnerDashboard && currentScreen != Screen.AdminPanel) {
        when (currentUser?.role?.lowercase()) {
            "owner" -> currentScreen = Screen.OwnerDashboard
            "admin" -> currentScreen = Screen.AdminPanel
            else -> currentScreen = Screen.CustomerHome
        }
    }

    val isOwner = currentUser?.role?.lowercase() == "owner"
    val isAdmin = currentUser?.role?.lowercase() == "admin"

    // Dynamic Bottom Nav Items based on Role
    val navItems = when {
        isOwner -> listOf(
            BottomNavItem(Screen.OwnerDashboard.route, "Dashboard", Icons.Default.Dashboard),
            BottomNavItem(Screen.OwnerVerify.route, "Verify", Icons.Default.QrCodeScanner),
            BottomNavItem(Screen.OwnerStock.route, "Stock", Icons.Default.PriceChange),
            BottomNavItem(Screen.OwnerReports.route, "Reports", Icons.Default.Assessment),
            BottomNavItem(Screen.Notifications.route, "Alerts", Icons.Default.Notifications, unreadNotifsCount > 0, unreadNotifsCount),
            BottomNavItem(Screen.Profile.route, "Profile", Icons.Default.Person)
        )
        isAdmin -> listOf(
            BottomNavItem(Screen.AdminPanel.route, "Control", Icons.Default.AdminPanelSettings),
            BottomNavItem(Screen.CustomerHome.route, "Pumps", Icons.Default.LocalGasStation),
            BottomNavItem(Screen.Notifications.route, "Alerts", Icons.Default.Notifications, unreadNotifsCount > 0, unreadNotifsCount),
            BottomNavItem(Screen.Profile.route, "Profile", Icons.Default.Person)
        )
        else -> listOf(
            BottomNavItem(Screen.CustomerHome.route, "Pumps", Icons.Default.LocalGasStation),
            BottomNavItem(Screen.OrderHistory.route, "Orders", Icons.Default.Receipt),
            BottomNavItem(Screen.Emergency.route, "Emergency", Icons.Default.Emergency),
            BottomNavItem(Screen.Notifications.route, "Alerts", Icons.Default.Notifications, unreadNotifsCount > 0, unreadNotifsCount),
            BottomNavItem(Screen.Profile.route, "Profile", Icons.Default.Person)
        )
    }

    val showBottomBar = currentScreen !in listOf(
        Screen.StationDetails,
        Screen.FuelPurchase,
        Screen.PaymentReceipt,
        Screen.Auth
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = NavyDark,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentScreen.route == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentScreen = when (item.route) {
                                    Screen.CustomerHome.route -> Screen.CustomerHome
                                    Screen.OrderHistory.route -> Screen.OrderHistory
                                    Screen.Emergency.route -> Screen.Emergency
                                    Screen.Notifications.route -> Screen.Notifications
                                    Screen.Profile.route -> Screen.Profile
                                    Screen.OwnerDashboard.route -> Screen.OwnerDashboard
                                    Screen.OwnerVerify.route -> Screen.OwnerVerify
                                    Screen.OwnerStock.route -> Screen.OwnerStock
                                    Screen.OwnerReports.route -> Screen.OwnerReports
                                    Screen.AdminPanel.route -> Screen.AdminPanel
                                    else -> Screen.CustomerHome
                                }
                            },
                            icon = {
                                if (item.hasBadge) {
                                    BadgedBox(badge = { Badge { Text("${item.badgeCount}") } }) {
                                        Icon(item.icon, contentDescription = item.title)
                                    }
                                } else {
                                    Icon(item.icon, contentDescription = item.title)
                                }
                            },
                            label = { Text(item.title, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                indicatorColor = when {
                                    isOwner -> PetrolTeal
                                    isAdmin -> Color(0xFF5D4037)
                                    else -> NavyPrimary
                                }
                            )
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
            when (currentScreen) {
                Screen.CustomerHome -> CustomerHomeScreen(
                    viewModel = viewModel,
                    onSelectStation = { station ->
                        viewModel.selectStation(station)
                        currentScreen = Screen.StationDetails
                    },
                    onOpenEmergency = { currentScreen = Screen.Emergency }
                )

                Screen.StationDetails -> StationDetailsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.CustomerHome },
                    onNavigateToPurchase = { fuel ->
                        selectedFuelForPurchase = fuel
                        currentScreen = Screen.FuelPurchase
                    }
                )

                Screen.FuelPurchase -> FuelPurchaseScreen(
                    viewModel = viewModel,
                    preselectedFuel = selectedFuelForPurchase,
                    onBack = { currentScreen = Screen.StationDetails },
                    onPaymentSuccess = { order ->
                        viewHistoryOrderReceipt = order
                        currentScreen = Screen.PaymentReceipt
                    }
                )

                Screen.PaymentReceipt -> {
                    val orderToDisplay = viewHistoryOrderReceipt ?: activeReceiptOrder
                    if (orderToDisplay != null) {
                        PaymentReceiptScreen(
                            order = orderToDisplay,
                            onHome = {
                                viewModel.clearActiveReceipt()
                                currentScreen = if (isOwner) Screen.OwnerDashboard else Screen.CustomerHome
                            },
                            onViewHistory = {
                                viewModel.clearActiveReceipt()
                                currentScreen = Screen.OrderHistory
                            }
                        )
                    } else {
                        CustomerHomeScreen(
                            viewModel = viewModel,
                            onSelectStation = { currentScreen = Screen.StationDetails },
                            onOpenEmergency = { currentScreen = Screen.Emergency }
                        )
                    }
                }

                Screen.OrderHistory -> OrderHistoryScreen(
                    viewModel = viewModel,
                    onViewReceipt = { order ->
                        viewHistoryOrderReceipt = order
                        currentScreen = Screen.PaymentReceipt
                    }
                )

                Screen.Emergency -> EmergencyScreen(viewModel = viewModel)

                Screen.Notifications -> NotificationsScreen(viewModel = viewModel)

                Screen.Profile -> ProfileScreen(viewModel = viewModel)

                Screen.OwnerDashboard -> OwnerDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToVerify = { currentScreen = Screen.OwnerVerify },
                    onNavigateToStock = { currentScreen = Screen.OwnerStock },
                    onNavigateToReports = { currentScreen = Screen.OwnerReports }
                )

                Screen.OwnerVerify -> VerifyDispenseScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.OwnerDashboard }
                )

                Screen.OwnerStock -> StockManagementScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.OwnerDashboard }
                )

                Screen.OwnerReports -> SalesReportsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.OwnerDashboard }
                )

                Screen.AdminPanel -> AdminPanelScreen(viewModel = viewModel)

                Screen.Auth -> AuthScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        currentScreen = when (currentUser?.role?.lowercase()) {
                            "owner" -> Screen.OwnerDashboard
                            "admin" -> Screen.AdminPanel
                            else -> Screen.CustomerHome
                        }
                    }
                )
            }
        }
    }
}
