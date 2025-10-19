package com.markopetrovic.leaflog.ui.screens.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.ui.graphics.vector.ImageVector

enum class LocationType {
    PLANT,
    MUSHROOM,
    PLANTING_SPOT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationTypeSelectorSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onTypeSelected: (LocationType) -> Unit
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select What You Want to Add",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LocationTypeElevatedButton(
                    label = "New Plant",
                    icon = Icons.Default.LocalFlorist,
                    onClick = { onTypeSelected(LocationType.PLANT) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LocationTypeElevatedButton(
                    label = "New Mushroom",
                    icon = Icons.Default.FilterHdr,
                    onClick = { onTypeSelected(LocationType.MUSHROOM) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LocationTypeElevatedButton(
                    label = "New Planting Spot",
                    icon = Icons.Default.AddLocation,
                    onClick = { onTypeSelected(LocationType.PLANTING_SPOT) }
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun LocationTypeElevatedButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(72.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(24.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
