package com.drakosanctis.auriga.geometry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VirtualAnchorTest {
    @Test
    fun convergingLinesProduceLowConfidenceHorizonCandidate() {
        val anchor = VirtualAnchor.estimateHorizon(
            segments = listOf(
                ImageLineSegment(0f, 400f, 500f, 200f),
                ImageLineSegment(1000f, 500f, 500f, 200f),
                ImageLineSegment(0f, 100f, 500f, 200f),
                ImageLineSegment(1000f, 100f, 500f, 200f)
            ),
            frameWidthPx = 1000,
            frameHeightPx = 800
        )

        assertNotNull(anchor)
        assertEquals(200f, anchor!!.yPx, 0.1f)
        assertTrue(anchor.confidence <= 0.35f)
    }

    @Test
    fun insufficientLinesDoNotProduceAnchor() {
        assertNull(
            VirtualAnchor.estimateHorizon(
                listOf(ImageLineSegment(0f, 0f, 100f, 100f)),
                frameWidthPx = 500,
                frameHeightPx = 500
            )
        )
    }
}
