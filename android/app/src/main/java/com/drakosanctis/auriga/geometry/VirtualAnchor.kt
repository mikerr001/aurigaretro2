package com.drakosanctis.auriga.geometry

import android.graphics.Bitmap
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class ImageLineSegment(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val strength: Float = 1f
)

/**
 * Experimental visual horizon refinement. It produces only a horizon
 * candidate; distance remains the ground-plane calculation, never a parallel
 * distance engine. Production use stays gated until field validation.
 */
object VirtualAnchor {
    private data class Intersection(val x: Float, val y: Float)

    /**
     * Finds a strong, near-horizontal image boundary as an experimental
     * horizon candidate. Walls, shelves, and furniture can produce false
     * candidates, so confidence is capped and the production feature gate
     * must remain off until scene-level validation is complete.
     */
    fun estimateHorizon(bitmap: Bitmap): HorizonEstimate? {
        if (bitmap.width < 16 || bitmap.height < 16) return null
        val width = 96
        val height = 72
        val scaled = Bitmap.createScaledBitmap(bitmap, width, height, false)
        val pixels = IntArray(width * height)
        try {
            scaled.getPixels(pixels, 0, width, 0, 0, width, height)
        } finally {
            if (scaled !== bitmap) scaled.recycle()
        }

        val luminance = IntArray(pixels.size) { index ->
            val color = pixels[index]
            (77 * ((color shr 16) and 0xff) +
                150 * ((color shr 8) and 0xff) +
                29 * (color and 0xff)) shr 8
        }
        val candidates = (height / 8 until height * 7 / 8).mapNotNull { y ->
            var supported = 0
            var contrastSum = 0
            for (x in 2 until width - 2) {
                val contrast = abs(luminance[(y + 1) * width + x] - luminance[(y - 1) * width + x])
                if (contrast >= 24) {
                    supported++
                    contrastSum += contrast
                }
            }
            val support = supported.toFloat() / (width - 4)
            if (support < 0.35f) null else {
                val meanContrast = if (supported == 0) 0f else contrastSum.toFloat() / supported
                Triple(y, support, meanContrast)
            }
        }
        val best = candidates.maxByOrNull { it.second * it.third } ?: return null
        val competing = candidates
            .filter { abs(it.first - best.first) > 3 }
            .maxOfOrNull { it.second * it.third }
            ?: 0f
        val uniqueness = ((best.second * best.third - competing) / max(1f, best.second * best.third))
            .coerceIn(0f, 1f)
        val confidence = min(0.35f, best.second * uniqueness * 0.5f)
        if (confidence < 0.05f) return null

        return HorizonEstimate(
            yPx = best.first * (bitmap.height.toFloat() / height),
            confidence = confidence,
            dispersionPx = (1f - best.second) * bitmap.height * 0.08f,
            source = "experimental horizontal-boundary candidate; not a validated horizon"
        )
    }

    fun estimateHorizon(
        segments: List<ImageLineSegment>,
        frameWidthPx: Int,
        frameHeightPx: Int
    ): HorizonEstimate? {
        if (frameWidthPx <= 0 || frameHeightPx <= 0) return null
        val usable = segments.filter {
            listOf(it.x1, it.y1, it.x2, it.y2, it.strength).all { value -> value.isFinite() } &&
                it.strength > 0f &&
                hypot((it.x2 - it.x1).toDouble(), (it.y2 - it.y1).toDouble()) >= frameWidthPx * 0.08
        }
        if (usable.size < 3) return null

        val intersections = buildList {
            for (firstIndex in 0 until usable.lastIndex) {
                for (secondIndex in firstIndex + 1 until usable.size) {
                    intersection(usable[firstIndex], usable[secondIndex])?.let { point ->
                        if (
                            point.x in (-frameWidthPx * 0.25f)..(frameWidthPx * 1.25f) &&
                            point.y in (-frameHeightPx * 0.25f)..(frameHeightPx * 1.25f)
                        ) add(point)
                    }
                }
            }
        }
        if (intersections.size < 3) return null

        val xTolerance = frameWidthPx * 0.18f
        val yTolerance = frameHeightPx * 0.08f
        val cluster = intersections.maxByOrNull { seed ->
            intersections.count { point ->
                abs(point.x - seed.x) <= xTolerance && abs(point.y - seed.y) <= yTolerance
            }
        } ?: return null
        val inliers = intersections.filter {
            abs(it.x - cluster.x) <= xTolerance && abs(it.y - cluster.y) <= yTolerance
        }
        if (inliers.size < 3) return null

        val medianY = inliers.map { it.y }.sorted()[inliers.size / 2]
        val dispersion = sqrt(inliers.map { (it.y - medianY) * (it.y - medianY) }.average()).toFloat()
        val support = (inliers.size / 8f).coerceIn(0.05f, 1f)
        val consistency = (1f - dispersion / yTolerance).coerceIn(0f, 1f)
        // This prototype is intentionally capped below the live-use threshold.
        val confidence = (support * consistency * 0.45f).coerceAtMost(0.35f)
        return HorizonEstimate(
            yPx = medianY,
            confidence = confidence,
            dispersionPx = dispersion,
            source = "experimental visual-line vanishing-point candidate"
        )
    }

    private fun intersection(first: ImageLineSegment, second: ImageLineSegment): Intersection? {
        val denominator =
            (first.x1 - first.x2) * (second.y1 - second.y2) -
                (first.y1 - first.y2) * (second.x1 - second.x2)
        if (abs(denominator) < 1e-3f) return null

        val firstDeterminant = first.x1 * first.y2 - first.y1 * first.x2
        val secondDeterminant = second.x1 * second.y2 - second.y1 * second.x2
        val x = (firstDeterminant * (second.x1 - second.x2) -
            (first.x1 - first.x2) * secondDeterminant) / denominator
        val y = (firstDeterminant * (second.y1 - second.y2) -
            (first.y1 - first.y2) * secondDeterminant) / denominator
        return if (x.isFinite() && y.isFinite()) Intersection(x, y) else null
    }
}
