package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer

import android.opengl.Matrix
import android.util.Log
import com.qubacy.moveanddraw.domain._common.model.drawing._common.DrawingContext
import com.qubacy.moveanddraw.domain._common.model.drawing.util.toVertexTripleArray
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.CanvasRenderer
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.util.GL2Util
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.data.FaceSketch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class EditorCanvasRenderer(

) : CanvasRenderer() {
    enum class EditorRendererMode {
        VIEWING, CREATING_FACE;
    }

    companion object {
        val HELPING_PLANE_DRAWING_COLOR = floatArrayOf(0f, 1f, 0f, 0.4f)
        val FACE_SKETCH_DRAWING_COLOR = floatArrayOf(1f, 0f, 1f, 1f)

        const val MIN_HELPING_PLANE_DISTANCE = DEFAULT_CAMERA_NEAR
        const val HELPING_PLANE_MODEL_GAP = 0.001f

        const val COORDS_PER_DOT = 2
    }

    private val mHelpingPlaneDrawing: GLDrawing = generateHelpingPlaneGLDrawing()
    private val mFaceSketchDrawing: GLDrawing = generateFaceSketchDrawing()
    private val mFaceSketchDotBuffer: MutableList<Pair<Float, Float>> = mutableListOf()

    private val mFaceSketchMutex: Mutex = Mutex(false)

    val faceSketchDotBuffer: List<Pair<Float, Float>> get() = mFaceSketchDotBuffer

    private var mLastFaceSketchVertexArray: FloatArray = floatArrayOf()
    private var mLastFaceSketchDrawingOrder: ShortArray = shortArrayOf()

    private var mEditorRendererMode: EditorRendererMode = EditorRendererMode.VIEWING
    private var mIsHelpingPlaneVisible: Boolean = false

    private val mProjWorldMatrix = FloatArray(16)

    suspend fun setFaceSketchDotBuffer(faceSketchDotArray: FloatArray) = mFaceSketchMutex.withLock {
        for (i in faceSketchDotArray.indices step COORDS_PER_DOT) {
            mFaceSketchDotBuffer.add(Pair(faceSketchDotArray[i], faceSketchDotArray[i + 1]))
        }
    }

    suspend fun saveAndGetFaceSketch(): FaceSketch? {
        val faceSketch = getLastFaceSketch() ?: return null

        mFigureMutex.withLock {
            val figure = if (mFigure == null) {
                generateFigure(mLastFaceSketchVertexArray, mLastFaceSketchDrawingOrder)

            } else {
                val editedFigure = mFigure

                val finalVertexArray = editedFigure!!.vertexArray.plus(mLastFaceSketchVertexArray)
                val vertexShift = editedFigure.vertexArray.size / DrawingContext.COORDS_PER_VERTEX

                val finalDrawingOrderArray =
                    if (editedFigure.vertexDrawingOrder != null) {
                        editedFigure.vertexDrawingOrder!!.plus(mLastFaceSketchDrawingOrder
                            .map { (it + vertexShift).toShort() })
                    } else {
                        mLastFaceSketchDrawingOrder
                    }

                editedFigure.setVertices(finalVertexArray, finalDrawingOrderArray)
                editedFigure
            }

            setFigureData(figure)
        }

        resetFaceSketchData()

        return faceSketch
    }

    private fun resetFaceSketchData() {
        mFaceSketchDotBuffer.clear()

        mLastFaceSketchVertexArray = floatArrayOf()
        mLastFaceSketchDrawingOrder = shortArrayOf()
    }

    private fun generateFigure(vertexArray: FloatArray, drawingOrderArray: ShortArray): GLDrawing {
        return GLDrawing(vertexArray, drawingOrderArray)
    }

    private suspend fun getLastFaceSketch(): FaceSketch? {
        var faceSketch: FaceSketch? = null

        mFaceSketchMutex.withLock {
            if (mLastFaceSketchVertexArray.isEmpty()) return null

            val vertexTripleArray = mLastFaceSketchVertexArray.toVertexTripleArray()
            val face = mLastFaceSketchDrawingOrder
                .map { Triple<Short, Short?, Short?>(it, null, null) }.toTypedArray()

            faceSketch = FaceSketch(vertexTripleArray, face)
        }

        return faceSketch
    }

    fun setMode(mode: EditorRendererMode) {
        mEditorRendererMode = mode

        setHelpingPlaneVisibilityByMode(mode)
        changeCameraNearByMode(mode)
        mFaceSketchDotBuffer.clear()
    }

    private fun changeCameraNearByMode(mode: EditorRendererMode) {
        val cameraNear = when (mode) {
            EditorRendererMode.VIEWING -> DEFAULT_CAMERA_NEAR
            EditorRendererMode.CREATING_FACE -> MIN_HELPING_PLANE_DISTANCE
        }

        changeCameraNear(cameraNear) // todo: isn't working..
    }

    private fun setHelpingPlaneVisibilityByMode(mode: EditorRendererMode) {
        mIsHelpingPlaneVisible = when (mode) {
            EditorRendererMode.VIEWING -> false
            EditorRendererMode.CREATING_FACE -> true
        }
    }

    override fun handleScale(scaleFactor: Float) {
        when (mEditorRendererMode) {
            EditorRendererMode.CREATING_FACE -> handleHelpingPlaneDistanceChange(scaleFactor)
            else -> super.handleScale(scaleFactor)
        }
    }

    override fun handleRotation(dx: Float, dy: Float) {
        when (mEditorRendererMode) {
            EditorRendererMode.CREATING_FACE -> {  } // nothing?
            else -> super.handleRotation(dx, dy)
        }
    }

    suspend fun handleClick(x: Float, y: Float) {
        when (mEditorRendererMode) {
            EditorRendererMode.CREATING_FACE -> addSketchVertex(x, y)
            else -> { }
        }
    }

    suspend fun removeLastSketchFaceVertex() = mFaceSketchMutex.withLock {
        if (mFaceSketchDotBuffer.isEmpty()) return@withLock

        mFaceSketchDotBuffer.removeLast()
    }

    private suspend fun addSketchVertex(x: Float, y: Float) = mFaceSketchMutex.withLock {
        Log.d(TAG, "addSketchVertex(): x = $x; y = $y;")

        mFaceSketchDotBuffer.add(Pair(x, y))
    }

    private fun handleHelpingPlaneDistanceChange(distanceFactor: Float) {
        val newNear = mCameraNear * distanceFactor
        val filteredNewNear =
            if (newNear <= MIN_HELPING_PLANE_DISTANCE) MIN_HELPING_PLANE_DISTANCE
            else if (newNear >= mSphereRadius * 2) mSphereRadius * 2 - HELPING_PLANE_MODEL_GAP
            else newNear

        changeCameraNear(filteredNewNear)
    }

    private fun changeCameraNear(near: Float) {
        Log.d(TAG, "changeCameraNear(): near = $near")

        mCameraNear = near

        setPerspective()
    }

    private fun generateHelpingPlaneGLDrawing(): GLDrawing {
        return GLDrawing(
            floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 0f, 0f),
            shortArrayOf(0, 1, 2, 0, 2, 3),
            GLContext.DrawingMode.FILLED,
            HELPING_PLANE_DRAWING_COLOR
        )
    }

    private fun generateFaceSketchDrawing(): GLDrawing {
        return GLDrawing(
            floatArrayOf(),
            shortArrayOf(),
            GLContext.DrawingMode.SKETCH,
            FACE_SKETCH_DRAWING_COLOR
        )
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
    }

    override fun onDrawFrame(gl: GL10?): Unit = runBlocking {
        super.onDrawFrame(gl)

        drawHelpingPlane()
        drawFaceSketch()
    }

    private fun drawHelpingPlane(): Unit = runBlocking {
        if (!mIsHelpingPlaneVisible) return@runBlocking
        if (!mHelpingPlaneDrawing.isInitialized) mHelpingPlaneDrawing.init()

        val helpingPlaneVertices = getHelpingPlaneVertices()

        mHelpingPlaneDrawing.setVertices(helpingPlaneVertices, mHelpingPlaneDrawing.vertexDrawingOrder)
        mHelpingPlaneDrawing.draw(mVPMatrix)
    }

    private fun getHelpingPlaneVertices(): FloatArray {
        val normalizedHelpingPlaneZ = -1f + HELPING_PLANE_MODEL_GAP

        val helpingPlaneVertices = arrayOf(
            1f, 1f, normalizedHelpingPlaneZ,
            -1f, 1f, normalizedHelpingPlaneZ,
            -1f, -1f, normalizedHelpingPlaneZ,
            1f, -1f, normalizedHelpingPlaneZ
        )

        Matrix.invertM(mProjWorldMatrix, 0, mVPMatrix, 0)

        val projHelpingPlaneVertices = FloatArray(helpingPlaneVertices.size)

        for (i in helpingPlaneVertices.indices step DrawingContext.COORDS_PER_VERTEX) {
            val curVertex = floatArrayOf(
                helpingPlaneVertices[i], helpingPlaneVertices[i + 1], helpingPlaneVertices[i + 2], 1f
            )
            val projVertex = FloatArray(curVertex.size)

            Matrix.multiplyMV(
                projVertex, 0,
                mProjWorldMatrix, 0,
                curVertex, 0
            )

            val normalizedProjVertex = projVertex.map { it / projVertex[3] }.toFloatArray()

            normalizedProjVertex.copyInto(
                projHelpingPlaneVertices, i, 0, DrawingContext.COORDS_PER_VERTEX)
        }

        return projHelpingPlaneVertices
    }

    private suspend fun drawFaceSketch() = mFaceSketchMutex.withLock {
        if (!mIsHelpingPlaneVisible) return@withLock
        if (!mFaceSketchDrawing.isInitialized) mFaceSketchDrawing.init()

        mLastFaceSketchVertexArray = getFaceSketchVerticesByDots(mFaceSketchDotBuffer)
        mLastFaceSketchDrawingOrder = getFaceSketchDrawingOrderByVertices(
            mLastFaceSketchVertexArray)

        mFaceSketchDrawing.setVertices(mLastFaceSketchVertexArray, mLastFaceSketchDrawingOrder)
        mFaceSketchDrawing.draw(mVPMatrix)
    }

    private fun getFaceSketchVerticesByDots(faceSketchDots: List<Pair<Float, Float>>): FloatArray {
        val halfWidth = mDeviceWidth / 2
        val halfHeight = mDeviceHeight / 2
        val normalizedZ = -1f + HELPING_PLANE_MODEL_GAP / 2

        val faceSketchVertices = FloatArray(faceSketchDots.size * DrawingContext.COORDS_PER_VERTEX)
        var curFaceSketchIndex = 0

        for (dot in faceSketchDots) {
            val normalizedVertex = floatArrayOf(
                dot.first / halfWidth - 1,
                (2 - dot.second / halfHeight) - 1,
                normalizedZ,
                1f
            )
            val projVertex = FloatArray(normalizedVertex.size)

            Matrix.multiplyMV(
                projVertex, 0,
                mProjWorldMatrix, 0,
                normalizedVertex, 0
            )

            val normalizedProjVertex = projVertex.map { it / projVertex[3] }.toFloatArray()

            normalizedProjVertex.copyInto(
                faceSketchVertices, curFaceSketchIndex, 0, DrawingContext.COORDS_PER_VERTEX)

            curFaceSketchIndex += DrawingContext.COORDS_PER_VERTEX
        }

        return faceSketchVertices
    }

    private fun getFaceSketchDrawingOrderByVertices(
        faceSketchVertices: FloatArray
    ): ShortArray {
        val vertexIdArray = IntRange(
            0,
            faceSketchVertices.size / DrawingContext.COORDS_PER_VERTEX - 1
        ).map { it.toShort() }.toShortArray()

        val vertexCount = vertexIdArray.size

        if (vertexCount < 3) return vertexIdArray

        val drawingOrder = GL2Util.polygonToTriangles(vertexIdArray)

        return drawingOrder.toShortArray()
    }
}