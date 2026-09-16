package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanionDao {
    // Macro Operations
    @Query("SELECT * FROM macros ORDER BY isPredefined DESC, name ASC")
    fun getAllMacrosFlow(): Flow<List<MacroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MacroEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacros(macros: List<MacroEntity>)

    @Delete
    suspend fun deleteMacro(macro: MacroEntity)

    @Query("SELECT * FROM macros WHERE id = :id")
    suspend fun getMacroById(id: Int): MacroEntity?

    @Query("DELETE FROM macros WHERE isPredefined = 0")
    suspend fun clearUserMacros()

    // Timer Operations
    @Query("SELECT * FROM timers ORDER BY targetTimestamp ASC")
    fun getAllTimersFlow(): Flow<List<TimerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: TimerEntity): Long

    @Delete
    suspend fun deleteTimer(timer: TimerEntity)

    @Query("DELETE FROM timers WHERE id = :id")
    suspend fun deleteTimerById(id: Int)

    @Query("SELECT * FROM timers WHERE id = :id")
    suspend fun getTimerById(id: Int): TimerEntity?
}
