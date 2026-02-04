import { Folder } from "@/types/file";
import { FolderRow } from "./FolderRow";

interface FileManagerViewProps {
  folders: Folder[];
  currentPath: string;
  onNavigate: (folderId: string) => void;
}

export const FileManagerView = ({ folders, currentPath, onNavigate }: FileManagerViewProps) => {
  return (
    <div className="p-6 animate-fade-in">
      {/* Path header */}
      <div className="mb-6">
        <h2 className="text-sm font-medium text-muted-foreground">{currentPath}</h2>
      </div>

      {/* Folder list */}
      <div className="space-y-3">
        {folders.map((folder, index) => (
          <FolderRow
            key={folder.id}
            folder={folder}
            index={index}
            onNavigate={onNavigate}
          />
        ))}
      </div>
    </div>
  );
};
