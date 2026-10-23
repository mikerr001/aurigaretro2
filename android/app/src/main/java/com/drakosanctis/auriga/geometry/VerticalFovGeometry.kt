package com.drakosanctis.auriga.geometry

import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Camera geometry for frames after CameraX rotation.
 *
 * Camera2 describes the sensor in its native orientation. CameraX may rotate
 * the analyzed frame into portrait, so the sensor's horizontal and vertical
 * fields of view must be swapped for 90/270 degree frames.
 */
data class FrameFieldOfView(
    val horizontalFovDegrees: Float,
    val verticalFovDegrees: Float,
    val focalLengthXPx: Float,
    val focalLengthYPx: Float,
    val rotationDegrees: Int
)

data class HorizonEstimate(
    val yPx: Float,
    val confidence: Float,
    val dispersionPx: Float,
    val source: String = "visual-line estimate"
)

data class CameraHeightEstimate(
    val meters: Float,
    val uncertaintyMeters: Float,
    val confidence: Float,
    val source: String
)

data class GroundPlaneDistanceEstimate(
    val meters: Float,
    val uncertaintyMeters: Float,
    val confidence: Float,
    val rayAngleBelowHorizonDegrees: Float
)

object VerticalFovGeometry {
    /**
     * Derive output-frame FOV and focal lengths from the sensor's native FOV.
     * Returns null rather than guessing when dimensions or rotation are invalid.
     */
    fun fromSensorFovs(
        sensorHorizontalFovDegrees: Float,
        sensorVerticalFovDegrees: Float,
        frameWidthPx: Int,
        frameHeightPx: Int,
        rotationDegrees: Int
    ): FrameFieldOfView? {
        if (frameWidthPx <= 0 || frameHeightPx <= 0) return null
        if (!validFov(sensorHorizontalFovDegrees) || !validFov(sensorVerticalFovDegrees)) return null

        val rotation = ((rotationDegrees % 360) + 360) % 360
        if (rotation !in setOf(0, 90, 180, 270)) return null
        val swapsAxes = rotation == 90 || rotation == 270
        val horizontalFov = if (swapsAxes) sensorVerticalFovDegrees else sensorHorizontalFovDegrees
        val verticalFov = if (swapsAxes) sensorHorizontalFovDegrees else sensorVerticalFovDegrees
        val focalX = frameWidthPx / (2f * tan(Math.toRadians((horizontalFov / 2f).toDouble())).toFloat())
        val focalY = frameHeightPx / (2f * tan(Math.toRadians((verticalFov / 2f).toDouble())).toFloat())

        if (!focalX.isFinite() || !focalY.isFinite() || focalX <= 0f || focalY <= 0f) return null
        return FrameFieldOfView(horizontalFov, verticalFov, focalX, focalY, rotation)
    }

    /**
     * Derive the other sensor axis from the frame aspect ratio when an
     * approximate legacy profile exposes only native horizontal FOV.
     */
    fun fromSensorHorizontalFov(
        sensorHorizontalFovDegrees: Float,
        frameWidthPx: Int,
        frameHeightPx: Int,
        rotationDegrees: Int
    ): FrameFieldOfView? {
        if (frameWidthPx <= 0 || frameHeightPx <= 0 || !validFov(sensorHorizontalFovDegrees)) return null
        val rotation = ((rotationDegrees % 360) + 360) % 360
        if (rotation !in setOf(0, 90, 180, 270)) return null
        val swapsAxes = rotation == 90 || rotation == 270
        val sensorAspect = if (swapsAxes) {
            frameHeightPx.toFloat() / frameWidthPx
        } else {
            frameWidthPx.toFloat() / frameHeightPx
        }
        val sensorVerticalFov = Math.toDegrees(
            2.0 * atan(
                tan(Math.toRadians((sensorHorizontalFovDegrees / 2f).toDouble())) / sensorAspect
            )
        ).toFloat()
        return fromSensorFovs(
            sensorHorizontalFovDegrees,
            sensorVerticalFov,
            frameWidthPx,
            frameHeightPx,
            rotation
        )
    }

    /** Positive angles are above the visual horizon; negative angles are below it. */
    fun elevationDegreesFromPixel(
        pixelY: Float,
        frameHeightPx: Int,
        focalLengthYPx: Float,
        horizonYpx: Float = frameHeightPx / 2f
    ): Float? {
        if (frameHeightPx <= 0 || focalLengthYPx <= 0f || !focalLengthYPx.isFinite()) return null
        if (!pixelY.isFinite() || !horizonYpx.isFinite()) return null
        return Math.toDegrees(atan2((horizonYpx - pixelY).toDouble(), focalLengthYPx.toDouble())).toFloat()
    }

    /**
     * Ground-plane range from a visible ground-contact point. This requires a
     * trusted visual horizon and camera-height estimate; otherwise it returns
     * null and the caller must retain the explicitly low-confidence fallback.
     */
    fun estimateGroundPlaneDistance(
        groundContactYpx: Float,
        frameHeightPx: Int,
        focalLengthYPx: Float,
        horizon: HorizonEstimate,
        cameraHeight: CameraHeightEstimate,
        minimumConfidence: Float = 0.55f
    ): GroundPlaneDistanceEstimate? {
        if (frameHeightPx <= 0 || focalLengthYPx <= 0f || !focalLengthYPx.isFinite()) return null
        if (!groundContactYpx.isFinite() || !horizon.yPx.isFinite()) return null
        if (horizon.confidence !in 0f..1f || cameraHeight.confidence !in 0f..1f) return null
        if (horizon.confidence < minimumConfidence || cameraHeight.confidence < minimumConfidence) return null
        if (cameraHeight.meters !in 0.45f..2.8f || cameraHeight.uncertaintyMeters < 0f) return null
        if (cameraHeight.uncertaintyMeters >= cameraHeight.meters * 0.9f) return null
        if (groundContactYpx <= horizon.yPx) return null

        val angleRadians = atan2(
            (groundContactYpx - horizon.yPx).toDouble(),
            focalLengthYPx.toDouble()
        )
        val angleDegrees = Math.toDegrees(angleRadians).toFloat()
        if (angleDegrees !in 2f..65f) return null

        val distance = cameraHeight.meters / tan(angleRadians).toFloat()
        if (!distance.isFinite() || distance !in 0.2f..30f) return null

        val pixelSigma = max(2f, horizon.dispersionPx)
        val angleSigma = atan(pixelSigma / focalLengthYPx)
        val nearAngle = min(Math.toRadians(85.0).toFloat(), angleRadians.toFloat() + angleSigma)
        val farAngle = max(Math.toRadians(1.0).toFloat(), angleRadians.toFloat() - angleSigma)
        val lowHeight = max(0.2f, cameraHeight.meters - cameraHeight.uncertaintyMeters)
        val highHeight = cameraHeight.meters + cameraHeight.uncertaintyMeters
        val nearDistance = lowHeight / tan(nearAngle)
        val farDistance = highHeight / tan(farAngle)
        val uncertainty = max(distance - nearDistance, farDistance - distance).coerceAtLeast(0f)
        if (!uncertainty.isFinite()) return null

        // Confidence remains bounded by the weakest input; this is not a
        // measurement guarantee and must not be interpreted as one.
        val confidence = min(horizon.confidence, cameraHeight.confidence)
        return GroundPlaneDistanceEstimate(distance, uncertainty, confidence, angleDegrees)
    }

    private fun validFov(value: Float): Boolean = value.isFinite() && value in 5f..170f
}
