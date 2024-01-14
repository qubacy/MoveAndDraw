package com.qubacy.moveanddraw.data.drawing.repository.source.local.serializer._common

import com.qubacy.moveanddraw.data.drawing.model.DataDrawing

interface DrawingSerializer {
    fun serialize(drawing: DataDrawing): String
}