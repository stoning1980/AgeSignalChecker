# Age Signal Checker

A lightweight Android developer/QA utility for testing the [Google Play Age Signals API](https://developer.android.com/google/play/age-signals) on a real device. Tap a button, and the app calls the API, measures latency, and displays the full result — including detailed error diagnostics when a call fails.

- **Package:** `com.baijiahu.test.age.signal`
- **Min SDK:** 23 (Android 6.0) · **Target/Compile SDK:** 34
- **Age Signals SDK:** `com.google.android.play:age-signals:0.0.4`

## Features

The app exposes the two-function architecture introduced in age-signals 0.0.4:

- **Request age signals access** — calls `requestAgeSignalsAccess()`, which triggers the Play in-app prompt for age range sharing. Displays the returned `ageSignalsStatus` (UNSPECIFIED, SHARED, NOT_SHARED, VERIFICATION_REQUIRED). Requires an `Activity`.
- **Check age signals** — calls `checkAgeSignals()` and displays:
  - `ageRangeSource` (UNSPECIFIED, TIER_A–TIER_D)
  - `significantChangeStatus` (UNSPECIFIED, APPROVED, PENDING, DECLINED)
  - `significantChangeApprovalDate`
  - Age lower/upper bounds and install ID
  - The raw result string

Both flows report API latency in milliseconds. Failures surface the exception type, message, error code (via `AgeSignalsException.getErrorCode()`), and full stack trace. Devices below API 23 show a clear "unsupported" message.

> Note: `userStatus` was deprecated and removed in 0.0.4; it is replaced by `ageRangeSource` and `significantChangeStatus`.

## Architecture

A single-screen app using a `ViewModel` and a `StateFlow` of a sealed UI state.

- `MainActivity.kt` — wires the two buttons and renders each UI state.
- `MainViewModel.kt` — wraps the callback-based SDK Tasks in coroutines and maps result codes to readable names.
- `AgeSignalUiState.kt` — sealed states: `Idle`, `Loading`, `Success`, `AccessSuccess`, `Error`, `Unsupported`.

## Building

```bash
# Debug APK
./gradlew :app:assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk

# Release APK (signed if keystore.properties is present)
./gradlew :app:assembleRelease
# -> app/build/outputs/apk/release/app-release.apk
```

Install on a connected device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Release signing

Release builds are signed when a `keystore.properties` file exists at the project root:

```properties
storeFile=release-keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```

`keystore.properties`, `*.jks`, and `*.keystore` are git-ignored. **Keep a secure backup** of the keystore — it is required to publish future updates. If no `keystore.properties` is present, the release build is left unsigned.

## Publishing

Play Store listing copy and metadata live in [`play-store/listing.md`](play-store/listing.md), with a privacy policy in `play-store/privacy-policy.html`.
