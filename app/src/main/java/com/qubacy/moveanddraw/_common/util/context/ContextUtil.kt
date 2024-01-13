package com.qubacy.moveanddraw._common.util.context

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun Context.resourceUri(resourceId: Int): Uri = with(resources) {
    Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(getResourcePackageName(resourceId))
        .appendPath(getResourceTypeName(resourceId))
        .appendPath(getResourceEntryName(resourceId))
        .build()
}

fun Context.getFileNameByUri(uri: Uri): String {
    return contentResolver.query(
        uri, null, null, null, null
    )?.use {
        val nameColumnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

        it.moveToFirst()

        it.getString(nameColumnIndex)
    }!!
}