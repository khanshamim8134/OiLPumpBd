package com.example.ui.screens.customer

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.StationEntity
import com.example.ui.components.RoleSwitcherBanner
import com.example.ui.components.StockBadge
import com.example.ui.theme.FuelAmber
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PetrolTeal
import com.example.ui.viewmodel.OilPumpViewModel

@Composable
fun CustomerHomeScreen(
    viewModel: OilPumpViewModel,
    onSelectStation: (StationEntity) -> Unit,
    onOpenEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val stations by viewModel.approvedStations.collectAsState()
    val radiusKm by viewModel.searchRadiusKm.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isMapView by remember { mutableStateOf(false) }

    // User mock location: Chandpur Sadar (23.2300, 90.6600)
    val userLat = 23.2300
    val userLng = 90.6600

    // Filter stations by query and radius
    val filteredStations = remember(stations, searchQuery, radiusKm) {
        stations.filter { st ->
            val matchQuery = searchQuery.isEmpty() ||
                    st.name.contains(searchQuery, ignoreCase = true) ||
                    st.address.contains(searchQuery, ignoreCase = true)

            // Approximate distance using simple planar metric for responsive demo
            val dLat = (st.latitude - userLat) * 111.0
            val dLng = (st.longitude - userLng) * 102.0
            val dist = Math.sqrt(dLat * dLat + dLng * dLng)
            matchQuery && dist <= radiusKm
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Role Switcher for seamless faculty/evaluator inspection
        RoleSwitcherBanner(
            currentUser = currentUser,
            onSwitchRole = { viewModel.switchUserRole(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar & View Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search pump name or location...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = NavyPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_station_input")
            )

            // Map vs List toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isMapView) PetrolTeal else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isMapView = !isMapView }
            ) {
                IconButton(onClick = { isMapView = !isMapView }) {
                    Icon(
                        imageVector = if (isMapView) Icons.Default.ViewList else Icons.Default.Map,
                        contentDescription = "Toggle View",
                        tint = if (isMapView) Color.White else NavyPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Radius Filter Chips (FR3 in thesis report)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.NearMe, contentDescription = "Radius", tint = NavyPrimary, modifier = Modifier.size(16.dp))
            Text("Radius:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            listOf(5, 15, 30, 50).forEach { r ->
                val selected = radiusKm == r
                Surface(
                    color = if (selected) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.setSearchRadiusKm(r) }
                ) {
                    Text(
                        text = "${r}km",
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isMapView) {
            // Interactive Map Simulation with GPS Station Pins
            SimulatedMapView(
                stations = filteredStations,
                onSelectStation = onSelectStation,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            // List of nearby stations with live details
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredStations.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No stations found in ${radiusKm}km radius", fontWeight = FontWeight.Bold)
                                Text("Try increasing search radius or adjust search keywords.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(filteredStations) { station ->
                        StationCardItem(
                            station = station,
                            userLat = userLat,
                            userLng = userLng,
                            onClick = { onSelectStation(station) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StationCardItem(
    station: StationEntity,
    userLat: Double,
    userLng: Double,
    onClick: () -> Unit
) {
    val dLat = (station.latitude - userLat) * 111.0
    val dLng = (station.longitude - userLng) * 102.0
    val distanceKm = String.format("%.1f", Math.sqrt(dLat * dLat + dLng * dLng))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("station_card_${station.stationId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = NavyPrimary)
                    }
                    Column {
                        Text(
                            text = station.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Text(
                                text = "${station.address} • $distanceKm km away",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Rating Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FuelAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = FuelAmber, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("${station.rating}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFB26A00))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = station.openHours,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    StockBadge(status = "AVAILABLE")
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("View & Buy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun SimulatedMapView(
    stations: List<StationEntity>,
    onSelectStation: (StationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEF))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Simulated Map Grid with Road Lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw roads
                drawLine(Color(0xFFD0D7DE), Offset(0f, h * 0.35f), Offset(w, h * 0.35f), strokeWidth = 14f)
                drawLine(Color(0xFFD0D7DE), Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 10f)
                drawLine(Color(0xFFD0D7DE), Offset(w * 0.4f, 0f), Offset(w * 0.4f, h), strokeWidth = 12f)
                drawLine(Color(0xFFD0D7DE), Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), strokeWidth = 8f)

                // User GPS pulse marker (Center)
                drawCircle(Color(0x331976D2), radius = 32f, center = Offset(w * 0.4f, h * 0.5f))
                drawCircle(Color(0xFF1976D2), radius = 12f, center = Offset(w * 0.4f, h * 0.5f))
                drawCircle(Color.White, radius = 5f, center = Offset(w * 0.4f, h * 0.5f))
            }

            // Station Markers on the map
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                stations.take(3).forEachIndexed { index, station ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectStation(station) },
                        horizontalArrangement = if (index % 2 == 0) Arrangement.Start else Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NavyPrimary,
                            shadowElevation = 6.dp,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Column {
                                    Text(station.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("★ ${station.rating} • Tap to view", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Legend badge
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF1976D2)))
                    Text("Your Location (Chandpur)", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
