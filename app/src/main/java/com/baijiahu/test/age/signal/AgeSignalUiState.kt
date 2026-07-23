package com.baijiahu.test.age.signal

sealed class AgeSignalUiState {
    object Idle : AgeSignalUiState()
    object Loading : AgeSignalUiState()

    data class Success(
        val ageRangeSource: Int?,
        val ageRangeSourceName: String,
        val significantChangeStatus: Int?,
        val significantChangeStatusName: String,
        val significantChangeApprovalDate: String?,
        val ageLower: Int?,
        val ageUpper: Int?,
        val installId: String?,
        val latencyMs: Long,
        val rawResultString: String
    ) : AgeSignalUiState()

    data class AccessSuccess(
        val ageSignalsStatus: Int?,
        val ageSignalsStatusName: String,
        val latencyMs: Long
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
