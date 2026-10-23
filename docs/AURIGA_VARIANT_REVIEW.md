# AURIGA variant review

## `auriga-app5`

This is the active Android base: Kotlin, CameraX, MediaPipe Tasks Vision, Room, and LiteRT-LM, with an Android APK workflow. Its Android source and build handoff are imported here. The broader archived pnpm landing/dashboard surfaces were not needed for this Android fusion.

The active app already included the selected work from the older Java archive: an exact-device-match catalog of approximate camera profiles, with live Camera2 characteristics preferred. Catalog values remain labeled as approximate.

## `_AURIGA-main`

This is the older Java Android implementation. Its camera, detector, service, guidance, and haptic classes overlap the Kotlin architecture, so they were not copied. Reintroducing them would create competing implementations and duplicate app components. The older archive's placeholder device profile and product-family/pricing ideas are not current product direction.

## Claude share

The user-approved geometry decisions were carried into the Kotlin implementation:

- Ground-plane trigonometry is primary; Virtual Anchor only estimates/refines the horizon used by that calculation.
- Elevation/depression comes from vertical FOV and pixel position. Accelerometer data is only a coarse orientation gate.
- Camera height may be inferred from an anonymous, approximate full-body person scale cue; it is never described as measured or used to identify someone.
- Assumed-width pinhole distance is a low-confidence fallback, not an exact spoken measurement.

## October 21 fused handoff

The newly supplied `DrakoSanctis-Fused` archive is a separate Java/Camera2 prototype with an OpenCV-backed sparse-map experiment and a camera-only geometry contract. Its strongest reusable guidance is to require a trained camera profile, a temporally validated visual plane, anchor continuity, and an observed ground-contact point before exposing metric range or suspended height. Its 167 dependency-free Java regression checks passed when run from a temporary extraction; this does not establish an Android build or device accuracy.

The Kotlin app remains the active base. I did not import the second app stack, its four product flavors, website/browser prototypes, or signing material. Those would duplicate the current architecture or conflict with AURIGA's single-product boundary. The geometry prototype remains disabled pending a real plane/contact solver and physical validation.

## `lingbot-map-main`

This is a separate Python/CUDA/GPU research system, not an Android library or portable on-device model. Its sample Android guide describes sending raw camera frames to a remote WebSocket demo endpoint. No endpoint, streamer, model weights, or camera-upload behavior was imported. See [the LingBot assessment](LINGBOT_MAP_EVALUATION.md).
