// 文档已备份至 https://bytedance.us.larkoffice.com/docx/CIHMds0ero8cgWxQwj2uFncdsaf

# Implementation Plan: Age Signal Test App

**Branch**: `001-age-signal-test-app` | **Date**: 2026-07-19 | **Spec**: `specs/001-age-signal-test-app/requirements.md`
**Input**: Feature specification from `/specs/001-age-signal-test-app/requirements.md`

## Summary

1. **Core changes**:
   - Create a new standalone Android project with single Activity
   - Implement "Fetch store age" button that triggers Google Play Age Signals API
   - Measure and display API call latency in milliseconds
   - Display all `AgeSignalsResult` fields (userStatus, ageLower, ageUpper, installId)
   - Display error details with error codes on failure

2. **Technical approach**:
   - Single-activity architecture with ViewModel for state management
   - Kotlin coroutines for async API call with `suspendCancellableCoroutine`
   - Simple XML layout with ScrollView for results display
   - No external architecture frameworks — minimal test app

3. **No AB/feature flag control** — standalone test app

## Technical Context

**Language/Version**: Kotlin 1.9+
**Primary Dependencies**: `com.google.android.play:age-signals`, AndroidX lifecycle (ViewModel), Kotlin coroutines
**Storage**: N/A
**Testing**: Manual on-device testing
**Target Platform**: Android 6.0+ (API 23+)
**Project Type**: Mobile - Android native (standalone test app)
**Performance Goals**: N/A (test app)
**Constraints**: Requires Google Play Services on device
**Scale/Scope**: Single screen, single button, display results

## Architecture Design

### 1. Project Setup (Gradle & Manifest)

**Files to Create**:
- `build.gradle.kts` (project-level)
- `settings.gradle.kts`
- `gradle.properties`
- `app/build.gradle.kts` (module-level)
- `app/src/main/AndroidManifest.xml`

**Design Approach**:

Standard AGP project with a single `app` module. The `com.google.android.play:age-signals` dependency is declared in the app module.

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.agesignaltest"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.agesignaltest"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
    implementation("com.google.android.play:age-signals:0.0.3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.android.material:material:1.11.0")
}
```

```xml
<!-- app/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="AgeSignalTest"
        android:theme="@style/Theme.MaterialComponents.Light.DarkActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### 2. UI State Model

**Files to Create**: `app/src/main/java/com/example/agesignaltest/AgeSignalUiState.kt`

**Design Approach**:

A sealed class represents all possible UI states after a fetch attempt. This covers success, failure, loading, and the initial idle state.

```kotlin
// AgeSignalUiState.kt
package com.example.agesignaltest

sealed class AgeSignalUiState {
    object Idle : AgeSignalUiState()
    object Loading : AgeSignalUiState()

    data class Success(
        val userStatus: Int,
        val userStatusName: String,  // human-readable
        val ageLower: Int?,
        val ageUpper: Int?,
        val installId: String?,
        val latencyMs: Long,
        val rawResultString: String  // toString() of the full AgeSignalsResult
    ) : AgeSignalUiState()

    data class Error(
        val exceptionType: String,
        val message: String,
        val errorCode: Int?,   // ApiException.getStatusCode() if available
        val latencyMs: Long,
        val stackTrace: String // full stack trace for debugging
    ) : AgeSignalUiState()

    data class Unsupported(
        val reason: String  // e.g., "API not supported on this device (requires API 23+)"
    ) : AgeSignalUiState()
}
```

### 3. ViewModel (Business Logic)

**Files to Create**: `app/src/main/java/com/example/agesignaltest/MainViewModel.kt`

**Design Approach**:

The ViewModel owns the fetch logic: creates the manager, measures latency with `System.currentTimeMillis()`, maps results to `AgeSignalUiState`, and exposes state via `StateFlow`.

```kotlin
// MainViewModel.kt
package com.example.agesignaltest

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AgeSignalUiState>(AgeSignalUiState.Idle)
    val uiState: StateFlow<AgeSignalUiState> = _uiState

    fun fetchAgeSignals(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            _uiState.value = AgeSignalUiState.Unsupported(
                "API not supported on this device (requires API 23+)"
            )
            return
        }

        _uiState.value = AgeSignalUiState.Loading
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                val result = checkAgeSignals(context)
                val latency = System.currentTimeMillis() - startTime
                _uiState.value = AgeSignalUiState.Success(
                    userStatus = result.userStatus(),
                    userStatusName = mapUserStatus(result.userStatus()),
                    ageLower = result.ageLower(),
                    ageUpper = result.ageUpper(),
                    installId = result.installId(),
                    latencyMs = latency,
                    rawResultString = result.toString()
                )
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - startTime
                val errorCode = tryGetStatusCode(e)
                _uiState.value = AgeSignalUiState.Error(
                    exceptionType = e.javaClass.simpleName,
                    message = e.message ?: "Unknown error",
                    errorCode = errorCode,
                    latencyMs = latency,
                    stackTrace = e.stackTraceToString()
                )
            }
        }
    }

    private suspend fun checkAgeSignals(context: Context): AgeSignalsResult =
        suspendCancellableCoroutine { cont ->
            try {
                val manager = AgeSignalsManagerFactory.create(context)
                manager.checkAgeSignals(AgeSignalsRequest.builder().build())
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }

    private fun mapUserStatus(status: Int): String = when (status) {
        0 -> "VERIFIED"
        1 -> "SUPERVISED"
        2 -> "SUPERVISED_APPROVAL_PENDING"
        3 -> "SUPERVISED_APPROVAL_DENIED"
        4 -> "UNKNOWN"
        5 -> "DECLARED"
        else -> "UNRECOGNIZED($status)"
    }

    private fun tryGetStatusCode(e: Exception): Int? {
        // Attempt to extract ApiException status code via reflection
        return try {
            val method = e.javaClass.getMethod("getStatusCode")
            method.invoke(e) as? Int
        } catch (_: Exception) { null }
    }
}
```

