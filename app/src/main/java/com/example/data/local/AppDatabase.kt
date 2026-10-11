package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        UserEntity::class,
        StationEntity::class,
        FuelTypeEntity::class,
        StationFuelEntity::class,
        VehicleTypeEntity::class,
        OrderEntity::class,
        ReviewEntity::class,
        EmergencyContactEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun stationDao(): StationDao
    abstract fun orderDao(): OrderDao
    abstract fun reviewDao(): ReviewDao
    abstract fun emergencyDao(): EmergencyDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oil_pump_bd.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
