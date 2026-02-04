import { Plus, Zap, Folder, FileText, Trash2, MoreVertical, ZapOff, Eye } from "lucide-react";
import { Album, QuickAccessItem } from "@/types/file";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { AlbumContextMenu } from "@/components/album/AlbumContextMenu";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

interface AppSidebarProps {
  albums: Album[];
  quickAccessItems: QuickAccessItem[];
  selectedAlbumId?: string;
  onSelectAlbum: (albumId: string | undefined) => void;
  onAddAlbum: () => void;
  onRenameAlbum: (albumId: string) => void;
  onDeleteAlbum: (albumId: string) => void;
  onAddFilesToAlbum: (albumId: string) => void;
  isCollapsed: boolean;
  recycleBinCount: number;
  isRecycleBinActive: boolean;
  onOpenRecycleBin: () => void;
  onOpenQuickAccessFile: (itemId: string) => void;
  onRemoveFromQuickAccess: (itemId: string) => void;
  onDeleteQuickAccessFile: (itemId: string) => void;
}

export const AppSidebar = ({
  albums,
  quickAccessItems,
  selectedAlbumId,
  onSelectAlbum,
  onAddAlbum,
  onRenameAlbum,
  onDeleteAlbum,
  onAddFilesToAlbum,
  isCollapsed,
  recycleBinCount,
  isRecycleBinActive,
  onOpenRecycleBin,
  onOpenQuickAccessFile,
  onRemoveFromQuickAccess,
  onDeleteQuickAccessFile,
}: AppSidebarProps) => {
  return (
    <aside
      className={cn(
        "h-full bg-sidebar border-r border-sidebar-border flex flex-col transition-all duration-300 ease-out",
        isCollapsed ? "w-0 overflow-hidden opacity-0" : "w-64 opacity-100"
      )}
    >
      {/* New Album Button */}
      <div className="p-4 border-b border-sidebar-border">
        <Button
          variant="ghost"
          className="w-full justify-start gap-2 text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground transition-colors"
          onClick={onAddAlbum}
        >
          <Plus size={16} />
          <span className="font-medium">New Album</span>
        </Button>
      </div>

      {/* Quick Access */}
      <div className="p-4 animate-fade-in" style={{ animationDelay: "0.1s" }}>
        <div className="flex items-center gap-2 text-sidebar-primary mb-3">
          <Zap size={14} className="fill-sidebar-primary" />
          <span className="text-sm font-semibold">Quick Access</span>
        </div>
        <div className="space-y-1">
          {quickAccessItems.map((item, index) => (
            <div
              key={item.id}
              className="group flex items-center gap-2 px-2 py-1.5 text-sm text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground rounded-md transition-colors animate-slide-in-left"
              style={{ animationDelay: `${0.15 + index * 0.05}s` }}
            >
              <button
                className="flex-1 flex items-center gap-2 min-w-0"
                onClick={() => onOpenQuickAccessFile(item.id)}
              >
                <FileText size={14} className="text-muted-foreground shrink-0" />
                <span className="truncate">{item.name}</span>
              </button>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <button
                    className="p-1 rounded opacity-0 group-hover:opacity-100 hover:bg-sidebar-accent transition-opacity"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <MoreVertical size={12} className="text-muted-foreground" />
                  </button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-48">
                  <DropdownMenuItem onClick={() => onOpenQuickAccessFile(item.id)}>
                    <Eye className="mr-2 h-4 w-4" />
                    Open
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => onRemoveFromQuickAccess(item.id)}>
                    <ZapOff className="mr-2 h-4 w-4" />
                    Remove from Quick Access
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onClick={() => onDeleteQuickAccessFile(item.id)}
                    className="text-destructive focus:text-destructive"
                  >
                    <Trash2 className="mr-2 h-4 w-4" />
                    Delete
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          ))}
        </div>
      </div>

      {/* Albums */}
      <div className="flex-1 p-4 overflow-auto animate-fade-in" style={{ animationDelay: "0.2s" }}>
        <div className="space-y-1">
          {albums.map((album, index) => (
            <AlbumContextMenu
              key={album.id}
              onOpen={() => onSelectAlbum(album.id)}
              onRename={() => onRenameAlbum(album.id)}
              onDelete={() => onDeleteAlbum(album.id)}
              onAddFiles={() => onAddFilesToAlbum(album.id)}
            >
              <button
                onClick={() => onSelectAlbum(selectedAlbumId === album.id ? undefined : album.id)}
                className={cn(
                  "w-full flex items-center gap-2 px-2 py-1.5 text-sm rounded-md transition-all duration-200 animate-slide-in-left",
                  selectedAlbumId === album.id
                    ? "bg-sidebar-accent text-sidebar-accent-foreground"
                    : "text-sidebar-foreground hover:bg-sidebar-accent/50"
                )}
                style={{ animationDelay: `${0.25 + index * 0.05}s` }}
              >
                <Folder size={14} className="text-muted-foreground" />
                <span className="truncate">{album.name}</span>
                <span className="ml-auto text-xs text-muted-foreground">{album.fileCount}</span>
              </button>
            </AlbumContextMenu>
          ))}
        </div>
      </div>

      {/* Recycle Bin */}
      <div className="p-4 border-t border-sidebar-border">
        <button
          onClick={onOpenRecycleBin}
          className={cn(
            "w-full flex items-center gap-2 px-2 py-1.5 text-sm rounded-md transition-colors",
            isRecycleBinActive
              ? "bg-sidebar-accent text-sidebar-accent-foreground"
              : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
          )}
        >
          <Trash2 size={14} className="text-muted-foreground" />
          <span>Recycle Bin</span>
          {recycleBinCount > 0 && (
            <span className="ml-auto text-xs bg-destructive/20 text-destructive px-1.5 py-0.5 rounded-full">
              {recycleBinCount}
            </span>
          )}
        </button>
      </div>
    </aside>
  );
};
