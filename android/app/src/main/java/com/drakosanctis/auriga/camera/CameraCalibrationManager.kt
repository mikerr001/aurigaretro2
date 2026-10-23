package com.drakosanctis.auriga.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import com.drakosanctis.auriga.geometry.VerticalFovGeometry

/**
 * Auriga Camera Calibration
 *
 * Reads the actual back camera's focal length and sensor size from
 * android.hardware.camera2.CameraCharacteristics, derives focal length in
 * pixels and horizontal FOV in degrees, and caches the result in
 * SharedPreferences so it's read once per device rather than on every
 * pipeline start.
 *
 * Falls back to the previous hardcoded constants (1400px focal length,
 * 70 degree horizontal FOV) if:
 * - The device doesn't expose LENS_INFO_AVAILABLE_FOCAL_LENGTHS or
 *   SENSOR_INFO_PHYSICAL_SIZE (some budget devices have incomplete
 *   Camera2 characteristic support).
 * - Camera access throws for any reason (permission timing, vendor bugs).
 *
 * This matters specifically for the target market: the wide range of
 * budget/mid-range Android phones means a single hardcoded calibration
 * constant will be measurably wrong on many real devices, directly
 * affecting Virtual Fiducial distance accuracy.
 */

data class CameraCalibrationProfile(
    val focalLengthPx: Float,
    val focalLengthYPx: Float,
    val horizontalFovDegrees: Float,
    val verticalFovDegrees: Float,
    val isRealDeviceData: Boolean,
    val source: String = if (isRealDeviceData) "Camera2 characteristics" else "generic fallback"
)

class CameraCalibrationManager(private val context: Context) {

