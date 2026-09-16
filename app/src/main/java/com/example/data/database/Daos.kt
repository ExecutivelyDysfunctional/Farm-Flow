package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FavoriteShortcut
import com.example.data.model.Macro
import com.example.data.model.MacroLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroDao {
    @Query("SELECT * FROM macros ORDER BY isSystemBuiltIn DESC, name ASC")
    fun getAllMacros(): Flow<List<Macro>>

    @Query("SELECT * FROM macros WHERE id = :id")
    suspend fun getMacroById(id: Int): Macro?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: Macro): Long

    @Update
    suspend fun updateMacro(macro: Macro)

    @Query("DELETE FROM macros WHERE id = :id")
    suspend fun deleteMacroById(id: Int)

    @Query("SELECT COUNT(*) FROM macros")
    suspend fun getMacroCount(): Int
}

@Dao
interface MacroLogDao {
    @Query("SELECT * FROM macro_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<MacroLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MacroLog): Long

    @Query("DELETE FROM macro_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM macro_logs WHERE macroId = :macroId")
    suspend fun clearLogsForMacro(macroId: Int)
}

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM favorite_shortcuts ORDER BY `order` ASC, title ASC")
    fun getAllShortcuts(): Flow<List<FavoriteShortcut>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: FavoriteShortcut): Long

    @Query("DELETE FROM favorite_shortcuts WHERE id = :id")
    suspend fun deleteShortcutById(id: Int)

    @Query("SELECT COUNT(*) FROM favorite_shortcuts")
    suspend fun getShortcutCount(): Int
}