### 4. Activity & UI Layout

**Files to Create**:
- `app/src/main/java/com/example/agesignaltest/MainActivity.kt`
- `app/src/main/res/layout/activity_main.xml`

**Design Approach**:

Single Activity observes the ViewModel's `StateFlow` and renders results into TextViews. The layout uses a ScrollView containing a LinearLayout with the button at top and result TextViews below.

```xml
<!-- app/src/main/res/layout/activity_main.xml -->
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <Button
            android:id="@+id/btnFetch"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Fetch store age" />

        <ProgressBar
            android:id="@+id/progressBar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:visibility="gone" />

        <TextView
            android:id="@+id/tvLatency"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:textSize="16sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvStatus"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:textSize="18sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvResults"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:textSize="14sp"
            android:fontFamily="monospace" />
    </LinearLayout>
</ScrollView>
```

```kotlin
// MainActivity.kt
package com.example.agesignaltest

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

        val btnFetch = findViewById<Button>(R.id.btnFetch)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvLatency = findViewById<TextView>(R.id.tvLatency)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvResults = findViewById<TextView>(R.id.tvResults)

        btnFetch.setOnClickListener {
            viewModel.fetchAgeSignals(applicationContext)
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is AgeSignalUiState.Idle -> {
                        progressBar.visibility = View.GONE
                        tvLatency.text = ""
                        tvStatus.text = ""
                        tvResults.text = "Tap the button to fetch age signals."
                    }
                    is AgeSignalUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        tvLatency.text = ""
                        tvStatus.text = ""
                        tvResults.text = "Fetching..."
                    }
                    is AgeSignalUiState.Success -> {
                        progressBar.visibility = View.GONE
                        tvLatency.text = "API Delay: ${state.latencyMs} ms"
                        tvStatus.text = "✅ SUCCESS"
                        tvStatus.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_dark))
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
                        tvLatency.text = "API Delay: ${state.latencyMs} ms"
                        tvStatus.text = "❌ ERROR"
                        tvStatus.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
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
                        tvLatency.text = ""
                        tvStatus.text = "⚠️ UNSUPPORTED"
                        tvStatus.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_orange_dark))
                        tvResults.text = state.reason
                    }
                }
            }
        }
    }
}
```

## Data Flow

### Button Click → API Call → Display Results

```
[User taps "Fetch store age"]
    ↓
[MainActivity.btnFetch.onClick]
    ↓
[MainViewModel.fetchAgeSignals(context)]
    ↓
[Check Build.VERSION.SDK_INT >= M]
    ↓ (if unsupported → emit Unsupported state → DONE)
[Emit Loading state]
    ↓
[Record startTime = System.currentTimeMillis()]
    ↓
[AgeSignalsManagerFactory.create(context)]
    ↓
[manager.checkAgeSignals(AgeSignalsRequest.builder().build())]
    ↓
┌─── addOnSuccessListener ───┐    ┌─── addOnFailureListener ───┐
│ Calculate latency           │    │ Calculate latency           │
│ Map userStatus to name      │    │ Extract error code          │
│ Emit Success state          │    │ Emit Error state            │
└─────────────────────────────┘    └─────────────────────────────┘
    ↓                                   ↓
[StateFlow emits new state]
    ↓
[MainActivity collects → updates UI]
```

## Project Structure

### Documentation (this feature)

```text
specs/001-age-signal-test-app/
├── spec.md               # Archived user input
├── requirements.md       # User stories and functional requirements
├── design.md            # This file - technical design
├── tasks.md             # Implementation tasks (to be created)
├── explore.md           # Exploration results
└── events.jsonl         # Event log (auto-created)
```

### Source Code (repository root)

```text
AgeSignalTest/
├── build.gradle.kts                     [NEW] Project-level Gradle config (AGP, Kotlin plugin)
├── settings.gradle.kts                  [NEW] Project settings, module include, repositories
├── gradle.properties                    [NEW] Gradle JVM args and AndroidX flag
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar           [NEW] Gradle wrapper binary
│       └── gradle-wrapper.properties    [NEW] Gradle distribution URL
├── gradlew                              [NEW] Gradle wrapper script (unix)
├── gradlew.bat                          [NEW] Gradle wrapper script (windows)
└── app/
    ├── build.gradle.kts                 [NEW] App module deps (age-signals, coroutines, lifecycle)
    └── src/main/
        ├── AndroidManifest.xml          [NEW] Single-activity manifest, minSdk 23
        ├── java/com/example/agesignaltest/
        │   ├── MainActivity.kt          [NEW] Single activity, observes ViewModel state
        │   ├── MainViewModel.kt         [NEW] Fetch logic, latency measurement, state mapping
        │   └── AgeSignalUiState.kt      [NEW] Sealed class for UI states
        └── res/
            └── layout/
                └── activity_main.xml    [NEW] Button + progress + result TextViews in ScrollView
```

**Structure Decision**: Minimal single-module Android project. No multi-module setup, no DI framework, no navigation — appropriate for a focused test app.
