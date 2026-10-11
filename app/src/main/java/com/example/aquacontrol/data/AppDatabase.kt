package com.example.aquacontrol.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aquacontrol.data.flushing.local.FlushingDao
import com.example.aquacontrol.data.flushing.local.FlushingEntity
import com.example.aquacontrol.data.temperatura.local.MedicionTemperaturaDao
import com.example.aquacontrol.data.temperatura.local.MedicionTemperaturaEntity

@Database(
    entities = [
        FlushingEntity::class,
        MedicionTemperaturaEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun flushingDao(): FlushingDao

    abstract fun medicionTemperaturaDao(): MedicionTemperaturaDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `mediciones_temperatura` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `lineaId` INTEGER NOT NULL,
                        `temperatura` REAL NOT NULL,
                        `fechaHora` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_mediciones_temperatura_lineaId_fechaHora`
                    ON `mediciones_temperatura` (`lineaId`, `fechaHora`)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE `mediciones_temperatura`
                    ADD COLUMN `origen` TEXT NOT NULL DEFAULT 'MANUAL'
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE `flushing`
                    ADD COLUMN `fotoRuta` TEXT
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aquacontrol.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build()
                    .also { database ->
                        INSTANCE = database
                    }
            }
        }
    }
}