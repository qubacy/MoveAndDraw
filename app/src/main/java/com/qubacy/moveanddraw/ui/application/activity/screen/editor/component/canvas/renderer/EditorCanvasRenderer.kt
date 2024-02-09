package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer

import android.opengl.Matrix
import android.util.Log
import com.qubacy.moveanddraw.domain._common.model.drawing._common.DrawingContext
import com.qubacy.moveanddraw.domain._common.model.drawing.util.toVertexTripleArray
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.Dot2D
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.camera._common.CameraContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.CanvasRenderer
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.util.GL2Util
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas._common.EditorCanvasContext
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.data.face.FaceSketch
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer.initializer.EditorRendererStepInitializer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class EditorCanvasRenderer(

) : CanvasRenderer() {

    companion object {
        val HELPING_PLANE_DRAWING_COLOR = floatArrayOf(0f, 1f, 0f, 0.4f)
        val FACE_SKETCH_DRAWING_COLOR = floatArrayOf(1f, 0f, 1f, 1f)

        const val MIN_HELPING_PLANE_DISTANCE = CameraContext.DEFAULT_CAMERA_NEAR
        const val HELPING_PLANE_MODEL_GAP = 0.001f
    }

    private val mHelpingPlaneDrawing: GLDrawing = generateHelpingPlaneGLDrawing()
    private val mFaceSketchDrawing: GLDrawing = generateFaceSketchDrawing()
    private val mFaceSketchDotBuffer: MutableList<Dot2D> = mutableListOf()

    private val mFaceSketchMutex: Mutex = Mutex(false)

    val faceSketchDotBuffer: List<Dot2D> get() = mFaceSketchDotBuffer

    private var mLastFaceSketchVertexArray: FloatArray = floatArrayOf()
    private var mLastFaceSketchDrawingOrder: IntArray = intArrayOf()

    private var mEditorRendererMode: EditorCanvasContext.Mode = EditorCanvasContext.Mode.VIEWING
    val editorRendererMode get() = mEditorRendererMode

    private var mIsHelpingPlaneVisible: Boolean = false

    private val mProjWorldMatrix = FloatArray(16)

    override val mInitializer: EditorRendererStepInitializer = EditorRendererStepInitializer()

    /**
     * Note: it can be changed multiple times having the same active Figure so 'isInitializing'
     * param is drastically important!
     */
    suspend fun setEditorRendererMode(
        editorRendererMode: EditorCanvasContext.Mode,
        isInitializing: Boolean = false
    ) {
        if (isInitializing) {
            mInitializerMutex.withLock {
                Log.d(TAG, "setEditorRendererMode(): editorRendererMode = $editorRendererMode;")
                Log.d(
                    TAG,
                    "setEditorRendererMode(): mInitializer.currentStep = ${mInitializer.currentStep};"
                )

                mInitializer.postponeEditorMode(editorRendererMode)

                if (mInitializer.currentStep != EditorRendererStepInitializer.EditorStep.EDITOR_MODE)
                    return

                onEditorModeInitializing()
            }
        } else {
            changeEditorMode(editorRendererMode)
        }
    }

    private fun onEditorModeInitializing() {
        Log.d(TAG, "onEditorModeInitializing(): entering..")

        changeEditorMode(mInitializer.editorMode!!)

        mInitializer.nextStep()

        if (mInitializer.sketch != null) onSketchStepInitializing()
    }

    private fun changeEditorMode(editorMode: EditorCanvasContext.Mode) {
        mEditorRendererMode = editorMode

        setHelpingPlaneVisibilityByMode(mEditorRendererMode)
        changeCameraNearByMode(mEditorRendererMode)
        resetFaceSketchData()
    }

    suspend fun setFaceSketchDotBuffer(
        faceSketchDots: List<Dot2D>
    ) {
        mInitializerMutex.withLock {
            Log.d(TAG, "setFaceSketchDotBuffer(): faceSketchDots = ${faceSketchDots.joinToString()};")
            Log.d(TAG, "setFaceSketchDotBuffer(): mInitializer.currentStep = ${mInitializer.currentStep}")

            mInitializer.postponeSketchDotList(faceSketchDots)

            if (mInitializer.currentStep != EditorRendererStepInitializer.EditorStep.SKETCH)
                return

            mFaceSketchMutex.withLock() { onSketchStepInitializing() }
        }
    }

    private fun onSketchStepInitializing() {
        Log.d(TAG, "onSketchStepInitializing(): entering..")

        changeFaceSketchDotBuffer(mInitializer.sketch!!)

        mInitializer.nextStep()
    }

    private fun changeFaceSketchDotBuffer(faceSketchDots: List<Dot2D>) {
        mFaceSketchDotBuffer.clear()
        mFaceSketchDotBuffer.addAll(faceSketchDots)
    }

    override fun onCameraStepInitializing() {
        super.onCameraStepInitializing()

        if (mInitializer.editorMode != null) onEditorModeInitializing()
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
                            .map { (it + vertexShift) })
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
        Log.d(TAG, "resetFaceSketchData(): mFaceSketchDotBuffer = ${mFaceSketchDotBuffer.joinToString()}")

        mFaceSketchDotBuffer.clear()

        mLastFaceSketchVertexArray = floatArrayOf()
        mLastFaceSketchDrawingOrder = intArrayOf()
    }

    private fun generateFigure(vertexArray: FloatArray, drawingOrderArray: IntArray): GLDrawing {
        return GLDrawing(vertexArray, drawingOrderArray)
    }

    private suspend fun getLastFaceSketch(): FaceSketch? {
        var faceSketch: FaceSketch? = null

        mFaceSketchMutex.withLock {
            if (mLastFaceSketchVertexArray.isEmpty()) return null

            val vertexTripleArray = mLastFaceSketchVertexArray.toVertexTripleArray()
            val face = mLastFaceSketchDrawingOrder
                .map { Triple<Int, Int?, Int?>(it, null, null) }.toTypedArray()

            faceSketch = FaceSketch(vertexTripleArray, face)
        }

        return faceSketch
    }

    private fun changeCameraNearByMode(mode: EditorCanvasContext.Mode) {
        val cameraNear = when (mode) {
            EditorCanvasContext.Mode.VIEWING -> CameraContext.DEFAULT_CAMERA_NEAR
            EditorCanvasContext.Mode.CREATING_FACE -> MIN_HELPING_PLANE_DISTANCE
        }

        changeCameraNear(cameraNear)
    }

    private fun setHelpingPlaneVisibilityByMode(mode: EditorCanvasContext.Mode) {
        mIsHelpingPlaneVisible = when (mode) {
            EditorCanvasContext.Mode.VIEWING -> false
            EditorCanvasContext.Mode.CREATING_FACE -> true
        }
    }

    override fun handleScale(gottenScaleFactor: Float) {
        when (mEditorRendererMode) {
            EditorCanvasContext.Mode.CREATING_FACE ->
                handleHelpingPlaneDistanceChange(gottenScaleFactor)
            else -> super.handleScale(gottenScaleFactor)
        }
    }

    override fun handleRotation(dx: Float, dy: Float) {
        when (mEditorRendererMode) {
            EditorCanvasContext.Mode.CREATING_FACE -> {  } // nothing?
            else -> super.handleRotation(dx, dy)
        }
    }

    suspend fun handleClick(x: Float, y: Float) {
        when (mEditorRendererMode) {
            EditorCanvasContext.Mode.CREATING_FACE -> addSketchVertex(x, y)
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
        val newNear = mCameraData.cameraNear * distanceFactor//mCameraNear * distanceFactor
        val filteredNewNear =
            if (newNear <= MIN_HELPING_PLANE_DISTANCE) MIN_HELPING_PLANE_DISTANCE
            else if (newNear >= mSphereRadius * 2) mSphereRadius * 2 - HELPING_PLANE_MODEL_GAP
            else newNear

        changeCameraNear(filteredNewNear)
    }

    private fun changeCameraNear(near: Float) {//= mCameraMutex.withLock {
        Log.d(TAG, "changeCameraNear(): near = $near")

        mCameraData.setCameraNear(near)
        //mCameraNear = near

        setPerspective()
    }

    private fun generateHelpingPlaneGLDrawing(): GLDrawing {
        return GLDrawing(
            floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 0f, 0f),
            intArrayOf(0, 1, 2, 0, 2, 3),
            GLContext.DrawingMode.FILLED,
            HELPING_PLANE_DRAWING_COLOR
        )
    }

    private fun generateFaceSketchDrawing(): GLDrawing {
        return GLDrawing(
            floatArrayOf(),
            intArrayOf(),
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

        Log.d(TAG, "getFaceSketchVerticesByDots(): faceSketchVertices = ${faceSketchVertices.joinToString()};")

        return faceSketchVertices
    }

    private fun getFaceSketchDrawingOrderByVertices(
        faceSketchVertices: FloatArray
    ): IntArray {
        val vertexIdArray = IntRange(
            0,
            faceSketchVertices.size / DrawingContext.COORDS_PER_VERTEX - 1
        ).toList().toIntArray()

        val vertexCount = vertexIdArray.size

        if (vertexCount < 3) return vertexIdArray

        val drawingOrder = GL2Util.polygonToTriangles(vertexIdArray)

        return drawingOrder.toIntArray()
    }
}