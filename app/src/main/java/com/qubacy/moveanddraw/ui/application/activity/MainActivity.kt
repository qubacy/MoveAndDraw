package com.qubacy.moveanddraw.ui.application.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw.ui.application.activity.file.picker.GetFileUriCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        const val DEFAULT_MIME_TYPE = "*/*"
    }

    private lateinit var mChooseLocalFileLauncher: ActivityResultLauncher<String>

    private var mChooseLocalFileCallback: GetFileUriCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        initChooseLocalFileLauncher()
    }

    private fun initChooseLocalFileLauncher() {
        val contract = ActivityResultContracts.GetContent()

        mChooseLocalFileLauncher = registerForActivityResult(contract) { onLocalFileChosen(it) }
    }

    fun chooseLocalFile(mimeType: String = DEFAULT_MIME_TYPE, callback: GetFileUriCallback) {
        mChooseLocalFileCallback = callback

        mChooseLocalFileLauncher.launch(mimeType)
    }

    private fun onLocalFileChosen(fileUri: Uri?) {
        mChooseLocalFileCallback?.onFileUriGotten(fileUri)
    }

    fun shareLocalFile(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType

            putExtra(Intent.EXTRA_STREAM, uri)
        }
        val shareIntent = Intent.createChooser(intent, null)

        startActivity(shareIntent)
    }
}