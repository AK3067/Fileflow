package com.example.data

import kotlinx.coroutines.flow.Flow

class FileRepository(private val fileDao: FileDao) {

    fun getFilesByParent(parentId: Int?): Flow<List<FileEntity>> = fileDao.getFilesByParent(parentId)
    fun getFavorites(): Flow<List<FileEntity>> = fileDao.getFavorites()
    fun getTrashedFiles(): Flow<List<FileEntity>> = fileDao.getTrashedFiles()
    fun getFilesByType(type: FileType): Flow<List<FileEntity>> = fileDao.getFilesByType(type)
    fun getFilesByAlbum(albumId: Int): Flow<List<FileEntity>> = fileDao.getFilesByAlbum(albumId)
    fun getAllAlbums(): Flow<List<AlbumEntity>> = fileDao.getAllAlbums()
    fun getAllNonTrashedFiles(): Flow<List<FileEntity>> = fileDao.getAllNonTrashedFiles()

    suspend fun countFiles(): Int = fileDao.countFiles()
    suspend fun getAllExistingNames(): List<String> = fileDao.getAllExistingNames()

    suspend fun insertFile(file: FileEntity) = fileDao.insertFile(file)
    suspend fun insertFiles(files: List<FileEntity>) = fileDao.insertFiles(files)
    suspend fun updateFile(file: FileEntity) = fileDao.updateFile(file)
    suspend fun deleteFileById(id: Int) = fileDao.deleteFileById(id)
    
    suspend fun trashFile(file: FileEntity) {
        val updated = file.copy(isTrashed = true, trashedAt = System.currentTimeMillis())
        fileDao.updateFile(updated)
    }

    suspend fun restoreFile(file: FileEntity) {
        val updated = file.copy(isTrashed = false, trashedAt = null)
        fileDao.updateFile(updated)
    }
    
    suspend fun toggleFavorite(file: FileEntity) {
        val updated = file.copy(isFavorite = !file.isFavorite)
        fileDao.updateFile(updated)
    }

    suspend fun insertAlbum(album: AlbumEntity) = fileDao.insertAlbum(album)
    suspend fun updateAlbum(album: AlbumEntity) = fileDao.updateAlbum(album)
    suspend fun deleteAlbumById(id: Int) = fileDao.deleteAlbumById(id)
    suspend fun clearAlbumFiles(albumId: Int) = fileDao.clearAlbumFiles(albumId)
    
    suspend fun deleteAllFiles() = fileDao.deleteAllFiles()
    suspend fun deleteAllAlbums() = fileDao.deleteAllAlbums()

    suspend fun deleteOldTrashedFiles() {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        fileDao.deleteOldTrashedFiles(thirtyDaysAgo)
    }
}
