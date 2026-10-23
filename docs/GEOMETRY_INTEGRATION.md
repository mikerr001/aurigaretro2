# Geometry integration status

## Active behavior

- Camera calibration now maps Camera2's native horizontal/vertical sensor FOV onto the rotated CameraX analysis frame. Cache entries include frame dimensions and rotation so portrait and landscape values are not mixed.
- The old device catalog remains an approximate fallback and derives the other FOV axis from the actual frame aspect ratio.
- Assumed-width pinhole results are capped at 0.25 confidence and carry at least 35% distance uncertainty.
- Low-confidence distances are excluded from object-velocity/TTC and hazard-proximity calculations. Spoken exact ranges require at least 0.75 confidence and a sufficiently narrow uncertainty interval; otherwise guidance says “nearby.”
- The accelerometer is not used as spatial ground truth or to calculate camera pitch. The coarse rejection gate starts only when the experimental geometry flag is enabled; it is off by default.

## Experimental geometry

`VirtualAnchor` can propose a horizon from image boundaries or converging lines. Such a candidate is not a validated horizon and is capped at 0.35 confidence. `CameraHeightEstimator` uses a transient, anonymous person-height prior and caps confidence at 0.25. Ground-plane distance calculation requires trusted horizon and camera-height inputs, so these prototypes cannot currently influence live navigation.

`ENABLE_GEOMETRY_REFINEMENT` in `AurigaPipeline` is intentionally `false`. The October 21 Java handoff reinforces the need for a trained profile, validated visual plane, anchor continuity, and an observed ground-contact point before metric output. The current Kotlin prototype does not yet have that complete visual solver. Enabling it requires representative indoor/outdoor validation, error/uncertainty calibration, and accessibility testing before spoken ranges or TTC may use those estimates.

## Verification status

Pure Kotlin unit tests cover FOV axis rotation, pixel elevation, ground-plane range, anonymous camera-height inference, line intersections, orientation gating, fallback confidence, and spoken-range suppression. An Android SDK build and device/emulator validation remain required before release.
