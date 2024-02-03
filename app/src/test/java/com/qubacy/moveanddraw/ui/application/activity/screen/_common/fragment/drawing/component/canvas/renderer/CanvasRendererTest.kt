package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.component.canvas.renderer

import com.qubacy.moveanddraw._common._test.util.value.ValueUtil
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.camera.mutable.MutableCameraData
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.CanvasRenderer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs

class CanvasRendererTest {
    private lateinit var mCanvasRenderer: CanvasRenderer

    @Before
    fun setup() {
        mCanvasRenderer = CanvasRenderer()
    }

    data class GetTranslatedCameraLocationTestCase(
        val initCameraPos: FloatArray,
        val sphereRadius: Float,
        val dx: Float = 0f,
        val dy: Float = 0f,
        val resultCameraPos: FloatArray
    )

    @Test
    fun getTranslatedCameraLocationTest() {
        val mCameraDataFieldReflection =
            CanvasRenderer::class.java.getDeclaredField("mCameraData").apply {
                isAccessible = true
            }
        val mSphereRadiusFieldReflection =
            CanvasRenderer::class.java.getDeclaredField("mSphereRadius").apply {
                isAccessible = true
            }
        val mCameraRadiusFieldReflection =
            CanvasRenderer::class.java.getDeclaredField("mCameraRadius").apply {
                isAccessible = true
            }

        val cameraDataRef = mCameraDataFieldReflection.get(mCanvasRenderer) as MutableCameraData

        val testCases = arrayOf(
            GetTranslatedCameraLocationTestCase(
                initCameraPos = floatArrayOf(1f, 0f, 0f),
                sphereRadius = 1f,
                resultCameraPos = floatArrayOf(1f, 0f, 0f)
            ),
            GetTranslatedCameraLocationTestCase(
                initCameraPos = floatArrayOf(1f, 0f, 0f),
                sphereRadius = 1f,
                dy = mCanvasRenderer.getVerticalCameraWayLength(),
                resultCameraPos = floatArrayOf(0.30901694f, 0f, 0.95105654f)
            ),
            GetTranslatedCameraLocationTestCase(
                initCameraPos = floatArrayOf(1f, 0f, 0f),
                sphereRadius = 1f,
                dx = mCanvasRenderer.getHorizontalCameraWayLength(),
                resultCameraPos = floatArrayOf(1f, 0f, 0f)
            ),
            GetTranslatedCameraLocationTestCase(
                initCameraPos = floatArrayOf(1f, 0f, 0f),
                sphereRadius = 1f,
                dx = mCanvasRenderer.getHorizontalCameraWayLength() / 2,
                resultCameraPos = floatArrayOf(-1f, 0f, 0f)
            ),
            GetTranslatedCameraLocationTestCase(
                initCameraPos = floatArrayOf(1f, 0f, 0f),
                sphereRadius = 1f,
                dx = mCanvasRenderer.getHorizontalCameraWayLength() / 2,
                dy = mCanvasRenderer.getVerticalCameraWayLength() / 2,
                resultCameraPos = floatArrayOf(-1f, 0f, 0f)
            ),
        )

        for (testCase in testCases) {
            cameraDataRef.apply {
                setPosition(testCase.initCameraPos)
                setMadeWayHorizontal(0f)
                setMadeWayVertical(0f)
            }

            mSphereRadiusFieldReflection.set(mCanvasRenderer, testCase.sphereRadius)
            mCameraRadiusFieldReflection.set(mCanvasRenderer, testCase.sphereRadius)

            val gottenResultCameraPos = mCanvasRenderer
                .getTranslatedCameraLocation(testCase.dx, testCase.dy)
            val processedGottenResultCameraPos = floatArrayOf(
                ValueUtil.floorValue(gottenResultCameraPos[0]),
                ValueUtil.floorValue(gottenResultCameraPos[1]),
                ValueUtil.floorValue(gottenResultCameraPos[2])
            )

            Assert.assertTrue(testCase.resultCameraPos.contentEquals(processedGottenResultCameraPos))
        }
    }

}