    companion object {
        private const val TAG = "AurigaCameraCalibration"
        private const val PREFS_NAME = "auriga_camera_calibration"
        private const val KEY_FOCAL_PX = "focal_length_px"
        private const val KEY_FOCAL_Y_PX = "focal_length_y_px"
        private const val KEY_FOV_DEG = "horizontal_fov_degrees"
        private const val KEY_VERTICAL_FOV_DEG = "vertical_fov_degrees"
        private const val KEY_IS_REAL = "is_real_device_data"
        private const val KEY_SOURCE = "source"
        private const val KEY_CACHED_FRAME_WIDTH = "cached_frame_width"
        private const val KEY_CACHED_FRAME_HEIGHT = "cached_frame_height"
        private const val KEY_CACHED_ROTATION = "cached_rotation"

        // Fallback constants — used only if real device data can't be read.
        const val FALLBACK_FOCAL_LENGTH_PX = 1400f
        const val FALLBACK_HORIZONTAL_FOV_DEGREES = 70f
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val deviceCatalog by lazy { DeviceCalibrationCatalog(context) }

    /**
     * Returns a calibration profile, preferring a cached real-device reading,
     * falling back to a fresh Camera2 read, falling back to hardcoded
     * constants if both of those fail. [analyzedFrameWidthPx] should be the
     * actual pixel width of frames being analyzed (the focal-length-in-pixels
     * conversion depends on the resolution actually used, not the sensor's
     * native resolution).
     */
    fun getCalibration(
        analyzedFrameWidthPx: Int,
        analyzedFrameHeightPx: Int,
        rotationDegrees: Int
    ): CameraCalibrationProfile {
        val rotation = ((rotationDegrees % 360) + 360) % 360
        val cached = readCache()
        if (
            cached != null &&
            prefs.getInt(KEY_CACHED_FRAME_WIDTH, -1) == analyzedFrameWidthPx &&
            prefs.getInt(KEY_CACHED_FRAME_HEIGHT, -1) == analyzedFrameHeightPx &&
            prefs.getInt(KEY_CACHED_ROTATION, -1) == rotation
        ) {
            return cached
        }

        val fresh = readFromCamera2(analyzedFrameWidthPx, analyzedFrameHeightPx, rotation)
        if (fresh != null) {
            writeCache(fresh, analyzedFrameWidthPx, analyzedFrameHeightPx, rotation)
            return fresh
        }

        val catalogProfile = deviceCatalog.profileForCurrentDevice(
            analyzedFrameWidthPx,
            analyzedFrameHeightPx,
            rotation
        )
        if (catalogProfile != null) {
            Log.w(
                TAG,
                "Using approximate bundled camera profile (${catalogProfile.source}); " +
                    "distance estimates are not device-calibrated."
            )
            return catalogProfile
        }

        Log.w(TAG, "Falling back to hardcoded calibration constants — could not read real device camera characteristics.")
        val fallbackFov = VerticalFovGeometry.fromSensorHorizontalFov(
            FALLBACK_HORIZONTAL_FOV_DEGREES,
            analyzedFrameWidthPx,
            analyzedFrameHeightPx,
            rotation
        )
        if (fallbackFov == null) {
            Log.e(TAG, "Invalid analyzed frame dimensions or rotation; using generic 70-degree profile.")
            return CameraCalibrationProfile(
                focalLengthPx = FALLBACK_FOCAL_LENGTH_PX,
                focalLengthYPx = FALLBACK_FOCAL_LENGTH_PX,
                horizontalFovDegrees = FALLBACK_HORIZONTAL_FOV_DEGREES,
                verticalFovDegrees = FALLBACK_HORIZONTAL_FOV_DEGREES,
                isRealDeviceData = false,
                source = "generic fallback; frame geometry unavailable"
            )
        }
        return CameraCalibrationProfile(
            focalLengthPx = fallbackFov.focalLengthXPx,
            focalLengthYPx = fallbackFov.focalLengthYPx,
            horizontalFovDegrees = fallbackFov.horizontalFovDegrees,
            verticalFovDegrees = fallbackFov.verticalFovDegrees,
            isRealDeviceData = false,
            source = "generic fallback"
        )
    }

    private fun readCache(): CameraCalibrationProfile? {
        if (
            !prefs.contains(KEY_FOCAL_PX) ||
            !prefs.contains(KEY_FOCAL_Y_PX) ||
            !prefs.contains(KEY_FOV_DEG) ||
            !prefs.contains(KEY_VERTICAL_FOV_DEG)
        ) return null
        return CameraCalibrationProfile(
            focalLengthPx = prefs.getFloat(KEY_FOCAL_PX, FALLBACK_FOCAL_LENGTH_PX),
            focalLengthYPx = prefs.getFloat(KEY_FOCAL_Y_PX, FALLBACK_FOCAL_LENGTH_PX),
            horizontalFovDegrees = prefs.getFloat(KEY_FOV_DEG, FALLBACK_HORIZONTAL_FOV_DEGREES),
            verticalFovDegrees = prefs.getFloat(KEY_VERTICAL_FOV_DEG, FALLBACK_HORIZONTAL_FOV_DEGREES),
            isRealDeviceData = prefs.getBoolean(KEY_IS_REAL, false),
            source = prefs.getString(
                KEY_SOURCE,
                if (prefs.getBoolean(KEY_IS_REAL, false)) "Camera2 characteristics" else "generic fallback"
            ) ?: "generic fallback"
        )
    }

    private fun writeCache(
        profile: CameraCalibrationProfile,
        frameWidth: Int,
        frameHeight: Int,
        rotationDegrees: Int
    ) {
        prefs.edit()
            .putFloat(KEY_FOCAL_PX, profile.focalLengthPx)
            .putFloat(KEY_FOCAL_Y_PX, profile.focalLengthYPx)
            .putFloat(KEY_FOV_DEG, profile.horizontalFovDegrees)
            .putFloat(KEY_VERTICAL_FOV_DEG, profile.verticalFovDegrees)
            .putBoolean(KEY_IS_REAL, profile.isRealDeviceData)
            .putString(KEY_SOURCE, profile.source)
            .putInt(KEY_CACHED_FRAME_WIDTH, frameWidth)
            .putInt(KEY_CACHED_FRAME_HEIGHT, frameHeight)
            .putInt(KEY_CACHED_ROTATION, rotationDegrees)
            .apply()
    }

    /**
     * Reads both physical sensor axes, then maps them onto the rotated
     * analyzed-frame axes before deriving focal lengths in pixels.
     *
     * Returns null if any required characteristic is unavailable, rather
     * than guessing — guessing silently would defeat the purpose of reading
     * real data in the first place.
     */
    private fun readFromCamera2(
        analyzedFrameWidthPx: Int,
        analyzedFrameHeightPx: Int,
        rotationDegrees: Int
    ): CameraCalibrationProfile? {
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return null

            val backCameraId = manager.cameraIdList.firstOrNull { id ->
                val characteristics = manager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_BACK
            } ?: return null

            val characteristics = manager.getCameraCharacteristics(backCameraId)

            val focalLengthsMm = characteristics.get(
                CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
            )
            val sensorSizeMm = characteristics.get(
                CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE
            )

            if (focalLengthsMm == null || focalLengthsMm.isEmpty() || sensorSizeMm == null) {
                Log.w(TAG, "Device does not expose focal length / sensor size characteristics.")
                return null
            }

            val focalLengthMm = focalLengthsMm[0]
            val sensorWidthMm = sensorSizeMm.width
            val sensorHeightMm = sensorSizeMm.height

            if (focalLengthMm <= 0f || sensorWidthMm <= 0f || sensorHeightMm <= 0f) return null

            val sensorHorizontalFov = Math.toDegrees(
                2.0 * kotlin.math.atan((sensorWidthMm / (2 * focalLengthMm)).toDouble())
            ).toFloat()
            val sensorVerticalFov = Math.toDegrees(
                2.0 * kotlin.math.atan((sensorHeightMm / (2 * focalLengthMm)).toDouble())
            ).toFloat()
            val frameFov = VerticalFovGeometry.fromSensorFovs(
                sensorHorizontalFov,
                sensorVerticalFov,
                analyzedFrameWidthPx,
                analyzedFrameHeightPx,
                rotationDegrees
            ) ?: return null

            Log.i(
                TAG,
                "Read Camera2 calibration: focalX=${frameFov.focalLengthXPx}, " +
                    "focalY=${frameFov.focalLengthYPx}, " +
                    "horizontalFov=${frameFov.horizontalFovDegrees}, " +
                    "verticalFov=${frameFov.verticalFovDegrees}, rotation=$rotationDegrees"
            )

            CameraCalibrationProfile(
                focalLengthPx = frameFov.focalLengthXPx,
                focalLengthYPx = frameFov.focalLengthYPx,
                horizontalFovDegrees = frameFov.horizontalFovDegrees,
                verticalFovDegrees = frameFov.verticalFovDegrees,
                isRealDeviceData = true,
                source = "Camera2 characteristics"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read Camera2 characteristics", e)
            null
        }
    }

    fun clearCache() {
        prefs.edit().clear().apply()
    }
}
