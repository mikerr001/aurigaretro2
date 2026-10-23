# AURIGA Android Fusion

AURIGA is an offline-first accessibility navigation co-pilot. This workspace contains the Kotlin Android app and the existing Replit API/Canvas artifacts.

## Run & Operate

- `cd android && ./gradlew testDebugUnitTest` — run Android unit tests (JDK 17 and Android SDK 34 required)
- `cd android && ./gradlew assembleDebug` — build a debug APK
- `pnpm --filter @workspace/api-server run dev` — run the existing API server
- `pnpm run typecheck` — typecheck the pnpm workspace
- `pnpm run build` — build the pnpm workspace
- `pnpm --filter @workspace/db run push` — push development database schema changes
- The API/ops workspace may require `DATABASE_URL`; the Android navigation core does not.

## Stack

- Android: Kotlin, Gradle, CameraX, Camera2 characteristics, MediaPipe Tasks Vision, Room, LiteRT-LM, coroutines
- Workspace services: pnpm, Node.js 24, TypeScript, Express, PostgreSQL/Drizzle

## Where things live

- `android/` — native Android app and Gradle wrapper
- `android/app/src/main/java/com/drakosanctis/auriga/core/AurigaPipeline.kt` — camera-to-guidance pipeline
- `android/app/src/main/java/com/drakosanctis/auriga/geometry/` — camera geometry, distance estimates, and experimental refinements
- `android/app/src/main/assets/device_calibration_profiles.json` — approximate fallback camera catalog
- `.github/workflows/build-apk.yml` — Android CI build; downloads and verifies the model assets
- `android/docs/` — Android risks, calibration notes, and build guidance
- `docs/AURIGA_VARIANT_REVIEW.md` — archive-by-archive merge decisions
- `docs/LINGBOT_MAP_EVALUATION.md` — separate research assessment of LingBot Map
- `NEXT_AGENT_PLAN.md` — detailed product decisions and sequenced project plan

## Architecture decisions

- The Kotlin app is the Android base. The old Java application is not copied wholesale; its validated calibration catalog is retained as an approximate fallback.
- Camera2 sensor axes are mapped onto the rotated analyzed-frame axes before horizontal/vertical FOV calculations.
- Assumed-width pinhole distances are low-confidence only. They do not drive TTC, hazard proximity scoring, or exact spoken ranges.
- Ground-plane trigonometry is the intended primary distance method. Visual horizon and camera-height estimates remain behind an internal feature gate pending field validation.
- Spatial ground truth must remain camera-only. The coarse accelerometer refusal gate is off by default and, if enabled experimentally, must never supply camera pitch or range.
- LingBot Map is not part of the Android runtime. Raw camera frames are not sent to its demo WebSocket service.
- AURIGA remains one accessibility product; no face/gait identification or persistent person tracking.

## Product

The Android app analyzes camera frames locally and connects object detections to hazard scoring, spoken guidance, haptics, and optional on-device language tasks. The active distance refinements are experimental and are not enabled for live navigation.

## User preferences

- Keep explanations concise; do not reduce implementation quality or omit necessary code for brevity.
- Prefer automated, approximate camera-height inference over calibration gestures.
- Treat person scale as anonymous and transient; do not identify people or retain person tracks.

## Gotchas

- A local Android build requires JDK 17 and Android SDK 34. The Android CI workflow also downloads the pinned detector and LiteRT-LM model assets.
- Do not trust assumed-width pinhole results as measured distances.
- The line-based visual horizon candidate is intentionally capped below the ground-plane estimator's acceptance threshold.
- The high-confidence spoken-range gate also checks the estimate's uncertainty interval.
- The API server and Canvas artifacts predate this Android import; their run commands and generated artifact manifests are unchanged.

## Pointers

- `android/docs/KNOWN_RISKS.md` — unresolved build/runtime risks
- `docs/GEOMETRY_INTEGRATION.md` — geometry behavior and field-validation gate
- `.agents/memory/MEMORY.md` — durable product boundary
