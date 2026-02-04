import { Pencil, MoreVertical } from "lucide-react";
import { FileItem, Album } from "@/types/file";
import { FileTypeIcon } from "@/components/icons/FileTypeIcon";
import { FileContextMenu } from "@/components/file/FileContextMenu";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Zap, ZapOff, FolderPlus, Share2, Info, Trash2, Download } from "lucide-react";

interface FileCardProps {
  file: FileItem;
  index: number;
  albums: Album[];
  isInQuickAccess?: boolean;
  onOpen: (file: FileItem) => void;
  onDownload: (file: FileItem) => void;
  onEdit: (file: FileItem) => void;
  onAddToAlbum: (file: FileItem, albumId: string) => void;
  onToggleQuickAccess: (file: FileItem) => void;
  onShare: (file: FileItem) => void;
  onRename: (file: FileItem) => void;
  onDetails: (file: FileItem) => void;
  onDelete: (file: FileItem) => void;
}

export const FileCard = ({
  file,
  index,
  albums,
  isInQuickAccess = false,
  onOpen,
  onDownload,
  onEdit,
  onAddToAlbum,
  onToggleQuickAccess,
  onShare,
  onRename,
  onDetails,
  onDelete,
}: FileCardProps) => {
  const hasImage = file.type === 'image' && file.thumbnail;

  return (
    <FileContextMenu
      file={file}
      albums={albums}
      isInQuickAccess={isInQuickAccess}
      onOpen={onOpen}
      onDownload={onDownload}
      onEdit={onEdit}
      onAddToAlbum={onAddToAlbum}
      onToggleQuickAccess={onToggleQuickAccess}
      onShare={onShare}
      onRename={onRename}
      onDetails={onDetails}
      onDelete={onDelete}
    >
      <div
        className="group relative bg-secondary rounded-xl overflow-hidden cursor-pointer hover-lift animate-fade-in"
        style={{ animationDelay: `${index * 0.05}s` }}
        onClick={() => onOpen(file)}
      >
        <div className="aspect-[4/3] relative flex items-center justify-center">
          {hasImage ? (
            <img
              src={file.thumbnail}
              alt={file.name}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center bg-secondary">
              <FileTypeIcon type={file.type} size={40} />
            </div>
          )}

          {/* Edit button */}
          <button
            className="absolute top-2 left-2 p-1.5 rounded-full bg-background/80 opacity-0 group-hover:opacity-100 transition-opacity duration-200 hover:bg-background"
            onClick={(e) => {
              e.stopPropagation();
              onEdit(file);
            }}
          >
            <Pencil size={14} className="text-foreground" />
          </button>

          {/* Three-dot menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                className="absolute top-2 right-2 p-1.5 rounded-full bg-background/80 opacity-0 group-hover:opacity-100 transition-opacity duration-200 hover:bg-background"
                onClick={(e) => e.stopPropagation()}
              >
                <MoreVertical size={14} className="text-foreground" />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-48" onClick={(e) => e.stopPropagation()}>
              <DropdownMenuItem onClick={(e) => { e.stopPropagation(); onToggleQuickAccess(file); }}>
                {isInQuickAccess ? (
                  <>
                    <ZapOff className="mr-2 h-4 w-4" />
                    Remove from Quick Access
                  </>
                ) : (
                  <>
                    <Zap className="mr-2 h-4 w-4" />
                    Add to Quick Access
                  </>
                )}
              </DropdownMenuItem>
              <DropdownMenuSub>
                <DropdownMenuSubTrigger onClick={(e) => e.stopPropagation()}>
                  <FolderPlus className="mr-2 h-4 w-4" />
                  Add to Album
                </DropdownMenuSubTrigger>
                <DropdownMenuSubContent className="w-48" onClick={(e) => e.stopPropagation()}>
                  {albums.length > 0 ? (
                    albums.map((album) => (
                      <DropdownMenuItem
                        key={album.id}
                        onClick={(e) => { e.stopPropagation(); onAddToAlbum(file, album.id); }}
                      >
                        {album.name}
                      </DropdownMenuItem>
                    ))
                  ) : (
                    <DropdownMenuItem disabled>No albums available</DropdownMenuItem>
                  )}
                </DropdownMenuSubContent>
              </DropdownMenuSub>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={(e) => { e.stopPropagation(); onDownload(file); }}>
                <Download className="mr-2 h-4 w-4" />
                Download
              </DropdownMenuItem>
              <DropdownMenuItem onClick={(e) => { e.stopPropagation(); onShare(file); }}>
                <Share2 className="mr-2 h-4 w-4" />
                Share
              </DropdownMenuItem>
              <DropdownMenuItem onClick={(e) => { e.stopPropagation(); onDetails(file); }}>
                <Info className="mr-2 h-4 w-4" />
                Details
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={(e) => { e.stopPropagation(); onDelete(file); }}
                className="text-destructive focus:text-destructive"
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>

          {/* Hover overlay */}
          <div className="absolute inset-0 bg-foreground/0 group-hover:bg-foreground/5 transition-colors duration-200 pointer-events-none" />
        </div>

        {/* File name tooltip on hover */}
        <div className="absolute bottom-0 left-0 right-0 p-2 bg-gradient-to-t from-foreground/80 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-200">
          <p className="text-xs text-primary-foreground truncate font-medium">
            {file.name}
          </p>
        </div>
      </div>
    </FileContextMenu>
  );
};