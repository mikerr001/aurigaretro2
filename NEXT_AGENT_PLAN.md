# AURIGA Next-Agent Execution Plan and Project Analysis

## 1. Purpose of this handoff

- This document is the working handoff for the next agent who continues AURIGA.
- It combines verified repository facts, user-confirmed product decisions, and scoped future work.
- It is intentionally an execution plan, not a claim that every item below is already implemented.
- The current agent has made a selective merge from the supplied older AURIGA archive.
- The current agent has selected and configured a LiteRT-LM-compatible Qwen3 model.
- The current agent imported the active Android source and added the planned geometry prototypes. JDK 17 is available, but no Android SDK is configured here.
- The next agent must inspect the actual branch and GitHub Actions result before relying on this status.
- The user requested this written handoff before the current code is pushed.
- The user explicitly chose the Qwen3-0.6B LiteRT-LM route.
- The user asked that the plan prioritize useful, sequenced work for a following agent.
- The handoff should reduce repeated investigation, not replace direct code inspection.
- Current code remains the source of truth for implementation details.
- User-confirmed decisions in this document take precedence over older assistant proposals.
- Historical archive content is evidence of prior work, not automatic product direction.
- If a new request conflicts with a recorded user decision, ask before changing the product boundary.

## 2. Executive status at handoff

- The active product is AURIGA, an accessibility navigation co-pilot.
- The Android application is a Kotlin app under the `android/` subproject.
- The repository is a pnpm workspace with separate web artifacts alongside Android.
- The current Android path uses CameraX, MediaPipe Tasks Vision, and LiteRT-LM.
- A device-calibration catalog from the older Java archive is now available as fallback data.
- Real Camera2 lens characteristics retain priority over catalog-derived values.
- The catalog is explicitly treated as approximate metadata, not measured calibration.
- Camera permission is checked before requesting the camera foreground service.
- The root GitHub Actions Android build downloads both model assets.
- The on-device LLM route is now Qwen3-0.6B dynamic INT4 in `.litertlm` format.
- The model repository revision and SHA-256 are pinned for reproducible distribution.
- The exact selected model artifact is about 345 MB.
- The object detector remains EfficientDet-Lite0 in `.tflite` format.
- The current CI workflow is `.github/workflows/build-apk.yml`.
- The nested `android/.github/workflows/` workflow was removed because GitHub ignores it.
- The older Java application was not copied wholesale into the Kotlin application.
- The LingBot Map archive is a separate research/server component, not a mobile library.
- The Claude share contains useful user decisions and architecture discussion.
- Local compilation remains unverified until an Android-enabled runner executes the workflow.
- The next agent should begin with CI results, then address the P0 checks below.

## 2.1. Fusion checkpoint — October 21, 2026

- The native Android subtree from `auriga-app5` is present in this workspace; the archive's web landing/dashboard surfaces were not part of the Android fusion.
- Camera2 horizontal and vertical sensor FOVs are mapped to the rotated analyzed-frame axes, with cache entries keyed by frame dimensions and rotation.
- Ground-plane math, anonymous person-scale camera-height inference, a visual-horizon candidate, and a coarse accelerometer orientation gate now have focused Kotlin tests.
- Assumed-width pinhole distances are capped at 0.25 confidence with at least 35% uncertainty. Low-confidence estimates no longer drive TTC/proximity scoring or exact spoken ranges.
- The ground-plane feature flag remains off. Current image-boundary confidence is capped at 0.35 and the anonymous person-height prior at 0.25, below the estimator's live-use threshold.
- The shared LingBot example remains research-only; no remote endpoint, weights, or camera streaming code was imported.
- A standalone Kotlin harness passed the pure geometry and speech-gating checks. Android Gradle tests/build remain blocked by the missing SDK; the Google SDK license was not accepted on the user's behalf.
- See `docs/AURIGA_VARIANT_REVIEW.md`, `docs/GEOMETRY_INTEGRATION.md`, and `docs/LINGBOT_MAP_EVALUATION.md` for the merge choices and current limitations.

## 2.2. October 21 fused handoff review

- The new `DrakoSanctis-Fused` archive is a distinct Java/Camera2 prototype, not a drop-in replacement for the active Kotlin app. Its core regression script passed 167 deterministic Java checks when run from a temporary extraction.
- Reuse its safety contract: camera-only spatial ground truth, physically trained calibration, stable visual plane evidence, anchor continuity, and a real observed ground-contact point. Do not treat its test pass as an Android build or device validation.
- Do not import its duplicate native app, four product flavors, browser products, or signing material. Keep the single AURIGA consumer scope.
- The default Kotlin app does not start an accelerometer listener. The user-approved coarse orientation rejection gate is only started if the experimental geometry flag is enabled; it never supplies camera pitch or range.

## 3. Product mission and boundary

- AURIGA is an accessibility navigation co-pilot for physical and digital environments.
- Physical navigation and digital accessibility are related product areas but require distinct safety controls.
- The product must help blind and low-vision users understand nearby hazards and routes.
- It must communicate uncertainty instead of presenting estimates as exact measurements.
- It must degrade safely when camera, models, network, or sensors are unavailable.
- It is not a medical device and must not claim clinical or diagnostic capability.
- It must not identify people or build persistent identity profiles from camera input.
- It must not use gait or face re-identification as a navigation feature.
- It must not continuously expose a user's location to relatives or institutions by default.
- The older archive's Navi, Sentinel, Aero, and Industrial product family proposals are not current scope.
- Do not reintroduce those separate vertical product identities without a fresh user decision.
- Do not import archive pricing, investor plans, or marketing claims as settled decisions.
- The product should stay useful when cloud access is unavailable.
- Any cloud service should be an optional enhancement, never a hidden requirement for core safety.
- Accessibility user control and privacy must outweigh analytics or growth goals.
- The user wants meaningful B2C value and views institutional support as complementary.
- The user's current accessibility scope includes physical and digital access.
- Keep user-facing language direct, calm, and non-alarming.
- Prefer explicit "unknown" and "not measured" states to fabricated precision.

