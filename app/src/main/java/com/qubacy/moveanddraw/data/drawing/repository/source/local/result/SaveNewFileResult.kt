package com.qubacy.moveanddraw.data.drawing.repository.source.local.result

import android.net.Uri

data class SaveNewFileResult(
    val filePath: String,
    val uri: Uri
) {

}