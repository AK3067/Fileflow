package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FileEntity
import com.example.data.FileType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(viewModel: FileViewModel) {
    val files by viewModel.currentFiles.collectAsStateWithLifecycle()
    val parentId by viewModel.currentParentId.collectAsStateWithLifecycle()
    var selectedFile by remember { mutableStateOf<FileEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    
    // Simulating parent-child navigation
    // We would ideally query parent info, but for this mock let's just allow going "up" to root (null)
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (parentId == null) "All Files" else "Folder") },
            navigationIcon = {
                if (parentId != null) {
                    IconButton(onClick = { viewModel.navigateToFolder(null) }) {
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            }
        )
        
        if (files.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No files found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(files) { file ->
                    FileGridItem(
                        file = file,
                        onClick = {
                            if (file.type == FileType.FOLDER) {
                                viewModel.navigateToFolder(file.id)
                            }
                            // Else open file viewer (mock)
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
                    headlineContent = { Text(if(file.isFavorite) "Remove Favorite" else "Add to Favorites") },
                    modifier = Modifier.clickable { 
                        viewModel.toggleFavorite(file)
                        showMenu = false 
                    }
                )
                ListItem(
                    headlineContent = { Text("Rename") },
                    modifier = Modifier.clickable { 
                        // Mock rename action
                        viewModel.renameFile(file, file.name + "_renamed")
                        showMenu = false 
                    }
                )
                ListItem(
                    headlineContent = { Text("Move to Trash") },
                    colors = ListItemDefaults.colors(headlineColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.clickable { 
                        viewModel.trashFile(file)
                        showMenu = false 
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
