# Age Signal Test App

## Goal
Develop a standalone Android test app using `com.google.android.play:age-signals` library to test the Google Play Store age signals API.

## Requirements
1. Calculate API delay (latency) and show it in UI
2. Show all the possible results of `AgeSignalsResult` and display them in UI
3. It shall be triggered after the user clicks a 'Fetch store age' button

## Reference Implementation

### API Usage (from GoogleRangeUtil.kt)
```kotlin
// Create an instance of a manager
val ageSignalsManager = AgeSignalsManagerFactory.create(context)
ageSignalsManager
    .checkAgeSignals(AgeSignalsRequest.builder().build())
    .addOnSuccessListener { ageSignalsResult ->
        val installId = ageSignalsResult.installId()
        val userStatus = ageSignalsResult.userStatus()
        val ageLower = ageSignalsResult.ageLower()
        val ageUpper = ageSignalsResult.ageUpper()
    }
    .addOnFailureListener { e ->
        // handle failure
    }
```

### AgeSignalsVerificationStatus Constants
```
VERIFIED = 0
SUPERVISED = 1
SUPERVISED_APPROVAL_PENDING = 2
SUPERVISED_APPROVAL_DENIED = 3
UNKNOWN = 4
```

### Key Classes
- `AgeSignalsManagerFactory` - creates the manager instance
- `AgeSignalsRequest` - request builder
- `AgeSignalsResult` - contains userStatus(), ageLower(), ageUpper(), installId()
- `FakeAgeSignalsManager` - for testing with fake results

## Notes
- This is a standalone test app (not integrated into TikTok)
- API requires Android M (API 23) or higher
- Uses `com.google.android.play:age-signals` dependency
