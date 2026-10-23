---
name: AURIGA product boundary
description: User-confirmed product scope and explicit exclusions from shared product discussions.
---

AURIGA is one accessibility navigation co-pilot covering physical and digital accessibility. Do not split it into the older archive's Navi, Sentinel, Aero, and Industrial product variants. Do not add person re-identification or gait tracking.

V1 spatial ground truth must come from camera intrinsics, developer calibration, visual anchors, and plane geometry. Do not use inertial sensors, GPS, ARCore/ARKit, LiDAR, or cloud depth as spatial ground truth. The coarse accelerometer refusal gate is optional, never supplies pitch or range, and remains off by default.

**Why:** The shared Claude conversation narrowed the product to one co-pilot; the October 21 handoff locks V1 spatial truth to camera geometry while allowing only a non-measuring orientation refusal gate.

**How to apply:** Treat the archive's vertical product profiles as historical context. Keep people-related perception anonymous and transient, keep inertial values out of spatial calculations, and ask before broadening the product into new verticals or identity tracking.
