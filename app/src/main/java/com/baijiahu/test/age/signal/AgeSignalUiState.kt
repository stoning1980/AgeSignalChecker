package com.baijiahu.test.age.signal

sealed class AgeSignalUiState {
    object Idle : AgeSignalUiState()
    object Loading : AgeSignalUiState()

    data class Success(
        val userStatus: Int,
        val userStatusName: String,
        val ageLower: Int?,
        val ageUpper: Int?,
        val installId: String?,
        val latencyMs: Long,
        val rawResultString: String
    ) : AgeSignalUiState()

    data class Error(
        val exceptionType: String,
        val message: String,
        val errorCode: Int?,
        val latencyMs: Long,
        val stackTrace: String
    ) : AgeSignalUiState()

    data class Unsupported(
        val reason: String
    ) : AgeSignalUiState()
}
