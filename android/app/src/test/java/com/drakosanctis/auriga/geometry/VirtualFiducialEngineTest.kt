package com.drakosanctis.auriga.geometry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VirtualFiducialEngineTest {
    @Test
    fun assumedWidthDistanceIsMarkedLowConfidenceAndWideUncertainty() {
        val result = VirtualFiducialEngine.estimateDistance(
            FiducialEstimationInput(
                focalLengthPx = 1000f,
                markerSizeMm = 500f,
                sensorWidthPx = 1000f,
                sensorHeightPx = 1000f,
                markerPixelWidth = 100f
            )
        )

        assertEquals(5f, result.estimatedDistanceM, 0.001f)
        assertEquals(DistanceEstimationMethod.ASSUMED_WIDTH_PINHOLE, result.method)
        assertTrue(result.confidenceScore <= 0.25f)
        assertTrue(result.uncertaintyM >= result.estimatedDistanceM * 0.35f)
    }

    @Test
    fun groundPlaneResultPreservesItsMethodAndInterval() {
        val result = VirtualFiducialEngine.fromGroundPlane(
            GroundPlaneDistanceEstimate(
                meters = 4f,
                uncertaintyMeters = 0.8f,
                confidence = 0.7f,
                rayAngleBelowHorizonDegrees = 20f
            )
        )

        assertEquals(DistanceEstimationMethod.GROUND_PLANE_TRIGONOMETRY, result.method)
        assertEquals(3.2f, result.confidenceLowerM, 0.001f)
        assertEquals(4.8f, result.confidenceUpperM, 0.001f)
    }
}
