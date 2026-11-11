package com.visionpoint.crm.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.visionpoint.crm.data.database.VisionPointDatabase
import com.visionpoint.crm.data.database.dao.CashbookDao
import com.visionpoint.crm.data.database.dao.CustomerDao
import com.visionpoint.crm.data.database.dao.PaymentDao
import com.visionpoint.crm.data.database.dao.PrescriptionDao
import com.visionpoint.crm.data.database.dao.SaleDao
import com.visionpoint.crm.data.database.dao.SaleItemDao
import com.visionpoint.crm.data.database.dao.SettingDao
import com.visionpoint.crm.data.repository.SettingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        settingDaoProvider: Provider<SettingDao>
    ): VisionPointDatabase {
        return Room.databaseBuilder(
            context,
            VisionPointDatabase::class.java,
            VisionPointDatabase.DATABASE_NAME
        )
            .addMigrations(
                VisionPointDatabase.MIGRATION_1_2,
                VisionPointDatabase.MIGRATION_2_3,
                VisionPointDatabase.MIGRATION_3_4
            )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Seed initial default settings on first database creation
                    CoroutineScope(Dispatchers.IO).launch {
                        val settingDao = settingDaoProvider.get()
                        val initialSettings = mapOf(
                            SettingRepository.KEY_SHOP_NAME to "Vision Point",
                            SettingRepository.KEY_SHOP_ADDRESS to SettingRepository.DEFAULT_SHOP_ADDRESS,
                            SettingRepository.KEY_SHOP_PHONE to "",
                            SettingRepository.KEY_ITEM_TYPES to "[\"Frames\",\"Sunglass\",\"Contact Lens\",\"Eye Testing\",\"Repair\"]",
                            SettingRepository.KEY_EYE_FIELDS to "[\"SPH\",\"CYL\",\"Axis\",\"Add\",\"V/A\"]",
                            SettingRepository.KEY_MSG_BIRTHDAY to SettingRepository.DEFAULT_MSG_BIRTHDAY,
                            SettingRepository.KEY_MSG_RECALL to SettingRepository.DEFAULT_MSG_RECALL,
                            SettingRepository.KEY_MSG_DUE to SettingRepository.DEFAULT_MSG_DUE,
                            SettingRepository.KEY_MSG_STATUS_PENDING to SettingRepository.DEFAULT_MSG_STATUS_PENDING,
                            SettingRepository.KEY_MSG_STATUS_READY to SettingRepository.DEFAULT_MSG_STATUS_READY,
                            SettingRepository.KEY_MSG_STATUS_COMPLETED to SettingRepository.DEFAULT_MSG_STATUS_COMPLETED,
                            SettingRepository.KEY_NOTIF_ENABLED to "true",
                            SettingRepository.KEY_NOTIF_RECALLS to "true",
                            SettingRepository.KEY_NOTIF_BIRTHDAYS to "true",
                            SettingRepository.KEY_NOTIF_DUES to "true",
                            SettingRepository.KEY_NOTIF_TIME to "08:00"
                        )
                        for ((key, value) in initialSettings) {
                            settingDao.upsert(com.visionpoint.crm.data.model.Setting(key = key, value = value))
                        }
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideCustomerDao(database: VisionPointDatabase): CustomerDao = database.customerDao()

    @Provides
    fun providePrescriptionDao(database: VisionPointDatabase): PrescriptionDao = database.prescriptionDao()

    @Provides
    fun provideSaleDao(database: VisionPointDatabase): SaleDao = database.saleDao()

    @Provides
    fun provideSaleItemDao(database: VisionPointDatabase): SaleItemDao = database.saleItemDao()

    @Provides
    fun providePaymentDao(database: VisionPointDatabase): PaymentDao = database.paymentDao()

    @Provides
    fun provideCashbookDao(database: VisionPointDatabase): CashbookDao = database.cashbookDao()

    @Provides
    fun provideSettingDao(database: VisionPointDatabase): SettingDao = database.settingDao()
}
