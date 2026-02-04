import { FileItem, FileBreakdown, Album } from "@/types/file";
import { FileCard } from "./FileCard";
import { FileBreakdownPopover } from "./FileBreakdownPopover";
import { Button } from "@/components/ui/button";
import { ArrowLeft, Folder } from "lucide-react";

interface GalleryViewProps {
  files: FileItem[];
  breakdown: FileBreakdown[];
  albums: Album[];
  quickAccessIds: string[];
  selectedAlbum?: Album;
  onClearAlbumFilter: () => void;
  onOpenFile: (file: FileItem) => void;
  onDownloadFile: (file: FileItem) => void;
  onEditFile: (file: FileItem) => void;
  onAddFileToAlbum: (file: FileItem, albumId: string) => void;
  onToggleQuickAccess: (file: FileItem) => void;
  onShareFile: (file: FileItem) => void;
  onRenameFile: (file: FileItem) => void;
  onFileDetails: (file: FileItem) => void;
  onDeleteFile: (file: FileItem) => void;
}

export const GalleryView = ({
  files,
  breakdown,
  albums,
  quickAccessIds,
  selectedAlbum,
  onClearAlbumFilter,
  onOpenFile,
  onDownloadFile,
  onEditFile,
  onAddFileToAlbum,
  onToggleQuickAccess,
  onShareFile,
  onRenameFile,
  onFileDetails,
  onDeleteFile,
}: GalleryViewProps) => {
  return (
    <div className="p-6 animate-fade-in">
      {/* Album header or file count */}
      <div className="mb-6">
        {selectedAlbum ? (
          <div className="flex items-center gap-4">
            <Button 
              variant="ghost" 
              size="icon"
              onClick={onClearAlbumFilter}
              className="shrink-0"
            >
              <ArrowLeft size={20} />
            </Button>
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <Folder size={20} className="text-primary" />
              </div>
              <div>
                <h2 className="text-lg font-semibold">{selectedAlbum.name}</h2>
                <p className="text-sm text-muted-foreground">
                  {files.length} {files.length === 1 ? "file" : "files"}
                </p>
              </div>
            </div>
          </div>
        ) : (
          <FileBreakdownPopover breakdown={breakdown} totalFiles={files.length} />
        )}
      </div>

      {/* File grid */}
      {files.length > 0 ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
          {files.map((file, index) => (
            <FileCard
              key={file.id}
              file={file}
              index={index}
              albums={albums}
              isInQuickAccess={quickAccessIds.includes(file.id)}
              onOpen={onOpenFile}
              onDownload={onDownloadFile}
              onEdit={onEditFile}
              onAddToAlbum={onAddFileToAlbum}
              onToggleQuickAccess={onToggleQuickAccess}
              onShare={onShareFile}
              onRename={onRenameFile}
              onDetails={onFileDetails}
              onDelete={onDeleteFile}
            />
          ))}
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-20 text-center">
          <div className="w-16 h-16 rounded-full bg-muted flex items-center justify-center mb-4">
            <Folder size={32} className="text-muted-foreground" />
          </div>
          <h3 className="text-lg font-medium mb-2">No files in this album</h3>
          <p className="text-sm text-muted-foreground max-w-sm">
            Right-click on any file and select "Add to Album" to add files here.
          </p>
        </div>
      )}
    </div>
  );
};
