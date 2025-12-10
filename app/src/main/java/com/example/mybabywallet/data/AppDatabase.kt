package com.example.mybabywallet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Modifica tu archivo AppDatabase.kt para que quede así:

@Database(entities = [Transaccion::class, Usuario::class], version = 3, exportSchema = false) // <--- OJO AQUÍ
abstract class AppDatabase : RoomDatabase() {

    abstract fun transaccionDao(): TransaccionDao
    abstract fun usuarioDao(): UsuarioDao // <--- NUEVO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "billetera_db"
                )
                    .fallbackToDestructiveMigration() // <--- ¡ESTA ES LA LÍNEA MÁGICA!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}