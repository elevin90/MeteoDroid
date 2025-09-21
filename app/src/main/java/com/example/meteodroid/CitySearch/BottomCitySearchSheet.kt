package com.example.meteodroid.CitySearch

import Location.LocationData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomCitySearchSheet(
    viewModel: CitySearchViewModel,
    onDismiss: () -> Unit,
    onCitySelected: (LocationData) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val caroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .padding(16.dp)
        ) {
            TextField(
                value = viewModel.cityQuery,
                onValueChange = { viewModel.onQueryChange(query = it) },
                label = { Text("Search city") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            when {
                viewModel.uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                viewModel.uiState.errorMessage != null -> {
                    Text(
                        text = viewModel.uiState.errorMessage.toString(),
                        textAlign = TextAlign.Center
                    )
                }

                viewModel.uiState.isEmpty -> {
                    Text(
                        text = "No results found",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    LazyColumn {
                        items(viewModel.uiState.results) { suggestion ->
                            ListItem(
                                headlineContent = { Text(suggestion.name) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        caroutineScope.launch {
                                            bottomSheetState.hide()
                                            onCitySelected(LocationData(suggestion.latitude, suggestion.longtitude))
                                            onDismiss()
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}