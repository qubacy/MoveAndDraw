package com.qubacy.moveanddraw.ui.application.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.qubacy.moveanddraw.ui.application.activity.file.picker.GetFileUriCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltTestActivity : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (findViewById<View>(android.R.id.content) as ViewGroup).removeAllViews()
    }

    override fun shareLocalFile(uri: Uri, mimeType: String) { }
    override fun chooseLocalFile(mimeType: String, callback: GetFileUriCallback) { }
}