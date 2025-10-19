package com.markopetrovic.leaflog.ui.screens.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markopetrovic.leaflog.data.models.ProfileDTO

@Composable
fun DetailRow(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Divider()
    }
}

@Composable
fun UserTile(publisher: ProfileDTO) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = "User Avatar",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(publisher.username, style = MaterialTheme.typography.titleMedium)
            Row {
                Text(
                    text = "${publisher.totalPoints}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "total points",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
