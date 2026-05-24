package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String, // "Makanan", "Shopping", "Transportasi", "Gaji", "Investasi", "Lainnya"
    val wallet: String, // "Tunai", "Bank", "E-Wallet"
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val rawText: String? = null // Holds original SMS/text if automatically recorded
)
