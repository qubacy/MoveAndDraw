package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.view

import android.content.Context
import android.util.AttributeSet
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer.EditorCanvasRenderer

class EditorCanvasView(
    context: Context, attributeSet: AttributeSet
) : CanvasView(
    context, attributeSet
) {
    override val mRenderer: EditorCanvasRenderer = EditorCanvasRenderer()

    private var mIsInEditorMode: Boolean = false

    suspend fun enableEditorMode(isEnabled: Boolean) {
        mIsInEditorMode = isEnabled
    }
}