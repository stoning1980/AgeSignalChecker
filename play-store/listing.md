# Google Play Store Listing — Age Signal Checker

Package name (Application ID): `com.baijiahu.test.age.signal`

Copy/paste the sections below into the Play Console. Character limits from the
Play Console are noted for each field.

---

## App name (max 30 chars)

```
Age Signal Checker
```

## Short description (max 80 chars)

```
Test and inspect Google Play Age Signals API responses right on your device.
```

## Full description (max 4000 chars)

```
Age Signal Checker is a lightweight developer and QA utility for testing the
Google Play Age Signals API on a real device. With a single tap it calls the
API, measures the response time, and displays the full result in a clear,
readable format — no setup, no account, no configuration required.

WHAT IT DOES
The app exposes both functions of the Age Signals API:

• "Request age signals access" triggers the Google Play in-app prompt for age
  range sharing and shows the resulting access status (UNSPECIFIED, SHARED,
  NOT_SHARED, or VERIFICATION_REQUIRED).
• "Check age signals" reads the current signals and shows you exactly what the
  API returned:
  - Age range source (UNSPECIFIED, TIER_A, TIER_B, TIER_C, or TIER_D), with the
    numeric code and its human-readable name side by side.
  - Significant change status (UNSPECIFIED, APPROVED, PENDING, or DECLINED) and
    the significant change approval date.
  - Age range — the lower and upper age bounds reported for the account.
  - Install ID associated with the request.
  - API latency in milliseconds, so you can see how fast the call resolved.
  - The raw API result string for deeper inspection.

BUILT-IN ERROR DIAGNOSTICS
When a call fails, the app doesn't hide the details. It surfaces the exception
type, the message, the status/error code when available, and the full stack
trace — making it easy to reproduce, understand, and report issues.

DEVICE COMPATIBILITY
Runs on Android 6.0 (API 23) and above. On devices or configurations where the
API is not supported, the app clearly explains why instead of failing silently.

WHO IT'S FOR
• Android developers integrating the Play Age Signals API.
• QA engineers verifying age-signal behavior across accounts and devices.
• Anyone who wants to see, in plain terms, what age signals Google Play reports
  for the current device and account.

PRIVACY & COST
• 100% free.
• No ads.
• No in-app purchases.
• No analytics or tracking SDKs.
The results shown are provided by Google Play in response to your request and
are displayed on-screen only.

Age Signal Checker keeps things simple: one screen, one button, and a complete,
honest view of what the Age Signals API returns.
```

---

## Store settings reference

- App or game: **App**
- Category (suggested): **Tools**
- Free or paid: **Free**
- Contains ads: **No**
- In-app purchases: **No**

## Content rating (IARC questionnaire)
- No user-generated content, no ads, no purchases, no violence/sexual/gambling
  content. Expected rating: **Everyone / PEGI 3**.

## Data safety
- No data collected or shared by the app itself. The Age Signals API call is
  handled by Google Play services; the app only displays the returned values
  locally and does not transmit them anywhere.

## Graphics assets you still need to supply in the Console
(Play requires these; they are not part of the app binary.)
- App icon: 512 x 512 PNG (32-bit, with alpha). A matching version of the
  in-app launcher icon can be exported at this size.
- Feature graphic: 1024 x 500 PNG or JPG.
- Phone screenshots: at least 2 (min 320 px, max 3840 px on any side).

## Release notes (What's new — max 500 chars)

```
Initial release. Request Age Signals access and inspect Google Play Age Signals
API results, including age range source, significant change status and approval
date, age range, install ID, latency, raw output, and detailed error
diagnostics.
```
