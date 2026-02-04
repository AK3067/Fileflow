import { LayoutGrid, FolderOpen } from "lucide-react";
import { cn } from "@/lib/utils";

type ViewMode = "gallery" | "fileManager";

interface ViewToggleProps {
  currentView: ViewMode;
  onViewChange: (view: ViewMode) => void;
}

export const ViewToggle = ({ currentView, onViewChange }: ViewToggleProps) => {
  return (
    <div className="inline-flex items-center bg-secondary rounded-lg p-1 gap-1">
      <button
        onClick={() => onViewChange("gallery")}
        className={cn(
          "flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition-all duration-200",
          currentView === "gallery"
            ? "bg-card text-foreground shadow-soft"
            : "text-muted-foreground hover:text-foreground"
        )}
      >
        <LayoutGrid size={16} />
        <span>Gallery</span>
      </button>
      <button
        onClick={() => onViewChange("fileManager")}
        className={cn(
          "flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition-all duration-200",
          currentView === "fileManager"
            ? "bg-card text-foreground shadow-soft"
            : "text-muted-foreground hover:text-foreground"
        )}
      >
        <FolderOpen size={16} />
        <span>File Manager</span>
      </button>
    </div>
  );
};
