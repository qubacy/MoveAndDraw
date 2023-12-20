package com.qubacy.moveanddraw.ui.application.activity.screen._common.component.dialog.error

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.qubacy.moveanddraw.R

class ErrorDialog(
    context: Context
) : AlertDialog(context) {
    class Builder(
        errorMessage: String,
        context: Context,
        onDismiss: Runnable
    ) : AlertDialog.Builder(
        context,
        com.google.android.material.R.style.MaterialAlertDialog_Material3
    ) {
        init {
            setTitle(R.string.component_error_dialog_title)
            setMessage(errorMessage)
            setNeutralButton(R.string.component_error_dialog_neutral_button_caption) {
                    dialog, which -> onDismiss.run()
            }
            setOnDismissListener {
                onDismiss.run()
            }
        }
    }
}