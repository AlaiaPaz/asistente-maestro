package com.tudominio.asistente.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_facts")
data class UserFactEntity(
    @PrimaryKey
    val factKey: String,
    val factValue: String,
    val category: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
