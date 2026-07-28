# Repository Conventions

## Branch strategy: two maintained release lines

This repo permanently maintains **two parallel release lines**, one per Age Signals
SDK version, each shipping as a **distinct app** (different `applicationId`) so they
can be installed side by side:

| Branch | Age Signals SDK | `applicationId` |
| --- | --- | --- |
| `p/junjiang.j/0.0.4` | `0.0.4` (two-function: `requestAgeSignalsAccess` + `checkAgeSignals`) | `com.baijiahu.test.age.signal` |
| `p/junjiang.j/0.0.3` | `0.0.3` (legacy single-function `checkAgeSignals` / `userStatus`) | `com.baijiahu.test.age.signal.legacy` |

Rules:
- Keep **both** branches alive and buildable. Do not delete or collapse either line.
- When applying a shared change (build tooling, target SDK, UI fixes, icons), apply it
  to both branches unless it is inherently version-specific.
- Never let a change to one line silently overwrite the other. The SDK version and
  `applicationId` are the intentional differences and must not be unified.
- The SDK version is declared once as `ageSignalsVersion` in `app/build.gradle.kts` and
  reused for both the dependency coordinate and `BuildConfig.AGE_SIGNALS_VERSION`
  (shown in the app title).

## Version codes: always bump, per branch

Google Play rejects an upload whose `versionCode` was already used. Each branch tracks
its own independent code/name (the two lines are separate apps):

- Whenever you produce a build intended for upload, **bump `versionCode` by 1 and update
  `versionName`** on that branch. Do this before building the bundle, not after.
- The two lines advance independently — do not sync their numbers. Bumping 0.0.4 does
  not require touching 0.0.3.

## Edge-to-edge / action bar (SDK 36)

Both branches target SDK 36. `android:windowOptOutEdgeToEdgeEnforcement` only works on
Android 15 (SDK 35), so it is a no-op at SDK 36 and the decor action bar overlaps content.

- The theme is `Theme.MaterialComponents.Light.NoActionBar`. Do not reintroduce a decor
  action bar. Render the title as the `tvTitle` TextView inside the layout.
- The root `ScrollView` has `android:id="@+id/rootScroll"` and `MainActivity` installs a
  `ViewCompat.setOnApplyWindowInsetsListener` that pads by `systemBars()` insets. Keep
  this — it is what prevents the status-bar overlap. Verify on-device after layout changes.

## Release builds: R8 + deobfuscation mapping

Release must keep `isMinifyEnabled = true` and `isShrinkResources = true`. Play Console
warns when a bundle ships without a deobfuscation file; R8 produces `mapping.txt`
(auto-embedded in the AAB) and roughly halves the download size.

- `app/proguard-rules.pro` keeps the Age Signals SDK API and the reflective error-code
  accessors (`getErrorCode` on 0.0.4, `getStatusCode` on 0.0.3). Do not strip these — the
  app reads status codes reflectively, and obfuscating the SDK breaks the runtime call.
- After changing R8 config or keep rules, install the **release** APK on a device and
  exercise the SDK button to confirm nothing was stripped. A clean debug build is not
  sufficient proof.

## Release artifact naming

`app/build.gradle.kts` renames outputs to embed the SDK version so the two lines' files
never collide in a shared output directory:

- APKs: `app-<buildType>-<ageSignalsVersion>.apk` (via `applicationVariants` output rename).
- AAB: the `renameReleaseBundle` task rewrites `app-release.aab` to
  `app-release-<ageSignalsVersion>.aab`. It only touches its own freshly built file, so a
  leftover AAB from the other branch is left in place intentionally — do not delete it.

## Play Store graphics & launcher icon: version badge

To tell the two listings apart, every Play Store PNG and the launcher icon carries a
white circular badge with the SDK's last digit ("3" or "4"):

- Play Store assets in `play-store/`: `play_store_icon_512.png` (badge top-right) and
  `feature_graphic_1024x500.png` (badge **bottom-right**, to avoid overlapping the keyword
  pills at the top).
- Launcher icons in `app/src/main/res/mipmap-*/`: badge `ic_launcher.png`,
  `ic_launcher_round.png`, and the adaptive `ic_launcher_foreground.png` across all
  densities. On the adaptive foreground, pull the badge inward from the corner so the
  circular launcher mask does not clip it; verify by simulating the circular mask.
- When bumping the SDK line or regenerating icons, re-badge with that branch's digit.

## Verify before reporting done

- UI/layout changes: build, install, launch on the connected device, and screenshot.
- Release/R8 changes: install the release APK and trigger the SDK call.
- Always end work back on `p/junjiang.j/0.0.4` unless told otherwise.
