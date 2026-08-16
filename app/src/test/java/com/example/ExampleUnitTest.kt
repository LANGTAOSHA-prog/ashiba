package com.example

import com.example.engine.ScaffoldEngine
import com.example.model.PlanInput
import com.example.model.SectionInput
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPlanCalculation() {
        val input = PlanInput(wallWidthMm = 7200.0, idealOffsetMm = 250.0)
        val candidates = ScaffoldEngine.calculatePlanCandidates(input)
        assertTrue(candidates.isNotEmpty())
        val standard = candidates.first()
        assertTrue(standard.totalScaffoldWidthMm >= 7200)
        assertTrue(standard.spans.isNotEmpty())
    }

    @Test
    fun testSectionCalculation() {
        val input = SectionInput(targetHeightMm = 6200.0)
        val result = ScaffoldEngine.calculateSection(input)
        assertTrue(result.totalTiers >= 3)
        assertTrue(result.outerPosts.isNotEmpty())
        assertTrue(result.innerPosts.isNotEmpty())
    }

    @Test
    fun testRoofSlope() {
        val gentleRoof = ScaffoldEngine.calculateRoofSlope(4.0, 4000.0)
        assertFalse(gentleRoof.requiresRoofScaffold)

        val steepRoof = ScaffoldEngine.calculateRoofSlope(6.5, 4000.0)
        assertTrue(steepRoof.requiresRoofScaffold)
    }
}
