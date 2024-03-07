package com.idt.luckycat.connect.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.idt.luckycat.connect.HostScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConnectViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(ConnectUiState())
    val uiState = _uiState.asStateFlow()

    private val scanner: HostScanner = HostScanner()
    private var scanJob: Job? = null

    fun onEnter() {
        initScanner()
        onScan()
    }

    fun onScan() {
        val context = getApplication<Application>()
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            scanner.findHosts(context, 18458)

            _uiState.update { it.copy(isScanning = false) }
        }
    }

    fun onSelect(host: String) {
        _uiState.update { it.copy(hostSelected = host) }
    }

    fun onConnect() {
        scanJob?.cancel()
    }

    private fun initScanner() = viewModelScope.launch {
        scanner.hosts.collect { hosts ->
            _uiState.update {
                it.copy(hosts = hosts.mapNotNull { host -> host.hostAddress })
            }
        }
    }
}

data class ConnectUiState(
    val hosts: List<String> = emptyList(),
    val hostSelected: String? = null,
    val isScanning: Boolean = false,
) {
    val connectButtonEnabled: Boolean = !hostSelected.isNullOrBlank()
}