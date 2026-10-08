package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.FileEntity
import com.example.data.FileType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LightboxScreen(
    initialFile: FileEntity,
    allFiles: List<FileEntity>,
    onDismiss: () -> Unit,
    onDelete: (FileEntity) -> Unit,
    onToggleQuickAccess: (FileEntity) -> Unit,
    onRename: (FileEntity, String) -> Unit
) {
    val context = LocalContext.current
    if (allFiles.isEmpty()) {
        SideEffect { onDismiss() }
        return
    }
    
    val initialIndex = remember(initialFile, allFiles) {
        val idx = allFiles.indexOfFirst { it.id == initialFile.id }
        if (idx >= 0) idx else 0
    }
    
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { allFiles.size }
    )
    
    val currentFile = allFiles.getOrNull(pagerState.currentPage) ?: initialFile
    
    var showControls by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showMediaEditor by remember { mutableStateOf(false) }
    var showDocEditor by remember { mutableStateOf(false) }
    
    // Video state
    var isPlaying by remember { mutableStateOf(false) }
    var playProgress by remember { mutableFloatStateOf(0f) }
    
    // Reset video states on page change
    LaunchedEffect(pagerState.currentPage) {
        isPlaying = false
        playProgress = 0f
    }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                kotlinx.coroutines.delay(100)
                playProgress += 0.015f
                if (playProgress >= 1f) {
                    playProgress = 0f
                    isPlaying = false
                }
            }
        }
    }
    
    // Motion Photo state
    val showLive = currentFile.type == FileType.IMAGE && currentFile.id % 4 == 0
    var isHoldingMotionPhoto by remember { mutableStateOf(false) }
    var liveFrameIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(isHoldingMotionPhoto) {
        if (isHoldingMotionPhoto && showLive) {
            while (isHoldingMotionPhoto) {
                kotlinx.coroutines.delay(180) // 5 fps
                liveFrameIndex = (liveFrameIndex + 1) % 4
            }
        } else {
            liveFrameIndex = 0
        }
    }
    
    val motionScale by animateFloatAsState(
        targetValue = if (isHoldingMotionPhoto && showLive) 1.12f else 1.0f,
        animationSpec = tween(300),
        label = "motionScale"
    )
    
    BackHandler(onBack = onDismiss)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageFile = allFiles.getOrNull(page) ?: return@HorizontalPager
            val isMedia = pageFile.type == FileType.IMAGE || pageFile.type == FileType.VIDEO

            Box(modifier = Modifier.fillMaxSize()) {
                if (isMedia) {
                    if (pageFile.type == FileType.IMAGE) {
                        val seedOffset = if (isHoldingMotionPhoto && showLive && page == pagerState.currentPage) liveFrameIndex else 0
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(showLive) {
                                    detectTapGestures(
                                        onTap = { showControls = !showControls },
                                        onPress = {
                                            if (showLive && page == pagerState.currentPage) {
                                                try {
                                                    isHoldingMotionPhoto = true
                                                    awaitRelease()
                                                } finally {
                                                    isHoldingMotionPhoto = false
                                                }
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = "https://picsum.photos/seed/${pageFile.id + pageFile.name.hashCode() + seedOffset}/1080/1920",
                                contentDescription = pageFile.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = if (page == pagerState.currentPage) motionScale else 1.0f,
                                        scaleY = if (page == pagerState.currentPage) motionScale else 1.0f
                                    )
                            )
                        }
                    } else {
                        // Video Representation
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { showControls = !showControls }
                                    )
                                }
                        ) {
                            AsyncImage(
                                model = "https://picsum.photos/seed/${pageFile.id + pageFile.name.hashCode()}/1080/1920",
                                contentDescription = pageFile.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Video Interactive controls
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { 
                                        if (page == pagerState.currentPage) {
                                            isPlaying = !isPlaying 
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val isPageCurrentlyActiveVideo = page == pagerState.currentPage
                                if (!isPlaying || showControls || !isPageCurrentlyActiveVideo) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.35f))
                                    )
                                }
                                
                                IconButton(
                                    onClick = { 
                                        if (isPageCurrentlyActiveVideo) {
                                            isPlaying = !isPlaying 
                                        }
                                    },
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying && isPageCurrentlyActiveVideo) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                                        contentDescription = if (isPlaying && isPageCurrentlyActiveVideo) "Pause" else "Play",
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                                
                                // Video dynamic timeline
                                if (isPageCurrentlyActiveVideo) {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .padding(bottom = 110.dp, start = 24.dp, end = 24.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            val elapsed = (playProgress * 154f).toInt()
                                            Text(formatSeconds(elapsed), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(formatSeconds(154), color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Slider(
                                            value = playProgress,
                                            onValueChange = { playProgress = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary,
                                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Documents/Office reader
                    when (pageFile.type) {
                        FileType.PDF -> PdfLightboxReader(pageFile)
                        FileType.DOCX, FileType.TEXT -> DocxLightboxReader(pageFile)
                        FileType.XLSX -> XlsxLightboxReader(pageFile)
                        FileType.PPTX -> PptxLightboxReader(pageFile)
                        else -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(onTap = { showControls = !showControls })
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = getIconForType(pageFile.type),
                                    contentDescription = pageFile.name,
                                    tint = getIconColorForType(pageFile.type),
                                    modifier = Modifier.size(100.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = pageFile.name,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Live badge for Motion Photos
        if (showLive) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isHoldingMotionPhoto) MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Adjust,
                    contentDescription = "Live Photo",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isHoldingMotionPhoto) "PLAYING" else "MOTION PHOTO",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            val isCurrentMedia = currentFile.type == FileType.IMAGE || currentFile.type == FileType.VIDEO
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Bar: Back, left-aligned office file name, three dot at far right
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    
                    if (!isCurrentMedia) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = currentFile.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    var showDropdown by remember { mutableStateOf(false) }
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(onClick = { showDropdown = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false },
                            modifier = Modifier.background(Color(0xFF1E1E1E))
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (currentFile.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = if (currentFile.isFavorite) Color.Yellow else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (currentFile.isFavorite) "Remove from Quick access" else "Add to Quick access",
                                            color = Color.White
                                        )
                                    }
                                },
                                onClick = {
                                    showDropdown = false
                                    onToggleQuickAccess(currentFile)
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TextFields,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Rename",
                                            color = Color.White
                                        )
                                    }
                                },
                                onClick = {
                                    showDropdown = false
                                    renameValue = currentFile.name
                                    showRenameDialog = true
                                }
                            )
                        }
                    }
                }
                
                // Bottom Controls (Glassmorphism)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                        .navigationBarsPadding(),
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val formattedSize = formatSize(currentFile.size)
                        LightboxAction(icon = Icons.Default.Share, label = "Share") { 
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Share File: ${currentFile.name}")
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT, 
                                    "Check out this file on FileFlow:\n\nName: ${currentFile.name}\nSize: $formattedSize\nLocation: ${currentFile.location ?: "Local Storage"}\nTags: ${currentFile.metadata ?: "None"}"
                                )
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share with"))
                        }
                        LightboxAction(icon = Icons.Default.Edit, label = "Edit") {
                            if (currentFile.type == FileType.IMAGE || currentFile.type == FileType.VIDEO) {
                                showMediaEditor = true
                            } else {
                                showDocEditor = true
                            }
                        }
                        LightboxAction(icon = Icons.Default.Delete, label = "Delete") { showDeleteDialog = true }
                    }
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { showInfoDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = "Move to Recycle Bin?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to move \"${currentFile.name}\" to the Recycle Bin? It will be deleted permanently after 30 days.",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDelete(currentFile)
                        }
                    ) {
                        Text("Move to Bin", color = Color(0xFFFF4D4D), fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename File", color = Color.White, fontWeight = FontWeight.Bold) },
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
                                onRename(currentFile, renameValue)
                                showRenameDialog = false
                            }
                        }
                    ) {
                        Text("Rename", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text("File Details", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Name: ${currentFile.name}", color = Color.LightGray, fontSize = 14.sp)
                        Text("Size: ${formatSize(currentFile.size)}", color = Color.LightGray, fontSize = 14.sp)
                        Text("Type: ${currentFile.type.name}", color = Color.LightGray, fontSize = 14.sp)
                        Text("Location: ${currentFile.location ?: "Local Storage"}", color = Color.LightGray, fontSize = 14.sp)
                        val dateString = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(currentFile.updatedAt))
                        Text("Modified: $dateString", color = Color.LightGray, fontSize = 14.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text("Close", color = MaterialTheme.colorScheme.primary)
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showMediaEditor) {
            MediaEditorOverlay(onDismiss = { showMediaEditor = false })
        }

        if (showDocEditor) {
            DocEditorOverlay(onDismiss = { showDocEditor = false })
        }
    }
}

