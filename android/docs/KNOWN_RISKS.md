# Known Risks / Likely First-Build Issues

This document was updated after a live web-research pass specifically aimed
at predicting real build failures before the first GitHub Actions run, given
the actual dependency set in this project. Several genuine, confirmed risks
were found and already fixed in this codebase (see "Already fixed" below).
The Android app as a whole has not been compiled. A standalone Kotlin harness
has compiled and tested the pure geometry and guidance logic, but it does not
validate Android or dependency integration.

## Already fixed, based on real research (not guesses)

1. **MediaPipe `tasks-genai` is deprecated — migrated to LiteRT-LM.**
   Google's own current documentation states the MediaPipe LLM Inference API
   (`com.google.mediapipe:tasks-genai`) is in maintenance-only mode with an
   explicit recommendation to migrate to LiteRT-LM
   (`com.google.ai.edge.litertlm`). Real-world GitHub issues against
   `tasks-genai` showed a `NoClassDefFoundError` on an internal proto class,
   a `dlopen failed` JNI `.so` resolution failure specifically on a Samsung
   device, and a JDK17 class-file-version mismatch — all matching the shape
   of the original "model failed to load: null" bug this project exists to
   fix. `MindEngine.kt` and `app/build.gradle.kts` were rewritten to use
   LiteRT-LM's `Engine`/`Conversation`/`EngineConfig` Kotlin API instead.

2. **Room + Kotlin entities needs KSP, not `annotationProcessor`.**
   Confirmed via Room's own documentation: Kotlin entities/DAOs need KSP
   (or kapt) — `annotationProcessor` only runs the Java-style processor and
   will not correctly generate `_Impl` classes for Kotlin sources in this
   project's `memory/` package. Fixed: KSP plugin added to both
   `build.gradle.kts` (root) and `app/build.gradle.kts`, Room compiler
   dependency changed from `annotationProcessor(...)` to `ksp(...)`.

3. **The original Qwen2.5-0.5B repository is not compatible with LiteRT-LM.**
   Direct inspection of `litert-community/Qwen2.5-0.5B-Instruct` shows that
   it distributes `.task` and `.tflite` model files for the MediaPipe LLM
   Inference API; it does not contain the `.litertlm` artifact previously
   assumed by the workflow. The app's `MindEngine` loads LiteRT-LM containers,
   so renaming or downloading those older files would not make them valid.
   The selected replacement is the public Apache-2.0 `litert-community/
   Qwen3-0.6B` package `Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm` (about
   345MB), which is explicitly tagged and packaged for LiteRT-LM. Validate
   generation quality, latency, and peak RAM on target devices before release.

4. **Device RAM fragmentation in target markets — added a pre-flight check,
   with explicit graceful degradation.** Real market research (Statcounter,
   GSMArena, industry reports) confirmed current-generation budget Android
   phones in Kenya/Nigeria/Ethiopia (Samsung Galaxy A05/A06 series, Tecno,
   Infinix) commonly ship with 4-6GB RAM, but a real, still-circulating
   population of older/lower-tier devices (some as low as 1GB RAM, partly
   via Android Go) remains part of the actual target user base.
   `MindEngine.hasSufficientRam()` checks total device RAM via
   `ActivityManager` before ever attempting to load the on-device model.
   On devices below the threshold (`BuildConfig.ONDEVICE_MODEL_MIN_DEVICE_RAM_BYTES`,
   currently 3GB as a placeholder pending real device testing):
   - The on-device model is never downloaded or loaded (no wasted
     bandwidth/storage, no OOM risk).
   - All other Auriga features — hazard detection, distance/bearing,
     guidance, audio, haptics, place memory — are completely unaffected.
   - The cloud LLM remains available normally if the device is online
     (`AurigaPipeline.askMind()` checks RAM first and routes accordingly,
     with a specific spoken message distinguishing this case from a
     generic failure — see `MindEngineState.OnDeviceUnavailableLowRam`).

## Still-open risks, ranked by probability

1. **MediaPipe Tasks Vision API surface drift.** `ObjectDetectionEngine.kt`
   still uses `com.google.mediapipe:tasks-vision:0.10.21` for object
   detection (this part was NOT affected by the tasks-genai deprecation —
   Tasks Vision and Tasks GenAI are separate APIs with separate maintenance
   status). MediaPipe's API has changed method/builder names across
   versions before; if the build fails here, check the exact installed
   version's javadoc for the real builder method names and adjust.

