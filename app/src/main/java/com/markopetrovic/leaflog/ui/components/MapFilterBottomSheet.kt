// file: ui/components/MapFilterBottomSheet.kt (FIXED AND TRANSLATED)

package com.markopetrovic.leaflog.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markopetrovic.leaflog.ui.viewmodels.LocationFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFilterBottomSheet(
    activeFilter: LocationFilter,
    onFilterSelected: (LocationFilter) -> Unit,
    currentRadius: Float, // <--- NEW PARAMETER
    onRadiusChanged: (Float) -> Unit, // <--- NEW PARAMETER
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Filter Locations",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- FILTER BY TYPE ---
            Text(
                text = "Filter by Type:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Filter Chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LocationFilter.entries.forEach { filter ->
                    FilterChip(
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter.name.replace("_", " ")) },
                        selected = filter == activeFilter,
                    )
                }
            }

            // --- NEW: RADIUS SLIDER ---
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Filter by Radius:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Display current radius value (convert meters to km)
            Text(
                text = "Radius: ${"%.1f".format(currentRadius / 1000f)} km",
                style = MaterialTheme.typography.bodyLarge
            )

            Slider(
                value = currentRadius,
                onValueChange = onRadiusChanged,
                valueRange = 500f..5000f, // 0.5km to 5km
                steps = 9 // Allows steps of 500m
            )
            Text(
                text = "Minimum 0.5 km, Maximum 5 km",
                style = MaterialTheme.typography.bodySmall
            )


            // Close Button
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Close Filters")
            }
        }
    }
}