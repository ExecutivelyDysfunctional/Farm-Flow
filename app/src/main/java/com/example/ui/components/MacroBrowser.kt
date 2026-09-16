package com.example.ui.components

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.viewmodel.CompanionViewModel

data class NavShortcut(val label: String, val url: String, val icon: String)

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun MacroBrowser(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf("https://farmrpg.com/") }
    var isLoading by remember { mutableStateOf(false) }
    val activeMacro by viewModel.activeMacro.collectAsState()

    val shortcuts = listOf(
        NavShortcut("Farm", "https://farmrpg.com/#!/farm.php", "🌾"),
        NavShortcut("Workshop", "https://farmrpg.com/#!/workshop.php", "🛠️"),
        NavShortcut("Market", "https://farmrpg.com/#!/market.php", "⚖️"),
        NavShortcut("Town", "https://farmrpg.com/#!/town.php", "🏛️"),
        NavShortcut("Fishing", "https://farmrpg.com/#!/fishing.php", "🐟"),
        NavShortcut("Inventory", "https://farmrpg.com/#!/inventory.php", "🎒"),
        NavShortcut("Home", "https://farmrpg.com/index.php", "🏠")
    )

    // Listen to JS Execution Queue from ViewModel
    LaunchedEffect(webViewInstance) {
        if (webViewInstance != null) {
            viewModel.jsExecutionQueue.collect { jsCode ->
                webViewInstance?.post {
                    webViewInstance?.evaluateJavascript(jsCode) { result ->
                        viewModel.onMacroExecuted(result)
                    }
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        
        // Active Macro Running HUD Indicator
        AnimatedVisibility(visible = activeMacro != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f))
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Macro Running: ${activeMacro?.name ?: ""}",
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(
                    onClick = { viewModel.stopRunningMacro() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSecondary)
                ) {
                    Text("STOP", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }

        // Web View Area
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                modifier = Modifier.fillMaxSize().testTag("farm_rpg_webview"),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                isLoading = true
                                url?.let { currentUrl = it }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                url?.let { currentUrl = it }
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                isLoading = newProgress < 100
                            }
                        }
                        
                        // Optimize web settings for FarmRPG
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                            userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36 FarmRPGWrapper/1.0"
                        }

                        // Intercept touch actions to record coordinate parameters for click automation macros
                        setOnTouchListener { v, event ->
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                val density = v.context.resources.displayMetrics.density
                                val touchX = event.x / density
                                val touchY = event.y / density
                                evaluateJavascript("window.lastTouchX = $touchX; window.lastTouchY = $touchY;", null)
                                viewModel.addLog("Set click target spot: (${touchX.toInt()}, ${touchY.toInt()})")
                            }
                            false
                        }

                        loadUrl("https://farmrpg.com/")
                        webViewInstance = this
                    }
                },
                update = { }
            )
            
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }

        // Navigation and Quick-Jump Shortcut Panel
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Browser Standard Controls (Back, Forward, Refresh, URL Display)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { webViewInstance?.goBack() },
                            enabled = webViewInstance?.canGoBack() == true
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = { webViewInstance?.goForward() },
                            enabled = webViewInstance?.canGoForward() == true
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Forward", modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { webViewInstance?.reload() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload", modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    // Tiny URL Display
                    Text(
                        text = currentUrl.replace("https://", "").take(32) + (if (currentUrl.length > 32) "..." else ""),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { webViewInstance?.loadUrl("https://farmrpg.com/") }
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "FarmRPG Home", modifier = Modifier.size(20.dp))
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                // FarmRPG Quick-Jump Shortcuts (Streamlined Navigation)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(shortcuts) { shortcut ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    webViewInstance?.loadUrl(shortcut.url)
                                    viewModel.addLog("Navigated shortcut: ${shortcut.label}")
                                }
                                .padding(vertical = 6.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = shortcut.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = shortcut.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
