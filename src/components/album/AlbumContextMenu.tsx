import {
  ContextMenu,
  ContextMenuContent,
  ContextMenuItem,
  ContextMenuSeparator,
  ContextMenuTrigger,
} from "@/components/ui/context-menu";
import { Pencil, Trash2, ImagePlus, FolderOpen } from "lucide-react";

interface AlbumContextMenuProps {
  children: React.ReactNode;
  onRename: () => void;
  onDelete: () => void;
  onAddFiles: () => void;
  onOpen: () => void;
}

export const AlbumContextMenu = ({
  children,
  onRename,
  onDelete,
  onAddFiles,
  onOpen,
}: AlbumContextMenuProps) => {
  return (
    <ContextMenu>
      <ContextMenuTrigger asChild>{children}</ContextMenuTrigger>
      <ContextMenuContent className="w-48 animate-scale-in">
        <ContextMenuItem onClick={onOpen} className="gap-2 cursor-pointer">
          <FolderOpen size={14} />
          Open Album
        </ContextMenuItem>
        <ContextMenuItem onClick={onAddFiles} className="gap-2 cursor-pointer">
          <ImagePlus size={14} />
          Add Files
        </ContextMenuItem>
        <ContextMenuSeparator />
        <ContextMenuItem onClick={onRename} className="gap-2 cursor-pointer">
          <Pencil size={14} />
          Rename
        </ContextMenuItem>
        <ContextMenuSeparator />
        <ContextMenuItem
          onClick={onDelete}
          className="gap-2 cursor-pointer text-destructive focus:text-destructive"
        >
          <Trash2 size={14} />
          Delete Album
        </ContextMenuItem>
      </ContextMenuContent>
    </ContextMenu>
  );
};
