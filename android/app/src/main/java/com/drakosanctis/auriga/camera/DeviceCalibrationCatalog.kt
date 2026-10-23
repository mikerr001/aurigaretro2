package com.drakosanctis.auriga.camera

import android.content.Context
import android.os.Build
import android.util.Log
import com.drakosanctis.auriga.geometry.VerticalFovGeometry
import org.json.JSONObject

/**
 * Offline, approximate camera-FOV profiles carried forward from the earlier
 * Auriga project. These profiles are only a fallback when Camera2 does not
 * expose usable lens characteristics; they are not device-measured
 * calibrations.
 */
internal class DeviceCalibrationCatalog(context: Context) {
    private val appContext = context.applicationContext

    private data class Entry(
        val manufacturer: String,
        val model: String,
        val codename: String,
        val horizontalFovDegrees: Float
    )

    private val entries: List<Entry> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        loadEntries()
    }

    fun profileForCurrentDevice(
        frameWidthPx: Int,
        frameHeightPx: Int,
        rotationDegrees: Int
    ): CameraCalibrationProfile? {
        if (frameWidthPx <= 0 || frameHeightPx <= 0) return null

        val manufacturer = normalize(Build.MANUFACTURER)
        val deviceIds = setOf(Build.MODEL, Build.DEVICE, Build.PRODUCT)
            .map(::normalize)
            .filter(String::isNotEmpty)
            .toSet()

        val match = entries
            .asSequence()
            .filter { normalize(it.manufacturer) == manufacturer }
            .mapNotNull { entry ->
                val catalogIds = listOf(entry.model, entry.codename)
                    .map(::normalize)
                    .filter(String::isNotEmpty)
                val matchedId = catalogIds.firstOrNull { it in deviceIds }
                matchedId?.let { entry to if (it == normalize(entry.codename)) 2 else 1 }
            }
            .maxByOrNull { it.second }
            ?.first
            ?: return null

        val fov = match.horizontalFovDegrees
        if (!fov.isFinite() || fov !in MIN_VALID_FOV_DEGREES..MAX_VALID_FOV_DEGREES) {
            return null
        }

        val frameFov = VerticalFovGeometry.fromSensorHorizontalFov(
            sensorHorizontalFovDegrees = fov,
            frameWidthPx = frameWidthPx,
            frameHeightPx = frameHeightPx,
            rotationDegrees = rotationDegrees
        ) ?: return null

        val label = match.model.ifBlank { match.codename }
        return CameraCalibrationProfile(
            focalLengthPx = frameFov.focalLengthXPx,
            focalLengthYPx = frameFov.focalLengthYPx,
            horizontalFovDegrees = frameFov.horizontalFovDegrees,
            verticalFovDegrees = frameFov.verticalFovDegrees,
            isRealDeviceData = false,
            source = "derived device profile: $label"
        )
    }

    private fun loadEntries(): List<Entry> {
        return try {
            val contents = appContext.assets
                .open(ASSET_PATH)
                .bufferedReader()
                .use { it.readText() }
            val profiles = JSONObject(contents).optJSONArray("profiles") ?: return emptyList()
            buildList {
                for (index in 0 until profiles.length()) {
                    val profile = profiles.optJSONObject(index) ?: continue
                    if (!profile.optBoolean("approved", false)) continue

                    val camera = profile.optJSONObject("camera") ?: continue
                    val fov = camera.optDouble("fov_horizontal_deg", Double.NaN).toFloat()
                    if (!fov.isFinite() || fov !in MIN_VALID_FOV_DEGREES..MAX_VALID_FOV_DEGREES) {
                        continue
                    }

                    add(
                        Entry(
                            manufacturer = profile.optString("manufacturer"),
                            model = profile.optString("model"),
                            codename = profile.optString("codename"),
                            horizontalFovDegrees = fov
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not load bundled device calibration profiles.", e)
            emptyList()
        }
    }

    private fun normalize(value: String?): String =
        value.orEmpty().filter(Char::isLetterOrDigit).lowercase()

    private companion object {
        const val TAG = "AurigaDeviceProfiles"
        const val ASSET_PATH = "device_calibration_profiles.json"
        const val MIN_VALID_FOV_DEGREES = 25f
        const val MAX_VALID_FOV_DEGREES = 130f
    }
}
