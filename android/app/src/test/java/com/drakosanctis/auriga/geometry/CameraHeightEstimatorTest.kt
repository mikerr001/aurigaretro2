package com.drakosanctis.auriga.geometry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.atan
import kotlin.math.tan

class CameraHeightEstimatorTest {
    @Test
    fun infersHeightFromAnonymousFullBodyScaleWithLowConfidence() {
        val cameraHeight = 1.2f
        val personHeight = 1.7f
        val horizontalDistance = 4f
        val focalLengthY = 1000f
        val horizonY = 500f
        val headAngle = atan((personHeight - cameraHeight) / horizontalDistance)
        val footAngle = atan(-cameraHeight / horizontalDistance)
        val topY = horizonY - tan(headAngle) * focalLengthY
        val bottomY = horizonY - tan(footAngle) * focalLengthY

        val estimate = CameraHeightEstimator.estimateFromAnonymousPerson(
            label = "person",
            topYpx = topY,
            bottomYpx = bottomY,
            frameHeightPx = 1000,
            focalLengthYPx = focalLengthY,
            detectionConfidence = 0.9f,
            horizon = HorizonEstimate(horizonY, 0.8f, 2f),
            assumedPersonHeightMeters = personHeight
        )

        assertNotNull(estimate)
        assertEquals(cameraHeight, estimate!!.meters, 0.02f)
        assertTrue(estimate.confidence <= 0.25f)
        assertTrue(estimate.uncertaintyMeters > 0f)
        assertTrue(estimate.source.contains("not measured"))
    }

    @Test
    fun rejectsOtherLabelsClippedPeopleAndWeakDetections() {
        val horizon = HorizonEstimate(500f, 0.8f, 2f)
        fun estimate(label: String, top: Float = 400f, confidence: Float = 0.9f) =
            CameraHeightEstimator.estimateFromAnonymousPerson(
                label, top, 800f, 1000, 1000f, confidence, horizon
            )

        assertNull(estimate("car"))
        assertNull(estimate("person", top = 0f))
        assertNull(estimate("person", confidence = 0.5f))
    }
}
