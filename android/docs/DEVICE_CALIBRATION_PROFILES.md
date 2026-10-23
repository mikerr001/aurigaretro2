# Bundled camera profile catalog

`app/src/main/assets/device_calibration_profiles.json` carries the approved
device entries from the earlier Auriga project. The catalog contains
manufacturer/model identifiers and a horizontal field-of-view estimate.

`CameraCalibrationManager` still prefers live Camera2 lens characteristics.
It consults this catalog only when Camera2 cannot provide usable values, and
only for an exact manufacturer plus model/device identifier match. The profile
is marked as derived rather than device-measured; it is not a substitute for
real-world validation. Devices without an exact catalog match continue to use
the existing generic fallback.

The catalog's `screen`, `ipd`, and other fields are retained as source data but
are not consumed by the current navigation pipeline.