## 4. User-confirmed technical and product decisions

- The on-device model choice for this iteration is Qwen3-0.6B LiteRT-LM.
- The selected artifact is the dynamic INT4 `.litertlm` package.
- The model choice supersedes previous Qwen2.5-0.5B documentation.
- Qwen2.5-0.5B's supplied repository contains `.task` and `.tflite` files for MediaPipe.
- Those Qwen2.5 files are not interchangeable with LiteRT-LM container files.
- Keep LiteRT-LM unless the user explicitly requests a runtime migration.
- Keep low-RAM graceful degradation: navigation remains available if the model is skipped.
- The camera should run continuously while the user is using the navigation experience.
- Do not add a manual shutter-button requirement to the core navigation flow.
- Use vertical field-of-view and image position for elevation/depression geometry.
- Do not use accelerometer values as a precision camera-pitch estimate.
- The user accepted only a coarse orientation sanity gate for sideways or upside-down handling.
- Virtual Anchor should improve ground-plane trigonometry, not become a competing distance engine.
- The user prefers automated camera-height inference over calibration gestures.
- Person scale may be used only as anonymous, approximate geometry when visibly relevant.
- Do not save person identity, face embeddings, gait signatures, or long-term person tracks.
- Family check-in is worth designing, but it must be consent-based and limited.
- The user wants to discuss LingBot Map and the Claude share after this merge.
- The user asked for a long, actionable handoff plan in the repository.
- The user requested a push after the plan and current changes are complete.

## 5. Source material and provenance

- The current Android app is the main implementation target.
- The `_AURIGA-main` archive is an older, separate Java Android application.
- That archive predates the Kotlin architecture in the current project.
- It contains useful vision and calibration ideas, but also duplicate app classes.
- Its default Galaxy A05 profile is explicitly marked as a placeholder.
- Do not treat placeholder or synthesized values as measured device calibration.
- The archive contains a 115-entry device calibration catalog.
- Catalog entries include manufacturer/model or codename labels and derived camera fields.
- The catalog was carried into the current app only as an approximate fallback.
- The archive contains Java implementations of camera, calibration, odometry, sonar, haptics, and orchestration.
- Several of those responsibilities already exist in the Kotlin application.
- Copying old Java app classes beside the current classes risks duplicate names and behavior.
- The archive's multiple product-profile concept conflicts with the current single AURIGA product boundary.
- The `lingbot-map` archive is a separate Python project for streaming 3D reconstruction.
- Its model/runtime stack is Python, PyTorch, CUDA, and GPU-oriented dependencies.
- Its Android integration notes describe a frame streamer to a remote server.
- The archive's example server address and cleartext transport are demonstration details only.
- Never reuse an example IP address or unauthenticated endpoint in production.
- The Claude share is product context, not a replacement for tests or current source inspection.
- Distinguish user-confirmed answers from Claude's recommendations when using that share.
- When in doubt, preserve citations or point to the public upstream README rather than copying large source files.

## 6. Current Android architecture to preserve

- `MainActivity` owns user interaction and permission-gated startup.
- `AurigaApplication` initializes process-level Android state.
- `AurigaCoPilotService` supports the foreground navigation session.
- `CameraEngine` uses CameraX for broader OEM compatibility.
- `ImageConversion` converts captured frames for downstream perception.
- `ObjectDetectionEngine` uses MediaPipe Tasks Vision.
- `AurigaPipeline` coordinates camera, detection, geometry, hazards, guidance, audio, and memory.
- `BearingEngine` estimates horizontal bearing from image position and camera field of view.
- `VirtualFiducialEngine` currently contains the simpler assumed-width distance approach.
- `ObjectTracker` is responsible for cross-frame matching and velocity estimates.
- `HazardScoringEngine` scores severity and confidence.
- `HazardPredictionEngine` predicts time-to-collision when the track signal is usable.
- `GuidanceEngine` turns hazard assessments into short spoken prompts.
- `AudioEngine` prioritizes speech and should remain safe across callback threads.
- `HapticEngine` supports nonverbal directional cues.
- Room-backed persistence holds the current lightweight place and observation data.
- `MindEngine` uses the LiteRT-LM Kotlin API for on-device language tasks.
- `ModelDownloadManager` resolves the bundled or downloaded model asset.
- `CloudMindClient` remains an interface/placeholder until a user approves a provider.
- Keep core hazard and navigation behavior independent from the LLM.
- Do not let a model response override deterministic hazard detection or emergency guidance.

## 7. What the current merge intentionally brought forward

- The device catalog is stored as an Android asset.
- `DeviceCalibrationCatalog` reads and filters the catalog at runtime.
- Catalog matching uses normalized manufacturer plus exact model/codename identity.
- Catalog entries must be marked approved before they are considered.
- A catalog profile is used only if the current device can be matched exactly.
- `CameraCalibrationManager` asks Camera2 for current physical camera characteristics first.
- A successful Camera2 result remains the highest-priority calibration.
- A matching catalog profile is used only when live camera characteristics are unavailable.
- A generic fallback remains available when neither source can provide a profile.
- The calibration source is carried through the profile so diagnostics can distinguish provenance.
- Catalog-based values are marked as derived rather than real device measurements.
- The service startup path now checks camera permission before launching the foreground service.
- A denied or revoked permission no longer triggers a blind camera-service start.
- Foreground-service start failures are logged without silently killing the visible activity flow.
- The root build workflow now owns the Android build and model-download steps.
- Model assets are placed under the paths the Kotlin runtime actually opens.
- The object detector is downloaded to its expected asset path.
- The handoff plan documents what remains out of scope and why.

## 8. What was deliberately not copied from the old AURIGA archive

