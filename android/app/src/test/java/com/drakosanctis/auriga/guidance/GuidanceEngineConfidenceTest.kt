package com.drakosanctis.auriga.guidance

import com.drakosanctis.auriga.bearing.Direction
import com.drakosanctis.auriga.geometry.DistanceEstimationMethod
import com.drakosanctis.auriga.geometry.FiducialEstimationResult
import com.drakosanctis.auriga.hazard.ScoringExplanation
import com.drakosanctis.auriga.hazard.ScoringResult
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidanceEngineConfidenceTest {
    private val scoring = ScoringResult(
        hazardSeverity = 0.2f,
        hazardProbability = 0.2f,
        hazardConfidence = 0.8f,
        riskScore = 0.3f,
        formula = "test",
        explanation = ScoringExplanation("test", "test", "test")
    )

    @Test
    fun lowConfidenceRangeIsNotSpokenAsAMeasurement() {
        val fallback = FiducialEstimationResult(
            estimatedDistanceM = 1.2f,
            confidenceScore = 0.25f,
            uncertaintyM = 0.5f,
            confidenceLowerM = 0.7f,
            confidenceUpperM = 1.7f,
            method = DistanceEstimationMethod.ASSUMED_WIDTH_PINHOLE
        )

        val utterance = GuidanceEngine.describeHazard(
            hazardClass = "vehicle",
            humanLabel = "car",
            distance = fallback,
            direction = Direction.CENTER,
            scoring = scoring
        )

        assertTrue(utterance.text.contains("nearby"))
        assertTrue(!utterance.text.contains("meter"))
    }

    @Test
    fun highConfidenceRangeCanBeSpoken() {
        val estimate = FiducialEstimationResult(
            estimatedDistanceM = 2f,
            confidenceScore = 0.8f,
            uncertaintyM = 0.2f,
            confidenceLowerM = 1.8f,
            confidenceUpperM = 2.2f,
            method = DistanceEstimationMethod.GROUND_PLANE_TRIGONOMETRY
        )

        val utterance = GuidanceEngine.describeHazard(
            hazardClass = "vehicle",
            humanLabel = "car",
            distance = estimate,
            direction = Direction.CENTER,
            scoring = scoring
        )

        assertTrue(utterance.text.contains("2 meters away"))
    }
}
