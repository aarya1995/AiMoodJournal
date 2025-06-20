package com.example.aimoodjournal.presentation.ui.journal_home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ImageSourceDialog(
    onDismissRequest: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onChooseFromGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                "Select Image Source",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    "Take Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTakePhotoClick() }
                        .padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    "Choose from Gallery",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChooseFromGalleryClick() }
                        .padding(vertical = 16.dp),
                    color = Color.White
                )
            }
        },
        confirmButton = {},
        dismissButton = {},
        containerColor = Color(0xFF2F1C19)
    )
} 