- Do not copy its Java `MainActivity` into the Kotlin package.
- Do not duplicate `CameraService` when CameraX and the current service already exist.
- Do not copy Java detection classes that duplicate MediaPipe pipeline behavior.
- Do not wire the old ground-plane LUT directly into the current assumed-width formula.
- Do not use the archive's synthetic device profile as a trusted camera calibration.
- Do not merge its four vertical app brands into the current single product.
- Do not copy the old release keystore or any keystore-like file.
- Do not copy old `local.properties` into the project.
- Do not copy nested ZIPs or large model weights into Git.
- Do not treat the archive's architecture slide or business plan as current product approval.
- Revisit one legacy subsystem at a time only after a concrete gap is identified.
- Prefer a focused Kotlin port with tests over a wholesale Java package transplant.
- Keep the old archive available in attached assets as source material during this discussion.
- Remove or archive uploaded ZIP files only if the user later asks for workspace cleanup.

## 9. Current model decision and compatibility facts

- The user chose Qwen3-0.6B LiteRT-LM after the format mismatch was explained.
- The selected repository is `litert-community/Qwen3-0.6B`.
- The selected artifact is `Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm`.
- The artifact is approximately 344,671,744 bytes.
- The artifact is published as a LiteRT-LM model and uses the `.litertlm` container format.
- The current model revision is pinned in Gradle configuration and CI.
- The model SHA-256 is pinned to `03e7da1eb1108b50dffaa9bb52cc7bcbad2eb0c66ca990267f480c1e545d2856`.
- The selected revision is `e6687177e00897731e7716267eebd4eba0dde7c2`.
- The repository is public and does not require a Hugging Face token for download.
- The model is Apache-2.0 licensed according to its model metadata.
- The workflow must never silently fall back to Qwen2.5 `.task` or `.tflite`.
- The workflow must never rename a `.task` or `.tflite` file to `.litertlm`.
- The app runtime must use the same repository, revision, filename, and checksum as CI.
- The selected smaller artifact is a different generation from the previously preferred Qwen2.5 model.
- Validate language quality, especially Swahili and the remaining target languages.
- Validate task following and refusal behavior before enabling sensitive actions.
- Validate cold-load latency and repeated request performance on target devices.
- Validate actual peak memory rather than assuming the filename quantization guarantees a RAM fit.
- The current 3GB RAM threshold is a protective placeholder until hardware testing supports a better value.

## 10. CI model-download pipeline requirements

- Keep the Android debug APK workflow at `.github/workflows/build-apk.yml`.
- Separate root workflows currently cover release packaging and general CI.
- Do not create a duplicate Android debug workflow under `android/.github/workflows/`.
- The workflow triggers for Android source changes and manual dispatch.
- The workflow should verify that the pinned Hugging Face revision exists.
- The workflow should verify the exact expected artifact filename.
- The workflow should fetch the artifact from the pinned revision, not a mutable `main` URL.
- The workflow should check the downloaded file exists and is at least 300MB.
- The workflow should compute SHA-256 and compare it to the pinned value.
- Any checksum mismatch must fail the action before Gradle packages the APK.
- Keep the model cache keyed by model family, quantization, pinned revision, and filename.
- Keep the object detector cache separate from the LLM cache.
- Keep model blobs out of Git history.
- Do not include personal tokens or secrets in the Hugging Face URL.
- Keep all `curl` calls fail-fast and retry transient failures.
- Set an explicit maximum retry policy to avoid endless CI runs.
- Ensure the workflow writes to `android/app/src/main/assets/models/`.
- Ensure the packaged names match `BuildConfig.ONDEVICE_MODEL_FILENAME`.
- Keep the model-size output visible in the build logs.
- Keep the object detector size verification in place.
- Consider adding a cached checksum manifest only if it improves reproducibility.
- Do not silently skip the LLM when the selected artifact is unavailable.
- If a future model is selected, update the workflow, BuildConfig, runtime downloader, docs, and tests together.

## 11. Runtime model download and fallback path

- The app should first look for the model in its packaged assets.
- It should copy a valid asset into app-private storage before initializing LiteRT-LM.
- The fallback URL should be composed from pinned repository, revision, and exact artifact filename.
- The runtime downloader should verify minimum size.
- The runtime downloader should verify the pinned SHA-256 before reporting success.
- A failed checksum should delete the corrupted file and return a specific failure result.
- Never load a partial download just because it is nonempty.
- Never treat a different model filename as an acceptable substitute without a migration.
- Keep download progress available to the user interface.
- State the expected download size before starting a runtime download.
- Warn before downloading a large model over mobile data.
- Consider Wi-Fi-only default for first download, with explicit user override.
- Avoid blocking the camera analysis thread while model download or initialization runs.
- Keep cancellation possible if the user leaves the model setup screen.
- Do not log user utterances when reporting model download or initialization errors.
- Keep the on-device route unavailable gracefully when the device is below the RAM threshold.
- Keep cloud routing optional and explicitly online-only.
- Do not hardcode cloud credentials into BuildConfig.
- If a cloud provider is chosen, use the workspace secrets flow for credentials.
- A full model update should remove stale app-private files after successful migration.

## 12. Camera profile catalog: intended semantics

- Treat the JSON catalog as a fallback directory, not a measurement database.
- Its profile values are manufacturer/model-derived and may differ from the actual rear lens.
- A phone's Camera2 characteristics are more authoritative whenever they are available.
- Camera2 output must identify which physical camera lens was selected.
- Verify that the current camera ID corresponds to the active CameraX lens.
- The current catalog matcher should not match by fuzzy marketing-name substring.
- Normalize case and common punctuation but preserve exact identity matching.
- Manufacturer comparison may be case-insensitive.
- Model and codename comparisons should be exact after normalization.
- Reject profiles with missing or nonfinite field-of-view values.
- Reject implausible horizontal field-of-view values outside the documented range.
- Do not infer a model from device brand alone.
- Do not apply one regional SKU's calibration to all variants of a phone.
- Distinguish horizontal FOV from vertical FOV in both storage and calculations.
- If the legacy catalog lacks vertical FOV, derive it only from sensor aspect ratio with provenance.
- Do not represent a derived vertical FOV as directly measured.
- Keep source labels in logs and support screens.
- Unknown devices should use a clearly marked generic approximation.
- A confidence score should decrease when profile values come from catalog fallback.
- User-facing distance guidance should reflect the lower confidence.
- Catalog updates should include source, date, and validation notes.
- Before release, provide a way to report a wrong catalog match without sharing raw images.

