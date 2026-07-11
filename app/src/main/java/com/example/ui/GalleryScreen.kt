package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.FileEntity
import com.example.data.FileType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(viewModel: FileViewModel) {
    val files by viewModel.currentFiles.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    var activeFilterType by remember { mutableStateOf<String?>(null) }
    var selectedLightboxFile by remember { mutableStateOf<FileEntity?>(null) }
    var openedCollection by remember { mutableStateOf<String?>(null) }
    
    var isSearching by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    
    var columnsF by remember { mutableStateOf(4f) }
    
    val filteredFiles = remember(files, searchQuery, activeFilterType) {
        val baseList = if (searchQuery.isBlank()) {
            files
        } else {
            files.filter { file ->
                file.name.contains(searchQuery, ignoreCase = true) ||
                (file.location?.contains(searchQuery, ignoreCase = true) == true) ||
                (file.metadata?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
        
        if (activeFilterType == null) {
            baseList
        } else {
            baseList.filter { file ->
                when (activeFilterType) {
                    "images" -> file.type == FileType.IMAGE
                    "videos" -> file.type == FileType.VIDEO
                    "documents" -> file.type == FileType.DOCX || file.type == FileType.TEXT || file.type == FileType.PDF
                    "excel" -> file.type == FileType.XLSX
                    "pdf" -> file.type == FileType.PDF
                    "ppt" -> file.type == FileType.PPTX
                    else -> true
                }
            }
        }
    }
    val sortedFiles = remember(filteredFiles) { filteredFiles.sortedBy { it.updatedAt } }
    
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.size > 1) {
                                        // Pinch zoom with multi-touch only
                                        val zoom = event.calculateZoom()
                                        if (zoom != 1f) {
                                            columnsF = (columnsF / zoom).coerceIn(1f, 8f)
                                        }
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            }
                        }
                ) {
                    // Dynamic-Column Square Grid
                    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                    
                    val groupedFiles = remember(sortedFiles) {
                        sortedFiles.groupBy { file ->
                            java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(file.updatedAt))
                        }
                    }
                    val totalGridItems = sortedFiles.size + groupedFiles.size
                    
                    androidx.compose.runtime.LaunchedEffect(totalGridItems) {
                        if (totalGridItems > 0) {
                            gridState.scrollToItem(totalGridItems - 1)
                        }
                    }
                    
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(columnsF.roundToInt()),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 140.dp, bottom = 120.dp, start = 12.dp, end = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        groupedFiles.forEach { (dateStr, filesInDate) ->
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = dateStr,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 24.dp, bottom = 4.dp, start = 4.dp)
                                )
                            }
                            items(filesInDate, key = { it.id }) { file ->
                                GalleryItemCard(
                                    file = file,
                                    onClick = { selectedLightboxFile = file },
                                    onToggleQuickAccess = { viewModel.toggleFavorite(it) },
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                        placementSpec = spring(stiffness = Spring.StiffnessLow)
                                    )
                                )
                            }
                        }
                    }

                    // Header Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .background(Color(0xE6050505))
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSearching) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    placeholder = { Text("Search images, locations, tags...", color = Color.Gray, fontSize = 14.sp) },
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 14.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                                    ),
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                )
                                IconButton(onClick = { 
                                    viewModel.setSearchQuery("") 
                                    isSearching = false 
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { isSearching = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                                }
                            }
                        }

                        // Subtitle
                        Row(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${filteredFiles.size} items",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal
                            )
                            IconButton(
                                onClick = { showStatsDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info, 
                                    contentDescription = "Info", 
                                    tint = Color.Gray, 
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                LibraryScreen(
                    viewModel = viewModel,
                    openedCollection = openedCollection,
                    onOpenedCollectionChanged = { openedCollection = it },
                    onNavigate = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
            }
        }

        // Floating Bottom Nav Bar
        if (openedCollection == null) {
            GalleryBottomNav(
                selectedTab = if (pagerState.currentPage == 0) "Photos" else "Library",
                onTabSelected = { tab ->
                    val targetPage = if (tab == "Photos") 0 else 1
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(targetPage)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            )
        }

        // Separate Floating Filter Button on bottom-right (for Photos tab) (Req: "not connect them")
        if (openedCollection == null && pagerState.currentPage == 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 90.dp, end = 24.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), CircleShape)
                    .clickable { showFilterSheet = true },
                contentAlignment = Alignment.Center
            ) {
                Box {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (activeFilterType != null) MaterialTheme.colorScheme.primary else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    if (activeFilterType != null) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }

        // Filter Bottom Sheet
        if (showFilterSheet) {
            androidx.compose.ui.window.Popup(
                alignment = Alignment.BottomEnd,
                offset = androidx.compose.ui.unit.IntOffset(-40, -200),
                onDismissRequest = { showFilterSheet = false }
            ) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(5000L)
                    showFilterSheet = false
                }
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.95f))
                        .padding(24.dp)
                ) {
                    val filters = listOf(
                        "images" to Icons.Default.Image,
                        "videos" to Icons.Default.VideoFile,
                        "documents" to Icons.Default.Description,
                        "excel" to Icons.Default.TableChart,
                        "pdf" to Icons.Default.PictureAsPdf,
                        "ppt" to Icons.Default.Slideshow
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (activeFilterType != null) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.Red,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable {
                                        activeFilterType = null
                                        showFilterSheet = false
                                    }
                            )
                            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.width(40.dp))
                        }
                        filters.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                row.forEach { (type, icon) ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (activeFilterType == type) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clickable {
                                                activeFilterType = type
                                                showFilterSheet = false
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Lightbox Overlay
        selectedLightboxFile?.let { file ->
            LightboxScreen(
                initialFile = file,
                allFiles = filteredFiles,
                onDismiss = { selectedLightboxFile = null },
                onDelete = { targetFile ->
                    viewModel.trashFile(targetFile)
                    selectedLightboxFile = null
                },
                onToggleQuickAccess = { targetFile -> viewModel.toggleFavorite(targetFile) },
                onRename = { targetFile, newName -> viewModel.renameFile(targetFile, newName) }
            )
        }

        // Storage Statistics Dialog Overlay
        if (showStatsDialog) {
            androidx.compose.ui.window.Popup(
                alignment = Alignment.TopStart,
                offset = androidx.compose.ui.unit.IntOffset(80, 280),
                onDismissRequest = { showStatsDialog = false }
            ) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(5000L)
                    showStatsDialog = false
                }
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF15161C).copy(alpha = 0.95f))
                        .padding(24.dp)
                ) {
                    val activeStats = listOf(
                        Triple(files.count { it.type == FileType.IMAGE }, Icons.Default.Image, Color(0xFFE53935)),
                        Triple(files.count { it.type == FileType.VIDEO }, Icons.Default.VideoFile, Color(0xFF3949AB)),
                        Triple(files.count { it.type == FileType.PDF }, Icons.Default.PictureAsPdf, Color(0xFFD32F2F)),
                        Triple(files.count { it.type == FileType.DOCX || it.type == FileType.TEXT }, Icons.Default.Description, Color(0xFF1E88E5)),
                        Triple(files.count { it.type == FileType.XLSX }, Icons.Default.TableChart, Color(0xFF43A047)),
                        Triple(files.count { it.type == FileType.PPTX }, Icons.Default.Slideshow, Color(0xFFFB8C00))
                    ).filter { it.first > 0 }

                    if (activeStats.isEmpty()) {
                        Text("0", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            activeStats.chunked(3).forEach { rowStats ->
                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    rowStats.forEach { (count, ic, col) ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(ic, contentDescription = null, tint = col, modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(count.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

@Composable
fun GalleryBottomNav(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100))
                .background(Color(0xB3222222)) // Glassmorphism feeling
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected("Photos") }
            ) {
                Icon(Icons.Default.Photo, contentDescription = "Photos", tint = if (selectedTab == "Photos") Color.White else Color.Gray)
                Text("Photos", color = if (selectedTab == "Photos") Color.White else Color.Gray, fontSize = 11.sp, fontWeight = if (selectedTab == "Photos") FontWeight.SemiBold else FontWeight.Normal)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected("Library") }
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Library", tint = if (selectedTab == "Library") Color.White else Color.Gray)
                Text("Library", color = if (selectedTab == "Library") Color.White else Color.Gray, fontSize = 11.sp, fontWeight = if (selectedTab == "Library") FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}


@Composable
fun GalleryItemCard(
    file: FileEntity,
    onClick: () -> Unit,
    onToggleQuickAccess: (FileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val showLive = remember(file.id) { file.id % 4 == 0 }
    
    val isMedia = file.type == FileType.IMAGE || file.type == FileType.VIDEO
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Square aspect ratio
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .clickable(onClick = onClick)
    ) {
        if (isMedia) {
            // Picsum placeholder to simulate photos and gallery
            AsyncImage(
                model = "https://picsum.photos/seed/${file.id + file.name.hashCode()}/300/400",
                contentDescription = file.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Document item styling
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = getIconForType(file.type),
                    contentDescription = file.name,
                    tint = getIconColorForType(file.type),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = file.name,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }

        // Play button for videos or Live Badge on random photos
        if (file.type == FileType.VIDEO) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Video Play",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp)
                )
            }
        } else if (showLive && file.type == FileType.IMAGE) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Adjust,
                    contentDescription = "Live Photo",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "LIVE",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
