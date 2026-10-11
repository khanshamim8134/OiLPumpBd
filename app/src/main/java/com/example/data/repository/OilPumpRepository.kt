package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.EmergencyContactEntity
import com.example.data.local.entities.FuelTypeEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.ReviewEntity
import com.example.data.local.entities.StationEntity
import com.example.data.local.entities.StationFuelEntity
import com.example.data.local.entities.UserEntity
import com.example.data.local.entities.VehicleTypeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class OilPumpRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val stationDao = db.stationDao()
    private val orderDao = db.orderDao()
    private val reviewDao = db.reviewDao()
    private val emergencyDao = db.emergencyDao()
    private val notificationDao = db.notificationDao()

    // --- Authentication ---
    suspend fun login(phone: String, passwordHash: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = userDao.login(phone.trim(), passwordHash.trim())
        if (user != null) {
            if (user.status == "suspended") {
                Result.failure(Exception("This account has been suspended by the administrator."))
            } else if (user.role == "owner" && user.status == "pending") {
                Result.failure(Exception("Your pump owner account is pending approval by CSTU/BPC administrator."))
            } else {
                Result.success(user)
            }
        } else {
            Result.failure(Exception("Invalid phone number or password."))
        }
    }

    suspend fun register(
        fullName: String,
        phone: String,
        email: String,
        password: String,
        role: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByPhone(phone.trim())
        if (existing != null) {
            return@withContext Result.failure(Exception("An account with phone number $phone already exists."))
        }
        val status = if (role == "owner") "pending" else "active"
        val newUser = UserEntity(
            fullName = fullName.trim(),
            phone = phone.trim(),
            email = email.trim(),
            passwordHash = password,
            role = role,
            status = status
        )
        val id = userDao.insertUser(newUser)
        val created = newUser.copy(userId = id)

        // Notification
        notificationDao.insertNotification(
            NotificationEntity(
                userId = id,
                targetRole = role.uppercase(),
                title = "Account Created",
                body = if (role == "owner") "Owner account pending admin approval. You will be notified once activated." else "Welcome to Oil Pump BD! Locate nearby fuel stations now.",
                eventType = "AUTH"
            )
        )

        Result.success(created)
    }

    suspend fun registerStation(
        ownerId: Long,
        name: String,
        address: String,
        district: String,
        lat: Double,
        lng: Double,
        phone: String,
        licenceNo: String
    ): Long = withContext(Dispatchers.IO) {
        val station = StationEntity(
            ownerId = ownerId,
            name = name,
            address = address,
            district = district,
            latitude = lat,
            longitude = lng,
            phone = phone,
            licenceNo = licenceNo,
            status = "pending"
        )
        val stationId = stationDao.insertStation(station)
        // Default fuels for new station
        stationDao.insertStationFuels(
            listOf(
                StationFuelEntity(stationId = stationId, fuelId = 1, fuelName = "Octane", pricePerLitre = 125.0, stockLitres = 2000.0),
                StationFuelEntity(stationId = stationId, fuelId = 2, fuelName = "Petrol", pricePerLitre = 121.0, stockLitres = 2000.0),
                StationFuelEntity(stationId = stationId, fuelId = 3, fuelName = "Diesel", pricePerLitre = 105.0, stockLitres = 3000.0)
            )
        )
        stationId
    }

    // --- Station Data ---
    fun getApprovedStations(): Flow<List<StationEntity>> = stationDao.getApprovedStations()
    fun getAllStations(): Flow<List<StationEntity>> = stationDao.getAllStations()
    fun getPendingStations(): Flow<List<StationEntity>> = stationDao.getPendingStations()
    suspend fun getStationById(stationId: Long): StationEntity? = stationDao.getStationById(stationId)
    fun getStationFuels(stationId: Long): Flow<List<StationFuelEntity>> = stationDao.getStationFuels(stationId)
    fun getVehicleTypes(): Flow<List<VehicleTypeEntity>> = stationDao.getAllVehicleTypes()
    fun getFuelTypes(): Flow<List<FuelTypeEntity>> = stationDao.getAllFuelTypes()

    suspend fun updateStationFuel(fuel: StationFuelEntity) = withContext(Dispatchers.IO) {
        stationDao.updateStationFuel(fuel.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun restockFuel(sfId: Long, addedLitres: Double) = withContext(Dispatchers.IO) {
        // Find existing fuel and add stock
        // For simplicity, search via stationFuels or direct update
    }

    // --- Order & Payment (Section 4.7 & 4.8) ---
    suspend fun createAndPayOrder(
        customer: UserEntity,
        station: StationEntity,
        fuel: StationFuelEntity,
        vtype: VehicleTypeEntity,
        quantityLitres: Double,
        paymentMethod: String // "bKash", "Nagad", "Card"
    ): Result<OrderEntity> = withContext(Dispatchers.IO) {
        // 1. Verify available stock
        val currentFuel = stationDao.getStationFuelDirect(station.stationId, fuel.fuelId)
            ?: return@withContext Result.failure(Exception("Fuel type unavailable at this station."))

        if (quantityLitres > currentFuel.availableLitres) {
            return@withContext Result.failure(Exception("Selected quantity exceeds available stock (${currentFuel.availableLitres}L left)."))
        }

        if (quantityLitres > vtype.maxLitresPerTxn) {
            return@withContext Result.failure(Exception("Maximum purchase limit for ${vtype.name} is ${vtype.maxLitresPerTxn}L."))
        }

        val totalAmount = Math.round(quantityLitres * currentFuel.pricePerLitre * 100.0) / 100.0
        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val randomHex = UUID.randomUUID().toString().substring(0, 4).uppercase()
        val txnCode = "OPM-$dateCode-$randomHex"
        val gatewayRef = when (paymentMethod) {
            "bKash" -> "BK" + (100000000L..999999999L).random()
            "Nagad" -> "NG" + (100000000L..999999999L).random()
            else -> "SSL" + (100000000L..999999999L).random()
        }

        // 2. Reserve stock in database transaction
        val updatedFuel = currentFuel.copy(
            reservedLitres = currentFuel.reservedLitres + quantityLitres,
            updatedAt = System.currentTimeMillis()
        )
        stationDao.updateStationFuel(updatedFuel)

        // 3. Create Order
        val order = OrderEntity(
            txnCode = txnCode,
            customerId = customer.userId,
            customerName = customer.fullName,
            customerPhone = customer.phone,
            stationId = station.stationId,
            stationName = station.name,
            fuelId = fuel.fuelId,
            fuelName = fuel.fuelName,
            vtypeId = vtype.vtypeId,
            vehicleName = vtype.name,
            quantityLitres = quantityLitres,
            unitPrice = currentFuel.pricePerLitre,
            amount = totalAmount,
            paymentMethod = paymentMethod,
            gatewayRef = gatewayRef,
            paymentStatus = "paid",
            status = "paid",
            createdAt = System.currentTimeMillis()
        )

        val id = orderDao.insertOrder(order)
        val savedOrder = order.copy(txnId = id)

        // 4. Notifications
        notificationDao.insertNotification(
            NotificationEntity(
                userId = customer.userId,
                targetRole = "CUSTOMER",
                title = "Payment Confirmed (${order.paymentMethod})",
                body = "Paid ৳${order.amount} for ${order.quantityLitres}L ${order.fuelName}. Transaction code: ${order.txnCode}",
                eventType = "PAYMENT"
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                userId = station.ownerId,
                targetRole = "OWNER",
                title = "New Paid Order: ${order.txnCode}",
                body = "${customer.fullName} bought ${order.quantityLitres}L ${order.fuelName} (৳${order.amount}). Ready to dispense.",
                eventType = "ORDER"
            )
        )

        Result.success(savedOrder)
    }

    suspend fun getOrderByCode(code: String): OrderEntity? = withContext(Dispatchers.IO) {
        orderDao.getOrderByCode(code.trim().uppercase())
    }

    suspend fun confirmDispense(order: OrderEntity): Result<OrderEntity> = withContext(Dispatchers.IO) {
        if (order.status == "dispensed") {
            return@withContext Result.failure(Exception("This transaction has already been dispensed."))
        }
        if (order.status != "paid") {
            return@withContext Result.failure(Exception("Cannot dispense order with status: ${order.status}"))
        }

        // Subtract from actual stock and release reserved stock
        val currentFuel = stationDao.getStationFuelDirect(order.stationId, order.fuelId)
        if (currentFuel != null) {
            val updatedFuel = currentFuel.copy(
                stockLitres = (currentFuel.stockLitres - order.quantityLitres).coerceAtLeast(0.0),
                reservedLitres = (currentFuel.reservedLitres - order.quantityLitres).coerceAtLeast(0.0),
                updatedAt = System.currentTimeMillis()
            )
            stationDao.updateStationFuel(updatedFuel)
        }

        val updatedOrder = order.copy(
            status = "dispensed",
            dispensedAt = System.currentTimeMillis()
        )
        orderDao.updateOrder(updatedOrder)

        // Customer notification
        notificationDao.insertNotification(
            NotificationEntity(
                userId = order.customerId,
                targetRole = "CUSTOMER",
                title = "Fuel Dispensed Successfully",
                body = "Dispensed ${order.quantityLitres}L ${order.fuelName} at ${order.stationName}. Please rate your experience!",
                eventType = "DISPENSE"
            )
        )

        Result.success(updatedOrder)
    }

    fun getCustomerOrders(customerId: Long): Flow<List<OrderEntity>> = orderDao.getOrdersByCustomer(customerId)
    fun getStationOrders(stationId: Long): Flow<List<OrderEntity>> = orderDao.getOrdersByStation(stationId)
    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()

    // --- Reviews ---
    fun getStationReviews(stationId: Long): Flow<List<ReviewEntity>> = reviewDao.getReviewsForStation(stationId)
    suspend fun submitReview(
        stationId: Long,
        customerId: Long,
        customerName: String,
        rating: Int,
        comment: String
    ) = withContext(Dispatchers.IO) {
        reviewDao.insertReview(
            ReviewEntity(
                stationId = stationId,
                customerId = customerId,
                customerName = customerName,
                rating = rating,
                comment = comment
            )
        )
    }

    // --- Emergency Contacts ---
    fun getEmergencyContacts(): Flow<List<EmergencyContactEntity>> = emergencyDao.getAllContacts()

    // --- Notifications ---
    fun getNotifications(userId: Long, role: String): Flow<List<NotificationEntity>> =
        notificationDao.getNotifications(userId, role)

    suspend fun markNotificationAsRead(notifId: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(notifId)
    }

    suspend fun markAllNotificationsAsRead(userId: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(userId)
    }

    suspend fun sendBroadcast(title: String, body: String, targetRole: String) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(
            NotificationEntity(
                userId = 0,
                targetRole = targetRole,
                title = title,
                body = body,
                eventType = "BROADCAST"
            )
        )
    }

    // --- Admin Operations ---
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun updateUserStatus(userId: Long, status: String) = withContext(Dispatchers.IO) {
        userDao.updateUserStatus(userId, status)
    }

    suspend fun updateStationStatus(stationId: Long, status: String) = withContext(Dispatchers.IO) {
        stationDao.updateStationStatus(stationId, status)
    }
}