## 13. Camera calibration validation work

- Add unit tests for case normalization and exact device matching.
- Add tests for manufacturer mismatch.
- Add tests for marketing model name not equal to internal device codename.
- Add tests for missing camera FOV values.
- Add tests for rejected FOV values below the supported range.
- Add tests for rejected FOV values above the supported range.
- Add tests for catalog approval flags.
- Add tests proving Camera2 success beats catalog fallback.
- Add tests proving catalog match beats generic fallback only after Camera2 failure.
- Add tests proving unknown devices reach the generic fallback.
- Add tests that persist and reload the calibration source.
- Add tests that persisted derived values remain marked as non-real.
- Add tests for orientation changes that alter image aspect ratio.
- Add tests for crop/zoom causing the effective FOV to differ from sensor FOV.
- Test front-camera selection does not use a rear camera catalog profile.
- Test wide, ultrawide, and telephoto camera selection where supported.
- On a Galaxy S22 Ultra, compare actual active Camera2 metadata with the chosen fallback.
- On one budget Samsung device, verify generic/derived fallback behavior.
- On one Tecno or Infinix target phone, verify device identity matching behavior.
- Document which test devices were physical and which were emulators.
- Do not use emulator results to claim camera calibration is validated for hardware.
- Do not update profile values from a single test image without a measurement method.
- Capture calibration test outcomes without retaining identifiable bystanders.
- Revisit whether the catalog should stay packaged or move to signed remote updates.

## 14. Foreground service and permission startup

- Camera permission must be granted before asking Android to start camera work.
- A permission denial should leave a clear, accessible explanation and a retry route.
- Do not request the foreground service while the activity is merely displaying a permission dialog.
- Start the foreground service only from an allowed, user-visible interaction.
- Handle the platform's foreground-service start restrictions explicitly.
- Keep the camera visible state truthful when the service could not be started.
- Do not claim background navigation is running if CameraX has stopped with the activity lifecycle.
- Confirm that the service's declared foreground-service types match its actual behavior.
- Confirm the notification remains visible for the duration Android requires.
- Ensure notification controls include a safe stop action.
- Ensure the service releases camera resources on stop or permission revocation.
- Re-check permission after returning from system settings.
- Test denied once, denied permanently, granted, revoked, and process-recreated flows.
- Test Android 12 and Android 14+ behavior separately.
- Test low-memory app restarts while the service is active.
- Avoid catching every `Throwable` and hiding programming defects.
- Log failure types and user-visible recovery without logging camera content.
- Keep startup state transitions deterministic and testable.
- Do not couple foreground service startup to LLM initialization.

## 15. Core distance and elevation roadmap

- Treat ground-plane or suspended-object trigonometry as the intended primary distance method.
- Keep assumed-width pinhole distance as a last-resort fallback.
- Determine the target object's relevant pixel point before calculating angles.
- For floor-contact hazards, use the bottom-center of a stable detection box as a candidate contact point.
- Do not assume every object's bottom-center touches the floor.
- Distinguish ground-contact, tabletop, wall-mounted, and overhead detections.
- Derive vertical FOV from real sensor intrinsics when possible.
- Otherwise derive vertical FOV from horizontal FOV and the active image aspect ratio.
- Keep the derivation source explicit in the calibration object.
- Compute image-relative elevation/depression from vertical FOV and pixel offset.
- Do not use accelerometer pitch as the precision elevation angle.
- Use the accelerometer only for a coarse wrong-orientation sanity gate.
- Suppress or downgrade geometric readings when the phone is sideways or upside down.
- Estimate camera height from anonymous scene/person/architecture scale only with confidence reporting.
- Do not require a user calibration gesture for basic navigation.
- Use visual geometry to improve camera-height estimates where enough evidence exists.
- Mark camera-height estimates as inferred, not measured.
- Keep distance uncertainty tied to detector box quality and camera calibration provenance.
- Compare trig distance with width-derived distance when both are available.
- Lower confidence when the methods disagree beyond an empirically defined tolerance.
- Never average incompatible readings blindly.
- Avoid speaking exact centimeter values for low-confidence estimates.
- Prefer categories such as near, approaching, or distance uncertain where measurement error is high.
- Maintain an audit trail of which method produced each reported estimate.
- Add recorded synthetic test cases for horizon shifts, floors, and overhead objects.
- Build one method at a time; do not replace several safety-critical engines in one change.

## 16. Virtual Anchor and room geometry

- Virtual Anchor is an accuracy multiplier for ground-plane trigonometry.
- It is not a parallel synthetic distance engine.
- It should improve the location of the horizon, room planes, and floor-contact geometry.
- Begin with line and vanishing-point extraction before attempting full room reconstruction.
- Reuse validated horizon-solving experiments if they are present in the workspace.
- Verify any previous notebook or success-rate claim directly before treating it as production evidence.
- Detect candidate near-horizontal and near-vertical structural lines.
- Group lines by likely vanishing direction and geometric consistency.
- Distinguish floor-wall boundaries from table edges and furniture lines.
- Track line confidence and the number of supporting pixels.
- Estimate a horizon only when there is enough line evidence.
- Use a no-room-edges path when the scene is outdoors or visually unstructured.
- Do not invent wall locations to make a virtual room look complete.
- Treat occlusion completion as a later enhancement, after visible geometry works.
- If a line is partially occluded, extrapolate only along a strongly supported vanishing direction.
- Keep the source frame and geometry transient unless the user explicitly saves a map.
- Validate geometry with varied corridors, open rooms, stairs, glass, and low-texture walls.
- Evaluate daylight, low light, motion blur, camera shake, and clutter separately.
- Add a fallback to floor-contact trig when room-edge detection is unavailable.
- Compare Virtual Anchor estimates to a physical reference distance in a controlled test.
- Establish failure thresholds before enabling voice prompts based on the estimate.
- Record confidence and failure class, not raw frames, in diagnostics by default.
- Measure runtime cost and thermal load on low-end phones.
- Keep the feature behind an internal flag until field-tested with blind users.

