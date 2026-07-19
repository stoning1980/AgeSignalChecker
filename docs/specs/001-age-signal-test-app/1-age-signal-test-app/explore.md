# Explore Results: Age Signal Test App

## API Contract Summary

### Core Classes (from `com.google.android.play.agesignals`)

| Class | Purpose |
|-------|---------|
| `AgeSignalsManagerFactory` | Factory to create manager via `create(context)` |
| `AgeSignalsRequest` | Request builder — `AgeSignalsRequest.builder().build()` (no params needed) |
| `AgeSignalsResult` | Response: `userStatus()`, `ageLower()`, `ageUpper()`, `installId()` |
| `FakeAgeSignalsManager` | Testing double from `com.google.android.play.agesignals.testing` |
| `AgeSignalsVerificationStatus` | Constants from `com.google.android.play.agesignals.model` |

### AgeSignalsVerificationStatus Constants

| Constant | Value |
|----------|-------|
| VERIFIED | 0 |
| SUPERVISED | 1 |
| SUPERVISED_APPROVAL_PENDING | 2 |
| SUPERVISED_APPROVAL_DENIED | 3 |
| UNKNOWN | 4 |

### Threading Model
- `checkAgeSignals()` returns a Task-like object with `addOnSuccessListener` / `addOnFailureListener`
- Listeners delivered asynchronously
- Can be wrapped in `suspendCancellableCoroutine` for coroutine usage

### Error Handling
- `addOnFailureListener` receives an `Exception` (could be `ApiException` with status code)
- Try/catch around `AgeSignalsManagerFactory.create()` for Play Services unavailability
- Device API level check: `Build.VERSION.SDK_INT >= Build.VERSION_CODES.M`

### Dependency
- `com.google.android.play:age-signals` from Google Maven
