import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { FileItem } from "@/types/file";
import { FileTypeIcon } from "@/components/icons/FileTypeIcon";
import { AudioPlayer } from "./AudioPlayer";
import { DocumentPreview } from "./previews/DocumentPreview";
import { SpreadsheetPreview } from "./previews/SpreadsheetPreview";
import { PresentationPreview } from "./previews/PresentationPreview";
import { ModelPreview } from "./previews/ModelPreview";
import {
  X,
  ChevronLeft,
  ChevronRight,
  Download,
  Share2,
  Pencil,
  Trash2,
  Info,
} from "lucide-react";

interface FileViewerModalProps {
  file: FileItem | null;
  files: FileItem[];
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onNavigate: (file: FileItem) => void;
  onDownload: (file: FileItem) => void;
  onShare: (file: FileItem) => void;
  onEdit: (file: FileItem) => void;
  onDelete: (file: FileItem) => void;
  onDetails: (file: FileItem) => void;
}

export const FileViewerModal = ({
  file,
  files,
  open,
  onOpenChange,
  onNavigate,
  onDownload,
  onShare,
  onEdit,
  onDelete,
  onDetails,
}: FileViewerModalProps) => {
  if (!file) return null;

  const currentIndex = files.findIndex((f) => f.id === file.id);
  const hasPrev = currentIndex > 0;
  const hasNext = currentIndex < files.length - 1;

  const handlePrev = () => {
    if (hasPrev) {
      onNavigate(files[currentIndex - 1]);
    }
  };

  const handleNext = () => {
    if (hasNext) {
      onNavigate(files[currentIndex + 1]);
    }
  };

  const renderPreview = () => {
    // Image preview
    if (file.type === "image") {
      const imageSrc = file.thumbnail || file.url;
      if (imageSrc) {
        return (
          <img
            src={imageSrc}
            alt={file.name}
            className="max-w-full max-h-[70vh] object-contain rounded-lg shadow-lg"
          />
        );
      }
    }

    // Video player
    if (file.type === "video") {
      const videoSrc = file.url || "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
      return (
        <div className="w-full max-w-4xl">
          <video
            src={videoSrc}
            controls
            autoPlay
            className="w-full max-h-[70vh] rounded-lg shadow-lg bg-black"
          >
            Your browser does not support the video tag.
          </video>
        </div>
      );
    }

    // Audio player
    if (file.type === "audio") {
      const audioSrc = file.url || "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
      return <AudioPlayer audioSrc={audioSrc} file={file} />;
    }

    // Document preview (DOCX, TXT, etc.)
    if (file.type === "document") {
      return <DocumentPreview file={file} />;
    }

    // Spreadsheet preview (XLSX, CSV)
    if (file.type === "spreadsheet") {
      return <SpreadsheetPreview file={file} />;
    }

    // Presentation preview (PPTX)
    if (file.type === "presentation") {
      return <PresentationPreview file={file} />;
    }

    // 3D Model preview
    if (file.type === "3d") {
      return <ModelPreview file={file} />;
    }

    // APK and other files - fallback
    return (
      <div className="w-full max-w-md bg-card border border-border rounded-xl flex flex-col items-center justify-center p-10 shadow-lg">
        <div className="w-24 h-24 rounded-2xl bg-muted flex items-center justify-center mb-6">
          <FileTypeIcon type={file.type} size={56} />
        </div>
        <p className="text-xl font-semibold text-center mb-2">{file.name}</p>
        {file.size && (
          <p className="text-sm text-muted-foreground mb-1">{file.size}</p>
        )}
        {file.modifiedAt && (
          <p className="text-sm text-muted-foreground mb-6">
            Modified: {file.modifiedAt.toLocaleDateString()}
          </p>
        )}
        <Button onClick={() => onDownload(file)} className="gap-2">
          <Download size={18} />
          Open / Download
        </Button>
      </div>
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-5xl w-full h-[90vh] p-0 bg-background/95 backdrop-blur-sm border-border">
        <DialogTitle className="sr-only">File Viewer - {file.name}</DialogTitle>
        
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-border">
          <div className="flex items-center gap-3">
            <FileTypeIcon type={file.type} size={24} />
            <span className="font-medium truncate max-w-[300px]">{file.name}</span>
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => onEdit(file)}
              title="Edit"
            >
              <Pencil size={18} />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => onShare(file)}
              title="Share"
            >
              <Share2 size={18} />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => onDownload(file)}
              title="Download"
            >
              <Download size={18} />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => onDetails(file)}
              title="Details"
            >
              <Info size={18} />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => onDelete(file)}
              title="Delete"
              className="text-destructive hover:text-destructive"
            >
              <Trash2 size={18} />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => onOpenChange(false)}
              title="Close"
            >
              <X size={18} />
            </Button>
          </div>
        </div>

        {/* Preview Area */}
        <div className="flex-1 flex items-center justify-center p-8 relative">
          {/* Navigation Arrows */}
          {hasPrev && (
            <Button
              variant="secondary"
              size="icon"
              className="absolute left-4 top-1/2 -translate-y-1/2 rounded-full shadow-lg"
              onClick={handlePrev}
            >
              <ChevronLeft size={24} />
            </Button>
          )}

          {renderPreview()}

          {hasNext && (
            <Button
              variant="secondary"
              size="icon"
              className="absolute right-4 top-1/2 -translate-y-1/2 rounded-full shadow-lg"
              onClick={handleNext}
            >
              <ChevronRight size={24} />
            </Button>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-border text-center text-sm text-muted-foreground">
          {currentIndex + 1} of {files.length}
        </div>
      </DialogContent>
    </Dialog>
  );
};
