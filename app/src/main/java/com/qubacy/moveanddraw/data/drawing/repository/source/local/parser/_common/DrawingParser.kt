package com.qubacy.moveanddraw.data.drawing.repository.source.local.parser._common

import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import java.io.InputStream

interface DrawingParser {
    fun parseStream(inputStream: InputStream): DataDrawing
}