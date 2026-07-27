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
