package com.baijiahu.test.age.signal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "${getString(R.string.app_name)} (SDK ${BuildConfig.AGE_SIGNALS_VERSION})"

        val btnFetch = findViewById<Button>(R.id.btnFetch)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvLatency = findViewById<TextView>(R.id.tvLatency)
        val tvResults = findViewById<TextView>(R.id.tvResults)

        btnFetch.setOnClickListener {
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
                            appendLine("User Status: ${state.userStatus} (${state.userStatusName})")
                            appendLine("Age Lower: ${state.ageLower ?: "null"}")
                            appendLine("Age Upper: ${state.ageUpper ?: "null"}")
                            appendLine("Install ID: ${state.installId ?: "null"}")
                            appendLine()
                            appendLine("--- Raw Result ---")
                            appendLine(state.rawResultString)
                            appendLine()
                            appendLine("--- Status Reference ---")
                            appendLine("0 = VERIFIED")
                            appendLine("1 = SUPERVISED")
                            appendLine("2 = SUPERVISED_APPROVAL_PENDING")
                            appendLine("3 = SUPERVISED_APPROVAL_DENIED")
                            appendLine("4 = UNKNOWN")
                            appendLine("5 = DECLARED")
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
