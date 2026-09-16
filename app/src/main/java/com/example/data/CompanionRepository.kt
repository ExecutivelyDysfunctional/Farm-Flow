package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CompanionRepository(private val companionDao: CompanionDao) {

    val allMacros: Flow<List<MacroEntity>> = companionDao.getAllMacrosFlow()
    val allTimers: Flow<List<TimerEntity>> = companionDao.getAllTimersFlow()

    suspend fun insertMacro(macro: MacroEntity): Long = companionDao.insertMacro(macro)

    suspend fun deleteMacro(macro: MacroEntity) = companionDao.deleteMacro(macro)

    suspend fun getMacroById(id: Int): MacroEntity? = companionDao.getMacroById(id)

    suspend fun insertTimer(timer: TimerEntity): Long = companionDao.insertTimer(timer)

    suspend fun deleteTimer(timer: TimerEntity) = companionDao.deleteTimer(timer)

    suspend fun deleteTimerById(id: Int) = companionDao.deleteTimerById(id)

    suspend fun getTimerById(id: Int): TimerEntity? = companionDao.getTimerById(id)

    suspend fun prepopulateMacros() {
        val existing = allMacros.first()
        if (existing.isEmpty()) {
            val defaults = listOf(
                MacroEntity(
                    name = "Auto-Explore",
                    type = "JS_INJECT",
                    jsCode = """
                        (function() {
                            var buttons = document.querySelectorAll('a.btn-explore, button.btn-explore, a.explore-btn, button.explore-btn, .explore-again, a[href*="explore.php"]');
                            if (buttons.length > 0) {
                                buttons[0].click();
                                return "Clicked explore button!";
                            }
                            var allA = document.querySelectorAll('a, button');
                            for (var i = 0; i < allA.length; i++) {
                                if (allA[i].textContent.includes('Explore Again') || allA[i].textContent.includes('Explore')) {
                                    allA[i].click();
                                    return "Clicked text button: " + allA[i].textContent;
                                }
                            }
                            return "Looking for explore buttons...";
                        })();
                    """.trimIndent(),
                    intervalMs = 1200L,
                    isPredefined = true,
                    description = "Repeatedly searches for and clicks the 'Explore' or 'Explore Again' button in any adventure area."
                ),
                MacroEntity(
                    name = "Auto-Fish (Water Tapper)",
                    type = "JS_INJECT",
                    jsCode = """
                        (function() {
                            var canvas = document.querySelector('canvas, .fishing-area, #fish-area, .fishing-bobber');
                            if (canvas) {
                                var rect = canvas.getBoundingClientRect();
                                var x = rect.left + rect.width / 2;
                                var y = rect.top + rect.height / 2;
                                var clickEvent = new MouseEvent('click', {
                                    clientX: x,
                                    clientY: y,
                                    bubbles: true,
                                    cancelable: true
                                });
                                canvas.dispatchEvent(clickEvent);
                                return "Cast net or clicked water center!";
                            }
                            var fishBtn = document.querySelector('.fish-btn, button[class*="fish"]');
                            if (fishBtn) {
                                fishBtn.click();
                                return "Clicked fish button";
                            }
                            return "Open the Fishing screen to auto-tap water.";
                        })();
                    """.trimIndent(),
                    intervalMs = 1500L,
                    isPredefined = true,
                    description = "Simulates touch actions in the fishing canvas or fish bobber to automate catching."
                ),
                MacroEntity(
                    name = "Custom Screen-Spot Tapper",
                    type = "AUTO_CLICKER",
                    jsCode = """
                        (function() {
                            var x = window.lastTouchX || (window.innerWidth / 2);
                            var y = window.lastTouchY || (window.innerHeight / 2);
                            var elem = document.elementFromPoint(x, y);
                            if (elem) {
                                var clickEvent = new MouseEvent('click', {
                                    clientX: x,
                                    clientY: y,
                                    bubbles: true,
                                    cancelable: true
                                });
                                elem.dispatchEvent(clickEvent);
                                return "Clicked spot (" + Math.round(x) + ", " + Math.round(y) + ")";
                            }
                            return "No element at last clicked spot.";
                        })();
                    """.trimIndent(),
                    intervalMs = 1000L,
                    isPredefined = true,
                    description = "Click anywhere on the web game once to set target, and this macro will continuously tap that exact coordinate."
                ),
                MacroEntity(
                    name = "Quick-Craft Helper",
                    type = "JS_INJECT",
                    jsCode = """
                        (function() {
                            var craftBtn = document.querySelector('.btn-craft, button.craft-btn, a[href*="craft"], button[id*="craft"]');
                            if (craftBtn) {
                                craftBtn.click();
                                return "Clicked craft button!";
                            }
                            var craftMax = document.querySelector('input[value="Craft Max"], button[class*="craft-max"], .craft-max-btn');
                            if (craftMax) {
                                craftMax.click();
                                return "Clicked Craft Max!";
                            }
                            return "Open a workshop recipe to automate crafting.";
                        })();
                    """.trimIndent(),
                    intervalMs = 2000L,
                    isPredefined = true,
                    description = "Automates repetitive tapping of craft buttons or 'Craft Max' inside the Workshop."
                )
            )
            companionDao.insertMacros(defaults)
        }
    }
}
