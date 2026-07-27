package com.baijiahu.test.age.signal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private companion object {
        const val DISABLED_ALPHA = 0.4f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.tvTitle).text =
            "${getString(R.string.app_name)} (SDK ${BuildConfig.AGE_SIGNALS_VERSION})"

        val rootScroll = findViewById<View>(R.id.rootScroll)
        val basePadding = rootScroll.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(rootScroll) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = basePadding + bars.left,
                top = basePadding + bars.top,
                right = basePadding + bars.right,
                bottom = basePadding + bars.bottom
            )
            insets
        }

        val btnRequestAccess = findViewById<Button>(R.id.btnRequestAccess)
        val btnFetch = findViewById<Button>(R.id.btnFetch)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvLatency = findViewById<TextView>(R.id.tvLatency)
        val tvResults = findViewById<TextView>(R.id.tvResults)

        btnFetch.alpha = DISABLED_ALPHA

        btnRequestAccess.setOnClickListener {
            viewModel.requestAgeSignalsAccess(this)
        }

        btnFetch.setOnClickListener {
            if (!viewModel.isAccessGranted) {
                Toast.makeText(
                    this,
                    "You need to request age signal access first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.fetchAgeSignals(applicationContext)
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is AgeSignalUiState.Idle -> {
                        progressBar.visibility = View.GONE
                        tvStatus.text = ""
                        tvLatency.text = ""
                        tvResults.text = "Tap the button to fetch age signals."
                    }
                    is AgeSignalUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        tvStatus.text = ""
                        tvLatency.text = ""
                        tvResults.text = "Fetching..."
                    }
                    is AgeSignalUiState.Success -> {
                        progressBar.visibility = View.GONE
                        tvStatus.text = "✅ SUCCESS"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_dark)
                        )
                        tvLatency.text = "API Delay: ${state.latencyMs} ms"
                        tvResults.text = buildString {
                            appendLine("Age Range Source: ${state.ageRangeSource ?: "null"} (${state.ageRangeSourceName})")
                            appendLine("Significant Change Status: ${state.significantChangeStatus ?: "null"} (${state.significantChangeStatusName})")
                            appendLine("Significant Change Approval Date: ${state.significantChangeApprovalDate ?: "null"}")
                            appendLine("Age Lower: ${state.ageLower ?: "null"}")
                            appendLine("Age Upper: ${state.ageUpper ?: "null"}")
                            appendLine("Install ID: ${state.installId ?: "null"}")
                            appendLine()
                            appendLine("--- Raw Result ---")
                            appendLine(state.rawResultString)
                            appendLine()
                            appendLine("--- Age Range Source Reference ---")
                            appendLine("0 = UNSPECIFIED")
                            appendLine("1 = TIER_A")
                            appendLine("2 = TIER_B")
                            appendLine("3 = TIER_C")
                            appendLine("4 = TIER_D")
                            appendLine()
                            appendLine("--- Significant Change Status Reference ---")
                            appendLine("0 = UNSPECIFIED")
                            appendLine("1 = APPROVED")
                            appendLine("2 = PENDING")
                            appendLine("3 = DECLINED")
                        }
                    }
                    is AgeSignalUiState.AccessSuccess -> {
                        progressBar.visibility = View.GONE
                        btnFetch.alpha = if (viewModel.isAccessGranted) 1f else DISABLED_ALPHA
                        tvStatus.text = "✅ ACCESS REQUEST DONE"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_dark)
                        )
                        tvLatency.text = "API Delay: ${state.latencyMs} ms"
                        tvResults.text = buildString {
                            appendLine("Age Signals Status: ${state.ageSignalsStatus ?: "null"} (${state.ageSignalsStatusName})")
                            appendLine()
                            appendLine("--- Age Signals Status Reference ---")
                            appendLine("0 = UNSPECIFIED")
                            appendLine("1 = SHARED")
                            appendLine("2 = NOT_SHARED")
                            appendLine("3 = VERIFICATION_REQUIRED")
                        }
                    }
                    is AgeSignalUiState.Error -> {
                        progressBar.visibility = View.GONE
                        tvStatus.text = "❌ ERROR"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark)
                        )
                        tvLatency.text = "API Delay: ${state.latencyMs} ms"
                        tvResults.text = buildString {
                            appendLine("Exception: ${state.exceptionType}")
                            appendLine("Message: ${state.message}")
                            if (state.errorCode != null) {
                                appendLine("Error Code: ${state.errorCode}")
                            }
                            appendLine()
                            appendLine("--- Stack Trace ---")
                            appendLine(state.stackTrace)
                        }
                    }
                    is AgeSignalUiState.Unsupported -> {
                        progressBar.visibility = View.GONE
                        tvStatus.text = "⚠️ UNSUPPORTED"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(this@MainActivity, android.R.color.holo_orange_dark)
                        )
                        tvLatency.text = ""
                        tvResults.text = state.reason
                    }
                }
            }
        }
    }
}
