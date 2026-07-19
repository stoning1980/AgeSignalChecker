package com.example.agesignaltest

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _uiState = MutableStateFlow<AgeSignalUiState>(AgeSignalUiState.Idle)
    val uiState: StateFlow<AgeSignalUiState> = _uiState

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
                    userStatus = result.userStatus() ?: -1,
                    userStatusName = mapUserStatus(result.userStatus() ?: -1),
                    ageLower = result.ageLower(),
                    ageUpper = result.ageUpper(),
                    installId = result.installId(),
                    latencyMs = latency,
                    rawResultString = result.toString()
                )
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - startTime
                val errorCode = tryGetStatusCode(e)
                _uiState.value = AgeSignalUiState.Error(
                    exceptionType = e.javaClass.simpleName,
                    message = e.message ?: "Unknown error",
                    errorCode = errorCode,
                    latencyMs = latency,
                    stackTrace = e.stackTraceToString()
                )
            }
        }
    }

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

    private fun mapUserStatus(status: Int): String = when (status) {
        0 -> "VERIFIED"
        1 -> "SUPERVISED"
        2 -> "SUPERVISED_APPROVAL_PENDING"
        3 -> "SUPERVISED_APPROVAL_DENIED"
        4 -> "UNKNOWN"
        5 -> "DECLARED"
        else -> "UNRECOGNIZED($status)"
    }

    private fun tryGetStatusCode(e: Exception): Int? {
        return try {
            val method = e.javaClass.getMethod("getStatusCode")
            method.invoke(e) as? Int
        } catch (_: Exception) {
            null
        }
    }
}
