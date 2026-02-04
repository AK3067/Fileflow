import { useState } from "react";
import { FileItem } from "@/types/file";
import { FileTypeIcon } from "@/components/icons/FileTypeIcon";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { ArrowLeft, Trash2, RotateCcw, Clock } from "lucide-react";
import { cn } from "@/lib/utils";

interface RecycleBinViewProps {
  deletedFiles: FileItem[];
  onBack: () => void;
  onRecover: (fileIds: string[]) => void;
  onPermanentDelete: (fileIds: string[]) => void;
  onRecoverAll: () => void;
  onDeleteAll: () => void;
}

export const RecycleBinView = ({
  deletedFiles,
  onBack,
  onRecover,
  onPermanentDelete,
  onRecoverAll,
  onDeleteAll,
}: RecycleBinViewProps) => {
  const [selectedFiles, setSelectedFiles] = useState<string[]>([]);

  const toggleFileSelection = (fileId: string) => {
    setSelectedFiles((prev) =>
      prev.includes(fileId)
        ? prev.filter((id) => id !== fileId)
        : [...prev, fileId]
    );
  };

  const selectAll = () => {
    if (selectedFiles.length === deletedFiles.length) {
      setSelectedFiles([]);
    } else {
      setSelectedFiles(deletedFiles.map((f) => f.id));
    }
  };

  const getDaysRemaining = (deletedAt?: Date) => {
    if (!deletedAt) return 30;
    const now = new Date();
    const deleted = new Date(deletedAt);
    const daysPassed = Math.floor(
      (now.getTime() - deleted.getTime()) / (1000 * 60 * 60 * 24)
    );
    return Math.max(0, 30 - daysPassed);
  };

  return (
    <div className="p-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Button
            variant="ghost"
            size="icon"
            onClick={onBack}
            className="hover:bg-muted"
          >
            <ArrowLeft size={20} />
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-foreground flex items-center gap-2">
              <Trash2 size={24} className="text-destructive" />
              Recycle Bin
            </h1>
            <p className="text-sm text-muted-foreground">
              {deletedFiles.length} items • Files are permanently deleted after 30 days
            </p>
          </div>
        </div>

        {deletedFiles.length > 0 && (
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={onRecoverAll}
              className="gap-2"
            >
              <RotateCcw size={14} />
              Recover All
            </Button>
            <Button
              variant="destructive"
              size="sm"
              onClick={onDeleteAll}
              className="gap-2"
            >
              <Trash2 size={14} />
              Delete All
            </Button>
          </div>
        )}
      </div>

      {/* Selection Actions */}
      {selectedFiles.length > 0 && (
        <div className="flex items-center gap-4 mb-4 p-3 bg-muted rounded-lg animate-fade-in">
          <span className="text-sm font-medium">
            {selectedFiles.length} selected
          </span>
          <Button
            variant="outline"
            size="sm"
            onClick={() => onRecover(selectedFiles)}
            className="gap-2"
          >
            <RotateCcw size={14} />
            Recover Selected
          </Button>
          <Button
            variant="destructive"
            size="sm"
            onClick={() => onPermanentDelete(selectedFiles)}
            className="gap-2"
          >
            <Trash2 size={14} />
            Delete Selected
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setSelectedFiles([])}
          >
            Clear Selection
          </Button>
        </div>
      )}

      {/* Empty State */}
      {deletedFiles.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-center">
          <div className="w-20 h-20 rounded-full bg-muted flex items-center justify-center mb-4">
            <Trash2 size={40} className="text-muted-foreground" />
          </div>
          <h3 className="text-lg font-medium text-foreground mb-2">
            Recycle Bin is Empty
          </h3>
          <p className="text-sm text-muted-foreground max-w-sm">
            Files you delete will appear here. They will be permanently deleted
            after 30 days.
          </p>
        </div>
      ) : (
        <>
          {/* Select All */}
          <div className="flex items-center gap-2 mb-4 pb-2 border-b border-border">
            <Checkbox
              checked={selectedFiles.length === deletedFiles.length}
              onCheckedChange={selectAll}
            />
            <span className="text-sm text-muted-foreground">Select All</span>
          </div>

          {/* File List */}
          <div className="space-y-2">
            {deletedFiles.map((file) => {
              const daysRemaining = getDaysRemaining(file.deletedAt);
              const isSelected = selectedFiles.includes(file.id);

              return (
                <div
                  key={file.id}
                  className={cn(
                    "flex items-center gap-4 p-3 rounded-lg border transition-all hover:bg-muted/50",
                    isSelected
                      ? "bg-primary/5 border-primary/30"
                      : "bg-card border-border"
                  )}
                >
                  <Checkbox
                    checked={isSelected}
                    onCheckedChange={() => toggleFileSelection(file.id)}
                  />

                  {/* Thumbnail or Icon */}
                  <div className="w-12 h-12 rounded-lg bg-muted flex items-center justify-center overflow-hidden flex-shrink-0">
                    {file.thumbnail ? (
                      <img
                        src={file.thumbnail}
                        alt={file.name}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <FileTypeIcon type={file.type} size={24} />
                    )}
                  </div>

                  {/* File Info */}
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-foreground truncate">
                      {file.name}
                    </p>
                    <div className="flex items-center gap-2 text-xs text-muted-foreground">
                      <span>{file.size}</span>
                      <span>•</span>
                      <span className="flex items-center gap-1">
                        <Clock size={12} />
                        {daysRemaining} days remaining
                      </span>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onRecover([file.id])}
                      className="gap-1 text-primary hover:text-primary hover:bg-primary/10"
                    >
                      <RotateCcw size={14} />
                      Recover
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onPermanentDelete([file.id])}
                      className="gap-1 text-destructive hover:text-destructive hover:bg-destructive/10"
                    >
                      <Trash2 size={14} />
                      Delete
                    </Button>
                  </div>
                </div>
              );
            })}
          </div>
        </>
      )}
    </div>
  );
};
