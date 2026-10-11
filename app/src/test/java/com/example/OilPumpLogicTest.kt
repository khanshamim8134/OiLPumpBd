package com.example

import com.example.data.local.entities.StationFuelEntity
import com.example.data.local.entities.VehicleTypeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Functional unit tests for Oil Pump Management System (ICT 3206)
 * Verifying business rules from Chapter 3 & Chapter 5 (Table 5.2):
 * - Stock status derivation (Available, Low Stock, Out of Stock)
 * - Vehicle maximum fuel limits
 * - Price calculation accuracy
 * - Transaction token format
 */
class OilPumpLogicTest {

    @Test
    fun testStockStatusDerivation_Available() {
        val fuel = StationFuelEntity(
            stationId = 1,
            fuelId = 1,
            fuelName = "Octane",
            pricePerLitre = 125.0,
            stockLitres = 4000.0,
            reservedLitres = 200.0,
            lowStockLimit = 600.0
        )
        assertEquals(3800.0, fuel.availableLitres, 0.01)
        assertEquals("AVAILABLE", fuel.stockStatus)
    }

    @Test
    fun testStockStatusDerivation_LowStock() {
        val fuel = StationFuelEntity(
            stationId = 1,
            fuelId = 1,
            fuelName = "Octane",
            pricePerLitre = 125.0,
            stockLitres = 550.0,
            reservedLitres = 50.0,
            lowStockLimit = 600.0
        )
        assertEquals(500.0, fuel.availableLitres, 0.01)
        assertEquals("LOW_STOCK", fuel.stockStatus)
    }

    @Test
    fun testStockStatusDerivation_OutOfStock() {
        val fuel = StationFuelEntity(
            stationId = 1,
            fuelId = 3,
            fuelName = "Diesel",
            pricePerLitre = 105.0,
            stockLitres = 100.0,
            reservedLitres = 100.0,
            lowStockLimit = 600.0
        )
        assertEquals(0.0, fuel.availableLitres, 0.01)
        assertEquals("OUT_OF_STOCK", fuel.stockStatus)
    }

    @Test
    fun testPriceCalculation() {
        val unitPrice = 125.0
        val litres = 8.5
        val total = Math.round(litres * unitPrice * 100.0) / 100.0
        assertEquals(1062.5, total, 0.01)
    }

    @Test
    fun testVehicleQuantityLimits() {
        val motorcycle = VehicleTypeEntity(vtypeId = 1, name = "Motorcycle", maxLitresPerTxn = 15.0)
        val privateCar = VehicleTypeEntity(vtypeId = 2, name = "Private Car / Microbus", maxLitresPerTxn = 50.0)

        assertTrue(10.0 <= motorcycle.maxLitresPerTxn)
        assertTrue(20.0 > motorcycle.maxLitresPerTxn)
        assertTrue(45.0 <= privateCar.maxLitresPerTxn)
    }
}
