package com.example.data.repository

import com.example.data.database.MacroDao
import com.example.data.database.MacroLogDao
import com.example.data.database.ShortcutDao
import com.example.data.model.FavoriteShortcut
import com.example.data.model.Macro
import com.example.data.model.MacroLog
import kotlinx.coroutines.flow.Flow

class MacroRepository(
    private val macroDao: MacroDao,
    private val macroLogDao: MacroLogDao
) {
    val allMacros: Flow<List<Macro>> = macroDao.getAllMacros()
    val recentLogs: Flow<List<MacroLog>> = macroLogDao.getRecentLogs()

    suspend fun getMacroById(id: Int): Macro? = macroDao.getMacroById(id)

    suspend fun insertMacro(macro: Macro): Long = macroDao.insertMacro(macro)

    suspend fun updateMacro(macro: Macro) = macroDao.updateMacro(macro)

    suspend fun deleteMacroById(id: Int) = macroDao.deleteMacroById(id)

    suspend fun addLog(log: MacroLog) = macroLogDao.insertLog(log)

    suspend fun clearLogs() = macroLogDao.clearAllLogs()

    suspend fun clearLogsForMacro(macroId: Int) = macroLogDao.clearLogsForMacro(macroId)
}

class ShortcutRepository(
    private val shortcutDao: ShortcutDao
) {
    val allShortcuts: Flow<List<FavoriteShortcut>> = shortcutDao.getAllShortcuts()

    suspend fun insertShortcut(shortcut: FavoriteShortcut): Long = shortcutDao.insertShortcut(shortcut)

    suspend fun deleteShortcut(id: Int) = shortcutDao.deleteShortcutById(id)
}
