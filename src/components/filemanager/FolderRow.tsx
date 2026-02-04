import { ChevronRight, Folder } from "lucide-react";
import { Folder as FolderType } from "@/types/file";

interface FolderRowProps {
  folder: FolderType;
  index: number;
  onNavigate: (folderId: string) => void;
}

export const FolderRow = ({ folder, index, onNavigate }: FolderRowProps) => {
  return (
    <button
      onClick={() => onNavigate(folder.id)}
      className="w-full flex items-center justify-between px-6 py-4 bg-card border border-border rounded-xl hover:border-primary/30 hover:shadow-soft transition-all duration-200 animate-fade-in"
      style={{ animationDelay: `${index * 0.05}s` }}
    >
      <div className="flex items-center gap-4">
        <Folder size={24} className="text-amber-500 fill-amber-100" />
        <span className="font-medium text-foreground">{folder.name}</span>
      </div>
      <ChevronRight size={20} className="text-muted-foreground" />
    </button>
  );
};
