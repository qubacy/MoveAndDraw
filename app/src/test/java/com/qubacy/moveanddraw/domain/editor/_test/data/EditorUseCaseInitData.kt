package com.qubacy.moveanddraw.domain.editor._test.data

import com.qubacy.moveanddraw._common._test.data.InitData
import com.qubacy.moveanddraw._common._test.util.mock.UriMockUtil
import com.qubacy.moveanddraw.data.drawing.repository.result.save._common.SaveDrawingResult

data class EditorUseCaseInitData(
    val saveDrawingResult: SaveDrawingResult =
        SaveDrawingResult(String(), UriMockUtil.getMockedUri())
) : InitData {

}