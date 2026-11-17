package com.asistente.maestro

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_facts")
data class UserFactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val value: String
)
