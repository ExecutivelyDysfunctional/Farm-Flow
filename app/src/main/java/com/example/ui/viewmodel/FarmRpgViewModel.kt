package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.PowerManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.FavoriteShortcut
import com.example.data.model.Macro
import com.example.data.model.MacroLog
import com.example.data.model.MacroStep
import com.example.data.repository.MacroRepository
import com.example.data.repository.ShortcutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FarmRpgViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val macroRepository = MacroRepository(database.macroDao(), database.macroLogDao())
    private val shortcutRepository = ShortcutRepository(database.shortcutDao())

    // Database UI States
    val macros: StateFlow<List<Macro>> = macroRepository.allMacros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortcuts: StateFlow<List<FavoriteShortcut>> = shortcutRepository.allShortcuts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<MacroLog>> = macroRepository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // WebView State
    private val _currentUrl = MutableStateFlow("https://farmrpg.com/index.php")
    val currentUrl = _currentUrl.asStateFlow()

    private val _webViewTitle = MutableStateFlow("Farm RPG")
    val webViewTitle = _webViewTitle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress = _loadingProgress.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward = _canGoForward.asStateFlow()

    // Macro Engine State
    private val _runningMacro = MutableStateFlow<Macro?>(null)
    val runningMacro = _runningMacro.asStateFlow()

    private val _isMacroRunning = MutableStateFlow(false)
    val isMacroRunning = _isMacroRunning.asStateFlow()

    private val _macroExecutionCount = MutableStateFlow(0)
    val macroExecutionCount = _macroExecutionCount.asStateFlow()

    private val _lastActionLog = MutableStateFlow("No macro running")
    val lastActionLog = _lastActionLog.asStateFlow()

    // Settings
    private val sharedPrefs = application.getSharedPreferences("farmrpg_prefs", Context.MODE_PRIVATE)
    
    private val _isWakeLockEnabled = MutableStateFlow(sharedPrefs.getBoolean("wake_lock", true))
    val isWakeLockEnabled = _isWakeLockEnabled.asStateFlow()

    private val _customUserAgent = MutableStateFlow(
        sharedPrefs.getString(
            "user_agent",
            "Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
        ) ?: ""
    )
    val customUserAgent = _customUserAgent.asStateFlow()

    // Channel for Web View Communication
    private val _jsExecutionChannel = Channel<String>(Channel.BUFFERED)
    val jsExecutionFlow = _jsExecutionChannel.receiveAsFlow()

    private val _navigateChannel = Channel<String>(Channel.BUFFERED)
    val navigateFlow = _navigateChannel.receiveAsFlow()

    // Background job for Macro loop
    private var macroJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    init {
        acquireOrReleaseWakeLock()
    }

    // WebView Callbacks
    fun onPageStarted(url: String) {
        _currentUrl.value = url
        _isLoading.value = true
    }

    fun onPageFinished(url: String, title: String?) {
        _currentUrl.value = url
        _isLoading.value = false
        if (!title.isNullOrEmpty() && !title.contains("farmrpg", ignoreCase = true)) {
            _webViewTitle.value = title
        } else {
            _webViewTitle.value = "Farm RPG"
        }
    }

    fun onProgressChanged(progress: Int) {
        _loadingProgress.value = progress
        _isLoading.value = progress < 100
    }

    fun updateNavigationState(canBack: Boolean, canForward: Boolean) {
        _canGoBack.value = canBack
        _canGoForward.value = canForward
    }

    // Navigation triggers
    fun navigateTo(url: String) {
        viewModelScope.launch {
            _navigateChannel.send(url)
        }
    }

    fun triggerReload() {
        viewModelScope.launch {
            _jsExecutionChannel.send("location.reload();")
        }
    }

    // Settings adjustments
    fun setWakeLockEnabled(enabled: Boolean) {
        _isWakeLockEnabled.value = enabled
        sharedPrefs.edit().putBoolean("wake_lock", enabled).apply()
        acquireOrReleaseWakeLock()
    }

    fun setCustomUserAgent(ua: String) {
        _customUserAgent.value = ua
        sharedPrefs.edit().putString("user_agent", ua).apply()
    }

    private fun acquireOrReleaseWakeLock() {
        try {
            val powerManager = getApplication<Application>().getSystemService(Context.POWER_SERVICE) as PowerManager
            if (_isWakeLockEnabled.value) {
                if (wakeLock == null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "FarmRPGCompanion::WakeLock")
                }
                if (wakeLock?.isHeld == false) {
                    wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
                }
            } else {
                if (wakeLock?.isHeld == true) {
                    wakeLock?.release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Macro Engine Operations
    fun startMacro(macro: Macro) {
        stopRunningMacro()

        _runningMacro.value = macro
        _isMacroRunning.value = true
        _macroExecutionCount.value = 0
        _lastActionLog.value = "Starting ${macro.name}..."

        logEvent(macro.id, macro.name, "Starting macro execution", "INFO")

        macroJob = viewModelScope.launch(Dispatchers.Main) {
            acquireOrReleaseWakeLock() // ensure screen remains dim/on during automation
            
            while (isMacroRunning.value) {
                val currentExecutionIndex = _macroExecutionCount.value + 1
                logEvent(macro.id, macro.name, "Running iteration #$currentExecutionIndex", "INFO")

                // Sort steps by order index
                val steps = macro.steps.sortedBy { it.order }
                if (steps.isEmpty()) {
                    logEvent(macro.id, macro.name, "No steps found in macro configuration.", "WARNING")
                    stopRunningMacro()
                    break
                }

                for (step in steps) {
                    if (!isMacroRunning.value) break

                    val jsCode = generateJsForStep(step)
                    if (jsCode.isNotEmpty()) {
                        _jsExecutionChannel.send(jsCode)
                        val stepDescription = when (step.type) {
                            "click_selector" -> "Tapping element matching selector: '${step.target}'"
                            "click_text" -> "Tapping button containing: '${step.target}'"
                            "input" -> "Entering '${step.value}' into '${step.target}'"
                            "scroll" -> "Scrolling by '${step.value}'px"
                            "refresh" -> "Refreshing page"
                            "wait" -> "Waiting"
                            else -> "Running standard action"
                        }
                        
                        _lastActionLog.value = stepDescription
                        logEvent(macro.id, macro.name, "Step ${step.order + 1}: $stepDescription", "INFO")
                    }

                    // Handle individual step wait delay if specified
                    val stepDelay = if (step.type == "wait") {
                        step.value.toLongOrNull() ?: macro.intervalMs
                    } else {
                        macro.intervalMs
                    }
                    delay(stepDelay)
                }

                _macroExecutionCount.value = currentExecutionIndex
                
                // If max repeats set and reached
                if (macro.repeatCount > 0 && currentExecutionIndex >= macro.repeatCount) {
                    logEvent(macro.id, macro.name, "Completed requested repetition target of ${macro.repeatCount}.", "SUCCESS")
                    _lastActionLog.value = "Finished ${macro.repeatCount} iterations!"
                    stopRunningMacro()
                    break
                }

                // Normal inter-iteration delay
                delay(macro.intervalMs)
            }
        }
    }

    fun stopRunningMacro() {
        macroJob?.cancel()
        macroJob = null
        val macro = _runningMacro.value
        if (macro != null) {
            logEvent(macro.id, macro.name, "Stopped macro execution after ${_macroExecutionCount.value} iterations", "WARNING")
        }
        _runningMacro.value = null
        _isMacroRunning.value = false
        _lastActionLog.value = "Macro stopped"
        
        // Release wake lock if held and no macro is running
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    private fun generateJsForStep(step: MacroStep): String {
        return when (step.type) {
            "click_selector" -> {
                """
                (function() {
                    try {
                        var selector = "${step.target.replace("\"", "\\\"")}";
                        var el = document.querySelector(selector);
                        if (el) {
                            el.click();
                            AndroidMacroRunner.postMessage("Found and tapped " + selector, "SUCCESS");
                            return true;
                        } else {
                            // Try common layout variations
                            var buttons = document.querySelectorAll("a, button, input");
                            for (var i = 0; i < buttons.length; i++) {
                                if (buttons[i].className.indexOf(selector.replace('.', '')) !== -1) {
                                    buttons[i].click();
                                    AndroidMacroRunner.postMessage("Tapped matching class " + selector, "SUCCESS");
                                    return true;
                                }
                            }
                        }
                        return false;
                    } catch(e) {
                        AndroidMacroRunner.postMessage("Error clicking selector: " + e.message, "ERROR");
                        return false;
                    }
                })();
                """.trimIndent()
            }
            "click_text" -> {
                """
                (function() {
                    try {
                        var targetText = "${step.target.replace("\"", "\\\"")}".toLowerCase().trim();
                        var elements = document.querySelectorAll("a, button, input[type='button'], input[type='submit'], .btn, .button, h4, p, span");
                        for (var i = 0; i < elements.length; i++) {
                            var el = elements[i];
                            var textVal = (el.textContent || el.value || "").toLowerCase().trim();
                            if (textVal.indexOf(targetText) !== -1) {
                                el.click();
                                AndroidMacroRunner.postMessage("Found and clicked: '" + targetText + "'", "SUCCESS");
                                return true;
                            }
                        }
                        return false;
                    } catch(e) {
                        AndroidMacroRunner.postMessage("Error in click_text: " + e.message, "ERROR");
                        return false;
                    }
                })();
                """.trimIndent()
            }
            "input" -> {
                """
                (function() {
                    try {
                        var selector = "${step.target.replace("\"", "\\\"")}";
                        var val = "${step.value.replace("\"", "\\\"")}";
                        var el = document.querySelector(selector);
                        if (el) {
                            el.value = val;
                            el.dispatchEvent(new Event('input', { bubbles: true }));
                            el.dispatchEvent(new Event('change', { bubbles: true }));
                            AndroidMacroRunner.postMessage("Set value '" + val + "' on " + selector, "SUCCESS");
                            return true;
                        }
                        return false;
                    } catch(e) {
                        return false;
                    }
                })();
                """.trimIndent()
            }
            "scroll" -> {
                val offset = step.value.toIntOrNull() ?: 200
                "window.scrollBy(0, $offset);"
            }
            "refresh" -> {
                "location.reload();"
            }
            "wait" -> {
                // Done on Kotlin coroutine side
                ""
            }
            else -> ""
        }
    }

    // Logging helpers
    fun logEvent(macroId: Int, macroName: String, message: String, type: String = "INFO") {
        viewModelScope.launch(Dispatchers.IO) {
            macroRepository.addLog(
                MacroLog(
                    macroId = macroId,
                    macroName = macroName,
                    message = message,
                    type = type
                )
            )
        }
    }

    fun clearLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            macroRepository.clearLogs()
        }
    }

    // Macro settings persistence
    fun saveMacro(macro: Macro) {
        viewModelScope.launch(Dispatchers.IO) {
            macroRepository.insertMacro(macro)
        }
    }

    fun deleteMacro(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            macroRepository.deleteMacroById(id)
        }
    }

    // Shortcuts persistence
    fun saveShortcut(shortcut: FavoriteShortcut) {
        viewModelScope.launch(Dispatchers.IO) {
            shortcutRepository.insertShortcut(shortcut)
        }
    }

    fun deleteShortcut(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            shortcutRepository.deleteShortcut(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRunningMacro()
    }
}
