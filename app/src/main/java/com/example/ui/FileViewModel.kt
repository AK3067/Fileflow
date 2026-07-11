package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlbumEntity
import com.example.data.FileEntity
import com.example.data.FileRepository
import com.example.data.FileType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FileViewModel(private val repository: FileRepository) : ViewModel() {

    private val _currentParentId = MutableStateFlow<Int?>(null)
    val currentParentId: StateFlow<Int?> = _currentParentId.asStateFlow()
    
    // Automatically observe files based on current parent ID
    private val _currentFiles = MutableStateFlow<List<FileEntity>>(emptyList())
    val currentFiles: StateFlow<List<FileEntity>> = _currentFiles.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allNonTrashedFiles: StateFlow<List<FileEntity>> = repository.getAllNonTrashedFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    init {
        viewModelScope.launch {
            _currentParentId.collect { parentId ->
                repository.getFilesByParent(parentId).collect { files ->
                    _currentFiles.value = files
                }
            }
        }
        
        viewModelScope.launch {
            repository.deleteOldTrashedFiles()
        }
        
        seedInitialData()
    }
    
    val allAlbums: StateFlow<List<AlbumEntity>> = repository.getAllAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteFiles: StateFlow<List<FileEntity>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashedFiles: StateFlow<List<FileEntity>> = repository.getTrashedFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateToFolder(folderId: Int?) {
        _currentParentId.value = folderId
    }
    
    fun getFilesByType(type: FileType): StateFlow<List<FileEntity>> {
        return repository.getFilesByType(type)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    fun getFilesByAlbum(albumId: Int): StateFlow<List<FileEntity>> {
         return repository.getFilesByAlbum(albumId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun createFile(name: String, type: FileType, size: Long = 0, albumId: Int? = null) {
        viewModelScope.launch {
            repository.insertFile(
                FileEntity(
                    name = name,
                    type = type,
                    size = size,
                    parentId = _currentParentId.value,
                    albumId = albumId
                )
            )
        }
    }

    fun renameFile(file: FileEntity, newName: String) {
        viewModelScope.launch {
            repository.updateFile(file.copy(name = newName, updatedAt = System.currentTimeMillis()))
        }
    }

    fun trashFile(file: FileEntity) {
        viewModelScope.launch {
            repository.trashFile(file)
        }
    }

    fun restoreFile(file: FileEntity) {
        viewModelScope.launch {
            repository.restoreFile(file)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            trashedFiles.value.forEach {
                repository.deleteFileById(it.id)
            }
        }
    }

    fun deleteFilePermanently(file: FileEntity) {
        viewModelScope.launch {
            repository.deleteFileById(file.id)
        }
    }

    fun toggleFavorite(file: FileEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(file)
        }
    }
    
    fun createAlbum(name: String, coverIcon: String = "folder") {
        viewModelScope.launch {
            repository.insertAlbum(AlbumEntity(name = name, coverIcon = coverIcon))
        }
    }
    
    fun renameAlbum(album: AlbumEntity, newName: String) {
        viewModelScope.launch {
            repository.updateAlbum(album.copy(name = newName))
        }
    }

    fun deleteAlbum(albumId: Int) {
        viewModelScope.launch {
            repository.clearAlbumFiles(albumId)
            repository.deleteAlbumById(albumId)
        }
    }

    fun togglePinAlbum(album: AlbumEntity) {
        viewModelScope.launch {
            repository.updateAlbum(album.copy(isPinned = !album.isPinned))
        }
    }
    
    private fun seedInitialData() {
        viewModelScope.launch {
            try {
                // Clear any existing database entries to satisfy the removal of unwanted data
                repository.deleteAllFiles()
                repository.deleteAllAlbums()

                // 1. Insert real, workable shelf albums in database
                val cameraAlbum = AlbumEntity(id = 1, name = "Camera", coverIcon = "camera")
                val docsAlbum = AlbumEntity(id = 2, name = "Documents", coverIcon = "description")
                val sheetsAlbum = AlbumEntity(id = 3, name = "Spreadsheets", coverIcon = "table_chart")
                val slidesAlbum = AlbumEntity(id = 4, name = "Presentations", coverIcon = "slideshow")

                repository.insertAlbum(cameraAlbum)
                repository.insertAlbum(docsAlbum)
                repository.insertAlbum(sheetsAlbum)
                repository.insertAlbum(slidesAlbum)

                // 2. Insert realistic device files (media + office suites)
                val initialFiles = listOf(
                    // Camera Album files
                    FileEntity(
                        name = "IMG_2026_Yosemite.jpg",
                        type = FileType.IMAGE,
                        size = 1048576L * 3 + 12000L,
                        albumId = 1,
                        location = "Yosemite National Park",
                        metadata = "National park, hiking, waterfall, scenic mountains"
                    ),
                    FileEntity(
                        name = "IMG_2026_FamilyDinner.png",
                        type = FileType.IMAGE,
                        size = 1048576L * 4 + 450000L,
                        albumId = 1,
                        location = "San Francisco, CA",
                        metadata = "Dinner, celebration, family, smiles, birthday",
                        isFavorite = true
                    ),
                    FileEntity(
                        name = "IMG_2026_SunsetMalibu.jpg",
                        type = FileType.IMAGE,
                        size = 1048576L * 2 + 880000L,
                        albumId = 1,
                        location = "Malibu, California",
                        metadata = "Sunset beach ocean shore coastal scenery beautiful",
                        isFavorite = true
                    ),
                    FileEntity(
                        name = "VID_2026_SurfingVlog.mp4",
                        type = FileType.VIDEO,
                        size = 1048576L * 85,
                        albumId = 1,
                        location = "Santa Cruz",
                        metadata = "Surf travel adventure sport ocean water waves"
                    ),
                    FileEntity(
                        name = "VID_2026_PuppyRun.mp4",
                        type = FileType.VIDEO,
                        size = 1048576L * 24,
                        albumId = 1,
                        location = "Dog Park, CA",
                        metadata = "Puppy dog animals playing fetch recreation"
                    ),

                    // Documents Album files (DOCX, PDF, TEXT)
                    FileEntity(
                        name = "Technical_Project_Proposal.docx",
                        type = FileType.DOCX,
                        size = 1024L * 180,
                        albumId = 2,
                        location = "Office HQ",
                        metadata = "Tech architecture project plan specs critical"
                    ),
                    FileEntity(
                        name = "Android_Kotlin_Handbook.pdf",
                        type = FileType.PDF,
                        size = 1048576L * 6 + 180000L,
                        albumId = 2,
                        location = "Developer Portal",
                        metadata = "Android SDK Jetpack Compose architecture handbook programming",
                        isFavorite = true
                    ),
                    FileEntity(
                        name = "Standard_Office_Lease.pdf",
                        type = FileType.PDF,
                        size = 1048576L * 1 + 500000L,
                        albumId = 2,
                        location = "Legal Desk",
                        metadata = "Contract signing agreement lease documentation office suite"
                    ),
                    FileEntity(
                        name = "Scratchpad_Notes.txt",
                        type = FileType.TEXT,
                        size = 1024L * 3,
                        albumId = 2,
                        location = "Desk workspace",
                        metadata = "Quick reminder list items ideas workspace",
                        isFavorite = false
                    ),

                    // Spreadsheets Album files (XLSX)
                    FileEntity(
                        name = "Monthly_Company_Budget.xlsx",
                        type = FileType.XLSX,
                        size = 1024L * 256,
                        albumId = 3,
                        location = "Finance Office",
                        metadata = "Cost expenses revenues balance sheet budget spreadsheet",
                        isFavorite = true
                    ),
                    FileEntity(
                        name = "Sales_Forecast_2026.xlsx",
                        type = FileType.XLSX,
                        size = 1024L * 312,
                        albumId = 3,
                        location = "Sales Dept Room 1",
                        metadata = "Forecasting model projections target reports quarterly spreadsheet"
                    ),

                    // Presentations Album files (PPTX)
                    FileEntity(
                        name = "Investor_Pitch_Deck.pptx",
                        type = FileType.PPTX,
                        size = 1048576L * 12 + 100000L,
                        albumId = 4,
                        location = "Main Boardroom",
                        metadata = "Slideshow pitch deck startup business funding presentation",
                        isFavorite = true
                    ),
                    FileEntity(
                        name = "System_Architecture_Slides.pptx",
                        type = FileType.PPTX,
                        size = 1048576L * 18 + 450000L,
                        albumId = 4,
                        location = "Engineering Bay",
                        metadata = "System design diagrams slides diagrams presentation flow"
                    ),

                    // Other Audio/System files
                    FileEntity(
                        name = "Podcast_Episode_34.mp3",
                        type = FileType.AUDIO,
                        size = 1048576L * 18,
                        location = "Audio Player",
                        metadata = "Podcasts recordings interview talk format speech"
                    ),
                    FileEntity(
                        name = "VoiceMemo_MeetingSync.m4a",
                        type = FileType.AUDIO,
                        size = 1024L * 750,
                        location = "Conference Room B",
                        metadata = "Voice note meeting recording audio memos"
                    ),
                    FileEntity(
                        name = "system_beta_rebuilt.apk",
                        type = FileType.APK,
                        size = 1048576L * 42,
                        location = "Development downloads",
                        metadata = "Android application binary package installer package"
                    )
                )

                repository.insertFiles(initialFiles)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun scanDeviceFiles(context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val existingNames = repository.getAllExistingNames().toSet()
                val scannedFiles = mutableListOf<FileEntity>()
                
                // Query MediaStore for Images
                val imageUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val mediaProjection = arrayOf(
                    android.provider.MediaStore.Images.Media._ID,
                    android.provider.MediaStore.Images.Media.DISPLAY_NAME,
                    android.provider.MediaStore.Images.Media.SIZE,
                    android.provider.MediaStore.Images.Media.DATE_MODIFIED
                )
                
                context.contentResolver.query(imageUri, mediaProjection, null, null, null)?.use { cursor ->
                    val nameCol = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.DISPLAY_NAME)
                    val sizeCol = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.SIZE)
                    val dateCol = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.DATE_MODIFIED)
                    
                    while (cursor.moveToNext() && scannedFiles.size < 60) {
                        val name = if (nameCol >= 0) cursor.getString(nameCol) ?: "Unknown Image" else "Unknown Image"
                        if (existingNames.contains(name)) continue
                        val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                        val date = if (dateCol >= 0) cursor.getLong(dateCol) * 1000 else System.currentTimeMillis()
                        scannedFiles.add(
                            FileEntity(name = name, type = FileType.IMAGE, size = size, updatedAt = date, albumId = 1)
                        )
                    }
                }
                
                // Query MediaStore for Video
                val videoUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                context.contentResolver.query(videoUri, mediaProjection, null, null, null)?.use { cursor ->
                    val nameCol = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.DISPLAY_NAME)
                    val sizeCol = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.SIZE)
                    val dateCol = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.DATE_MODIFIED)
                    while (cursor.moveToNext() && scannedFiles.size < 120) {
                        val name = if (nameCol >= 0) cursor.getString(nameCol) ?: "Unknown Video" else "Unknown Video"
                        if (existingNames.contains(name)) continue
                        val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                        val date = if (dateCol >= 0) cursor.getLong(dateCol) * 1000 else System.currentTimeMillis()
                        scannedFiles.add(
                            FileEntity(name = name, type = FileType.VIDEO, size = size, updatedAt = date, albumId = 1)
                        )
                    }
                }

                // Query MediaStore for Audio
                val audioUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                context.contentResolver.query(audioUri, mediaProjection, null, null, null)?.use { cursor ->
                    val nameCol = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DISPLAY_NAME)
                    val sizeCol = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.SIZE)
                    val dateCol = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATE_MODIFIED)
                    while (cursor.moveToNext() && scannedFiles.size < 180) {
                        val name = if (nameCol >= 0) cursor.getString(nameCol) ?: "Unknown Audio" else "Unknown Audio"
                        if (existingNames.contains(name)) continue
                        val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                        val date = if (dateCol >= 0) cursor.getLong(dateCol) * 1000 else System.currentTimeMillis()
                        scannedFiles.add(
                            FileEntity(name = name, type = FileType.AUDIO, size = size, updatedAt = date)
                        )
                    }
                }

                // Query MediaStore.Files for Office Suite Files
                val filesUri = android.provider.MediaStore.Files.getContentUri("external")
                val filesProjection = arrayOf(
                    android.provider.MediaStore.Files.FileColumns._ID,
                    android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME,
                    android.provider.MediaStore.Files.FileColumns.SIZE,
                    android.provider.MediaStore.Files.FileColumns.DATE_MODIFIED
                )
                context.contentResolver.query(filesUri, filesProjection, null, null, null)?.use { cursor ->
                    val nameCol = cursor.getColumnIndex(android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val sizeCol = cursor.getColumnIndex(android.provider.MediaStore.Files.FileColumns.SIZE)
                    val dateCol = cursor.getColumnIndex(android.provider.MediaStore.Files.FileColumns.DATE_MODIFIED)
                    
                    while (cursor.moveToNext() && scannedFiles.size < 250) {
                        val name = if (nameCol >= 0) cursor.getString(nameCol) ?: continue else continue
                        if (name.isBlank() || existingNames.contains(name)) continue
                        
                        val lowerName = name.lowercase()
                        val type: FileType = when {
                            lowerName.endsWith(".pdf") -> FileType.PDF
                            lowerName.endsWith(".docx") || lowerName.endsWith(".doc") -> FileType.DOCX
                            lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls") -> FileType.XLSX
                            lowerName.endsWith(".pptx") || lowerName.endsWith(".ppt") -> FileType.PPTX
                            lowerName.endsWith(".txt") -> FileType.TEXT
                            lowerName.endsWith(".apk") -> FileType.APK
                            else -> continue
                        }

                        val albumId = when (type) {
                            FileType.PDF, FileType.DOCX, FileType.TEXT -> 2
                            FileType.XLSX -> 3
                            FileType.PPTX -> 4
                            else -> null
                        }

                        val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                        val date = if (dateCol >= 0) cursor.getLong(dateCol) * 1000 else System.currentTimeMillis()

                        scannedFiles.add(
                            FileEntity(
                                name = name,
                                type = type,
                                size = size,
                                updatedAt = date,
                                albumId = albumId
                            )
                        )
                    }
                }

                if (scannedFiles.isNotEmpty()) {
                    repository.insertFiles(scannedFiles)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
