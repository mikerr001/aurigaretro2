package com.drakosanctis.auriga.geometry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VerticalFovGeometryTest {
    @Test
    fun portraitRotationSwapsSensorAxes() {
        val fov = VerticalFovGeometry.fromSensorFovs(
            sensorHorizontalFovDegrees = 70f,
            sensorVerticalFovDegrees = 45f,
            frameWidthPx = 1080,
            frameHeightPx = 1920,
            rotationDegrees = 90
        )

        assertNotNull(fov)
        assertEquals(45f, fov!!.horizontalFovDegrees, 0.001f)
        assertEquals(70f, fov.verticalFovDegrees, 0.001f)
        assertTrue(fov.focalLengthXPx > 0f)
        assertTrue(fov.focalLengthYPx > 0f)
    }

    @Test
    fun sensorHorizontalFovDerivesVerticalFovForRotatedFrame() {
        val fov = VerticalFovGeometry.fromSensorHorizontalFov(
            sensorHorizontalFovDegrees = 70f,
            frameWidthPx = 1080,
            frameHeightPx = 1920,
            rotationDegrees = 90
        )

        assertNotNull(fov)
        assertTrue(fov!!.horizontalFovDegrees < fov.verticalFovDegrees)
    }

    @Test
    fun invalidDimensionsAndRotationsAreRejected() {
        assertNull(VerticalFovGeometry.fromSensorHorizontalFov(70f, 0, 1920, 90))
        assertNull(VerticalFovGeometry.fromSensorHorizontalFov(70f, 1080, 1920, 45))
    }

    @Test
    fun elevationUsesPixelPositionRelativeToHorizon() {
        val above = VerticalFovGeometry.elevationDegreesFromPixel(400f, 1000, 1000f, 500f)
        val center = VerticalFovGeometry.elevationDegreesFromPixel(500f, 1000, 1000f, 500f)
        val below = VerticalFovGeometry.elevationDegreesFromPixel(600f, 1000, 1000f, 500f)

        assertTrue(above!! > 0f)
        assertEquals(0f, center!!, 0.001f)
        assertTrue(below!! < 0f)
    }

    @Test
    fun groundPlaneDistanceUsesHorizonAndRejectsUntrustedInputs() {
        val horizon = HorizonEstimate(yPx = 500f, confidence = 0.8f, dispersionPx = 1f)
        val height = CameraHeightEstimate(1.5f, 0.1f, 0.8f, "test")
        val result = VerticalFovGeometry.estimateGroundPlaneDistance(
            groundContactYpx = 800f,
            frameHeightPx = 1000,
            focalLengthYPx = 1000f,
            horizon = horizon,
            cameraHeight = height
        )

        assertNotNull(result)
        assertEquals(5f, result!!.meters, 0.02f)
        assertNull(
            VerticalFovGeometry.estimateGroundPlaneDistance(
                groundContactYpx = 800f,
                frameHeightPx = 1000,
                focalLengthYPx = 1000f,
                horizon = horizon.copy(confidence = 0.3f),
                cameraHeight = height
            )
        )
        assertNull(
            VerticalFovGeometry.estimateGroundPlaneDistance(
                groundContactYpx = 450f,
                frameHeightPx = 1000,
                focalLengthYPx = 1000f,
                horizon = horizon,
                cameraHeight = height
            )
        )
    }
}
