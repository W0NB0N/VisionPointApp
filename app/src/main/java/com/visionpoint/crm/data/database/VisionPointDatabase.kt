package com.visionpoint.crm.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.visionpoint.crm.data.database.dao.CashbookDao
import com.visionpoint.crm.data.database.dao.CustomerDao
import com.visionpoint.crm.data.database.dao.PaymentDao
import com.visionpoint.crm.data.database.dao.PrescriptionDao
import com.visionpoint.crm.data.database.dao.SaleDao
import com.visionpoint.crm.data.database.dao.SaleItemDao
import com.visionpoint.crm.data.database.dao.SettingDao
import com.visionpoint.crm.data.model.Cashbook
import com.visionpoint.crm.data.model.Customer
import com.visionpoint.crm.data.model.Payment
import com.visionpoint.crm.data.model.Prescription
import com.visionpoint.crm.data.model.Sale
import com.visionpoint.crm.data.model.SaleItem
import com.visionpoint.crm.data.model.Setting

@Database(
    entities = [
        Customer::class,
        Prescription::class,
        Sale::class,
        SaleItem::class,
        Payment::class,
        Cashbook::class,
        Setting::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VisionPointDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun paymentDao(): PaymentDao
    abstract fun cashbookDao(): CashbookDao
    abstract fun settingDao(): SettingDao

    companion object {
        const val DATABASE_NAME = "vision_point.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE customers ADD COLUMN address TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE prescriptions ADD COLUMN notes TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE prescriptions ADD COLUMN isPreviousLens INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sales ADD COLUMN discountType TEXT NOT NULL DEFAULT 'percentage'")
                db.execSQL("ALTER TABLE sales ADD COLUMN discountAmount REAL NOT NULL DEFAULT 0.0")
            }
        }
    }
}
