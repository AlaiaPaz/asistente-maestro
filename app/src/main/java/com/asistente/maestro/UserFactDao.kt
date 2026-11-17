package com.asistente.maestro

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserFactDao {
    @Query("SELECT * FROM user_facts")
    suspend fun getAllFacts(): List<UserFactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: UserFactEntity)
}
