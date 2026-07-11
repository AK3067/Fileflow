package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.data.FileEntity
import com.example.data.FileType
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: FileViewModel,
    openedCollection: String?,
    onOpenedCollectionChanged: (String?) -> Unit,
    onNavigate: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Collect DB states
    val rawFiles by viewModel.currentFiles.collectAsStateWithLifecycle()
    val trashedList by viewModel.trashedFiles.collectAsStateWithLifecycle()
    val favoritesList by viewModel.favoriteFiles.collectAsStateWithLifecycle()
    val albumsList by viewModel.allAlbums.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    
    // Filtered files list based on search query
    val files = remember(rawFiles, searchQuery) {
        if (searchQuery.isBlank()) {
            rawFiles
        } else {
            rawFiles.filter { file ->
                file.name.contains(searchQuery, ignoreCase = true) ||
                (file.location?.contains(searchQuery, ignoreCase = true) == true) ||
                (file.metadata?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }
    
    // Cached derived states
    val quickAccessList = remember(files) { files.filter { it.isFavorite } }
    val screenshotList = remember(files) { files.filter { it.type == FileType.IMAGE && (it.name.contains("screen", ignoreCase = true) || it.id % 3 == 0) } }

    // Multi-select state for Recycle Bin
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedFiles = remember { androidx.compose.runtime.mutableStateListOf<FileEntity>() }
    
    // Recycle Bin dialogues confirmation state
    var showEmptyBinConfirm by remember { mutableStateOf(false) }
    var showDeleteSelectedConfirm by remember { mutableStateOf(false) }
    var showRestoreSelectedConfirm by remember { mutableStateOf(false) }
    
    // Selected lightbox item inside collections
    var selectedLightboxFileInCollection by remember { mutableStateOf<FileEntity?>(null) }
    
    val currentCollectionList = remember(files, openedCollection, quickAccessList, favoritesList, screenshotList, albumsList) {
        if (openedCollection == null) {
            emptyList()
        } else {
            when (openedCollection) {
                "Quick access" -> quickAccessList
                "Favorites" -> favoritesList
                "Screenshot" -> screenshotList
                else -> {
                    val matchedAlbum = albumsList.find { it.name.equals(openedCollection, ignoreCase = true) }
                    if (matchedAlbum != null) {
                        files.filter { it.albumId == matchedAlbum.id }
                    } else {
                        emptyList()
                    }
                }
            }
        }
    }
    
    // Customized state controllers (Req 5, 6, 7)
    var showAllShelvesDetail by remember { mutableStateOf(false) }
    var showSettingsDropdown by remember { mutableStateOf(false) }
    var showCreateAlbumDialog by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }
    var selectedAlbumIcon by remember { mutableStateOf("folder") }

    // Clear selection state whenever openedCollection changes
    remember(openedCollection) {
        isMultiSelectMode = false
        selectedFiles.clear()
        true
    }

    // Dark glassmorphism background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
    ) {
        // Ambient background gradients for the "glass" to interact with
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-50).dp, y = (-50).dp)
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2A4B8C).copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(350.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6A2A8C).copy(alpha = 0.4f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 110.dp, bottom = 120.dp),
        ) {
            item {
                // Top Utilities Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LibraryUtilityTile(
                        modifier = Modifier.weight(1f),
                        title = "Recycle Bin",
                        icon = Icons.Outlined.DeleteOutline,
                        onClick = { onOpenedCollectionChanged("Recycle Bin") }
                    )
                    LibraryUtilityTile(
                        modifier = Modifier.weight(1f),
                        title = "Quick access",
                        icon = Icons.Default.Star,
                        onClick = { onOpenedCollectionChanged("Quick access") }
                    )
                    LibraryUtilityTile(
                        modifier = Modifier.weight(1f),
                        title = "Favorites",
                        icon = Icons.Outlined.FavoriteBorder,
                        onClick = { onOpenedCollectionChanged("Favorites") }
                    )
                    LibraryUtilityTile(
                        modifier = Modifier.weight(1f),
                        title = "Screenshot",
                        icon = Icons.Outlined.Crop,
                        onClick = { onOpenedCollectionChanged("Screenshot") }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // My Shelves Header (Req 7)
                SectionHeader("My Shelves", onViewAllClick = { showAllShelvesDetail = true })
                
                // Main body: My Shelves list inside a Glassmorphism card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(max = 350.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    if (albumsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No shelves created yet",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        albumsList.forEachIndexed { index, album ->
                            val albumCount = files.count { it.albumId == album.id }
                            AlbumListItem(
                                album = album,
                                count = albumCount,
                                subtitle = if (album.name == "Camera") "Media & DCIM" else "Office Suite Workspace",
                                onClick = { onOpenedCollectionChanged(album.name) },
                                onRename = { targetAlbum, newName -> viewModel.renameAlbum(targetAlbum, newName) },
                                onDelete = { targetId -> viewModel.deleteAlbum(targetId) },
                                onTogglePin = { targetAlbum -> viewModel.togglePinAlbum(targetAlbum) }
                            )
                            if (index < albumsList.size - 1) {
                                Divider(
                                    color = Color.White.copy(alpha = 0.05f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                // Shared Library Header
                SectionHeader("Shared Library")
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This feature is coming soon",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Header Overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                // Glassmorphism top bar
                .background(Brush.verticalGradient(
                    colors = listOf(Color(0xFF090A0F), Color(0xFF090A0F).copy(alpha = 0f))
                ))
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Library",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        IconButton(onClick = { showSettingsDropdown = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showSettingsDropdown,
                            onDismissRequest = { showSettingsDropdown = false },
                            modifier = Modifier.background(Color(0xFF1E1E1E))
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Clear Unused Cache", color = Color.White, fontSize = 14.sp)
                                    }
                                },
                                onClick = {
                                    showSettingsDropdown = false
                                    viewModel.emptyTrash() // clear trash
                                    // Simulated storage purge toast or feedback can be done inside the screen or a custom action
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Sync, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sync Storage Indexes", color = Color.White, fontSize = 14.sp)
                                    }
                                },
                                onClick = {
                                    showSettingsDropdown = false
                                    viewModel.scanDeviceFiles(context)
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("About FileFlow 1.4", color = Color.White, fontSize = 14.sp)
                                    }
                                },
                                onClick = {
                                    showSettingsDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Overlay for opened collection
        openedCollection?.let { collectionName ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF090A0F)) // Opaque dark background
                    .clickable(enabled = false) {} // block touches
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onOpenedCollectionChanged(null) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = collectionName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (collectionName == "Recycle Bin") {
                            if (trashedList.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(text = "Recycle bin is empty", color = Color.Gray, fontSize = 16.sp)
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(trashedList, key = { it.id }) { file ->
                                            val isSelected = selectedFiles.contains(file)
                                            Box(
                                                modifier = Modifier
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color.White.copy(alpha = 0.05f))
                                                    .clickable {
                                                        if (isMultiSelectMode) {
                                                            if (isSelected) selectedFiles.remove(file) else selectedFiles.add(file)
                                                        } else {
                                                            selectedFiles.clear()
                                                            selectedFiles.add(file)
                                                            showRestoreSelectedConfirm = true
                                                        }
                                                    }
                                            ) {
                                                if (file.type == FileType.IMAGE || file.type == FileType.VIDEO) {
                                                    AsyncImage(
                                                        model = "https://picsum.photos/seed/${file.id + file.name.hashCode()}/300/400",
                                                        contentDescription = file.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Column(
                                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = getIconForType(file.type),
                                                            contentDescription = null,
                                                            tint = getIconColorForType(file.type),
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(file.name, color = Color.White, fontSize = 10.sp, maxLines = 1)
                                                    }
                                                }
                                                
                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(alpha = 0.4f))
                                                    )
                                                }
                                                
                                                if (isMultiSelectMode) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(6.dp)
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color(0xFF4CAF50) else Color.Black.copy(alpha = 0.4f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isSelected) {
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    // Mid Bottom Floating pill buttons
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 32.dp)
                                            .clip(RoundedCornerShape(32.dp))
                                            .background(Color(0xE6222222))
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!isMultiSelectMode) {
                                            Button(
                                                onClick = {
                                                    isMultiSelectMode = true
                                                    selectedFiles.clear()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                                shape = RoundedCornerShape(20.dp),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Select", color = Color.White, fontSize = 13.sp)
                                            }

                                            Button(
                                                onClick = { showEmptyBinConfirm = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3333)),
                                                shape = RoundedCornerShape(20.dp),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Delete All", color = Color.White, fontSize = 13.sp)
                                            }
                                        } else {
                                            Button(
                                                onClick = { showRestoreSelectedConfirm = true },
                                                enabled = selectedFiles.isNotEmpty(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.White.copy(alpha = 0.2f),
                                                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                                                ),
                                                shape = RoundedCornerShape(20.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Restore, contentDescription = null, tint = if (selectedFiles.isNotEmpty()) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Restore (${selectedFiles.size})", color = if (selectedFiles.isNotEmpty()) Color.White else Color.Gray, fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = { showDeleteSelectedConfirm = true },
                                                enabled = selectedFiles.isNotEmpty(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFFF3333),
                                                    disabledContainerColor = Color(0xFFFF3333).copy(alpha = 0.3f)
                                                ),
                                                shape = RoundedCornerShape(20.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Delete (${selectedFiles.size})", color = Color.White, fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    isMultiSelectMode = false
                                                    selectedFiles.clear()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                                shape = RoundedCornerShape(20.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                            ) {
                                                Text("Cancel", color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            val currentList = when (collectionName) {
                                "Quick access" -> quickAccessList
                                "Favorites" -> favoritesList
                                "Screenshot" -> screenshotList
                                else -> {
                                    val matchedAlbum = albumsList.find { it.name.equals(collectionName, ignoreCase = true) }
                                    if (matchedAlbum != null) {
                                        files.filter { it.albumId == matchedAlbum.id }
                                    } else {
                                        emptyList()
                                    }
                                }
                            }
                            
                            if (currentList.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(text = "No items found", color = Color.Gray, fontSize = 16.sp)
                                    }
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(currentList, key = { it.id }) { file ->
                                        Box(
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White.copy(alpha = 0.05f))
                                                .clickable {
                                                    selectedLightboxFileInCollection = file
                                                }
                                        ) {
                                            if (file.type == FileType.IMAGE || file.type == FileType.VIDEO) {
                                                AsyncImage(
                                                    model = "https://picsum.photos/seed/${file.id + file.name.hashCode()}/300/400",
                                                    contentDescription = file.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Column(
                                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = getIconForType(file.type),
                                                        contentDescription = null,
                                                        tint = getIconColorForType(file.type),
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(file.name, color = Color.White, fontSize = 10.sp, maxLines = 1)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Collection Lightbox Details
        selectedLightboxFileInCollection?.let { file ->
            LightboxScreen(
                initialFile = file,
                allFiles = currentCollectionList,
                onDismiss = { selectedLightboxFileInCollection = null },
                onDelete = { targetFile ->
                    viewModel.trashFile(targetFile)
                    selectedLightboxFileInCollection = null
                },
                onToggleQuickAccess = { targetFile -> viewModel.toggleFavorite(targetFile) },
                onRename = { targetFile, newName -> viewModel.renameFile(targetFile, newName) }
            )
        }

        // Dialog Actions
        if (showEmptyBinConfirm) {
            AlertDialog(
                onDismissRequest = { showEmptyBinConfirm = false },
                title = { Text("Empty Recycle Bin?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete all items in the Recycle Bin? This action cannot be undone.", color = Color.LightGray) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.emptyTrash()
                            showEmptyBinConfirm = false
                            isMultiSelectMode = false
                        }
                    ) {
                        Text("Delete All", color = Color(0xFFFF3333), fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmptyBinConfirm = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showDeleteSelectedConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteSelectedConfirm = false },
                title = { Text("Delete selected items?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete these ${selectedFiles.size} items? This action cannot be undone.", color = Color.LightGray) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedFiles.forEach { file ->
                                viewModel.deleteFilePermanently(file)
                            }
                            selectedFiles.clear()
                            isMultiSelectMode = false
                            showDeleteSelectedConfirm = false
                        }
                    ) {
                        Text("Delete Permanent", color = Color(0xFFFF3333), fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteSelectedConfirm = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showRestoreSelectedConfirm) {
            AlertDialog(
                onDismissRequest = { showRestoreSelectedConfirm = false },
                title = { Text("Restore selected items?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Do you want to restore these selected items back to your library?", color = Color.LightGray) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedFiles.forEach { file ->
                                viewModel.restoreFile(file)
                            }
                            selectedFiles.clear()
                            isMultiSelectMode = false
                            showRestoreSelectedConfirm = false
                        }
                    ) {
                        Text("Restore", color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreSelectedConfirm = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(24.dp)
            )
        }

        // Glassmorphic FAB to create new albums bottom-right (Req 2, 8)
        if (openedCollection == null && !showAllShelvesDetail) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 90.dp, end = 24.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), CircleShape)
                    .clickable { showCreateAlbumDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Album/Shelf",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Create Album dialogue overlay simplified (Req 1)
        if (showCreateAlbumDialog) {
            AlertDialog(
                onDismissRequest = { showCreateAlbumDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Shelf", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Give your custom shelf a secure, unique folder name.", color = Color.LightGray, fontSize = 13.sp)
                        OutlinedTextField(
                            value = newAlbumName,
                            onValueChange = { newAlbumName = it },
                            placeholder = { Text("Album title (e.g. Europe Trips)", color = Color.Gray) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newAlbumName.isNotBlank()) {
                                viewModel.createAlbum(newAlbumName, "folder")
                                newAlbumName = ""
                                showCreateAlbumDialog = false
                            }
                        }
                    ) {
                        Text("Create", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateAlbumDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF15161C),
                shape = RoundedCornerShape(24.dp)
            )
        }

        // All Shelves Workspace overlay (Req 7)
        if (showAllShelvesDetail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF090A0F)) // opaque background
                    .clickable(enabled = false) {} // block touches
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All Shelves Workspace",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showAllShelvesDetail = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    
                    Text(
                        text = "Viewing total of ${albumsList.size} synchronized shelf directories locally cached.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(albumsList) { album ->
                            val albumCount = rawFiles.count { it.albumId == album.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable {
                                        showAllShelvesDetail = false
                                        onOpenedCollectionChanged(album.name)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (album.coverIcon) {
                                            "camera" -> Icons.Default.Camera
                                            "description" -> Icons.Default.Description
                                            "table_chart" -> Icons.Default.TableChart
                                            "slideshow" -> Icons.Default.Slideshow
                                            else -> Icons.Default.Folder
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(album.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("Shelf Index Type: ${album.coverIcon.uppercase()}", color = Color.Gray, fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("$albumCount items", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Active", color = Color.Green, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onViewAllClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (onViewAllClick != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onViewAllClick() }) {
                Text(
                    text = "View All",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun LibraryUtilityTile(modifier: Modifier = Modifier, title: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .aspectRatio(1.1f)
            .clip(RoundedCornerShape(12.dp))
            // Glassmorphism card
            .background(Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.1f), 
                    Color.White.copy(alpha = 0.02f)
                )
            ))
            .clickable { onClick() }
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}

data class AlbumMock(val title: String, val count: Int, val subtitle: String)

@Composable
fun AlbumListItem(
    album: com.example.data.AlbumEntity,
    count: Int,
    subtitle: String,
    onClick: () -> Unit,
    onRename: (com.example.data.AlbumEntity, String) -> Unit,
    onDelete: (Int) -> Unit,
    onTogglePin: (com.example.data.AlbumEntity) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf(album.name) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "https://picsum.photos/seed/${album.name.hashCode()}/200/200",
                contentDescription = "Album preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Title & Item Count
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = album.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (album.isPinned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            val subText = if (subtitle.isNotEmpty()) "$count Items • $subtitle" else "$count Items"
            Text(
                text = subText,
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
        
        // Triple dot menu
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Color(0xFF1E1E24))
            ) {
                DropdownMenuItem(
                    text = { Text(if (album.isPinned) "Unpin" else "Pin to Top", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = if (album.isPinned) Color(0xFFE91E63) else Color.Gray) },
                    onClick = {
                        showMenu = false
                        onTogglePin(album)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray) },
                    onClick = {
                        showMenu = false
                        showRenameDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete Shelf", color = Color(0xFFFF5252)) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                    onClick = {
                        showMenu = false
                        onDelete(album.id)
                    }
                )
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Shelf", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameValue.isNotBlank()) {
                            onRename(album, renameValue)
                            showRenameDialog = false
                        }
                    }
                ) {
                    Text("Rename", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}



