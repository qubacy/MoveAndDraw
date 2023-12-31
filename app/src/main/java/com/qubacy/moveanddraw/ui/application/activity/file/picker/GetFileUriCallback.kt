package com.qubacy.moveanddraw.ui.application.activity.file.picker

import android.net.Uri

interface GetFileUriCallback {
    fun onFileUriGotten(fileUri: Uri?)
}