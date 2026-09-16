package com.example.ui.viewmodel

import android.app.Application
import android.media.RingtoneManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Recipe Model
data class CraftingRecipe(
    val name: String,
    val ingredients: Map<String, Int>, // "Wood", "Iron", "Stone", "Clay", "Straw", "Feather"
    val sellPrice: Int,
    val description: String
)

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CompanionDatabase.getDatabase(application)
    private val repository = CompanionRepository(database.companionDao())

    // --- State: Database Collections ---
    val macros: StateFlow<List<MacroEntity>> = repository.allMacros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbTimers: StateFlow<List<TimerEntity>> = repository.allTimers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State: Live UI Timers (updated in tick loop) ---
    private val _liveTimers = MutableStateFlow<List<LiveTimerState>>(emptyList())
    val liveTimers: StateFlow<List<LiveTimerState>> = _liveTimers.asStateFlow()

    // --- State: Active Macro Execution ---
    private val _activeMacro = MutableStateFlow<MacroEntity?>(null)
    val activeMacro: StateFlow<MacroEntity?> = _activeMacro.asStateFlow()

    private val _macroIntervalMs = MutableStateFlow(1000L)
    val macroIntervalMs: StateFlow<Long> = _macroIntervalMs.asStateFlow()

    private val _macroLogs = MutableStateFlow<List<String>>(listOf("System Ready."))
    val macroLogs: StateFlow<List<String>> = _macroLogs.asStateFlow()

    // Channel to push JavaScript scripts to the WebView
    private val _jsExecutionQueue = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val jsExecutionQueue: SharedFlow<String> = _jsExecutionQueue.asSharedFlow()

    private var macroJob: Job? = null

    // --- State: Crafting Calculator ---
    // Standard recipe listings in FarmRPG
    val recipes = listOf(
        CraftingRecipe("Wood Shield", mapOf("Wood" to 4, "Iron" to 2), 400, "Excellent starter shield, highly profitable early on."),
        CraftingRecipe("Iron Ring", mapOf("Iron" to 1, "Stone" to 3), 350, "A standard market item, quick to gather."),
        CraftingRecipe("Fancy Pipe", mapOf("Wood" to 5, "Iron" to 3, "Clay" to 2), 1200, "Premium merchant item with high silver returns."),
        CraftingRecipe("Wooden Sword", mapOf("Wood" to 6, "Stone" to 1), 380, "Classic martial equipment with easy crafting materials."),
        CraftingRecipe("Birdhouse", mapOf("Wood" to 10, "Straw" to 5, "Feather" to 2), 1800, "Advanced craft, premium utility decoration."),
        CraftingRecipe("Iron Spade", mapOf("Wood" to 8, "Iron" to 5), 900, "Sturdy digging equipment, sells well in Town.")
    )

    // User inventories: "Wood", "Iron", "Stone", "Clay", "Straw", "Feather"
    private val _userInventory = MutableStateFlow(
        mapOf("Wood" to 20, "Iron" to 10, "Stone" to 15, "Clay" to 5, "Straw" to 8, "Feather" to 2)
    )
    val userInventory: StateFlow<Map<String, Int>> = _userInventory.asStateFlow()

    init {
        // Populate default macros on first start
        viewModelScope.launch {
            repository.prepopulateMacros()
        }
        
        // Start the background tick loop for active crop timers and running macros
        startTimerTickLoop()
    }

    // --- Macro Management ---
    fun toggleMacro(macro: MacroEntity) {
        if (_activeMacro.value?.id == macro.id) {
            stopRunningMacro()
        } else {
            startRunningMacro(macro)
        }
    }

    fun startRunningMacro(macro: MacroEntity) {
        stopRunningMacro()
        _activeMacro.value = macro
        _macroIntervalMs.value = macro.intervalMs
        addLog("Starting Macro [${macro.name}] at ${macro.intervalMs}ms intervals...")
        
        macroJob = viewModelScope.launch {
            while (true) {
                delay(_macroIntervalMs.value)
                _jsExecutionQueue.emit(macro.jsCode)
            }
        }
    }

    fun stopRunningMacro() {
        macroJob?.cancel()
        macroJob = null
        val old = _activeMacro.value
        _activeMacro.value = null
        if (old != null) {
            addLog("Stopped Macro [${old.name}].")
        }
    }

    fun updateMacroInterval(ms: Long) {
        _macroIntervalMs.value = ms.coerceAtLeast(200L)
        _activeMacro.value?.let { active ->
            // Restart with new interval
            startRunningMacro(active.copy(intervalMs = _macroIntervalMs.value))
        }
    }

    fun addLog(message: String) {
        val current = _macroLogs.value.toMutableList()
        current.add(0, "[${System.currentTimeMillis().toTimeString()}] $message")
        if (current.size > 50) current.removeAt(current.size - 1)
        _macroLogs.value = current
    }

    fun onMacroExecuted(result: String?) {
        val cleanResult = result?.removeSurrounding("\"")?.removeSurrounding("'") ?: "Done."
        if (cleanResult != "null" && cleanResult.isNotBlank()) {
            addLog("Result: $cleanResult")
        }
    }

    fun addNewMacro(name: String, jsCode: String, intervalMs: Long, description: String) {
        viewModelScope.launch {
            repository.insertMacro(
                MacroEntity(
                    name = name,
                    type = "JS_INJECT",
                    jsCode = jsCode,
                    intervalMs = intervalMs,
                    description = description,
                    isPredefined = false
                )
            )
            addLog("Created custom macro: $name")
        }
    }

    fun deleteMacro(macro: MacroEntity) {
        if (_activeMacro.value?.id == macro.id) {
            stopRunningMacro()
        }
        viewModelScope.launch {
            repository.deleteMacro(macro)
            addLog("Deleted macro: ${macro.name}")
        }
    }

    // --- Timer Management ---
    fun addTimer(label: String, durationSeconds: Long, category: String) {
        viewModelScope.launch {
            val target = System.currentTimeMillis() + (durationSeconds * 1000)
            repository.insertTimer(
                TimerEntity(
                    label = label,
                    totalDurationSeconds = durationSeconds,
                    targetTimestamp = target,
                    category = category
                )
            )
            addLog("Set Timer [$label] for ${durationSeconds.toDurationString()}")
        }
    }

    fun deleteTimer(timerId: Int) {
        viewModelScope.launch {
            repository.deleteTimerById(timerId)
        }
    }

    // --- Calculator Management ---
    fun updateInventory(item: String, amount: Int) {
        val current = _userInventory.value.toMutableMap()
        current[item] = amount.coerceAtLeast(0)
        _userInventory.value = current
    }

    // --- Background Tick Loop for Timers ---
    private fun startTimerTickLoop() {
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val activeDbTimers = dbTimers.value
                
                val updatedLiveList = activeDbTimers.map { timer ->
                    val timeLeftMs = timer.targetTimestamp - now
                    val timeLeftSeconds = (timeLeftMs / 1000).coerceAtLeast(0)
                    val progress = if (timer.totalDurationSeconds > 0) {
                        (timeLeftSeconds.toFloat() / timer.totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    
                    val isExpired = timeLeftSeconds <= 0 && timer.isActive
                    if (isExpired) {
                        // Mark completion / Trigger Sound
                        triggerTimerSound()
                        // Deactivate timer
                        viewModelScope.launch {
                            repository.insertTimer(timer.copy(isActive = false))
                        }
                    }
                    
                    LiveTimerState(
                        id = timer.id,
                        label = timer.label,
                        category = timer.category,
                        totalSeconds = timer.totalDurationSeconds,
                        secondsLeft = timeLeftSeconds,
                        progress = progress,
                        isActive = timer.isActive && timeLeftSeconds > 0
                    )
                }
                
                _liveTimers.value = updatedLiveList
                delay(1000L)
            }
        }
    }

    private fun triggerTimerSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(getApplication<Application>().applicationContext, notification)
            ringtone?.play()
            addLog("Alarm! A timer has finished.")
        } catch (e: Exception) {
            addLog("Alarm triggered (sound failed)")
        }
    }

    // Extent Helpers
    private fun Long.toTimeString(): String {
        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(java.util.Date(this))
    }

    private fun Long.toDurationString(): String {
        val h = this / 3600
        val m = (this % 3600) / 60
        val s = this % 60
        return if (h > 0) {
            String.format("%dh %dm %ds", h, m, s)
        } else if (m > 0) {
            String.format("%dm %ds", m, s)
        } else {
            String.format("%ds", s)
        }
    }
}

// UI State for Live Countdown Progress
data class LiveTimerState(
    val id: Int,
    val label: String,
    val category: String,
    val totalSeconds: Long,
    val secondsLeft: Long,
    val progress: Float, // 1f down to 0f
    val isActive: Boolean
)
