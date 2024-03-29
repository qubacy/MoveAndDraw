package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.base

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AttrRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.BaseViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.util.color.getColorByAttr
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.util.statusbar.setLightSystemBarIconColor
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.util.statusbar.setNavigationBarBackgroundColorByColorInt
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.util.statusbar.setStatusBarBackgroundColorByColorInt
import kotlinx.coroutines.runBlocking

// TODO: it could be implemented via a base class with some common fields for all
//  the fragments (mModel, for instance) & 2 other interfaces with their implementations:
//  StyleableFragment (for style-related stuff) and PermissionFragment. With this concept,
//  it'd be possible to inherit each of them by demand instead of just sharing it with all
//  the fragments by default;
abstract class BaseFragment<
    UiStateType : UiState,
    ViewModelType : BaseViewModel<UiStateType>
>() : Fragment() {
    protected abstract val mModel: ViewModelType

    private lateinit var mPermissionRequestLauncher: ActivityResultLauncher<Array<String>>

    protected open val mIsAutomaticPermissionRequestEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (mIsAutomaticPermissionRequestEnabled) requestPermissions()
    }

    protected fun requestPermissions(endAction: (() -> Unit)? = null) {
        if (getPermissionsToRequest() == null) return

        when {
            checkPermissions() -> {
                onRequestedPermissionsGranted(endAction)
            }
            else -> {
                mPermissionRequestLauncher = getPermissionRequestLauncher(endAction)

                mPermissionRequestLauncher.launch(getPermissionsToRequest())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            setSystemBarsColor()

        mModel.uiState.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            processUiState(it)
        }
    }

    override fun onStop() {
        mModel.resetUiState()

        super.onStop()
    }

    protected open fun setSystemBarsColor() {
        setSystemBarsColorByAttr(com.google.android.material.R.attr.colorSurface)
    }

    protected fun setSystemBarsColorByAttr(@AttrRes attr: Int) {
        setStatusBarColorByAttr(attr)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            setNavigationBarColorByAttr(attr)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            setLightSystemBarIconColor()
    }

    protected fun setStatusBarColorByAttr(@AttrRes attr: Int) {
        val statusBarBackgroundColor = getColorByAttr(attr)

        setStatusBarBackgroundColorByColorInt(statusBarBackgroundColor)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    protected fun setNavigationBarColorByAttr(@AttrRes attr: Int) {
        val navigationBarBackgroundColor = getColorByAttr(attr)

        setNavigationBarBackgroundColorByColorInt(navigationBarBackgroundColor)
    }

    protected open fun processUiState(uiState: UiStateType) = runBlocking {
        setUiElementsState(uiState)

        while (true) {
            val uiOperation = uiState.pendingOperations.take() ?: break

            when (uiOperation::class) {
                ShowErrorUiOperation::class ->
                    processShowErrorUiOperation(uiOperation as ShowErrorUiOperation)
                else -> processUiOperation(uiOperation)
            }
        }
    }

    protected open fun processShowErrorUiOperation(uiOperation: ShowErrorUiOperation) {
        onErrorOccurred(uiOperation.error)
    }

    protected abstract fun setUiElementsState(uiState: UiStateType)
    protected open fun processUiOperation(uiOperation: UiOperation) = Unit

    open fun onMessageOccurred(
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(requireContext(), message, duration).show()
    }

    fun onMessageOccurred(
        @StringRes message: Int,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        onMessageOccurred(getString(message), duration)
    }

    open fun onErrorOccurred(error: Error, callback: (() -> Unit)? = null) {
        val onDismiss = Runnable {
            handleError(error)
            callback?.invoke()
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.component_error_dialog_title)
            .setMessage(error.message)
            .setNeutralButton(R.string.component_error_dialog_neutral_button_caption) { _, _ ->
                onDismiss.run()
            }
            .setOnDismissListener {
                onDismiss.run()
            }
            .show()
    }

    open fun handleError(error: Error) {
        if (error.isCritical) {
            requireActivity().finishAndRemoveTask()
        }
    }

    private fun checkPermissions(): Boolean {
        for (permission in getPermissionsToRequest()!!) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED)
            {
                return false
            }
        }

        return true
    }

    private fun getPermissionRequestLauncher(
        endAction: (() -> Unit)? = null
    ): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            getPermissionRequestCallback(endAction)
        )
    }

    private fun getPermissionRequestCallback(
        endAction: (() -> Unit)? = null
    ): ActivityResultCallback<Map<String, Boolean>> {
        return ActivityResultCallback<Map<String, Boolean>> {
            val deniedPermissions = mutableListOf<String>()

            for (requestedPermission in getPermissionsToRequest()!!) {
                if (!it.containsKey(requestedPermission)) return@ActivityResultCallback

                if (it[requestedPermission] != true) {
                    deniedPermissions.add(requestedPermission)
                }
            }

            if (deniedPermissions.isEmpty()) onRequestedPermissionsGranted(endAction)
            else onRequestedPermissionsDenied(deniedPermissions)
        }
    }

    open fun getPermissionsToRequest(): Array<String>? {
        return null
    }

    open fun onRequestedPermissionsGranted(endAction: (() -> Unit)? = null) {
        endAction?.invoke()
    }

    open fun onRequestedPermissionsDenied(deniedPermissions: List<String>) {

    }

    fun closeSoftKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

        inputMethodManager?.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}