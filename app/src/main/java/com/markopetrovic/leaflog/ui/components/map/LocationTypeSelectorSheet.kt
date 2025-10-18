package com.markopetrovic.leaflog.ui.components.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite

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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select what you want to add",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LocationTypeButton(
                    label = "New Plant",
                    icon = Icons.Default.CheckCircle,
                    onClick = { onTypeSelected(LocationType.PLANT) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LocationTypeButton(
                    label = "New Mushroom",
                    icon = Icons.Default.Favorite,
                    onClick = { onTypeSelected(LocationType.MUSHROOM) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LocationTypeButton(
                    label = "New Planting Spot",
                    icon = Icons.Default.LocationOn,
                    onClick = { onTypeSelected(LocationType.PLANTING_SPOT) }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun LocationTypeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label)
    }
}