package com.qubacy.moveanddraw.ui.application.activity

import android.net.Uri
import com.qubacy.moveanddraw.ui.application.activity.file.picker.GetFileUriCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltTestActivity : MainActivity() {
    override fun shareLocalFile(uri: Uri, mimeType: String) { }
    override fun chooseLocalFile(mimeType: String, callback: GetFileUriCallback) { }
}