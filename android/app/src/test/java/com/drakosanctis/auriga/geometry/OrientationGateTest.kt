package com.drakosanctis.auriga.geometry

import org.junit.Assert.assertEquals
import org.junit.Test

class OrientationGateTest {
    @Test
    fun classifiesOnlyCoarsePose() {
        assertEquals(CoarseOrientation.UPRIGHT, OrientationGate.classify(0f, 9.8f, 0f))
        assertEquals(CoarseOrientation.UPSIDE_DOWN, OrientationGate.classify(0f, -9.8f, 0f))
        assertEquals(CoarseOrientation.SIDEWAYS, OrientationGate.classify(9.8f, 0f, 0f))
        assertEquals(CoarseOrientation.INDETERMINATE, OrientationGate.classify(0f, 0f, 9.8f))
        assertEquals(CoarseOrientation.INDETERMINATE, OrientationGate.classify(0f, 0f, 0f))
    }
}
