package com.qubacy.moveanddraw.domain.initial.result

import android.net.Uri
import com.qubacy.moveanddraw.domain._common.usecase.result._common.Result

class GetExamplePreviewsResult(
    val previewUris: List<Uri>
) : Result {

}