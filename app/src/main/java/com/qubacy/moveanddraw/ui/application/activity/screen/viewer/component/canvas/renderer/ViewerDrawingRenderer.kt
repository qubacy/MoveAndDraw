package com.qubacy.moveanddraw.ui.application.activity.screen.viewer.component.canvas.renderer

import android.opengl.GLSurfaceView.Renderer
import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ViewerDrawingRenderer(

) : Renderer {
    private var mCurrentDrawing: Drawing? = null

    fun setDrawing(drawing: Drawing) {
        mCurrentDrawing = drawing

        // todo: something else?..


    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gl!!.glClearColor(15f, 15f, 15f, 0f)
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {
        if (mCurrentDrawing == null) return

        gl!!.glClearColor(15f, 15f, 15f, 0f)
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gl.glLoadIdentity()

        gl.glTranslatef(0.0f, 0.0f, -5.0f)

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mCurrentDrawing!!.vertexBuffer)
        gl.glNormalPointer(GL10.GL_FLOAT, 0, mCurrentDrawing!!.normalBuffer)
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mCurrentDrawing!!.textureBuffer)

        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, mCurrentDrawing!!.numVertices)

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY)
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
    }
}