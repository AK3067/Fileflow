import { useState, useMemo, useEffect } from "react";
import { AppSidebar } from "@/components/layout/AppSidebar";
import { AppHeader } from "@/components/layout/AppHeader";
import { ViewToggle } from "@/components/gallery/ViewToggle";
import { GalleryView } from "@/components/gallery/GalleryView";
import { FileManagerView } from "@/components/filemanager/FileManagerView";
import { RecycleBinView } from "@/components/recyclebin/RecycleBinView";
import { CreateAlbumDialog } from "@/components/album/CreateAlbumDialog";
import { DeleteAlbumDialog } from "@/components/album/DeleteAlbumDialog";
import { FileViewerModal } from "@/components/file/FileViewerModal";
import { FileDetailsModal } from "@/components/file/FileDetailsModal";
import { FileEditorModal } from "@/components/file/FileEditorModal";
import { RenameFileDialog } from "@/components/file/RenameFileDialog";
import { DeleteFileDialog } from "@/components/file/DeleteFileDialog";
import {
  mockAlbums as initialAlbums,
  mockQuickAccess as initialQuickAccess,
  mockFiles as initialFiles,
  mockFolders,
  mockBreakdown,
} from "@/data/mockData";
import { toast } from "@/hooks/use-toast";
import { Album, FileItem, QuickAccessItem } from "@/types/file";

type ViewMode = "gallery" | "fileManager" | "recycleBin";

