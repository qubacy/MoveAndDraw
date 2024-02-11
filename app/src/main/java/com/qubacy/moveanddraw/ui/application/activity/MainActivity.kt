package com.qubacy.moveanddraw.ui.application.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw.ui.application.activity.file.picker.GetFileUriCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class MainActivity : AppCompatActivity() {
    companion object {
        const val DEFAULT_MIME_TYPE = "*/*"
    }

    private lateinit var mChooseLocalFileLauncher: ActivityResultLauncher<String>

    private var mChooseLocalFileCallback: GetFileUriCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        setContentView(R.layout.activity_main)
        initChooseLocalFileLauncher()
    }

    private fun initChooseLocalFileLauncher() {
        val contract = ActivityResultContracts.GetContent()

        mChooseLocalFileLauncher = registerForActivityResult(contract) { onLocalFileChosen(it) }
    }

    open fun chooseLocalFile(mimeType: String = DEFAULT_MIME_TYPE, callback: GetFileUriCallback) {
        mChooseLocalFileCallback = callback

        mChooseLocalFileLauncher.launch(mimeType)
    }

    private fun onLocalFileChosen(fileUri: Uri?) {
        mChooseLocalFileCallback?.onFileUriGotten(fileUri)
    }

    open fun shareLocalFile(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        val shareIntent = Intent.createChooser(intent, null)

        startActivity(shareIntent)
    }

    open fun setStatusBarBackgroundColor(@ColorInt color: Int) {
        window.statusBarColor = color
    }

    @RequiresApi(Build.VERSION_CODES.O)
    open fun setNavigationBarBackgroundColor(@ColorInt color: Int) {
        window.navigationBarColor = color
    }

    @RequiresApi(Build.VERSION_CODES.M)
    open fun setLightSystemBarIconColor() {
        window.decorView.systemUiVisibility =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}