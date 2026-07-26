package com.baijiahu.test.age.signal

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.agesignals.AgeSignalsAccessRequest
import com.google.android.play.agesignals.AgeSignalsAccessResult
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainViewModel : ViewModel() {

    private companion object {
        const val AGE_SIGNALS_STATUS_SHARED = 1
    }

    private val _uiState = MutableStateFlow<AgeSignalUiState>(AgeSignalUiState.Idle)
    val uiState: StateFlow<AgeSignalUiState> = _uiState

    var isAccessGranted: Boolean = false
        private set

    fun fetchAgeSignals(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            _uiState.value = AgeSignalUiState.Unsupported(
                "API not supported on this device (requires API 23+)"
            )
            return
        }

        _uiState.value = AgeSignalUiState.Loading
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                val result = checkAgeSignals(context)
                val latency = System.currentTimeMillis() - startTime
                _uiState.value = AgeSignalUiState.Success(
                    ageRangeSource = result.ageRangeSource(),
                    ageRangeSourceName = mapAgeRangeSource(result.ageRangeSource()),
                    significantChangeStatus = result.significantChangeStatus(),
                    significantChangeStatusName = mapSignificantChangeStatus(result.significantChangeStatus()),
                    significantChangeApprovalDate = result.significantChangeApprovalDate()?.toString(),
                    ageLower = result.ageLower(),
                    ageUpper = result.ageUpper(),
                    installId = result.installId(),
                    latencyMs = latency,
                    rawResultString = result.toString()
                )
            } catch (e: Exception) {
                _uiState.value = buildError(e, System.currentTimeMillis() - startTime)
            }
        }
    }

    fun requestAgeSignalsAccess(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            _uiState.value = AgeSignalUiState.Unsupported(
                "API not supported on this device (requires API 23+)"
            )
            return
        }

        _uiState.value = AgeSignalUiState.Loading
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                val result = requestAccess(activity)
                val latency = System.currentTimeMillis() - startTime
                isAccessGranted = result.ageSignalsStatus() == AGE_SIGNALS_STATUS_SHARED
                _uiState.value = AgeSignalUiState.AccessSuccess(
                    ageSignalsStatus = result.ageSignalsStatus(),
                    ageSignalsStatusName = mapAgeSignalsStatus(result.ageSignalsStatus()),
                    latencyMs = latency
                )
            } catch (e: Exception) {
                _uiState.value = buildError(e, System.currentTimeMillis() - startTime)
            }
        }
    }

    private fun buildError(e: Exception, latency: Long): AgeSignalUiState.Error =
        AgeSignalUiState.Error(
            exceptionType = e.javaClass.simpleName,
            message = e.message ?: "Unknown error",
            errorCode = tryGetStatusCode(e),
            latencyMs = latency,
            stackTrace = e.stackTraceToString()
        )

    private suspend fun checkAgeSignals(context: Context): AgeSignalsResult =
        suspendCancellableCoroutine { cont ->
            try {
                val manager = AgeSignalsManagerFactory.create(context)
                manager.checkAgeSignals(AgeSignalsRequest.builder().build())
                    .addOnSuccessListener { result ->
                        cont.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        cont.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }

    private suspend fun requestAccess(activity: Activity): AgeSignalsAccessResult =
        suspendCancellableCoroutine { cont ->
            try {
                val manager = AgeSignalsManagerFactory.create(activity)
                val request = AgeSignalsAccessRequest.builder()
                    .setActivity(activity)
                    .build()
                manager.requestAgeSignalsAccess(request)
                    .addOnSuccessListener { result ->
                        cont.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        cont.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }

    private fun mapAgeRangeSource(source: Int?): String = when (source) {
        null -> "null"
        0 -> "UNSPECIFIED"
        1 -> "TIER_A"
        2 -> "TIER_B"
        3 -> "TIER_C"
        4 -> "TIER_D"
        else -> "UNRECOGNIZED($source)"
    }

    private fun mapSignificantChangeStatus(status: Int?): String = when (status) {
        null -> "null"
        0 -> "UNSPECIFIED"
        1 -> "APPROVED"
        2 -> "PENDING"
        3 -> "DECLINED"
        else -> "UNRECOGNIZED($status)"
    }

    private fun mapAgeSignalsStatus(status: Int?): String = when (status) {
        null -> "null"
        0 -> "UNSPECIFIED"
        1 -> "SHARED"
        2 -> "NOT_SHARED"
        3 -> "VERIFICATION_REQUIRED"
        else -> "UNRECOGNIZED($status)"
    }

    private fun tryGetStatusCode(e: Exception): Int? {
        return try {
            val method = e.javaClass.getMethod("getErrorCode")
            method.invoke(e) as? Int
        } catch (_: Exception) {
            null
        }
    }
}
