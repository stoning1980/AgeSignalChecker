// 文档已备份至 https://bytedance.us.larkoffice.com/docx/BW7pdMKOZo6Gjlxc4IMudWgYsxh

# Feature Specification: Age Signal Test App

**Feature Branch**: `001-age-signal-test-app`  
**Created**: 2026-07-19  
**Status**: Draft  
**Input**: User description: "com.google.android.play:age-signals test app to calculate API delay, show all possible AgeSignalsResult fields, triggered by a 'Fetch store age' button"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Fetch Age Signals on Button Click (Priority: P1)

As a developer/tester, I want to tap a "Fetch store age" button so that the app calls the Google Play Age Signals API and displays results on screen.

**Why this priority**: This is the core interaction — without the button trigger, nothing else works.

**Independent Test**: Tap the button on a device with Google Play services and verify an API call is made and results appear on screen.

**Acceptance Scenarios**:

1. **Given** the app is open on the main screen, **When** I tap "Fetch store age", **Then** the app calls `AgeSignalsManagerFactory.create(context).checkAgeSignals(...)` and displays results in the UI.
2. **Given** an API call is in progress, **When** the button is tapped again, **Then** a new API call is initiated (no debounce required — this is a test app).

---

### User Story 2 - Display API Latency (Priority: P1)

As a developer/tester, I want to see how long the Age Signals API call takes (in milliseconds) so I can evaluate performance.

**Why this priority**: Latency measurement is a primary requirement for the test app.

**Independent Test**: Tap "Fetch store age" and verify a latency value (e.g., "API Delay: 245 ms") appears in the UI.

**Acceptance Scenarios**:

1. **Given** the button is tapped, **When** the API call completes successfully, **Then** the elapsed time from request start to response is displayed in milliseconds.
2. **Given** the button is tapped, **When** the API call fails with an exception, **Then** the elapsed time until the failure is still displayed.

---

### User Story 3 - Display All AgeSignalsResult Fields (Priority: P1)

As a developer/tester, I want to see all fields from `AgeSignalsResult` displayed in the UI so I can verify the API response content.

**Why this priority**: Showing all possible result data is the second primary requirement.

**Independent Test**: Tap "Fetch store age" and verify each field of AgeSignalsResult is shown with its label and value.

**Acceptance Scenarios**:

1. **Given** the API call succeeds, **When** results are displayed, **Then** I can see:
   - `userStatus` with its integer value AND human-readable name (VERIFIED=0, SUPERVISED=1, SUPERVISED_APPROVAL_PENDING=2, SUPERVISED_APPROVAL_DENIED=3, UNKNOWN=4)
   - `ageLower` (lower bound of age range, nullable)
   - `ageUpper` (upper bound of age range, nullable)
   - `installId` (installation identifier string)
2. **Given** the API call fails, **When** an error occurs, **Then** the error message/exception details (including error code when available) are displayed in the results area.

---

### Edge Cases

- What happens when Google Play Services is not available on the device? → Display error message with detailed error code (e.g., `ServiceDisabledException`, `ApiException` code).
- What happens when the device runs below Android M (API 23)? → Display "API not supported on this device (requires API 23+)".
- What happens when `ageLower` or `ageUpper` is null? → Display "null" explicitly.
- What happens when the API throws an exception? → Display the exception type, message, and detailed error code (e.g., `ApiException.getStatusCode()` if available).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: App MUST display a "Fetch store age" button on the main screen.
- **FR-002**: Tapping the button MUST trigger the Google Play Age Signals API call via `AgeSignalsManagerFactory.create(context).checkAgeSignals(AgeSignalsRequest.builder().build())`.
- **FR-003**: App MUST measure elapsed time from the moment the API call starts to when it completes (success or failure) and display it in milliseconds.
- **FR-004**: On success, app MUST display all fields from `AgeSignalsResult`:
  - `userStatus()` — integer value with human-readable label
  - `ageLower()` — integer or null
  - `ageUpper()` — integer or null
  - `installId()` — string
- **FR-005**: On failure, app MUST display the error/exception details including exception type, message, and error code (e.g., `ApiException.getStatusCode()`) when available.
- **FR-006**: App MUST display the human-readable mapping of `userStatus` values: VERIFIED(0), SUPERVISED(1), SUPERVISED_APPROVAL_PENDING(2), SUPERVISED_APPROVAL_DENIED(3), UNKNOWN(4).
- **FR-007**: App MUST check device API level and show a message if below Android M (API 23).

### Key Entities

- **AgeSignalsResult**: The response object from Google Play Age Signals API containing userStatus, ageLower, ageUpper, and installId.
- **AgeSignalsVerificationStatus**: Enum-like integer constants representing user verification states (0-4).

## Constraints and Dependencies

### Technical Constraints
- **Code Scope**:
  - Allowed modification paths:
    - `app/src/main/**` (all app source code)
    - `app/build.gradle` or `app/build.gradle.kts` (dependency declaration)
    - `build.gradle` or `build.gradle.kts` (project-level config)
    - `settings.gradle` or `settings.gradle.kts`
- **Implementation Constraints**:
  - Dependency: `com.google.android.play:age-signals` (latest stable version)
  - Minimum SDK: API 23 (Android M)
  - Target: Single-activity test app
  - Language: Kotlin
  - No external architecture frameworks required (this is a minimal test app)

### External Dependencies
- Google Play Services must be available on the test device
- `com.google.android.play:age-signals` library from Google Maven repository

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Tapping "Fetch store age" initiates the API call and shows results within the UI (no logcat-only output).
- **SC-002**: API latency is accurately displayed in milliseconds after each call.
- **SC-003**: All four fields of AgeSignalsResult (userStatus, ageLower, ageUpper, installId) are visible in the UI after a successful call.
- **SC-004**: Error scenarios display meaningful messages to the tester.
- **SC-005**: App compiles and runs on any device with API 23+ and Google Play Services.
