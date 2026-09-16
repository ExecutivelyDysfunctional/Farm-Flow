package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.viewmodel.FarmRpgViewModel
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    viewModel: FarmRpgViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Remember WebView across recompositions to prevent destruction of state
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            // Modern, secure, and robust settings
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                useWideViewPort = true
                loadWithOverviewMode = true
                supportZoom()
                builtInZoomControls = true
                displayZoomControls = false
                
                // Allow cookies and media playback without gestures
                mediaPlaybackRequiresUserGesture = false
            }

            // Cookie management
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        }
    }

    // Set custom user agent when it changes
    LaunchedEffect(viewModel.customUserAgent) {
        viewModel.customUserAgent.collectLatest { ua ->
            if (ua.isNotEmpty()) {
                webView.settings.userAgentString = ua
            }
        }
    }

    // Listen to JS Injection triggers
    LaunchedEffect(viewModel.jsExecutionFlow) {
        viewModel.jsExecutionFlow.collect { js ->
            webView.evaluateJavascript(js, null)
        }
    }

    // Listen to explicit Navigation triggers
    LaunchedEffect(viewModel.navigateFlow) {
        viewModel.navigateFlow.collect { url ->
            when (url) {
                "BACK" -> if (webView.canGoBack()) webView.goBack()
                "FORWARD" -> if (webView.canGoForward()) webView.goForward()
                else -> webView.loadUrl(url)
            }
        }
    }

    // Configure Clients and JS Bridge
    DisposableEffect(webView) {
        // Setup clients
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { viewModel.onPageStarted(it) }
                viewModel.updateNavigationState(webView.canGoBack(), webView.canGoForward())
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { viewModel.onPageFinished(it, webView.title) }
                viewModel.updateNavigationState(webView.canGoBack(), webView.canGoForward())
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                viewModel.onProgressChanged(newProgress)
            }
        }

        // JS Bridge Object
        class WebAppInterface {
            @JavascriptInterface
            fun postMessage(message: String, type: String) {
                val currentMacro = viewModel.runningMacro.value
                val macroId = currentMacro?.id ?: 0
                val macroName = currentMacro?.name ?: "System"
                viewModel.logEvent(macroId, macroName, message, type)
            }
        }

        webView.addJavascriptInterface(WebAppInterface(), "AndroidMacroRunner")

        // Initial Load
        webView.loadUrl(viewModel.currentUrl.value)

        onDispose {
            // Safe teardown
            webView.removeJavascriptInterface("AndroidMacroRunner")
            webView.stopLoading()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}