## 17. Sensor policy

- Camera intrinsics, developer calibration, visual anchors and plane geometry are the only spatial ground truth.
- Do not use accelerometer, gyroscope, magnetometer, GPS, ARCore/ARKit, LiDAR or cloud depth as spatial ground truth.
- The coarse accelerometer refusal gate is optional, never supplies pitch or range, and remains inactive unless experimental geometry is enabled.
- Do not fuse noisy inertial data into distance output without a quantified error model.
- Detect portrait, landscape, upside-down, and extreme tilt conditions coarsely.
- Provide a short warning or suppress geometry when the camera orientation is unsupported.
- Avoid excessive sensor sampling because it wastes battery and creates noisy data.
- Document sensor units, coordinate frames, and orientation conventions in code.
- Test device rotation, stabilization, and camera crop effects.
- Do not assume image center is the horizon in every environment.
- Use line-based evidence or a low-confidence default when no horizon is available.
- Keep sensor fallback behavior separate from the primary camera calibration logic.
- A sensor signal should never silently increase confidence without evidence.
- Log only orientation class and confidence, not continuous raw sensor streams.
- Revisit this policy only after real-device validation proves a measurable benefit.

## 18. Object detection and temporal tracking

- Confirm EfficientDet-Lite0's label set matches the app's hazard mapping.
- Confirm the model file is loaded from the expected asset path.
- Verify object detector initialization failure is visible and does not crash camera startup.
- Measure detector latency and frame throughput on budget phones.
- Keep camera frame closure in a `finally` block.
- Keep analyzer backpressure configured to avoid an unbounded frame queue.
- Do not store camera frames for routine model diagnostics.
- Track detections across frames only to estimate motion and time-to-collision.
- Use class plus IoU/centroid proximity as an explainable baseline tracker.
- Cap the persistence of a track after its object disappears.
- Avoid identity re-identification across sessions or locations.
- Do not store biometric or appearance embeddings.
- Verify that multiple identical detections cannot collide in a map key.
- Test false matches when people cross or vehicles pass each other.
- Require stable track confidence before announcing approach speed.
- If speed is unknown, do not fabricate a time-to-collision.
- Ensure hazard detection continues if voice or model components fail.
- Add tests for detector labels, missing labels, empty frames, and corrupt model files.

## 19. LingBot Map: what the second ZIP contains

- LingBot Map is a streaming 3D reconstruction research project.
- Its repository is Python-based and expects a modern machine-learning stack.
- Its declared dependencies include PyTorch/torchvision, image tooling, and model support.
- Its documented accelerated path targets CUDA-capable GPUs.
- The project advertises roughly 20 FPS at about 518 by 378 input on its target hardware.
- That reported throughput is not a measurement on Android phones.
- The archive includes a server/demo path that accepts camera frames over WebSocket.
- The example Android integration encodes frames as JPEG and queues them for upload.
- Its recommended transmission rate is around five frames per second.
- It expects the server operator to run the reconstruction process and viewer.
- The integration guide also describes a CameraX source for development.
- It is not a packaged Android SDK or drop-in TFLite model.
- The model's compute requirements make direct offline phone deployment unlikely without a new port.
- The archive's sample server URL uses plain `ws://` and a hardcoded address.
- That sample address must never ship in the app.
- The project is Apache-2.0 licensed according to its README.
- Confirm the exact upstream license and model license before distributing adapted components.
- Treat the second ZIP as a separate prototype/research service, not part of the Android merge.
- It may have research value for scene mapping or visual relocalization.
- It should be compared to the current camera/geometry baseline before any product adoption.

## 20. LingBot Map: feasible evaluation path

- First determine the desired user outcome: immediate hazard detection, map memory, or relocalization.
- Do not evaluate a 3D reconstruction tool by visual quality alone.
- Define whether any processing must work offline.
- Define whether sending camera frames to a remote server is acceptable.
- Define who owns, hosts, monitors, and pays for that server.
- Define whether the target camera is a phone or wearable glasses.
- Do not assume Meta Wearables integration is already available in the current app.
- Build a separate feasibility prototype only after the user agrees to remote processing.
- Use an authenticated `wss://` endpoint with TLS for any network test.
- Never permit arbitrary server URLs from untrusted configuration.
- Use short-lived credentials and avoid embedding long-lived access tokens in APKs.
- Add explicit user consent before the first frame is transmitted.
- Provide visible and spoken indication while streaming is active.
- Provide a one-action stop control that immediately stops frame upload.
- Keep an offline baseline that works when the server is unreachable.
- Rate-limit, compress, and bound the queue to prevent unbounded memory use.
- Measure end-to-end latency, dropped frames, data usage, battery, and heat.
- Measure accessibility task completion, not only reconstruction FPS.
- Test weak cellular networks and Wi-Fi loss.
- Decide whether maps are transient, session-local, or saved by the user.
- If maps are saved, define retention, export, deletion, and access controls.
- Review model and dataset licenses independently from the code license.
- Do not reuse the archive's server IP or cleartext Android configuration.
- Compare map quality with simple room-edge geometry before adding new services.
- Record a go/no-go decision with evidence before production integration.

## 21. Digital accessibility and app task execution

- The user intends to explore opening apps and interacting with phone tasks.
- The proposed scope includes reading or drafting messages, scheduling tasks, and notifications.
- This work is distinct from camera-based physical navigation.
- Start by listing user goals, not by enabling broad system access.
- Map each goal to the narrowest Android API or user-mediated flow.
- Evaluate whether Android AccessibilityService is necessary for each action.
- Review current Google Play policy requirements before using AccessibilityService.
- Do not bypass OS permission prompts or app security boundaries.
- The user must explicitly enable any AccessibilityService in Android settings.
- Explain what the service can observe and which actions it can perform.
- Use a prominent persistent indicator while broad accessibility access is active.
- Prefer Android intents, notification actions, calendar APIs, and share sheets when sufficient.
- Require confirmation before sending, deleting, publishing, or committing a message/task.
- A model may draft content but must not autonomously send it without confirmation.
- Confirm recipient and message content aloud or in a clear accessible preview.
- Avoid reading private notification contents aloud in public settings by default.
- Provide a mode to redact message bodies or sensitive names.
- Keep high-impact operations allowlisted and auditable.
- Use strict command schemas rather than executing unconstrained model-generated actions.
- Reject malformed, ambiguous, or out-of-scope model action requests.
- Never expose authentication tokens or passwords to the model.
- Keep local action history minimal and user-deletable.
- Make cloud processing explicit for any content leaving the device.
- Design this as a separate scoped phase after physical navigation reliability work.
- Do not bundle digital task execution into the next patch without a user-approved design.

