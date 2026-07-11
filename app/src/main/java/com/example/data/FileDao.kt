package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT COUNT(*) FROM files")
    suspend fun countFiles(): Int
    
    @Query("SELECT * FROM files WHERE parentId IS :parentId AND isTrashed = 0 ORDER BY type ASC, name ASC")
    fun getFilesByParent(parentId: Int?): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isFavorite = 1 AND isTrashed = 0 ORDER BY name ASC")
    fun getFavorites(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isTrashed = 1 ORDER BY trashedAt DESC")
    fun getTrashedFiles(): Flow<List<FileEntity>>
    
    @Query("SELECT * FROM files WHERE type = :type AND isTrashed = 0 ORDER BY name ASC")
    fun getFilesByType(type: FileType): Flow<List<FileEntity>>
    
    @Query("SELECT * FROM files WHERE albumId = :albumId AND isTrashed = 0")
    fun getFilesByAlbum(albumId: Int): Flow<List<FileEntity>>
    
    @Query("SELECT * FROM albums ORDER BY isPinned DESC, name ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM files WHERE isTrashed = 0 ORDER BY name ASC")
    fun getAllNonTrashedFiles(): Flow<List<FileEntity>>

    @Query("SELECT name FROM files")
    suspend fun getAllExistingNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)
    
    @Insert
    suspend fun insertFiles(files: List<FileEntity>)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteFileById(id: Int)
    
    @Query("DELETE FROM files WHERE isTrashed = 1 AND trashedAt < :timestamp")
    suspend fun deleteOldTrashedFiles(timestamp: Long)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)
    
    @Update
    suspend fun updateAlbum(album: AlbumEntity)
    
    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun deleteAlbumById(id: Int)

    @Query("UPDATE files SET albumId = NULL WHERE albumId = :albumId")
    suspend fun clearAlbumFiles(albumId: Int)

    @Query("DELETE FROM files")
    suspend fun deleteAllFiles()

    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()
}
