package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FavoriteShortcut
import com.example.data.model.Macro
import com.example.data.model.MacroLog
import com.example.data.model.MacroStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Macro::class, MacroLog::class, FavoriteShortcut::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun macroDao(): MacroDao
    abstract fun macroLogDao(): MacroLogDao
    abstract fun shortcutDao(): ShortcutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "farmrpg_helper_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Populate default macros and shortcuts in a background coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    populateDefaults(database)
                }
            }

            private suspend fun populateDefaults(db: AppDatabase) {
                // Check if already populated
                if (db.macroDao().getMacroCount() == 0) {
                    val defaultMacros = listOf(
                        Macro(
                            name = "Auto-Explorer",
                            description = "Repeatedly taps the 'Explore' action buttons to automatically search zones.",
                            intervalMs = 600,
                            isSystemBuiltIn = true,
                            steps = listOf(
                                MacroStep("click_selector", ".btn-explore", "", 0),
                                MacroStep("click_text", "Explore", "", 1),
                                MacroStep("click_text", "Explore Again", "", 2)
                            )
                        ),
                        Macro(
                            name = "Auto-Fisher",
                            description = "Regularly triggers fishing actions to help catch fish.",
                            intervalMs = 1200,
                            isSystemBuiltIn = true,
                            steps = listOf(
                                MacroStep("click_selector", ".fish-btn", "", 0),
                                MacroStep("click_text", "Cast Line", "", 1),
                                MacroStep("click_text", "Fish Again", "", 2)
                            )
                        ),
                        Macro(
                            name = "Auto-Craft Planks",
                            description = "Performs automated plank crafting in the workshop by clicking craft buttons.",
                            intervalMs = 1500,
                            isSystemBuiltIn = true,
                            steps = listOf(
                                MacroStep("click_text", "Craft Wood Plank", "", 0),
                                MacroStep("click_selector", ".btn-craft", "", 1)
                            )
                        ),
                        Macro(
                            name = "Quick Harvest & Plant",
                            description = "Quickly harvests your crops and replants seeds automatically in one go.",
                            intervalMs = 1000,
                            isSystemBuiltIn = true,
                            steps = listOf(
                                MacroStep("click_text", "Harvest All", "", 0),
                                MacroStep("wait", "", "1000", 1),
                                MacroStep("click_text", "Plant All", "", 2)
                            )
                        )
                    )
                    defaultMacros.forEach { db.macroDao().insertMacro(it) }
                }

                if (db.shortcutDao().getShortcutCount() == 0) {
                    val defaultShortcuts = listOf(
                        FavoriteShortcut(title = "Home / Farm", url = "https://farmrpg.com/index.php", iconName = "home", order = 0),
                        FavoriteShortcut(title = "Workshop", url = "https://farmrpg.com/workshop.php", iconName = "workshop", order = 1),
                        FavoriteShortcut(title = "Explore Areas", url = "https://farmrpg.com/areas.php", iconName = "explore", order = 2),
                        FavoriteShortcut(title = "Fishing spots", url = "https://farmrpg.com/fishing.php", iconName = "fishing", order = 3),
                        FavoriteShortcut(title = "Town & Market", url = "https://farmrpg.com/town.php", iconName = "town", order = 4)
                    )
                    defaultShortcuts.forEach { db.shortcutDao().insertShortcut(it) }
                }
            }
        }
    }
}
