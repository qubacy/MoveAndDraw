package com.qubacy.moveanddraw.data.drawing.repository.result.save._common

import android.net.Uri
import com.qubacy.moveanddraw.data._common.repository._common.result._common.DataResult

class SaveDrawingResult(
    val filePath: String,
    val uri: Uri
) : DataResult {

}