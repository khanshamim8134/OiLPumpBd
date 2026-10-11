package com.example.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.StationFuelEntity
import com.example.ui.components.RoleSwitcherBanner
import com.example.ui.components.StockBadge
import com.example.ui.theme.FuelAmber
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PetrolTeal
import com.example.ui.theme.StockAvailableGreen
import com.example.ui.theme.StockEmptyRed
import com.example.ui.theme.StockLowAmber
import com.example.ui.viewmodel.OilPumpViewModel

@Composable
fun OwnerDashboardScreen(
    viewModel: OilPumpViewModel,
    onNavigateToVerify: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val stations by viewModel.approvedStations.collectAsState()
    val station = stations.firstOrNull() // Default to owner's primary station (Padma Oil)
    val fuels by viewModel.selectedStationFuels.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    // Calculate Today's Stats
    val todayOrders = allOrders.filter { it.stationId == (station?.stationId ?: 1) }
    val todayDispensed = todayOrders.filter { it.status == "dispensed" }
    val todaySalesAmount = todayDispensed.sumOf { it.amount }
    val pendingDispenseCount = todayOrders.count { it.status == "paid" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Role switch banner
        RoleSwitcherBanner(
            currentUser = currentUser,
            onSwitchRole = { viewModel.switchUserRole(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Station Overview Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PetrolTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = PetrolTeal)
                        }
                        Column {
                            Text(
                                text = station?.name ?: "Padma Oil Filling Station",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Station ID: #${station?.stationId ?: 1} • Lic: ${station?.licenceNo ?: "LIC-CP-9021/BPC"}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Quick Actions (Verify Dispense is #1 for pump attendant)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToVerify,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verify Dispense", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToStock,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.PriceChange, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stock & Prices", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Metrics Cards (Sales & Orders)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetricCard(
                        title = "Today's Dispensed",
                        value = "৳${String.format("%.0f", todaySalesAmount)}",
                        subtitle = "${todayDispensed.size} orders",
                        icon = Icons.Default.TrendingUp,
                        color = PetrolTeal,
                        modifier = Modifier.weight(1f)
                    )

                    DashboardMetricCard(
                        title = "Waiting Dispense",
                        value = "$pendingDispenseCount Orders",
                        subtitle = "Paid tokens ready",
                        icon = Icons.Default.PendingActions,
                        color = if (pendingDispenseCount > 0) FuelAmber else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Underground Tank Stock Gauges (FR9, FR10 in Thesis)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Underground Fuel Storage Tanks",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tap to update",
                        fontSize = 11.sp,
                        color = NavyPrimary,
                        modifier = Modifier.clickable { onNavigateToStock() }
                    )
                }
            }

            items(fuels) { fuel ->
                TankStockCard(fuel = fuel, onEdit = onNavigateToStock)
            }

            // Sales Report Button
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToReports() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = NavyPrimary)
                            Column {
                                Text("Sales Reports & Settlement", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Daily, weekly, and monthly fuel volume summaries", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = NavyPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = Color.Gray)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TankStockCard(fuel: StationFuelEntity, onEdit: () -> Unit) {
    val tankCapacity = 10000.0 // 10,000L standard tank capacity
    val fraction = (fuel.availableLitres / tankCapacity).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = fuel.fuelName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    StockBadge(status = fuel.stockStatus)
                }

                Text(
                    text = "৳${fuel.pricePerLitre}/L",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = NavyPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when (fuel.stockStatus) {
                    "AVAILABLE" -> StockAvailableGreen
                    "LOW_STOCK" -> StockLowAmber
                    else -> StockEmptyRed
                },
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "In Tank: ${String.format("%.0f", fuel.stockLitres)}L (Reserved: ${String.format("%.0f", fuel.reservedLitres)}L)",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Threshold: ${fuel.lowStockLimit.toInt()}L",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
