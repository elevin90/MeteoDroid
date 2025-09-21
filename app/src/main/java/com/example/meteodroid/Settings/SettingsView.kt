package com.example.meteodroid.Settings

import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.meteodroid.R

enum class TemperatureUnit(val label: String) { CELSIUS("Metric"), FAHRENHEIT("Fahrenheit"), KELVIN("Kelvin") }

val TemperatureUnit.symbol: String
    get() = when (this) {
        TemperatureUnit.CELSIUS -> "°C"
        TemperatureUnit.FAHRENHEIT -> "°F"
        TemperatureUnit.KELVIN -> "K"
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DegreeUnitSelector(
    value: TemperatureUnit,
    onValueChange: (TemperatureUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = TemperatureUnit.entries

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = value.label,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.Units_title)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    onDismiss: () -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val bottoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val unit by viewModel.unit.collectAsState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottoSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.5f)
        ) {

            Text(
                stringResource(R.string.Settings_title),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Row(modifier = Modifier.padding(top = 24.dp)) {
                DegreeUnitSelector(value = unit, onValueChange = { viewModel.setUnit(it) })
            }
        }
    }
}