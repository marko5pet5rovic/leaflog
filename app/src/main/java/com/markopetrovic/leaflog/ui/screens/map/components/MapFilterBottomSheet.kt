@file:OptIn(ExperimentalMaterial3Api::class)

package com.markopetrovic.leaflog.ui.screens.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markopetrovic.leaflog.ui.viewmodels.LocationFilter

@Composable
fun MapFilterBottomSheet(
    activeFilter: LocationFilter,
    onFilterSelected: (LocationFilter) -> Unit,
    currentRadius: Float,
    onRadiusChanged: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Filter Locations",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Filter by Type:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocationFilter.entries.forEach { filter ->
                    FilterChip(
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter.name.replace("_", " ").toUpperCase()) },
                        selected = filter == activeFilter,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Filter by Radius:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Radius: ${"%.1f".format(currentRadius / 1000f)} km",
                style = MaterialTheme.typography.bodyLarge
            )

            Slider(
                value = currentRadius,
                onValueChange = onRadiusChanged,
                valueRange = 500f..20000f,
                steps = 9
            )
            Text(
                text = "Minimum 0.5 km, Maximum 20 km",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Apply and Close")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
