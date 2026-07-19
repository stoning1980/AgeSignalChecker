// 文档已备份至 https://bytedance.us.larkoffice.com/docx/PzOkda5RboXxwBxxHWAuVhZSskc

# Tasks Document

## Overview

Age Signal Test App — standalone Android test app to call Google Play Age Signals API, measure latency, and display all result fields with color-coded status.

**Execution order**: 1) Project Scaffold → 2) UI State & ViewModel → 3) Activity & Layout

---

- [x] 1. Create Project Scaffold (Gradle, Manifest, Wrapper)
  - Dependencies: None
  - Files:
    - `build.gradle.kts` (project-level)
    - `settings.gradle.kts`
    - `gradle.properties`
    - `gradle/wrapper/gradle-wrapper.properties`
    - `app/build.gradle.kts`
    - `app/src/main/AndroidManifest.xml`
  - Description: Set up the Android project structure with AGP, Kotlin plugin, age-signals 0.0.3 dependency, and all required AndroidX dependencies. Include Gradle wrapper config.
  - Purpose: Establish a compilable project shell that subsequent tasks build upon.
  - _Leverage: design.md Section "1. Project Setup (Gradle & Manifest)"_
  - _Requirements: FR-002, FR-007 (dependency + minSdk 23)_
  - <!-- _Prompt:
    Role: Android Client Development Engineer
    Task: Create the full Android project scaffold for AgeSignalTest. Create build.gradle.kts (project-level with AGP 8.2.0 + Kotlin 1.9.22 plugins), settings.gradle.kts (google/mavenCentral repos, include :app), gradle.properties (AndroidX enabled), gradle/wrapper/gradle-wrapper.properties (Gradle 8.4), app/build.gradle.kts (namespace com.example.agesignaltest, compileSdk 34, minSdk 23, targetSdk 34, dependency on com.google.android.play:age-signals:0.0.3 plus appcompat, activity-ktx, lifecycle-viewmodel-ktx, coroutines-android, material), and AndroidManifest.xml (single MainActivity with LAUNCHER intent-filter, theme MaterialComponents.Light.DarkActionBar).
    Restrictions: Use exact versions from design.md. No extra dependencies. No test dependencies needed.
    Success: Project structure matches design.md Section 1 exactly. All files exist with correct content.
    Control Flow: After completion mark [x] and proceed to the next task. -->

---

- [x] 2. Implement UI State Model & ViewModel
  - Dependencies: 1
  - Files:
    - `app/src/main/java/com/example/agesignaltest/AgeSignalUiState.kt`
    - `app/src/main/java/com/example/agesignaltest/MainViewModel.kt`
  - Description: Create the sealed UI state class (Idle, Loading, Success, Error, Unsupported) and the ViewModel that calls Age Signals API, measures latency, maps results to state, and extracts error codes.
  - Purpose: Implement all business logic — API call, latency timing, result mapping, error handling with status codes.
  - _Leverage: design.md Sections "2. UI State Model" and "3. ViewModel (Business Logic)"_
  - _Requirements: FR-002, FR-003, FR-004, FR-005, FR-006, FR-007_
  - <!-- _Prompt:
    Role: Android Client Development Engineer
    Task: Create AgeSignalUiState.kt (sealed class with Idle, Loading, Success containing userStatus/userStatusName/ageLower/ageUpper/installId/latencyMs/rawResultString, Error containing exceptionType/message/errorCode/latencyMs/stackTrace, and Unsupported containing reason). Create MainViewModel.kt with fetchAgeSignals(context) that checks API level, emits Loading, calls AgeSignalsManagerFactory.create(context).checkAgeSignals() via suspendCancellableCoroutine, measures latency with System.currentTimeMillis(), maps userStatus int to name (0=VERIFIED through 5=DECLARED), extracts ApiException statusCode via reflection on failure.
    Restrictions: Follow design.md code exactly. Use viewModelScope.launch for coroutine. Include all 6 status mappings (VERIFIED, SUPERVISED, SUPERVISED_APPROVAL_PENDING, SUPERVISED_APPROVAL_DENIED, UNKNOWN, DECLARED).
    Success: Both files compile. ViewModel correctly wraps the async Task API into a coroutine, measures latency, and emits appropriate states for success/failure/unsupported.
    Control Flow: After completion mark [x] and proceed to the next task. -->

---

- [x] 3. Create Activity & XML Layout with Color-Coded Status
  - Dependencies: 2
  - Files:
    - `app/src/main/res/layout/activity_main.xml`
    - `app/src/main/java/com/example/agesignaltest/MainActivity.kt`
  - Description: Create the XML layout (ScrollView with Button, ProgressBar, status TextView, latency TextView, results TextView) and MainActivity that observes ViewModel StateFlow and renders each state with color-coded status indicators (green for success, red for error, orange for unsupported).
  - Purpose: Wire the UI to the ViewModel and provide clear visual feedback to the tester.
  - _Leverage: design.md Section "4. Activity & UI Layout"_
  - _Requirements: FR-001, FR-003, FR-004, FR-005, FR-006, FR-007_
  - <!-- _Prompt:
    Role: Android Client Development Engineer
    Task: Create activity_main.xml (ScrollView > LinearLayout with Button id=btnFetch text="Fetch store age", ProgressBar id=progressBar visibility=gone, TextView id=tvStatus textSize=18sp bold, TextView id=tvLatency textSize=16sp bold, TextView id=tvResults textSize=14sp monospace). Create MainActivity.kt that uses viewModels() delegate, collects uiState via lifecycleScope.launch + collectLatest, and renders: Idle (hint text), Loading (show progress), Success (green tvStatus "✅ SUCCESS", show latency, show all fields + raw result + status reference table), Error (red tvStatus "❌ ERROR", show latency, exception details, error code, stack trace), Unsupported (orange tvStatus "⚠️ UNSUPPORTED", show reason). Use ContextCompat.getColor with android.R.color.holo_green_dark/holo_red_dark/holo_orange_dark.
    Restrictions: Follow design.md code exactly. Button text must be "Fetch store age". All result fields must be visible. Status reference table must list all 6 statuses (0-5).
    Success: App launches with button visible. Tapping button triggers API call, shows loading spinner, then displays color-coded results with latency. All AgeSignalsResult fields visible on success. Error details with code visible on failure.
    Control Flow: After completion mark [x] and proceed to the next task. -->
