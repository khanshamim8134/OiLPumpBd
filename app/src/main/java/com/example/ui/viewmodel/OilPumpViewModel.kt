package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PrepopulateData
import com.example.data.local.entities.EmergencyContactEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.ReviewEntity
import com.example.data.local.entities.StationEntity
import com.example.data.local.entities.StationFuelEntity
import com.example.data.local.entities.UserEntity
import com.example.data.local.entities.VehicleTypeEntity
import com.example.data.repository.OilPumpRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OilPumpViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = OilPumpRepository(db)

    // Current Session
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Station list
    private val _approvedStations = MutableStateFlow<List<StationEntity>>(emptyList())
    val approvedStations: StateFlow<List<StationEntity>> = _approvedStations.asStateFlow()

    private val _allStations = MutableStateFlow<List<StationEntity>>(emptyList())
    val allStations: StateFlow<List<StationEntity>> = _allStations.asStateFlow()

    // Search & Radius
    private val _searchRadiusKm = MutableStateFlow(15)
    val searchRadiusKm: StateFlow<Int> = _searchRadiusKm.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected Station for details
    private val _selectedStation = MutableStateFlow<StationEntity?>(null)
    val selectedStation: StateFlow<StationEntity?> = _selectedStation.asStateFlow()

    private val _selectedStationFuels = MutableStateFlow<List<StationFuelEntity>>(emptyList())
    val selectedStationFuels: StateFlow<List<StationFuelEntity>> = _selectedStationFuels.asStateFlow()

    private val _selectedStationReviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val selectedStationReviews: StateFlow<List<ReviewEntity>> = _selectedStationReviews.asStateFlow()

    // Vehicle types
    private val _vehicleTypes = MutableStateFlow<List<VehicleTypeEntity>>(emptyList())
    val vehicleTypes: StateFlow<List<VehicleTypeEntity>> = _vehicleTypes.asStateFlow()

    // Orders
    private val _customerOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val customerOrders: StateFlow<List<OrderEntity>> = _customerOrders.asStateFlow()

    private val _allOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val allOrders: StateFlow<List<OrderEntity>> = _allOrders.asStateFlow()

    // Active receipt
    private val _activeReceiptOrder = MutableStateFlow<OrderEntity?>(null)
    val activeReceiptOrder: StateFlow<OrderEntity?> = _activeReceiptOrder.asStateFlow()

    // Emergency Contacts
    private val _emergencyContacts = MutableStateFlow<List<EmergencyContactEntity>>(emptyList())
    val emergencyContacts: StateFlow<List<EmergencyContactEntity>> = _emergencyContacts.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    // Admin Users
    private val _allUsers = MutableStateFlow<List<UserEntity>>(emptyList())
    val allUsers: StateFlow<List<UserEntity>> = _allUsers.asStateFlow()

    // UI Feedback Event
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            // Seed sample database
            PrepopulateData.populateIfEmpty(db)

            // Auto-login default customer (Md. Shamim Hossain) so app is live immediately
            val defaultUser = db.userDao().getUserById(1)
            _currentUser.value = defaultUser

            // Collect stations
            launch {
                repository.getApprovedStations().collectLatest {
                    _approvedStations.value = it
                    if (_selectedStation.value == null && it.isNotEmpty()) {
                        selectStation(it.first())
                    }
                }
            }

            launch {
                repository.getAllStations().collectLatest {
                    _allStations.value = it
                }
            }

            launch {
                repository.getVehicleTypes().collectLatest {
                    _vehicleTypes.value = it
                }
            }

            launch {
                repository.getEmergencyContacts().collectLatest {
                    _emergencyContacts.value = it
                }
            }

            launch {
                repository.getAllOrders().collectLatest {
                    _allOrders.value = it
                }
            }

            launch {
                repository.getAllUsers().collectLatest {
                    _allUsers.value = it
                }
            }

            // Sync user orders and notifications
            observeUserData()
        }
    }

    private fun observeUserData() {
        viewModelScope.launch {
            currentUser.collectLatest { user ->
                if (user != null) {
                    launch {
                        repository.getCustomerOrders(user.userId).collectLatest {
                            _customerOrders.value = it
                        }
                    }
                    launch {
                        repository.getNotifications(user.userId, user.role.uppercase()).collectLatest {
                            _notifications.value = it
                        }
                    }
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchRadiusKm(radius: Int) {
        _searchRadiusKm.value = radius
    }

    fun selectStation(station: StationEntity) {
        _selectedStation.value = station
        viewModelScope.launch {
            launch {
                repository.getStationFuels(station.stationId).collectLatest {
                    _selectedStationFuels.value = it
                }
            }
            launch {
                repository.getStationReviews(station.stationId).collectLatest {
                    _selectedStationReviews.value = it
                }
            }
        }
    }

    // Role switching & Auth
    fun switchUserRole(role: String) {
        viewModelScope.launch {
            val user = when (role.lowercase()) {
                "owner" -> db.userDao().getUserById(2)
                "admin" -> db.userDao().getUserById(4)
                else -> db.userDao().getUserById(1)
            }
            _currentUser.value = user
            _toastMessage.emit("Switched to ${user?.role?.uppercase()} mode: ${user?.fullName}")
        }
    }

    fun login(phone: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.login(phone, pass)
            result.onSuccess {
                _currentUser.value = it
                _toastMessage.emit("Welcome back, ${it.fullName}!")
                onSuccess()
            }.onFailure {
                _toastMessage.emit(it.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, phone: String, email: String, pass: String, role: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.register(name, phone, email, pass, role)
            result.onSuccess {
                if (it.role == "owner") {
                    _toastMessage.emit("Owner account registered! Pending admin review.")
                } else {
                    _currentUser.value = it
                    _toastMessage.emit("Registration successful! Welcome ${it.fullName}")
                    onSuccess()
                }
            }.onFailure {
                _toastMessage.emit(it.message ?: "Registration failed")
            }
        }
    }

    fun registerStation(
        name: String,
        address: String,
        district: String,
        lat: Double,
        lng: Double,
        phone: String,
        licenceNo: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val owner = _currentUser.value ?: return@launch
            repository.registerStation(owner.userId, name, address, district, lat, lng, phone, licenceNo)
            _toastMessage.emit("Station submitted for approval!")
            onSuccess()
        }
    }

    // Fuel Purchase Flow
    fun buyFuel(
        station: StationEntity,
        fuel: StationFuelEntity,
        vtype: VehicleTypeEntity,
        litres: Double,
        method: String,
        onSuccess: (OrderEntity) -> Unit
    ) {
        viewModelScope.launch {
            val customer = _currentUser.value ?: return@launch
            val res = repository.createAndPayOrder(customer, station, fuel, vtype, litres, method)
            res.onSuccess { order ->
                _activeReceiptOrder.value = order
                _toastMessage.emit("Payment Successful! Code: ${order.txnCode}")
                onSuccess(order)
            }.onFailure { err ->
                _toastMessage.emit(err.message ?: "Purchase failed")
            }
        }
    }

    fun viewReceipt(order: OrderEntity) {
        _activeReceiptOrder.value = order
    }

    fun clearActiveReceipt() {
        _activeReceiptOrder.value = null
    }

    // Owner Operations
    fun verifyAndDispense(txnCode: String, onResult: (OrderEntity?, String?) -> Unit) {
        viewModelScope.launch {
            val order = repository.getOrderByCode(txnCode)
            if (order == null) {
                onResult(null, "Transaction code not found! Please check.")
                return@launch
            }
            if (order.status == "dispensed") {
                onResult(order, "This transaction has ALREADY been dispensed at: ${order.dispensedAt}")
                return@launch
            }
            if (order.status != "paid") {
                onResult(order, "Order is in status '${order.status}'. Payment not verified.")
                return@launch
            }

            val dispenseRes = repository.confirmDispense(order)
            dispenseRes.onSuccess { updated ->
                _toastMessage.emit("Dispense confirmed for ${updated.quantityLitres}L ${updated.fuelName}!")
                onResult(updated, null)
            }.onFailure { err ->
                onResult(order, err.message)
            }
        }
    }

    fun updateFuelStockAndPrice(fuel: StationFuelEntity, newPrice: Double, newStock: Double, newLowLimit: Double) {
        viewModelScope.launch {
            val updated = fuel.copy(
                pricePerLitre = newPrice,
                stockLitres = newStock,
                lowStockLimit = newLowLimit,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateStationFuel(updated)
            _toastMessage.emit("Updated ${fuel.fuelName}: ৳$newPrice/L, Stock: ${newStock}L")
        }
    }

    fun addFuelDelivery(fuel: StationFuelEntity, deliveryLitres: Double) {
        viewModelScope.launch {
            val updated = fuel.copy(
                stockLitres = fuel.stockLitres + deliveryLitres,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateStationFuel(updated)
            _toastMessage.emit("Recorded delivery: +${deliveryLitres}L of ${fuel.fuelName}")
        }
    }

    // Review
    fun submitReview(stationId: Long, rating: Int, comment: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.submitReview(stationId, user.userId, user.fullName, rating, comment)
            _toastMessage.emit("Thank you! Review submitted.")
        }
    }

    // Admin Operations
    fun approveStation(stationId: Long, approve: Boolean) {
        viewModelScope.launch {
            val status = if (approve) "approved" else "rejected"
            repository.updateStationStatus(stationId, status)
            _toastMessage.emit("Station status updated to $status")
        }
    }

    fun toggleUserSuspension(user: UserEntity) {
        viewModelScope.launch {
            val nextStatus = if (user.status == "suspended") "active" else "suspended"
            repository.updateUserStatus(user.userId, nextStatus)
            _toastMessage.emit("User ${user.fullName} is now $nextStatus")
        }
    }

    fun sendBroadcast(title: String, body: String, targetRole: String) {
        viewModelScope.launch {
            repository.sendBroadcast(title, body, targetRole)
            _toastMessage.emit("Broadcast announcement dispatched to $targetRole!")
        }
    }

    fun markNotificationRead(notifId: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notifId)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            _currentUser.value?.let { repository.markAllNotificationsAsRead(it.userId) }
        }
    }
}
