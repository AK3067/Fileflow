package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: FileType,
    val size: Long = 0L,
    val parentId: Int? = null,
    val isTrashed: Boolean = false,
    val trashedAt: Long? = null,
    val isFavorite: Boolean = false,
    val albumId: Int? = null, // for albums feature
    val location: String? = null, // for search by location
    val metadata: String? = null, // for search by metadata/tags
    val updatedAt: Long = System.currentTimeMillis()
)

enum class FileType {
    FOLDER, IMAGE, AUDIO, VIDEO, DOCX, XLSX, PPTX, APK, TEXT, PDF
}
