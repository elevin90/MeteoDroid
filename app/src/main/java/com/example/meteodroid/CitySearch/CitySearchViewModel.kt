package com.example.meteodroid.CitySearch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CitySearchViewModel @Inject constructor(
    private val citySearchRepository: CitySearchRepository
): ViewModel() {
    var cityQuery by mutableStateOf("")
        private  set

    var uiState by mutableStateOf(CitySearchUIState())
        private  set

    init {
        observeQuery()
    }

    fun onQueryChange(query: String) {
        cityQuery = query
    }

    fun clearLocation() {
        cityQuery = ""
        uiState = CitySearchUIState()
    }

    private fun observeQuery() {
        viewModelScope.launch {
            snapshotFlow { cityQuery }
                .debounce(400)
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .collectLatest { cityQuery ->
                    uiState = uiState.copy(
                        isLoading = true,
                        errorMessage = null,
                        results = emptyList(),
                        isEmpty = false
                    )
                    try {
                        val result = citySearchRepository.searchCity(
                            query = cityQuery,
                            apiKey = ""
                        )
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = null,
                            results = result,
                            isEmpty = result.isEmpty()
                        )
                    } catch (e: Exception) {
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "Something went wrong",
                            results = emptyList(),
                            isEmpty = false
                        )
                    }
                }
        }
    }
}