## 22. Privacy and safety rules

- Camera frames are sensitive even when no person is intentionally identified.
- Keep frames in memory only for the shortest processing window practical.
- Do not upload camera frames by default.
- Do not save raw frames in crash reports.
- Avoid logging exact user movement paths unless the user explicitly opts into research.
- Keep crash diagnostics free of message contents and personal identifiers.
- Do not infer identities from faces, gait, clothing, or repeated visits.
- Do not build hidden behavioral profiles from navigation use.
- Separate optional analytics from safety-critical processing.
- Allow analytics opt-out without disabling core navigation.
- Use local storage encryption where durable sensitive records are necessary.
- Provide deletion controls for saved places and history.
- Keep retention windows short and documented.
- Protect all network endpoints with TLS.
- Authenticate any remote spatial mapping service.
- Minimize telemetry to error class, model version, device capability bucket, and opt-in performance data.
- Do not log raw coordinates for routine reliability analytics.
- Define a privacy impact review before launching family or institution dashboards.
- Avoid claims that a user is "safe" based only on app connectivity.
- Provide a clear failure state when confidence is too low.
- Design for a lost phone, stolen phone, and revoked permission scenario.
- Use threat modeling before enabling automatic cross-app actions.
- Do not use a user's disability status as a reason to weaken security.
- Ensure consent can be revoked without requiring support intervention.
- Keep accessibility controls usable with screen readers, switch access, and speech input.

## 23. Family check-in concept

- The user has said a family check-in feature is worth designing.
- Treat it as reassurance, not surveillance.
- Do not expose continuous live location by default.
- Do not expose the user's spoken queries or message contents.
- Share only minimal signals that the user explicitly chooses.
- Let the user select recipients and the duration of sharing.
- Make the active sharing state visible and understandable.
- Provide a clear pause/stop control.
- Allow revoking a recipient immediately.
- Avoid notifying a family member that the user is "safe" without a valid, user-approved signal.
- Do not treat missing check-ins as proof of an emergency.
- Establish failure behavior when the phone is offline or battery is low.
- Protect against coercive sharing by making consent revocable and private.
- Offer on-device scheduling rather than always-on tracking where feasible.
- Run a privacy and abuse-case review before implementation.
- Validate the feature with people with disabilities and their chosen supporters.

## 24. Language and spoken guidance

- The product plans support for 25 languages.
- Swahili is a priority language.
- Include under-served African and South Asian languages in validation.
- Verify each Android TTS voice availability on target OEM devices.
- Keep a confidence tier for voice quality and language completeness.
- Do not claim native-quality speech when the OS voice is unavailable.
- Provide a clear fallback voice or visual/accessible text route.
- Keep navigation sentences short and unambiguous.
- Avoid gendered or stigmatizing descriptions of people in the environment.
- Avoid noisy repeated alerts through priority and cooldown rules.
- Ensure critical hazards can interrupt lower-priority conversational speech.
- Ensure model-generated language never masks deterministic emergency prompts.
- Validate pronunciation of local road terms, place names, and common hazards.
- Provide a way to report poor translation without sharing raw conversation logs.
- Test voice input in quiet, street, transport, and indoor noise conditions.
- Keep the LLM optional for core fixed navigation prompts.
- Evaluate Qwen3 performance in Swahili before assuming the model choice is adequate.
- Test low-bandwidth and offline language behavior.
- Document missing locales instead of silently routing to unrelated voice output.

## 25. User research and success measures

- Include blind and low-vision testers in all safety-critical feature validation.
- Include orientation-and-mobility professionals as reviewers, not as substitutes for users.
- Test walking-speed scenarios in controlled, consented environments.
- Include low-end devices representative of target markets.
- Test with both headset and speaker audio configurations.
- Test sound competition from traffic, stations, and indoor announcements.
- Measure hazard detection recall and false-alert rate.
- Measure distance and bearing error against a physical reference.
- Measure time-to-collision false positive and false negative rates.
- Measure model cold-start time, memory, thermal load, and crash rate.
- Measure task completion and user understanding of uncertainty.
- Track safety reports and adverse events with a careful triage process.
- Do not optimize only for daily active users or session duration.
- Do not present screen time as a proxy for accessibility value.
- Use structured feedback with user consent and data minimization.
- Pay and credit lived-experience contributors appropriately.
- Share what changed after testing with participants.
- Never frame an unvalidated feature as a safety guarantee.

## 26. Automated test strategy

- Add pure unit tests for catalog parsing and profile selection.
- Add tests for malformed JSON and missing optional profile fields.
- Add tests for source provenance propagation.
- Add tests for generic fallback behavior.
- Add tests for Camera2-first behavior using a fake reader abstraction if needed.
- Add a regression test for permission denial before foreground-service startup.
- Add a test for foreground-service start rejection logging/recovery.
- Add tests for distance-method selection and confidence degradation.
- Add tests for vertical FOV conversion across different aspect ratios.
- Add tests for pixel-to-angle sign conventions.
- Add tests for coarse orientation gate states.
- Add tests for no accelerometer precision dependency.
- Add tests for checksum verification of a small injected test file.
- Keep the production Qwen model out of ordinary unit-test fixtures.
- Add a workflow validation step for YAML and shell syntax.
- Add a workflow check that the pinned model file exists at the pinned revision.
- Add a workflow check that the downloaded model checksum matches the pin.
- Add a workflow check that the detector model exists and exceeds its expected size.
- Test missing and corrupt assets explicitly.
- Test low-RAM state without allocating the actual large model.
- Ensure unit tests do not depend on a network connection.
- Ensure deterministic outputs by mocking clocks and device metadata.
- Use instrumentation tests for Android lifecycle and CameraX behavior where practical.
- Keep results and test artifacts out of Git history.
- Include test commands and expected outputs in the PR description.

