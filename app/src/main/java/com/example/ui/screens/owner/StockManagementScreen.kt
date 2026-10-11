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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.StationFuelEntity
import com.example.ui.components.StockBadge
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PetrolTeal
import com.example.ui.theme.StockAvailableGreen
import com.example.ui.viewmodel.OilPumpViewModel

@Composable
fun StockManagementScreen(
    viewModel: OilPumpViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fuels by viewModel.selectedStationFuels.collectAsState()

    var activeFuelForEdit by remember { mutableStateOf<StationFuelEntity?>(null) }
    var activeFuelForDelivery by remember { mutableStateOf<StationFuelEntity?>(null) }

    var editPriceInput by remember { mutableStateOf("") }
    var editStockInput by remember { mutableStateOf("") }
    var editThresholdInput by remember { mutableStateOf("") }
    var deliveryLitresInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Surface(color = NavyDark, shadowElevation = 4.dp) {
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
                    text = "Inventory, Prices & Restock",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.PriceChange, contentDescription = null, tint = NavyPrimary)
                        Text(
                            text = "Prices are synchronized with Bangladesh Petroleum Corporation (BPC) gazettes. Stock changes update immediately in customer app.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(fuels) { fuel ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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
                                Text(text = fuel.fuelName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                StockBadge(status = fuel.stockStatus)
                            }

                            Text(
                                text = "৳${fuel.pricePerLitre}/L",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = NavyPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Storage in Tank:", fontSize = 12.sp, color = Color.Gray)
                            Text("${fuel.stockLitres.toInt()} Litres", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Reserved by Pending Orders:", fontSize = 12.sp, color = Color.Gray)
                            Text("${fuel.reservedLitres.toInt()} Litres", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Low Stock Warning Limit:", fontSize = 12.sp, color = Color.Gray)
                            Text("${fuel.lowStockLimit.toInt()} Litres", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Action buttons: Edit Price/Stock & Record Delivery
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    activeFuelForEdit = fuel
                                    editPriceInput = fuel.pricePerLitre.toString()
                                    editStockInput = fuel.stockLitres.toInt().toString()
                                    editThresholdInput = fuel.lowStockLimit.toInt().toString()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Price & Stock", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    activeFuelForDelivery = fuel
                                    deliveryLitresInput = "2000"
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PetrolTeal),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Delivery", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Price & Stock Dialog
    if (activeFuelForEdit != null) {
        val f = activeFuelForEdit!!
        AlertDialog(
            onDismissRequest = { activeFuelForEdit = null },
            title = { Text("Update ${f.fuelName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editPriceInput,
                        onValueChange = { editPriceInput = it },
                        label = { Text("Price per Litre (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editStockInput,
                        onValueChange = { editStockInput = it },
                        label = { Text("Actual Tank Stock (Litres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editThresholdInput,
                        onValueChange = { editThresholdInput = it },
                        label = { Text("Low Stock Alert Threshold (Litres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = editPriceInput.toDoubleOrNull() ?: f.pricePerLitre
                        val s = editStockInput.toDoubleOrNull() ?: f.stockLitres
                        val th = editThresholdInput.toDoubleOrNull() ?: f.lowStockLimit
                        viewModel.updateFuelStockAndPrice(f, p, s, th)
                        activeFuelForEdit = null
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeFuelForEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Record Delivery Dialog
    if (activeFuelForDelivery != null) {
        val f = activeFuelForDelivery!!
        AlertDialog(
            onDismissRequest = { activeFuelForDelivery = null },
            title = { Text("Record Fuel Tanker Delivery") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add newly unloaded bulk fuel from petroleum tanker into ${f.fuelName} underground tank.")
                    OutlinedTextField(
                        value = deliveryLitresInput,
                        onValueChange = { deliveryLitresInput = it },
                        label = { Text("Delivered Volume (Litres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val added = deliveryLitresInput.toDoubleOrNull() ?: 0.0
                        if (added > 0.0) {
                            viewModel.addFuelDelivery(f, added)
                        }
                        activeFuelForDelivery = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PetrolTeal)
                ) {
                    Text("Confirm Delivery")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeFuelForDelivery = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
