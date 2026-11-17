package com.tudominio.asistente.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface UserFactDao {
    @Upsert
    suspend fun upsert(fact: UserFactEntity)

    @Query("SELECT * FROM user_facts WHERE factKey = :key LIMIT 1")
    suspend fun getByKey(key: String): UserFactEntity?

    @Query("SELECT * FROM user_facts")
    suspend fun getAll(): List<UserFactEntity>

    @Query("SELECT COUNT(*) FROM user_facts WHERE isSynced = 0")
    suspend fun countUnsynced(): Int
}
