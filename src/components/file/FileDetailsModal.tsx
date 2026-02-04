import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { FileItem } from "@/types/file";
import { FileTypeIcon } from "@/components/icons/FileTypeIcon";
import { X } from "lucide-react";

interface FileDetailsModalProps {
  file: FileItem | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export const FileDetailsModal = ({
  file,
  open,
  onOpenChange,
}: FileDetailsModalProps) => {
  if (!file) return null;

  const details = [
    { label: "Name", value: file.name },
    { label: "Type", value: file.type.charAt(0).toUpperCase() + file.type.slice(1) },
    { label: "Size", value: file.size || "Unknown" },
    { label: "Modified", value: file.modifiedAt?.toLocaleDateString() || "Unknown" },
    { label: "Starred", value: file.starred ? "Yes" : "No" },
    { label: "Album", value: file.albumId || "None" },
  ];

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader className="relative">
          <DialogTitle>File Details</DialogTitle>
          <button
            onClick={() => onOpenChange(false)}
            className="absolute right-0 top-0 p-1 rounded-full hover:bg-secondary transition-colors"
          >
            <X className="h-4 w-4" />
          </button>
        </DialogHeader>
        <div className="flex flex-col items-center py-4">
          <div className="w-20 h-20 rounded-xl bg-secondary flex items-center justify-center mb-4">
            <FileTypeIcon type={file.type} size={48} />
          </div>
          <div className="w-full space-y-3">
            {details.map((detail) => (
              <div
                key={detail.label}
                className="flex justify-between items-center py-2 border-b border-border last:border-0"
              >
                <span className="text-muted-foreground">{detail.label}</span>
                <span className="font-medium truncate max-w-[200px]">{detail.value}</span>
              </div>
            ))}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};