@Composable
fun MediaEditorOverlay(onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // Mock editor image
            AsyncImage(
                model = "https://picsum.photos/seed/editormock/1080/1920",
                contentDescription = "Editor Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(bottom = 140.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFF15161C), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cancel", color = Color.Gray, modifier = Modifier.clickable { onDismiss() })
                    Text("Edit Media", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onDismiss() })
                }
                
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    EditorTool(icon = Icons.Default.Crop, label = "Crop")
                    EditorTool(icon = Icons.Default.Tune, label = "Adjust")
                    EditorTool(icon = Icons.Default.FilterVintage, label = "Filter")
                    EditorTool(icon = Icons.Default.Brush, label = "Draw")
                    EditorTool(icon = Icons.Default.TextFields, label = "Text")
                }
            }
        }
    }
}

@Composable
fun DocEditorOverlay(onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = "Back", 
                        tint = Color.Black,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                    Text("Document Editor", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onDismiss() })
                }
                
                // Tools
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = Color.DarkGray)
                    Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = Color.DarkGray)
                    Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline", tint = Color.DarkGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.FormatAlignLeft, contentDescription = "Left", tint = Color.DarkGray)
                    Icon(Icons.Default.FormatAlignCenter, contentDescription = "Center", tint = Color.DarkGray)
                    Icon(Icons.Default.FormatAlignRight, contentDescription = "Right", tint = Color.DarkGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Image", tint = Color.DarkGray)
                    Icon(Icons.Default.ImageNotSupported, contentDescription = "Remove Image", tint = Color.DarkGray)
                    Icon(Icons.Default.Link, contentDescription = "Add Link", tint = Color.DarkGray)
                }
                HorizontalDivider()
                
                // Content area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF9F9F9))
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            var documentText by androidx.compose.runtime.remember { 
                                androidx.compose.runtime.mutableStateOf("Start editing your document here...\n\nThis is a full rich-text editor workspace where you can format text, insert images, and prepare your files for publication.") 
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = documentText,
                                onValueChange = { documentText = it },
                                modifier = Modifier.fillMaxSize(),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.DarkGray,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditorTool(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { }) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}

fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}

@Composable
fun LightboxAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PdfLightboxReader(file: FileEntity) {
    var currentPage by remember { mutableStateOf(1) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2C2C2C))
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PDF Reader", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (currentPage > 1) currentPage-- }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text("Page $currentPage of 3", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = { if (currentPage < 3) currentPage++ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (currentPage == 1) "EXECUTIVE OVERVIEW" else if (currentPage == 2) "ARCHITECTURE FLOW" else "PERFORMANCE INDEX",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                repeat(4) { idx ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (idx == 3) 0.6f else 1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.LightGray.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (currentPage == 1) {
                    Text("1. Introduction", color = Color(0xFF1E88E5), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        "This manual outlines Kotlin and Compose platform configurations for optimal mobile file sync operations.", 
                        color = Color.DarkGray, 
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (currentPage == 2) {
                    Text("2. Core Architecture State", color = Color(0xFF1E88E5), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE3F2FD))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("[Database Sync Engine] ===> [StateFlow UI]", color = Color(0xFF1E88E5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("3. Signing & Verification", color = Color(0xFF1E88E5), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Approved Digitally", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("System Administrator", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocxLightboxReader(file: FileEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2C2C2C))
                .padding(vertical = 4.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Word Suite", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                    Text("B", color = Color.White, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                    Text("I", color = Color.White, fontWeight = FontWeight.Normal)
                }
                IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                    Text("U", color = Color.White, fontWeight = FontWeight.Normal)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFCFC)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(file.name, color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Last updated: Just now", color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("This is an inline office suite mockup of your workspace. FileFlow loads your documentation directly into localized frames for fast, high-performance viewing.", color = Color.Black, fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("• Fast, lightweight architecture", color = Color.Black, fontSize = 14.sp)
                Text("• Fully interactive client-side document parser", color = Color.Black, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(2.dp, 16.dp).background(MaterialTheme.colorScheme.primary))
                    Text(" Start typing to edit this workspace...", color = Color.Gray, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun XlsxLightboxReader(file: FileEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, bottom = 100.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1B5E20))
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Excel Sheet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Active Budget", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.background(Color(0xFFE8F5E9))) {
                    Box(modifier = Modifier.width(40.dp).padding(6.dp), contentAlignment = Alignment.Center) {
                        Text("#", color = Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    val headers = listOf("Category", "Projection", "Actual", "Variance")
                    headers.forEach { h ->
                        Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.LightGray).padding(6.dp), contentAlignment = Alignment.Center) {
                            Text(h, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                val rows = listOf(
                    listOf("Development", "$12,500.00", "$11,250.00", "-$1,250.00"),
                    listOf("Product Design", "$4,000.00", "$4,100.00", "+$100.00"),
                    listOf("Cloud Hosting", "$3,500.00", "$2,950.00", "-$550.00"),
                    listOf("Office Suites", "$1,200.00", "$1,200.00", "$0.00"),
                    listOf("Marketing", "$8,000.00", "$8,150.00", "+$150.00"),
                    listOf("Consulting", "$5,000.00", "$4,800.00", "-$200.00")
                )
                rows.forEachIndexed { rIdx, row ->
                    Row(modifier = Modifier.border(0.5.dp, Color.LightGray.copy(alpha = 0.5f))) {
                        Box(modifier = Modifier.width(40.dp).background(Color(0xFFF5F5F5)).padding(6.dp), contentAlignment = Alignment.Center) {
                            Text((rIdx + 1).toString(), color = Color.Gray, fontSize = 11.sp)
                        }
                        row.forEachIndexed { cIdx, cell ->
                            val textColor = if (cIdx == 3) {
                                if (cell.startsWith("-")) Color(0xFFC62828) else if (cell.startsWith("+")) Color(0xFF2E7D32) else Color.Black
                            } else Color.Black
                            Box(modifier = Modifier.weight(1f).border(0.2.dp, Color.LightGray).padding(6.dp)) {
                                Text(cell, color = textColor, fontSize = 11.sp)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(modifier = Modifier.background(Color(0xFFE8F5E9)).padding(vertical = 12.dp)) {
                    Box(modifier = Modifier.width(40.dp))
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                        Text("TOTAL COST", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                        Text("$35,700.00", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                        Text("$33,900.00", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                        Text("-$1,800.00", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PptxLightboxReader(file: FileEntity) {
    var activeSlide by remember { mutableStateOf(1) }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, bottom = 100.dp, start = 12.dp, end = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                val slideNum = index + 1
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(6.dp))
                        .border(
                            width = if (activeSlide == slideNum) 2.dp else 1.dp,
                            color = if (activeSlide == slideNum) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                        )
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { activeSlide = slideNum },
                    contentAlignment = Alignment.Center
                ) {
                    Text("$slideNum", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE65100))
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Presentation Workspace", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("$activeSlide / 3", color = Color.White, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (activeSlide) {
                        1 -> {
                            Text("TITLE SLIDE", color = Color(0xFFE65100), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("FileFlow Cloud Storage Suite", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Innovative architecture design specs, high-fidelity responsive performance previews, and instant data security parameters.", color = Color.DarkGray, fontSize = 12.sp)
                        }
                        2 -> {
                            Text("ARCHITECTURE", color = Color(0xFFE65100), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Jetpack Compose Optimization", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Single view, lightweight state flow\n• Instant Room indexing caching\n• Adaptive window layout capabilities", color = Color.DarkGray, fontSize = 12.sp, lineHeight = 20.sp)
                        }
                        3 -> {
                            Text("DEPLOYMENT MATRIX", color = Color(0xFFE65100), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Launch & Testing", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFF3E0)),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFFE65100))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ready to compile applet", color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
