package com.proto.focusonwork.presentation

import androidx.lifecycle.ViewModel
import com.proto.focusonwork.domain.model.FocusSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.proto.focusonwork.presentation.session.remainingSeconds

/** UI state for the dashboard and the active focus session. */
data class FocusUiState(
    val durationMinutes: Int = 45,
    val selectedPackages: Set<String> = emptySet(),
    val activeSession: FocusSession? = null
)

class FocusViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    fun selectDuration(minutes: Int) {
        require(minutes > 0) { "Duration must be positive" }
        _uiState.value = _uiState.value.copy(durationMinutes = minutes)
    }

    fun togglePackage(packageName: String) {
        val selected = _uiState.value.selectedPackages
        val updated = if (packageName in selected) selected - packageName else selected + packageName
        _uiState.value = _uiState.value.copy(selectedPackages = updated)
    }

    fun startSession(nowMillis: Long = System.currentTimeMillis()) {
        val state = _uiState.value
        _uiState.value = state.copy(
            activeSession = FocusSession(
                durationMinutes = state.durationMinutes,
                startedAtMillis = nowMillis,
                endsAtMillis = nowMillis + state.durationMinutes * 60_000L,
                blockedPackages = state.selectedPackages
            )
        )
    }

    fun endSession() {
        _uiState.value = _uiState.value.copy(activeSession = null)
    }

    fun remainingSeconds(nowMillis: Long = System.currentTimeMillis()): Long =
        _uiState.value.activeSession?.let { remainingSeconds(it.endsAtMillis, nowMillis) } ?: 0L
}
