package com.drakosanctis.auriga.geometry

import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Experimental, anonymous camera-height inference using a fully visible
 * person as an approximate scale cue. It does not track or identify people.
 */
object CameraHeightEstimator {
    private const val DEFAULT_PERSON_HEIGHT_M = 1.67f
    private const val PERSON_HEIGHT_UNCERTAINTY_M = 0.25f
    private const val MAX_CONFIDENCE = 0.25f

    fun estimateFromAnonymousPerson(
        label: String,
        topYpx: Float,
        bottomYpx: Float,
        frameHeightPx: Int,
        focalLengthYPx: Float,
        detectionConfidence: Float,
        horizon: HorizonEstimate,
        assumedPersonHeightMeters: Float = DEFAULT_PERSON_HEIGHT_M
    ): CameraHeightEstimate? {
        if (label.trim().lowercase() != "person") return null
        if (frameHeightPx <= 0 || focalLengthYPx <= 0f) return null
        if (!topYpx.isFinite() || !bottomYpx.isFinite() || topYpx >= bottomYpx) return null
        if (topYpx < frameHeightPx * 0.02f || bottomYpx > frameHeightPx * 0.98f) return null
        if (detectionConfidence < 0.65f || detectionConfidence > 1f) return null
        if (horizon.confidence !in 0.2f..1f || assumedPersonHeightMeters !in 1.2f..2.2f) return null

        val headAngle = atan2((horizon.yPx - topYpx).toDouble(), focalLengthYPx.toDouble())
        val footAngle = atan2((horizon.yPx - bottomYpx).toDouble(), focalLengthYPx.toDouble())
        if (footAngle >= 0.0 || headAngle <= footAngle) return null

        val tangentDifference = tan(headAngle) - tan(footAngle)
        if (tangentDifference <= 0.0) return null
        val horizontalDistance = assumedPersonHeightMeters.toDouble() / tangentDifference
        val inferredHeight = (-horizontalDistance * tan(footAngle)).toFloat()
        if (!inferredHeight.isFinite() || inferredHeight !in 0.45f..2.8f) return null

        val priorUncertainty = inferredHeight * PERSON_HEIGHT_UNCERTAINTY_M / assumedPersonHeightMeters
        val pixelUncertainty = (inferredHeight * 0.12f).coerceAtLeast(0.08f)
        val uncertainty = sqrt(priorUncertainty * priorUncertainty + pixelUncertainty * pixelUncertainty)
            .coerceAtLeast(0.2f)
        val confidence = minOf(MAX_CONFIDENCE, detectionConfidence * horizon.confidence * MAX_CONFIDENCE)

        return CameraHeightEstimate(
            meters = inferredHeight,
            uncertaintyMeters = uncertainty,
            confidence = confidence,
            source = "anonymous person-scale prior; inferred, not measured"
        )
    }
}
