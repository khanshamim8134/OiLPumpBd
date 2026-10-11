package com.example.data.local

import com.example.data.local.dao.EmergencyDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.ReviewDao
import com.example.data.local.dao.StationDao
import com.example.data.local.dao.UserDao
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
import kotlinx.coroutines.withContext

object PrepopulateData {
    suspend fun populateIfEmpty(db: AppDatabase) = withContext(Dispatchers.IO) {
        val userDao = db.userDao()
        val stationDao = db.stationDao()
        val emergencyDao = db.emergencyDao()
        val orderDao = db.orderDao()
        val reviewDao = db.reviewDao()
        val notificationDao = db.notificationDao()

        // Check if already populated
        val existingUser = userDao.getUserByPhone("01700000001")
        if (existingUser != null) return@withContext

        // 1. Insert Fuel Types
        stationDao.insertFuelTypes(
            listOf(
                FuelTypeEntity(fuelId = 1, name = "Octane", unit = "Litre"),
                FuelTypeEntity(fuelId = 2, name = "Petrol", unit = "Litre"),
                FuelTypeEntity(fuelId = 3, name = "Diesel", unit = "Litre"),
                FuelTypeEntity(fuelId = 4, name = "CNG", unit = "m³"),
                FuelTypeEntity(fuelId = 5, name = "LPG", unit = "Litre")
            )
        )

        // 2. Insert Vehicle Types
        stationDao.insertVehicleTypes(
            listOf(
                VehicleTypeEntity(vtypeId = 1, name = "Motorcycle", maxLitresPerTxn = 15.0),
                VehicleTypeEntity(vtypeId = 2, name = "Private Car / Microbus", maxLitresPerTxn = 50.0),
                VehicleTypeEntity(vtypeId = 3, name = "Pickup / Light Truck", maxLitresPerTxn = 80.0),
                VehicleTypeEntity(vtypeId = 4, name = "Bus / Heavy Truck", maxLitresPerTxn = 200.0),
                VehicleTypeEntity(vtypeId = 5, name = "Other / Generator / Container", maxLitresPerTxn = 30.0)
            )
        )

        // 3. Insert Users (Customer, Owner, Pending Owner, Admin)
        val customerId = userDao.insertUser(
            UserEntity(
                userId = 1,
                fullName = "Md. Shamim Hossain",
                phone = "01700000001",
                email = "shamim@cstu.ac.bd",
                passwordHash = "password123",
                role = "customer",
                status = "active"
            )
        )

        val ownerId = userDao.insertUser(
            UserEntity(
                userId = 2,
                fullName = "Alhaj Kabir Ahmed",
                phone = "01800000002",
                email = "kabir.padma@gmail.com",
                passwordHash = "password123",
                role = "owner",
                status = "active"
            )
        )

        val pendingOwnerId = userDao.insertUser(
            UserEntity(
                userId = 3,
                fullName = "Rafiqul Islam",
                phone = "01900000003",
                email = "rafiqul.sonargaon@gmail.com",
                passwordHash = "password123",
                role = "owner",
                status = "pending"
            )
        )

        val adminId = userDao.insertUser(
            UserEntity(
                userId = 4,
                fullName = "Admin Refat Hossain",
                phone = "01500000000",
                email = "admin@oilpump.gov.bd",
                passwordHash = "admin123",
                role = "admin",
                status = "active"
            )
        )

        // 4. Insert Stations
        val station1Id = stationDao.insertStation(
            StationEntity(
                stationId = 1,
                ownerId = ownerId,
                name = "Padma Oil Filling Station",
                address = "Stadium Road, Wireless Mor, Chandpur",
                district = "Chandpur",
                latitude = 23.2321,
                longitude = 90.6631,
                phone = "01811223344",
                licenceNo = "LIC-CP-9021/BPC",
                openHours = "24 Hours (Open)",
                status = "approved",
                rating = 4.8f,
                reviewCount = 38
            )
        )

        val station2Id = stationDao.insertStation(
            StationEntity(
                stationId = 2,
                ownerId = ownerId,
                name = "Meghna Petroleum Agency",
                address = "Bus Stand bypass, Chandpur Sadar",
                district = "Chandpur",
                latitude = 23.2285,
                longitude = 90.6580,
                phone = "01722334455",
                licenceNo = "LIC-CP-8842/BPC",
                openHours = "06:00 AM - 11:30 PM",
                status = "approved",
                rating = 4.6f,
                reviewCount = 21
            )
        )

        val station3Id = stationDao.insertStation(
            StationEntity(
                stationId = 3,
                ownerId = ownerId,
                name = "Jamuna Fuel & CNG Filling Station",
                address = "Baburhat Road, Chandpur",
                district = "Chandpur",
                latitude = 23.2450,
                longitude = 90.6810,
                phone = "01933445566",
                licenceNo = "LIC-CP-7711/BPC",
                openHours = "24 Hours (Open)",
                status = "approved",
                rating = 4.5f,
                reviewCount = 17
            )
        )

        val pendingStationId = stationDao.insertStation(
            StationEntity(
                stationId = 4,
                ownerId = pendingOwnerId,
                name = "Sonar Bangla Fuel Station",
                address = "Hajiganj Highway, Chandpur",
                district = "Chandpur",
                latitude = 23.2600,
                longitude = 90.7200,
                phone = "01655667788",
                licenceNo = "LIC-CP-4301/PENDING",
                openHours = "07:00 AM - 10:00 PM",
                status = "pending",
                rating = 4.0f,
                reviewCount = 0
            )
        )

        // 5. Insert Fuels for Stations
        stationDao.insertStationFuels(
            listOf(
                // Station 1: Padma Oil
                StationFuelEntity(stationId = station1Id, fuelId = 1, fuelName = "Octane", pricePerLitre = 125.0, stockLitres = 4500.0, lowStockLimit = 600.0),
                StationFuelEntity(stationId = station1Id, fuelId = 2, fuelName = "Petrol", pricePerLitre = 121.0, stockLitres = 3800.0, lowStockLimit = 500.0),
                StationFuelEntity(stationId = station1Id, fuelId = 3, fuelName = "Diesel", pricePerLitre = 105.0, stockLitres = 8200.0, lowStockLimit = 1000.0),
                StationFuelEntity(stationId = station1Id, fuelId = 4, fuelName = "CNG", pricePerLitre = 45.0, stockLitres = 2800.0, lowStockLimit = 400.0),

                // Station 2: Meghna Petroleum (Has Low Stock and Out Of Stock examples)
                StationFuelEntity(stationId = station2Id, fuelId = 1, fuelName = "Octane", pricePerLitre = 125.0, stockLitres = 420.0, lowStockLimit = 600.0), // Low stock
                StationFuelEntity(stationId = station2Id, fuelId = 2, fuelName = "Petrol", pricePerLitre = 121.0, stockLitres = 2900.0, lowStockLimit = 500.0),
                StationFuelEntity(stationId = station2Id, fuelId = 3, fuelName = "Diesel", pricePerLitre = 105.0, stockLitres = 0.0, lowStockLimit = 1000.0), // Out of stock

                // Station 3: Jamuna
                StationFuelEntity(stationId = station3Id, fuelId = 1, fuelName = "Octane", pricePerLitre = 125.0, stockLitres = 5200.0, lowStockLimit = 600.0),
                StationFuelEntity(stationId = station3Id, fuelId = 2, fuelName = "Petrol", pricePerLitre = 121.0, stockLitres = 4100.0, lowStockLimit = 500.0),
                StationFuelEntity(stationId = station3Id, fuelId = 3, fuelName = "Diesel", pricePerLitre = 105.0, stockLitres = 6500.0, lowStockLimit = 1000.0),
                StationFuelEntity(stationId = station3Id, fuelId = 5, fuelName = "LPG", pricePerLitre = 110.0, stockLitres = 3200.0, lowStockLimit = 500.0)
            )
        )

        // 6. Insert Reviews
        reviewDao.insertReview(
            ReviewEntity(
                stationId = station1Id,
                customerId = customerId,
                customerName = "Shamim Hossain",
                rating = 5,
                comment = "Accurate octane measurement and very fast digital verification. Highly recommended!"
            )
        )
        reviewDao.insertReview(
            ReviewEntity(
                stationId = station1Id,
                customerId = 1,
                customerName = "Tariqul Anam",
                rating = 5,
                comment = "Staff was courteous and scanned my OPM QR code in seconds without any delay."
            )
        )
        reviewDao.insertReview(
            ReviewEntity(
                stationId = station2Id,
                customerId = 1,
                customerName = "Farhan Ahmed",
                rating = 4,
                comment = "Good service, but diesel queue was congested in the evening."
            )
        )

        // 7. Insert Emergency Contacts
        emergencyDao.insertContacts(
            listOf(
                EmergencyContactEntity(title = "National Emergency Service", phone = "999", type = "National"),
                EmergencyContactEntity(title = "Fire Service & Civil Defence", phone = "16163", type = "Fire Service"),
                EmergencyContactEntity(title = "Bangladesh Highway Police", phone = "01320000000", type = "Highway Police"),
                EmergencyContactEntity(title = "Ambulance Hotline", phone = "199", type = "National"),
                EmergencyContactEntity(stationId = station1Id, title = "Padma Filling Station Helpline", phone = "01811223344", type = "Station"),
                EmergencyContactEntity(stationId = station2Id, title = "Meghna Station Manager", phone = "01722334455", type = "Station")
            )
        )

        // 8. Sample Past Orders
        orderDao.insertOrder(
            OrderEntity(
                txnCode = "OPM-20261010-8812",
                customerId = customerId,
                customerName = "Md. Shamim Hossain",
                customerPhone = "01700000001",
                stationId = station1Id,
                stationName = "Padma Oil Filling Station",
                fuelId = 1,
                fuelName = "Octane",
                vtypeId = 1,
                vehicleName = "Motorcycle",
                quantityLitres = 5.0,
                unitPrice = 125.0,
                amount = 625.0,
                paymentMethod = "bKash",
                gatewayRef = "BK789456123",
                paymentStatus = "paid",
                status = "dispensed",
                createdAt = System.currentTimeMillis() - 7200000L,
                dispensedAt = System.currentTimeMillis() - 6900000L
            )
        )

        orderDao.insertOrder(
            OrderEntity(
                txnCode = "OPM-20261010-9421",
                customerId = customerId,
                customerName = "Md. Shamim Hossain",
                customerPhone = "01700000001",
                stationId = station1Id,
                stationName = "Padma Oil Filling Station",
                fuelId = 3,
                fuelName = "Diesel",
                vtypeId = 2,
                vehicleName = "Private Car / Microbus",
                quantityLitres = 20.0,
                unitPrice = 105.0,
                amount = 2100.0,
                paymentMethod = "Nagad",
                gatewayRef = "NG11223344",
                paymentStatus = "paid",
                status = "paid", // Ready at station for owner to verify and dispense
                createdAt = System.currentTimeMillis() - 600000L
            )
        )

        // 9. Notifications
        notificationDao.insertNotification(
            NotificationEntity(
                userId = customerId,
                targetRole = "ALL",
                title = "Welcome to Oil Pump BD",
                body = "Find nearby pumps, check real-time fuel availability & pay digitally with bKash and Nagad.",
                eventType = "SYSTEM"
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                userId = customerId,
                targetRole = "CUSTOMER",
                title = "Order OPM-20261010-9421 Ready",
                body = "Your payment for 20L Diesel is confirmed. Show the QR code at the Padma Oil dispenser.",
                eventType = "PAYMENT"
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                userId = ownerId,
                targetRole = "OWNER",
                title = "New Dispense Request",
                body = "Order OPM-20261010-9421 (20L Diesel) is waiting for verification at your pump.",
                eventType = "ORDER"
            )
        )
    }
}
