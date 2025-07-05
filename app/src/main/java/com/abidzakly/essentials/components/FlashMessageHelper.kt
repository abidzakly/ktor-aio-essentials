package com.abidzakly.essentials.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Define colors inline to avoid external dependencies
private val AppBrightRed = Color(0xFFFF0000)
private val AppGreenSuccess = Color(0xFF12C178)

/**
 * Creates and remembers a SnackbarHostState for managing flash messages.
 */
@Composable
fun rememberFlashMessageState(): SnackbarHostState {
    return remember { SnackbarHostState() }
}

/**
 * Displays flash messages with custom styling and positioning.
 *
 * @param snackbarHostState The SnackbarHostState to observe for messages
 */
@Composable
fun FlashMessageHost(
    snackbarHostState: SnackbarHostState,
) {
    SnackbarHost(hostState = snackbarHostState) { data ->
        val backgroundColor = when (data.visuals.actionLabel) {
            "success" -> AppGreenSuccess
            "error" -> AppBrightRed
            else -> Color.DarkGray
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 50.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Snackbar(
                modifier = Modifier
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = backgroundColor,
                contentColor = Color.White,
            ) {
                Text(
                    text = data.visuals.message,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * BEST PRACTICE: Top-level functions for utility operations
 * Shows an error flash message with red background.
 */
fun showErrorMessage(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    message: String,
    onDismiss: () -> Unit = {},
) {
    showFlashMessage(snackbarHostState, coroutineScope, message, "error", onDismiss)
}

/**
 * Shows a success flash message with green background.
 */
fun showSuccessMessage(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    message: String,
    onDismiss: () -> Unit = {},
) {
    showFlashMessage(snackbarHostState, coroutineScope, message, "success", onDismiss)
}

/**
 * BEST PRACTICE: Private implementation details as top-level private functions
 * Shows a generic flash message with custom type.
 */
private fun showFlashMessage(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    message: String,
    type: String,
    onDismiss: () -> Unit = {},
) {
    coroutineScope.launch {
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = type,
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.Dismissed || result == SnackbarResult.ActionPerformed) {
            onDismiss()
        }
    }
}

/**
 * BEST PRACTICE: Extension functions for more idiomatic Kotlin
 * Extension function to show error message directly from SnackbarHostState
 */
fun SnackbarHostState.showError(
    coroutineScope: CoroutineScope,
    message: String,
    onDismiss: () -> Unit = {}
) {
    showErrorMessage(this, coroutineScope, message, onDismiss)
}

/**
 * Extension function to show success message directly from SnackbarHostState
 */
fun SnackbarHostState.showSuccess(
    coroutineScope: CoroutineScope,
    message: String,
    onDismiss: () -> Unit = {}
) {
    showSuccessMessage(this, coroutineScope, message, onDismiss)
}

// =====================================
// ALTERNATIVE APPROACHES (Choose one):
// =====================================

/**
 * ALTERNATIVE 1: Object declaration (Singleton pattern)
 * Use when you need to maintain state or have complex initialization
 */
object FlashMessageManager {
    private val defaultColors = mapOf(
        "success" to AppGreenSuccess,
        "error" to AppBrightRed,
        "info" to Color.Blue,
        "warning" to Color(0xFFFF9800)
    )

    fun getColorForType(type: String): Color {
        return defaultColors[type] ?: Color.DarkGray
    }

    @Composable
    fun HostWithTheme(
        snackbarHostState: SnackbarHostState,
        customColors: Map<String, Color> = emptyMap()
    ) {
        val colors = defaultColors + customColors

        SnackbarHost(hostState = snackbarHostState) { data ->
            val backgroundColor = colors[data.visuals.actionLabel] ?: Color.DarkGray

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = 50.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Snackbar(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    containerColor = backgroundColor,
                    contentColor = Color.White,
                ) {
                    Text(
                        text = data.visuals.message,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * ALTERNATIVE 2: Data class approach for configuration
 * Use when you want to provide customizable configurations
 */
data class FlashMessageConfig(
    val successColor: Color = AppGreenSuccess,
    val errorColor: Color = AppBrightRed,
    val infoColor: Color = Color.Blue,
    val warningColor: Color = Color(0xFFFF9800),
    val defaultColor: Color = Color.DarkGray,
    val cornerRadius: Int = 12,
    val fontSize: Int = 14,
    val topOffset: Int = 50
)

@Composable
fun ConfigurableFlashMessageHost(
    snackbarHostState: SnackbarHostState,
    config: FlashMessageConfig = FlashMessageConfig()
) {
    SnackbarHost(hostState = snackbarHostState) { data ->
        val backgroundColor = when (data.visuals.actionLabel) {
            "success" -> config.successColor
            "error" -> config.errorColor
            "info" -> config.infoColor
            "warning" -> config.warningColor
            else -> config.defaultColor
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = config.topOffset.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Snackbar(
                modifier = Modifier
                    .padding(12.dp)
                    .clip(RoundedCornerShape(config.cornerRadius.dp)),
                containerColor = backgroundColor,
                contentColor = Color.White,
            ) {
                Text(
                    text = data.visuals.message,
                    fontSize = config.fontSize.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * USAGE EXAMPLES:
 *
 * // OPTION 1: Top-level functions (RECOMMENDED)
 * @Composable
 * fun MyScreen() {
 *     val snackbarHostState = rememberFlashMessageState()
 *     val coroutineScope = rememberCoroutineScope()
 *
 *     Column {
 *         Button(onClick = {
 *             snackbarHostState.showSuccess(coroutineScope, "Success!")
 *         }) {
 *             Text("Show Success")
 *         }
 *
 *         Button(onClick = {
 *             showErrorMessage(snackbarHostState, coroutineScope, "Error!")
 *         }) {
 *             Text("Show Error")
 *         }
 *
 *         FlashMessageHost(snackbarHostState)
 *     }
 * }
 *
 * // OPTION 2: Object approach
 * @Composable
 * fun MyScreenWithObject() {
 *     val snackbarHostState = rememberFlashMessageState()
 *
 *     Column {
 *         // Your content
 *         FlashMessageManager.HostWithTheme(
 *             snackbarHostState = snackbarHostState,
 *             customColors = mapOf("custom" to Color.Purple)
 *         )
 *     }
 * }
 *
 * // OPTION 3: Configurable approach
 * @Composable
 * fun MyScreenWithConfig() {
 *     val snackbarHostState = rememberFlashMessageState()
 *     val config = FlashMessageConfig(
 *         successColor = Color.Green,
 *         cornerRadius = 16,
 *         fontSize = 16
 *     )
 *
 *     Column {
 *         // Your content
 *         ConfigurableFlashMessageHost(snackbarHostState, config)
 *     }
 * }
 */