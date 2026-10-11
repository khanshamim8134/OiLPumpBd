package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.EmergencyContactEntity
import com.example.data.local.entities.FuelTypeEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.ReviewEntity
import com.example.data.local.entities.StationEntity
import com.example.data.local.entities.StationFuelEntity
import com.example.data.local.entities.UserEntity
import com.example.data.local.entities.VehicleTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phone = :phone AND passwordHash = :passwordHash LIMIT 1")
    suspend fun login(phone: String, passwordHash: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("UPDATE users SET status = :status WHERE userId = :userId")
    suspend fun updateUserStatus(userId: Long, status: String)

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'owner' AND status = 'pending'")
    fun getPendingOwners(): Flow<List<UserEntity>>
}

@Dao
interface StationDao {
    @Query("SELECT * FROM stations ORDER BY rating DESC")
    fun getAllStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE status = 'approved' ORDER BY rating DESC")
    fun getApprovedStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE status = 'pending' ORDER BY stationId DESC")
    fun getPendingStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE stationId = :stationId LIMIT 1")
    suspend fun getStationById(stationId: Long): StationEntity?

    @Query("SELECT * FROM stations WHERE ownerId = :ownerId")
    fun getStationsByOwner(ownerId: Long): Flow<List<StationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: StationEntity): Long

    @Update
    suspend fun updateStation(station: StationEntity)

    @Query("UPDATE stations SET status = :status WHERE stationId = :stationId")
    suspend fun updateStationStatus(stationId: Long, status: String)

    @Query("SELECT * FROM station_fuel WHERE stationId = :stationId")
    fun getStationFuels(stationId: Long): Flow<List<StationFuelEntity>>

    @Query("SELECT * FROM station_fuel WHERE stationId = :stationId AND fuelId = :fuelId LIMIT 1")
    suspend fun getStationFuelDirect(stationId: Long, fuelId: Long): StationFuelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStationFuel(fuel: StationFuelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStationFuels(fuels: List<StationFuelEntity>)

    @Update
    suspend fun updateStationFuel(fuel: StationFuelEntity)

    @Query("SELECT * FROM fuel_types")
    fun getAllFuelTypes(): Flow<List<FuelTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelTypes(types: List<FuelTypeEntity>)

    @Query("SELECT * FROM vehicle_types")
    fun getAllVehicleTypes(): Flow<List<VehicleTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleTypes(types: List<VehicleTypeEntity>)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersByCustomer(customerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM transactions WHERE stationId = :stationId ORDER BY createdAt DESC")
    fun getOrdersByStation(stationId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM transactions WHERE txnCode = :txnCode LIMIT 1")
    suspend fun getOrderByCode(txnCode: String): OrderEntity?

    @Query("SELECT * FROM transactions WHERE txnId = :txnId LIMIT 1")
    suspend fun getOrderById(txnId: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE stationId = :stationId ORDER BY createdAt DESC")
    fun getReviewsForStation(stationId: Long): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Query("DELETE FROM reviews WHERE reviewId = :reviewId")
    suspend fun deleteReview(reviewId: Long)
}

@Dao
interface EmergencyDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY contactId ASC")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<EmergencyContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity): Long
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId OR targetRole = 'ALL' OR targetRole = :role ORDER BY createdAt DESC")
    fun getNotifications(userId: Long, role: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE notifId = :notifId")
    suspend fun markAsRead(notifId: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR targetRole = 'ALL'")
    suspend fun markAllAsRead(userId: Long)
}