## 27. Physical-device test matrix

- Include at least one recent Samsung flagship.
- Include the previously referenced Galaxy S22 Ultra for regression comparison.
- Include a budget Samsung A-series phone.
- Include at least one Tecno or Infinix phone in the target market.
- Include a device below the configured LLM RAM threshold.
- Include Android 12 or 13 behavior for service-start comparison.
- Include Android 14 or newer foreground-service enforcement.
- Test rear wide camera selection.
- Test ultrawide selection if the app can select it.
- Test rotation, zoom, and camera stabilization.
- Test camera permission denial and revocation.
- Test model download over Wi-Fi.
- Test model download over cellular with user consent.
- Test model download cancellation and retry.
- Test offline startup with no on-device model available.
- Test low battery and thermal throttling behavior.
- Test noisy outdoor audio and TTS interruption priorities.
- Record only the information needed to reproduce each test.
- Do not collect video without explicit participant consent and retention limits.

## 28. Build and release gates

- Run the single root Android workflow after every change to Android build inputs.
- Confirm the workflow checks out the intended `main` commit.
- Confirm the model repo and revision match BuildConfig.
- Confirm the model filename and SHA match the pinned values.
- Confirm the Android build produces an APK at the expected artifact path.
- Confirm the APK packages the exact `.litertlm` and detector assets.
- Confirm the final APK size is reasonable for the intended distribution channel.
- Confirm the application launches on a physical device.
- Confirm Camera2 data is preferred over a catalog profile on a supported device.
- Confirm the catalog fallback reports its derived source.
- Confirm low-RAM devices skip model loading gracefully.
- Confirm the main camera and hazard path works without internet.
- Confirm the model does not block the main thread.
- Confirm notification and service behavior on target Android versions.
- Do not publish a release solely because CI compiles.
- Do not make production claims from debug APK testing.
- Keep release signing keys in Replit Secrets or a managed signing system.
- Never commit a keystore or key password.
- Preserve a known-good release artifact and rollback path.
- Review license attribution for model and detector assets before distribution.

## 29. Priority sequence for the next agent

- P0: Run the Android build and unit tests in the intended CI/repository; the current workspace has not been pushed to a verified matching remote.
- P0: Fix any Android compile errors before enabling experimental geometry.
- P0: Confirm the exact pinned Qwen3 file was downloaded and its SHA passed.
- P0: Confirm asset path and BuildConfig filename agree exactly.
- P0: Confirm `ModelDownloadManager` runtime URL uses the same pinned revision.
- P1: Add automated tests for calibration catalog exact matching and fallback order.
- P1: Add tests for camera permission gating and foreground service start rejection.
- P1: Run the app on Galaxy S22 Ultra and one budget target phone.
- P1: Record actual model initialization RAM and latency.
- P1: Confirm the Qwen3 language behavior needed for Swahili.
- P2: Complete a camera-only visual plane and ground-contact solver; current geometry prototypes are not sufficient for metric use.
- P2: Add validation for calibration provenance and method disagreement against measured physical targets.
- P2: Keep the Virtual Anchor candidate behind the internal flag until tested across representative scenes.
- P2: Validate geometry with blind users before connecting it to live spoken output.
- P3: Run a LingBot feasibility study without sending user camera frames.
- P3: Decide server/offline, phone/wearable, and retention policy before integration.
- P3: Design digital action permissions separately from physical navigation.
- P4: Design consent-based family check-in only after a privacy review.
- Do not start P2-P4 work while a P0 CI or startup issue remains open.

## 30. Action cards and completion evidence

- Build task: attach the GitHub Actions URL and successful APK artifact name.
- Model task: record source repo, pinned revision, exact filename, byte size, and verified SHA.
- Device task: record manufacturer/model, Android version, RAM tier, and camera selection.
- Calibration task: record whether values came from Camera2, catalog, or generic fallback.
- Geometry task: record reference distance, estimated distance, confidence, and error.
- Orientation task: record expected and actual suppression state for each phone orientation.
- Voice task: record selected locale, TTS engine, voice quality tier, and interruption behavior.
- Offline task: prove core guidance continues without Internet access.
- Privacy task: prove no raw frame or unrelated message content is uploaded.
- LingBot task: record latency, dropped frames, bandwidth, energy, and server status.
- Digital action task: record confirmation behavior for every external side effect.
- Family task: prove sharing can be paused and revoked immediately.
- Release task: record model license, model SHA, APK size, and signing state.
- For each finding, distinguish reproduced fact from unverified hypothesis.
- For each failed test, preserve the smallest useful log excerpt without personal content.
- If an issue is environment-related, state the missing toolchain and use CI evidence.
- Do not mark a feature complete when only its UI or only its backend exists.
- Use user-visible error states instead of silent fallback.
- Update this handoff when a major decision changes, not for routine code edits.

## 31. Known risks and mitigations