2. **LiteRT-LM artifact version pinning.** `app/build.gradle.kts` currently
   pins `com.google.ai.edge.litertlm:litertlm-android:0.13.1`. Preserve the
   pin and upgrade it only after checking the release notes, Kotlin metadata
   compatibility, and a real Android build.

3. **`BuildConfig` fields referenced before sync.** `CloudMindClient.kt` and
   `ModelDownloadManager.kt` reference fields like `BuildConfig.CLOUD_LLM_ENDPOINT`
   and `BuildConfig.ONDEVICE_MODEL_HF_REPO`. These are generated from the
   `buildConfigField(...)` calls in `app/build.gradle.kts` —
   `buildFeatures { buildConfig = true }` is set, which should be
   sufficient, but double check after first sync.

4. **CameraX / MediaPipe Image type bridging.** `ImageConversion.kt` does a
   YUV_420_888 -> NV21 -> JPEG -> Bitmap round trip, which works but is not
   the most efficient path. Chosen for implementation simplicity/correctness
   over performance for MVP v1. If frame rate is too low on real devices,
   replace with a direct YUV->RGB conversion.

5. **Gradle/AGP/Kotlin/KSP version compatibility.** Current versions are
   AGP 8.10.1, Kotlin 2.3.21, KSP 2.3.9, Gradle 8.11.1, and compile/target
   SDK 34. KSP must remain compatible with the selected Kotlin version.
   Confirm the first Gradle failure against this actual toolchain rather than
   relying on stale version notes from the older Java project.

6. **`gradle-wrapper.jar` provenance.** Copied from a prior working AURIGA
   project rather than freshly generated (no network access available to
   download Gradle's official wrapper jar in the build environment). It is
   a real, valid jar (verified as a valid zip archive). Regenerate yourself
   once you have the project locally for full certainty:
   `gradle wrapper --gradle-version 8.5`.

7. **Model provenance and integrity.** Debug and release workflows download
   the Qwen3 `.litertlm` file and EfficientDet Lite0 detector into the
   expected asset locations. Both assets are checked against pinned SHA-256
   digests; update a digest only after reviewing the upstream revision and
   validating the replacement file.

8. **On-device LLM model download is a ~345MB CI step.** The build
   workflow downloads the public Qwen3-0.6B dynamic INT4 `.litertlm` file
   (cached across runs via `actions/cache@v4`) and bundles it into the APK.
   This makes the
   resulting debug APK substantially larger than a build without it. If
   you'd rather ship a smaller APK and rely on `ModelDownloadManager`'s
   runtime download path instead, remove the three model-bundling steps
   from the workflow — both paths exist in the codebase; CI bundling is
   additive, not required.

## What was NOT possible to verify

- No Android Gradle build or JUnit run was possible: JDK 17 is now available,
  but this environment has no Android SDK configured. The SDK license was not
  accepted on the user's behalf.
- Geometry JUnit tests have been added. A standalone Kotlin 2.1.20 harness
  compiled and exercised the pure geometry, orientation, confidence, and
  speech-gating code, but that is not a substitute for the Android Gradle test
  task or an Android build.
- No real device or emulator testing.
- Dependency version numbers (LiteRT-LM, KSP, MediaPipe Tasks Vision) were
  cross-checked against live web search results during this update, which
  is meaningfully more reliable than the original pass's training-data-only
  guesses, but still not the same as a live Maven dependency resolution.

## Recommended first steps after pushing to GitHub

1. Open Actions and watch the `Build Auriga APK` workflow run.
   The first run will take longer than usual (~345MB model download);
   subsequent runs reuse the cached model file.
2. If it fails, read the Gradle error output — it will point at the exact
   file/line. Start with the still-open risks above in order.
3. Once it builds: object detection will not work until a `.tflite` model
   is bundled (the on-device LLM model IS bundled by this workflow, unlike
   in the prior revision of this document). Both failure modes produce
   clear, specific log messages rather than crashing — that's expected,
   and is a deliberate design choice (never swallow a failure silently),
   not a regression.
