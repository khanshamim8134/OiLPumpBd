package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.StationFuelEntity
import com.example.data.local.entities.VehicleTypeEntity
import com.example.ui.components.StockBadge
import com.example.ui.theme.BkashPink
import com.example.ui.theme.CardBlue
import com.example.ui.theme.FuelAmber
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PetrolTeal
import com.example.ui.viewmodel.OilPumpViewModel

@Composable
fun FuelPurchaseScreen(
    viewModel: OilPumpViewModel,
    preselectedFuel: StationFuelEntity?,
    onBack: () -> Unit,
    onPaymentSuccess: (OrderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val station by viewModel.selectedStation.collectAsState()
    val fuels by viewModel.selectedStationFuels.collectAsState()
    val vehicleTypes by viewModel.vehicleTypes.collectAsState()

    var selectedVehicle by remember(vehicleTypes) {
        mutableStateOf(vehicleTypes.firstOrNull() ?: VehicleTypeEntity(1, "Motorcycle", 15.0))
    }

    var selectedFuel by remember(fuels, preselectedFuel) {
        mutableStateOf(preselectedFuel ?: fuels.firstOrNull())
    }

    var quantityInput by remember { mutableStateOf("5") }
    var selectedPaymentMethod by remember { mutableStateOf("bKash") }
    var isProcessing by remember { mutableStateOf(false) }

    val litres = quantityInput.toDoubleOrNull() ?: 0.0
    val unitPrice = selectedFuel?.pricePerLitre ?: 0.0
    val totalAmount = Math.round(litres * unitPrice * 100.0) / 100.0

    val isQuantityValid = litres > 0.0 &&
            litres <= (selectedFuel?.availableLitres ?: 0.0) &&
            litres <= selectedVehicle.maxLitresPerTxn

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Surface(color = NavyPrimary, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Fuel Checkout & Payment",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selected Station Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = NavyPrimary)
                        }
                        Column {
                            Text(station?.name ?: "Fuel Station", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(station?.address ?: "", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Step 1: Vehicle Selection (FR5)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. Select Vehicle Type",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(vehicleTypes) { vtype ->
                            val isSelected = selectedVehicle.vtypeId == vtype.vtypeId
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedVehicle = vtype }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = if (vtype.name.contains("Motorcycle")) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else NavyPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = vtype.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Max: ${vtype.maxLitresPerTxn.toInt()}L",
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Fuel Type Selection (FR4, FR5)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "2. Select Fuel Type",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        fuels.forEach { f ->
                            val isSelected = selectedFuel?.sfId == f.sfId
                            val isAvailable = f.stockStatus != "OUT_OF_STOCK"

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = isAvailable) { selectedFuel = f },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) NavyPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, NavyPrimary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, if (isSelected) NavyPrimary else Color.Gray, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(NavyPrimary)
                                                )
                                            }
                                        }

                                        Column {
                                            Text(f.fuelName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Stock: ${f.availableLitres.toInt()}L left", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        StockBadge(status = f.stockStatus)
                                        Text(
                                            text = "৳${f.pricePerLitre}/L",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = NavyPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 3: Quantity Input (FR5)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "3. Enter Quantity (Litres)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        label = { Text("Volume in Litres") },
                        trailingIcon = { Text("Litres", modifier = Modifier.padding(end = 12.dp), fontWeight = FontWeight.Bold, color = NavyPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fuel_quantity_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Quick presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("2", "5", "10", "20", "40").forEach { q ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { quantityInput = q }
                            ) {
                                Text(
                                    text = "${q}L",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (!isQuantityValid && litres > 0.0) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                            Text(
                                text = if (litres > (selectedFuel?.availableLitres ?: 0.0))
                                    "Exceeds available stock!"
                                else
                                    "Exceeds max limit (${selectedVehicle.maxLitresPerTxn.toInt()}L) for ${selectedVehicle.name}",
                                color = Color.Red,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Step 4: Digital Payment Method (FR6)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "4. Select Payment Gateway",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("bKash", BkashPink, "bKash"),
                            Triple("Nagad", NagadOrange, "Nagad"),
                            Triple("Card", CardBlue, "Cards / SSL")
                        ).forEach { (key, color, label) ->
                            val isSelected = selectedPaymentMethod == key
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) color else Color.LightGray
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedPaymentMethod = key }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payment,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = color
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Step 5: Price Breakdown & Safety Note
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fuel Rate:", fontSize = 12.sp, color = Color.Gray)
                            Text("৳$unitPrice / Litre", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Selected Volume:", fontSize = 12.sp, color = Color.Gray)
                            Text("$litres Litres", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Govt. Fuel Levy / VAT:", fontSize = 12.sp, color = Color.Gray)
                            Text("৳0.00 (Included)", fontSize = 12.sp, color = Color.Gray)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Payable Amount:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("৳$totalAmount", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NavyPrimary)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = FuelAmber, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Safety Note: Please switch off your vehicle engine at the pump. Your QR code is valid for 30 minutes after payment.",
                        fontSize = 11.sp,
                        color = Color(0xFF6D4C41)
                    )
                }
            }
        }

        // Bottom Confirmation & Pay Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (station != null && selectedFuel != null && isQuantityValid) {
                        isProcessing = true
                        viewModel.buyFuel(
                            station = station!!,
                            fuel = selectedFuel!!,
                            vtype = selectedVehicle,
                            litres = litres,
                            method = selectedPaymentMethod,
                            onSuccess = { order ->
                                isProcessing = false
                                onPaymentSuccess(order)
                            }
                        )
                    }
                },
                enabled = isQuantityValid && !isProcessing && selectedFuel != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedPaymentMethod) {
                        "bKash" -> BkashPink
                        "Nagad" -> NagadOrange
                        else -> NavyPrimary
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp)
                    .testTag("confirm_payment_button")
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connecting Payment Gateway...", fontSize = 14.sp)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay ৳$totalAmount with $selectedPaymentMethod", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
