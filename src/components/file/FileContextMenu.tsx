import {
  ContextMenu,
  ContextMenuContent,
  ContextMenuItem,
  ContextMenuSeparator,
  ContextMenuSub,
  ContextMenuSubContent,
  ContextMenuSubTrigger,
  ContextMenuTrigger,
} from "@/components/ui/context-menu";
import {
  Eye,
  Download,
  FolderPlus,
  Zap,
  ZapOff,
  Share2,
  Pencil,
  Info,
  Trash2,
} from "lucide-react";
import { FileItem, Album } from "@/types/file";

interface FileContextMenuProps {
  children: React.ReactNode;
  file: FileItem;
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

export const FileContextMenu = ({
  children,
  file,
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
}: FileContextMenuProps) => {
  return (
    <ContextMenu>
      <ContextMenuTrigger asChild>{children}</ContextMenuTrigger>
      <ContextMenuContent className="w-56">
        <ContextMenuItem onClick={() => onOpen(file)}>
          <Eye className="mr-2 h-4 w-4" />
          Open
        </ContextMenuItem>
        <ContextMenuItem onClick={() => onDownload(file)}>
          <Download className="mr-2 h-4 w-4" />
          Download
        </ContextMenuItem>
        <ContextMenuSeparator />
        <ContextMenuItem onClick={() => onEdit(file)}>
          <Pencil className="mr-2 h-4 w-4" />
          Edit
        </ContextMenuItem>
        <ContextMenuSub>
          <ContextMenuSubTrigger>
            <FolderPlus className="mr-2 h-4 w-4" />
            Add to Album
          </ContextMenuSubTrigger>
          <ContextMenuSubContent className="w-48">
            {albums.length > 0 ? (
              albums.map((album) => (
                <ContextMenuItem
                  key={album.id}
                  onClick={() => onAddToAlbum(file, album.id)}
                >
                  {album.name}
                </ContextMenuItem>
              ))
            ) : (
              <ContextMenuItem disabled>No albums available</ContextMenuItem>
            )}
          </ContextMenuSubContent>
        </ContextMenuSub>
        <ContextMenuItem onClick={() => onToggleQuickAccess(file)}>
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
        </ContextMenuItem>
        <ContextMenuSeparator />
        <ContextMenuItem onClick={() => onShare(file)}>
          <Share2 className="mr-2 h-4 w-4" />
          Share
        </ContextMenuItem>
        <ContextMenuItem onClick={() => onRename(file)}>
          <Pencil className="mr-2 h-4 w-4" />
          Rename
        </ContextMenuItem>
        <ContextMenuItem onClick={() => onDetails(file)}>
          <Info className="mr-2 h-4 w-4" />
          Details
        </ContextMenuItem>
        <ContextMenuSeparator />
        <ContextMenuItem
          onClick={() => onDelete(file)}
          className="text-destructive focus:text-destructive"
        >
          <Trash2 className="mr-2 h-4 w-4" />
          Delete
        </ContextMenuItem>
      </ContextMenuContent>
    </ContextMenu>
  );
};
