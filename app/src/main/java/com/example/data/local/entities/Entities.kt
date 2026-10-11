package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val fullName: String,
    val phone: String,
    val email: String = "",
    val passwordHash: String,
    val role: String, // "customer", "owner", "admin"
    val status: String = "active", // "active", "pending", "suspended"
    val fcmToken: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey(autoGenerate = true)
    val stationId: Long = 0,
    val ownerId: Long,
    val name: String,
    val address: String,
    val district: String = "Chandpur",
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val licenceNo: String,
    val openHours: String = "24 Hours",
    val status: String = "approved", // "pending", "approved", "rejected", "closed"
    val rating: Float = 4.5f,
    val reviewCount: Int = 12
)

@Entity(tableName = "fuel_types")
data class FuelTypeEntity(
    @PrimaryKey
    val fuelId: Long,
    val name: String,
    val unit: String = "Litre"
)

@Entity(tableName = "station_fuel")
data class StationFuelEntity(
    @PrimaryKey(autoGenerate = true)
    val sfId: Long = 0,
    val stationId: Long,
    val fuelId: Long,
    val fuelName: String,
    val pricePerLitre: Double,
    val stockLitres: Double,
    val reservedLitres: Double = 0.0,
    val lowStockLimit: Double = 500.0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val availableLitres: Double get() = (stockLitres - reservedLitres).coerceAtLeast(0.0)
    
    val stockStatus: String get() = when {
        availableLitres <= 0.0 -> "OUT_OF_STOCK"
        availableLitres <= lowStockLimit -> "LOW_STOCK"
        else -> "AVAILABLE"
    }
}

@Entity(tableName = "vehicle_types")
data class VehicleTypeEntity(
    @PrimaryKey
    val vtypeId: Long,
    val name: String,
    val maxLitresPerTxn: Double
)

@Entity(tableName = "transactions")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val txnId: Long = 0,
    val txnCode: String, // e.g. "OPM-20261010-9A4B"
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    val stationId: Long,
    val stationName: String,
    val fuelId: Long,
    val fuelName: String,
    val vtypeId: Long,
    val vehicleName: String,
    val quantityLitres: Double,
    val unitPrice: Double,
    val amount: Double,
    val paymentMethod: String, // "bKash", "Nagad", "Card"
    val gatewayRef: String = "",
    val paymentStatus: String = "pending", // "pending", "paid", "failed", "refunded"
    val status: String = "created", // "created", "paid", "dispensed", "expired", "cancelled"
    val createdAt: Long = System.currentTimeMillis(),
    val dispensedAt: Long? = null,
    val expiresAt: Long = System.currentTimeMillis() + (30 * 60 * 1000L) // 30 minutes validity
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val reviewId: Long = 0,
    val stationId: Long,
    val customerId: Long,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true)
    val contactId: Long = 0,
    val stationId: Long? = null,
    val title: String,
    val phone: String,
    val type: String // "National", "Highway Police", "Fire Service", "Station"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val notifId: Long = 0,
    val userId: Long, // 0 means broadcast to all
    val targetRole: String = "ALL", // "ALL", "CUSTOMER", "OWNER", "ADMIN"
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val eventType: String = "GENERAL"
)
