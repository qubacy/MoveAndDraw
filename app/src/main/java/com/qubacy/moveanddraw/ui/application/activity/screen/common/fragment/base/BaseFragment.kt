package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.base

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.BaseViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation

abstract class BaseFragment<
    UiStateType : UiState,
    ViewModelType : BaseViewModel<UiStateType>
>() : Fragment() {
    protected abstract val mModel: ViewModelType

    private lateinit var mPermissionRequestLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (getPermissionsToRequest() == null) return

        when {
            checkPermissions() -> {
                onRequestedPermissionsGranted()
            }
            else -> {
                mPermissionRequestLauncher = getPermissionRequestLauncher()

                mPermissionRequestLauncher.launch(getPermissionsToRequest())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mModel.uiState.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            processUiState(it)
        }
    }

    protected open fun processUiState(uiState: UiStateType) {
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

    private fun getPermissionRequestLauncher(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(), getPermissionRequestCallback())
    }

    private fun getPermissionRequestCallback(): ActivityResultCallback<Map<String, Boolean>> {
        return ActivityResultCallback<Map<String, Boolean>> {
            val deniedPermissions = mutableListOf<String>()

            for (requestedPermission in getPermissionsToRequest()!!) {
                if (!it.containsKey(requestedPermission)) return@ActivityResultCallback

                if (it[requestedPermission] != true) {
                    deniedPermissions.add(requestedPermission)
                }
            }

            if (deniedPermissions.isEmpty()) onRequestedPermissionsGranted()
            else onRequestedPermissionsDenied(deniedPermissions)
        }
    }

    open fun getPermissionsToRequest(): Array<String>? {
        return null
    }

    open fun onRequestedPermissionsGranted() {

    }

    open fun onRequestedPermissionsDenied(deniedPermissions: List<String>) {

    }

    fun closeSoftKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

        inputMethodManager?.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}