- Risk: Qwen3 performs poorly in a target language.
- Mitigation: benchmark spoken-navigation tasks before enabling model output in that locale.
- Risk: model cold-load or memory use is too high on mid-range hardware.
- Mitigation: collect target-device measurements and adjust low-RAM routing.
- Risk: the catalog match describes a different physical camera module.
- Mitigation: exact identity checks, provenance, and physical-device validation.
- Risk: camera crop or digital zoom invalidates FOV assumptions.
- Mitigation: account for active crop region and verify zoom behavior.
- Risk: room lines are confused with furniture edges.
- Mitigation: confidence, multiple supporting lines, and fallback to trig.
- Risk: false approach-speed alerts arise from unstable tracking.
- Mitigation: require stable tracks and suppress predictions without reliable velocity.
- Risk: the detector misses doors, stairs, or locally relevant hazards.
- Mitigation: validate the label taxonomy and gather user-led gap reports.
- Risk: remote reconstruction exposes sensitive environments.
- Mitigation: no default streaming, explicit consent, secure transport, and retention limits.
- Risk: network loss stops remote mapping.
- Mitigation: keep local navigation functional and announce degraded capability.
- Risk: accessibility automation sends a message incorrectly.
- Mitigation: require recipient/content confirmation before sending.
- Risk: family sharing becomes coercive.
- Mitigation: revocable consent, minimal signals, and no continuous location default.
- Risk: CI consumes large storage or bandwidth.
- Mitigation: cache pinned artifacts and evaluate smaller compatible artifacts only with tests.
- Risk: public model files change upstream.
- Mitigation: pin revision and SHA, and update only through reviewed changes.
- Risk: broad archive transplant creates duplicate or contradictory behavior.
- Mitigation: continue selective Kotlin ports with tests.
- Risk: no local toolchain prevents immediate compiler feedback.
- Mitigation: use the root GitHub Actions build and report this limitation accurately.

## 32. Working rules for the next agent

- Read the relevant Kotlin and Gradle files before editing.
- Confirm the repository status and branch before committing.
- Do not overwrite `.replit` or change unrelated web artifacts during Android work.
- Do not add the uploaded ZIPs to a Git commit.
- Keep model binaries and generated APKs out of Git history.
- Use the existing root workflow instead of creating another Android workflow.
- Run `git diff --check` before committing.
- Parse workflow YAML and run `bash -n` on shell steps after changes.
- Verify model sources online before changing filenames or size expectations.
- Do not use model file extensions as evidence of runtime compatibility by themselves.
- Check the Android dependency API before changing model/runtime integration.
- Avoid catch-all exception handling that hides the root cause.
- Keep safety-critical outputs deterministic where possible.
- Keep feature flags around unvalidated perception changes.
- Ask the user before broadening camera upload, tracking, or automation permissions.
- Ask the user before replacing the selected model generation again.
- Propose a checkpoint rollback if a major refactor becomes unsafe to continue.
- If publishing becomes relevant, suggest the publish action rather than publishing autonomously.
- Push only when the user explicitly asks for a remote update.
- Do not put credentials, signing material, or personal data in this document.

## 33. Discussion points to return to with the user

- Ask whether LingBot Map should remain a research reference or become a separate prototype.
- If prototyping, ask whether the target device is phone camera or wearable glasses.
- Ask whether any environment frame may be sent to a server at all.
- Ask who would host and pay for a remote reconstruction service.
- Ask whether maps should be ephemeral or user-saved.
- Ask whether a local-only mapping alternative is preferred despite lower quality.
- Confirm whether the user wants a formal side-by-side evaluation before implementation.
- Discuss the Claude share's Virtual Anchor plan as an accuracy improvement to ground-plane trig.
- Confirm how much user uncertainty should be spoken in an urgent navigation moment.
- Confirm the first set of target hazards for geometry validation.
- Confirm whether anonymized person height is an acceptable optional scale cue.
- Preserve the user's rejection of person identity tracking.
- Discuss digital app control as a separate workflow and security boundary.
- Clarify which message/calendar actions require spoken confirmation.
- Decide which languages are first-release quality targets versus later tiers.
- Decide what family check-in signal, if any, is acceptable after abuse-case review.
- Do not convert unanswered questions into assumptions.
- Capture final decisions in code, tests, and project memory where appropriate.

## 34. Review and acceptance checklist for this handoff

- The product boundary is explicit and aligned with current user decisions.
- The two archives are distinguished by purpose and runtime.
- The old Java application is not represented as already merged wholesale.
- The calibration catalog is clearly described as approximate fallback data.
- Camera2 remains higher priority than catalog values.
- The current foreground-service permission gate is documented.
- The selected Qwen3 model and format are named precisely.
- The model repository revision and checksum are recorded.
- The Qwen2.5 `.task`/`.tflite` incompatibility is explained.
- The app runtime fallback is described alongside CI bundling.
- The LingBot implementation constraints and privacy risks are concrete.
- The Claude share's user-confirmed geometry choices are stated separately from suggestions.
- The future ground-plane and Virtual Anchor steps are ordered.
- Digital accessibility actions are explicitly separated from navigation.
- Family check-in privacy boundaries are documented.
- Testing spans unit, CI, and real devices.
- Release gates include licensing, asset integrity, and APK verification.
- The next-agent priorities are ordered by risk.
- The actions include observable completion evidence.
- No secrets, credentials, or user-identifying data are included.
- The document is an actionable plan, not an assertion that future work is complete.

## 35. Sources and verification notes

- Current Android source and Gradle configuration in this repository.
- Root Android workflow at `.github/workflows/build-apk.yml`.
- The supplied `_AURIGA-main` ZIP, inspected from workspace assets.
- The supplied `lingbot-map-main` ZIP, inspected from workspace assets.
- User-shared Claude conversation at `https://claude.ai/share/1b5b6e07-2215-43ba-ab24-816e8d02cb95`.
- LiteRT-LM Android documentation: `https://developers.google.com/edge/litert-lm/android`.
- LiteRT-LM file builder documentation: `https://developers.google.com/edge/litert-lm/file_builder`.
- Qwen3 model repository: `https://huggingface.co/litert-community/Qwen3-0.6B`.
- Qwen2.5 legacy model repository: `https://huggingface.co/litert-community/Qwen2.5-0.5B-Instruct`.
- LingBot Map upstream repository and included integration guide in the supplied ZIP.
- Online repository metadata was checked for the selected model filename, revision, size, and SHA.
- The direct model asset endpoint returned HTTP 200 after following Hugging Face's redirect.
- JDK 17 is available in this workspace, but Android Gradle build/tests remain blocked because no Android SDK is configured. The Google SDK license was not accepted on the user's behalf.
- The next agent should confirm the pushed GitHub Actions result before marking Android compilation successful.
