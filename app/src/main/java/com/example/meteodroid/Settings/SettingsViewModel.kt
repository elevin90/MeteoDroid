package com.example.meteodroid.Settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor (
    private val repo: SettingsRepository
) : ViewModel() {
    val unit: StateFlow<TemperatureUnit> = repo.unitFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), TemperatureUnit.CELSIUS
    )

    fun setUnit(u: TemperatureUnit) = viewModelScope.launch { repo.setUnit(u) }
}