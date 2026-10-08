package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(viewModel: FileViewModel) {
    val trashedFiles by viewModel.trashedFiles.collectAsStateWithLifecycle()
    var selectedFile by remember { mutableStateOf<FileEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Recycle Bin") },
            actions = {
                if (trashedFiles.isNotEmpty()) {
                    IconButton(onClick = { viewModel.emptyTrash() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Empty Trash")
                    }
                }
            }
        )
        
        Text(
            text = "Files are permanently deleted after 30 days.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )

        if (trashedFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Trash is empty", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(trashedFiles) { file ->
                    FileGridItem(
                        file = file,
                        onClick = {
                            selectedFile = file
                            showMenu = true
                        },
                        onMenuClick = {
                            selectedFile = file
                            showMenu = true
                        }
                    )
                }
            }
        }
    }

    if (showMenu && selectedFile != null) {
        val file = selectedFile!!
        ModalBottomSheet(onDismissRequest = { showMenu = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(file.name, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                ListItem(
                    headlineContent = { Text("Restore") },
                    modifier = Modifier.clickable { 
                        viewModel.restoreFile(file)
                        showMenu = false 
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