const Index = () => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [currentView, setCurrentView] = useState<ViewMode>("gallery");
  const [selectedAlbumId, setSelectedAlbumId] = useState<string | undefined>();
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPath, setCurrentPath] = useState("Device Storage");

  // Data state
  const [files, setFiles] = useState<FileItem[]>(initialFiles);
  const [albums, setAlbums] = useState<Album[]>(initialAlbums);
  const [quickAccess, setQuickAccess] = useState<QuickAccessItem[]>(initialQuickAccess);

  // Album dialogs
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [renameDialogOpen, setRenameDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [editingAlbumId, setEditingAlbumId] = useState<string | null>(null);

  // File modals
  const [viewerOpen, setViewerOpen] = useState(false);
  const [viewingFile, setViewingFile] = useState<FileItem | null>(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [detailsFile, setDetailsFile] = useState<FileItem | null>(null);
  const [renameFileOpen, setRenameFileOpen] = useState(false);
  const [renamingFile, setRenamingFile] = useState<FileItem | null>(null);
  const [deleteFileOpen, setDeleteFileOpen] = useState(false);
  const [deletingFile, setDeletingFile] = useState<FileItem | null>(null);
  const [editorOpen, setEditorOpen] = useState(false);
  const [editingFile, setEditingFile] = useState<FileItem | null>(null);

  const editingAlbum = albums.find((a) => a.id === editingAlbumId);

  // Filter out deleted files for normal view
  const activeFiles = useMemo(() => {
    return files.filter((file) => !file.deletedAt);
  }, [files]);

  // Get deleted files for recycle bin
  const deletedFiles = useMemo(() => {
    return files.filter((file) => file.deletedAt);
  }, [files]);

  // Auto-delete files after 30 days
  useEffect(() => {
    const now = new Date();
    const filesToPermanentlyDelete = files.filter((file) => {
      if (!file.deletedAt) return false;
      const deleted = new Date(file.deletedAt);
      const daysPassed = Math.floor(
        (now.getTime() - deleted.getTime()) / (1000 * 60 * 60 * 24)
      );
      return daysPassed >= 30;
    });

    if (filesToPermanentlyDelete.length > 0) {
      setFiles((prev) =>
        prev.filter(
          (f) => !filesToPermanentlyDelete.some((df) => df.id === f.id)
        )
      );
    }
  }, [files]);

  const filteredFiles = useMemo(() => {
    let result = activeFiles;
    
    // Filter by selected album
    if (selectedAlbumId) {
      result = result.filter((file) => file.albumId === selectedAlbumId);
    }
    
    // Filter by search query
    if (searchQuery) {
      result = result.filter((file) =>
        file.name.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }
    
    return result;
  }, [searchQuery, activeFiles, selectedAlbumId]);

  // Album handlers
  const handleAddAlbum = () => {
    setCreateDialogOpen(true);
  };

  const handleCreateAlbum = (name: string) => {
    const newAlbum: Album = {
      id: `album-${Date.now()}`,
      name,
      fileCount: 0,
    };
    setAlbums((prev) => [...prev, newAlbum]);
    toast({
      title: "Album Created",
      description: `"${name}" has been created successfully.`,
    });
  };

  const handleRenameAlbum = (albumId: string) => {
    setEditingAlbumId(albumId);
    setRenameDialogOpen(true);
  };

  const handleRenameSubmit = (newName: string) => {
    setAlbums((prev) =>
      prev.map((album) =>
        album.id === editingAlbumId ? { ...album, name: newName } : album
      )
    );
    toast({
      title: "Album Renamed",
      description: `Album renamed to "${newName}".`,
    });
    setEditingAlbumId(null);
  };

  const handleDeleteAlbum = (albumId: string) => {
    setEditingAlbumId(albumId);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = () => {
    const albumName = editingAlbum?.name;
    setAlbums((prev) => prev.filter((album) => album.id !== editingAlbumId));
    if (selectedAlbumId === editingAlbumId) {
      setSelectedAlbumId(undefined);
    }
    toast({
      title: "Album Deleted",
      description: `"${albumName}" has been deleted.`,
    });
    setEditingAlbumId(null);
    setDeleteDialogOpen(false);
  };

  const handleAddFilesToAlbum = (albumId: string) => {
    const album = albums.find((a) => a.id === albumId);
    toast({
      title: "Add Files",
      description: `Select files to add to "${album?.name}".`,
    });
  };

  // File handlers
  const handleOpenFile = (file: FileItem) => {
    setViewingFile(file);
    setViewerOpen(true);
  };

  const handleDownloadFile = (file: FileItem) => {
    toast({
      title: "Downloading",
      description: `"${file.name}" is being downloaded.`,
    });
  };

  const handleEditFile = (file: FileItem) => {
    setEditingFile(file);
    setEditorOpen(true);
  };

  const handleSaveEditedFile = (file: FileItem, editedData: string) => {
    // Update file with edited content
    setFiles((prev) =>
      prev.map((f) =>
        f.id === file.id ? { ...f, editedContent: editedData, modifiedAt: new Date() } : f
      )
    );
    toast({
      title: "File Saved",
      description: `"${file.name}" has been updated.`,
    });
  };

  const handleAddFileToAlbum = (file: FileItem, albumId: string) => {
    const album = albums.find((a) => a.id === albumId);
    setFiles((prev) =>
      prev.map((f) =>
        f.id === file.id ? { ...f, albumId } : f
      )
    );
    setAlbums((prev) =>
      prev.map((a) =>
        a.id === albumId ? { ...a, fileCount: a.fileCount + 1 } : a
      )
    );
    toast({
      title: "Added to Album",
      description: `"${file.name}" added to "${album?.name}".`,
    });
  };

  const handleToggleQuickAccess = (file: FileItem) => {
    const exists = quickAccess.some((q) => q.id === file.id);
    if (exists) {
      setQuickAccess((prev) => prev.filter((q) => q.id !== file.id));
      toast({
        title: "Removed from Quick Access",
        description: `"${file.name}" removed from Quick Access.`,
      });
    } else {
      setQuickAccess((prev) => [
        ...prev,
        { id: file.id, name: file.name, type: file.type },
      ]);
      toast({
        title: "Added to Quick Access",
        description: `"${file.name}" added to Quick Access.`,
      });
    }
  };

  const handleRemoveFromQuickAccess = (itemId: string) => {
    const item = quickAccess.find((q) => q.id === itemId);
    setQuickAccess((prev) => prev.filter((q) => q.id !== itemId));
    toast({
      title: "Removed from Quick Access",
      description: `"${item?.name}" removed from Quick Access.`,
    });
  };

  const handleOpenQuickAccessFile = (itemId: string) => {
    const file = files.find((f) => f.id === itemId);
    if (file) {
      handleOpenFile(file);
    }
  };

  const handleDeleteQuickAccessFile = (itemId: string) => {
    const file = files.find((f) => f.id === itemId);
    if (file) {
      handleDeleteFile(file);
    }
  };

  const handleShareFile = (file: FileItem) => {
    toast({
      title: "Share",
      description: `Sharing "${file.name}"...`,
    });
  };

  const handleRenameFile = (file: FileItem) => {
    setRenamingFile(file);
    setRenameFileOpen(true);
  };

  const handleRenameFileSubmit = (file: FileItem, newName: string) => {
    setFiles((prev) =>
      prev.map((f) =>
        f.id === file.id ? { ...f, name: newName } : f
      )
    );
    toast({
      title: "File Renamed",
      description: `File renamed to "${newName}".`,
    });
  };

  const handleFileDetails = (file: FileItem) => {
    setDetailsFile(file);
    setDetailsOpen(true);
  };

  const handleDeleteFile = (file: FileItem) => {
    setDeletingFile(file);
    setDeleteFileOpen(true);
  };

  const handleDeleteFileConfirm = (file: FileItem) => {
    // Move to recycle bin instead of permanent delete
    setFiles((prev) =>
      prev.map((f) =>
        f.id === file.id ? { ...f, deletedAt: new Date() } : f
      )
    );
    setQuickAccess((prev) => prev.filter((q) => q.id !== file.id));
    if (viewingFile?.id === file.id) {
      setViewerOpen(false);
      setViewingFile(null);
    }
    toast({
      title: "Moved to Recycle Bin",
      description: `"${file.name}" will be permanently deleted in 30 days.`,
    });
    setDeleteFileOpen(false);
    setDeletingFile(null);
  };

  // Recycle bin handlers
  const handleOpenRecycleBin = () => {
    setSelectedAlbumId(undefined);
    setCurrentView("recycleBin");
  };

  const handleRecoverFiles = (fileIds: string[]) => {
    setFiles((prev) =>
      prev.map((f) =>
        fileIds.includes(f.id) ? { ...f, deletedAt: undefined } : f
      )
    );
    toast({
      title: "Files Recovered",
      description: `${fileIds.length} file(s) have been restored.`,
    });
  };

  const handlePermanentDelete = (fileIds: string[]) => {
    setFiles((prev) => prev.filter((f) => !fileIds.includes(f.id)));
    toast({
      title: "Permanently Deleted",
      description: `${fileIds.length} file(s) have been permanently deleted.`,
    });
  };

  const handleRecoverAll = () => {
    const count = deletedFiles.length;
    setFiles((prev) =>
      prev.map((f) => (f.deletedAt ? { ...f, deletedAt: undefined } : f))
    );
    toast({
      title: "All Files Recovered",
      description: `${count} file(s) have been restored.`,
    });
  };

  const handleDeleteAll = () => {
    const count = deletedFiles.length;
    setFiles((prev) => prev.filter((f) => !f.deletedAt));
    toast({
      title: "Recycle Bin Emptied",
      description: `${count} file(s) have been permanently deleted.`,
    });
  };

  const handleFolderNavigate = (folderId: string) => {
    const folder = mockFolders.find((f) => f.id === folderId);
    if (folder) {
      setCurrentPath(`Device Storage / ${folder.name}`);
      toast({
        title: `Opened ${folder.name}`,
        description: "Folder navigation is a demo feature.",
      });
    }
  };

  return (
    <div className="min-h-screen bg-background flex">
      {/* Sidebar */}
      <AppSidebar
        albums={albums}
        quickAccessItems={quickAccess}
        selectedAlbumId={selectedAlbumId}
        onSelectAlbum={(albumId) => {
          setSelectedAlbumId(albumId);
          if (currentView === "recycleBin") {
            setCurrentView("gallery");
          }
        }}
        onAddAlbum={handleAddAlbum}
        onRenameAlbum={handleRenameAlbum}
        onDeleteAlbum={handleDeleteAlbum}
        onAddFilesToAlbum={handleAddFilesToAlbum}
        isCollapsed={!sidebarOpen}
        recycleBinCount={deletedFiles.length}
        isRecycleBinActive={currentView === "recycleBin"}
        onOpenRecycleBin={handleOpenRecycleBin}
        onOpenQuickAccessFile={handleOpenQuickAccessFile}
        onRemoveFromQuickAccess={handleRemoveFromQuickAccess}
        onDeleteQuickAccessFile={handleDeleteQuickAccessFile}
      />

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Header */}
        <AppHeader
          onSearch={setSearchQuery}
          onToggleSidebar={() => setSidebarOpen(!sidebarOpen)}
          isSidebarOpen={sidebarOpen}
        />

        {/* View Toggle - only show for gallery/fileManager */}
        {currentView !== "recycleBin" && (
          <div className="flex justify-center py-4 border-b border-border bg-card">
            <ViewToggle
              currentView={currentView === "gallery" ? "gallery" : "fileManager"}
              onViewChange={(view) => setCurrentView(view as ViewMode)}
            />
          </div>
        )}

        {/* Content Area */}
        <main className="flex-1 overflow-auto">
          {currentView === "recycleBin" ? (
            <RecycleBinView
              deletedFiles={deletedFiles}
              onBack={() => setCurrentView("gallery")}
              onRecover={handleRecoverFiles}
              onPermanentDelete={handlePermanentDelete}
              onRecoverAll={handleRecoverAll}
              onDeleteAll={handleDeleteAll}
            />
          ) : currentView === "gallery" ? (
            <GalleryView
              files={filteredFiles}
              breakdown={mockBreakdown}
              albums={albums}
              quickAccessIds={quickAccess.map((q) => q.id)}
              selectedAlbum={albums.find(a => a.id === selectedAlbumId)}
              onClearAlbumFilter={() => setSelectedAlbumId(undefined)}
              onOpenFile={handleOpenFile}
              onDownloadFile={handleDownloadFile}
              onEditFile={handleEditFile}
              onAddFileToAlbum={handleAddFileToAlbum}
              onToggleQuickAccess={handleToggleQuickAccess}
              onShareFile={handleShareFile}
              onRenameFile={handleRenameFile}
              onFileDetails={handleFileDetails}
              onDeleteFile={handleDeleteFile}
            />
          ) : (
            <FileManagerView
              folders={mockFolders}
              currentPath={currentPath}
              onNavigate={handleFolderNavigate}
            />
          )}
        </main>
      </div>

      {/* Album Dialogs */}
      <CreateAlbumDialog
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
        onCreateAlbum={handleCreateAlbum}
        mode="create"
      />

      <CreateAlbumDialog
        open={renameDialogOpen}
        onOpenChange={setRenameDialogOpen}
        onCreateAlbum={handleRenameSubmit}
        mode="rename"
        initialName={editingAlbum?.name || ""}
      />

      <DeleteAlbumDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        albumName={editingAlbum?.name || ""}
        onConfirm={handleDeleteConfirm}
      />

      {/* File Modals */}
      <FileViewerModal
        file={viewingFile}
        files={filteredFiles}
        open={viewerOpen}
        onOpenChange={setViewerOpen}
        onNavigate={setViewingFile}
        onDownload={handleDownloadFile}
        onShare={handleShareFile}
        onEdit={handleEditFile}
        onDelete={handleDeleteFile}
        onDetails={handleFileDetails}
      />

      <FileDetailsModal
        file={detailsFile}
        open={detailsOpen}
        onOpenChange={setDetailsOpen}
      />

      <RenameFileDialog
        file={renamingFile}
        open={renameFileOpen}
        onOpenChange={setRenameFileOpen}
        onRename={handleRenameFileSubmit}
      />

      <DeleteFileDialog
        file={deletingFile}
        open={deleteFileOpen}
        onOpenChange={setDeleteFileOpen}
        onConfirm={handleDeleteFileConfirm}
      />

      <FileEditorModal
        file={editingFile}
        open={editorOpen}
        onOpenChange={setEditorOpen}
        onSave={handleSaveEditedFile}
      />
    </div>
  );
};

export default Index;
