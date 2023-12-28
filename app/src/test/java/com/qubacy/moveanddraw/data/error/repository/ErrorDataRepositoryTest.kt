package com.qubacy.moveanddraw.data.error.repository

import com.qubacy.moveanddraw.data.error.repository.source.local.LocalErrorDataSource
import com.qubacy.moveanddraw.data.error.repository.source.local.model.ErrorEntity
import com.qubacy.moveanddraw.data.error.repository.source.local.model.toError
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class ErrorDataRepositoryTest {
    private lateinit var mErrorDataRepository: ErrorDataRepository

    private fun initRepository(
        errorEntity: ErrorEntity
    ) {
        val localErrorDataSourceMock = Mockito.mock(LocalErrorDataSource::class.java)

        Mockito.`when`(localErrorDataSourceMock.getErrorById(Mockito.anyLong(), Mockito.anyString()))
            .thenReturn(errorEntity)

        mErrorDataRepository = ErrorDataRepository(localErrorDataSourceMock)
    }

    @Before
    fun setup() {

    }

    @Test
    fun getErrorTest() {
        val errorEntity = ErrorEntity(0, "en", "test", false)
        val error = errorEntity.toError()

        initRepository(errorEntity)

        val gottenError = mErrorDataRepository.getError(errorEntity.id)

        Assert.assertEquals(error, gottenError)
    }
}