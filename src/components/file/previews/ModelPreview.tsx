import { FileItem } from "@/types/file";
import { FileTypeIcon } from "@/components/icons/FileTypeIcon";
import { Box, RotateCw } from "lucide-react";

interface ModelPreviewProps {
  file: FileItem;
}

export const ModelPreview = ({ file }: ModelPreviewProps) => {
  return (
    <div className="w-full max-w-2xl bg-card border border-border rounded-xl shadow-lg overflow-hidden">
      <div className="bg-muted/50 px-6 py-3 border-b border-border flex items-center justify-between">
        <h3 className="font-medium text-sm">{file.name}</h3>
        {file.size && (
          <span className="text-xs text-muted-foreground">{file.size}</span>
        )}
      </div>
      
      <div className="aspect-square bg-gradient-to-br from-muted/50 to-muted flex flex-col items-center justify-center p-8 relative">
        {/* Animated 3D preview placeholder */}
        <div className="relative">
          <div className="w-32 h-32 rounded-2xl bg-gradient-to-br from-primary/20 to-secondary/20 flex items-center justify-center animate-pulse">
            <FileTypeIcon type="3d" size={64} />
          </div>
          
          {/* Rotation indicator */}
          <div className="absolute -bottom-2 -right-2 w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center">
            <RotateCw size={16} className="text-primary animate-spin" style={{ animationDuration: '3s' }} />
          </div>
        </div>
        
        <div className="mt-8 text-center">
          <div className="flex items-center justify-center gap-2 text-muted-foreground mb-2">
            <Box size={18} />
            <span className="font-medium">3D Model</span>
          </div>
          <p className="text-sm text-muted-foreground">
            Interactive 3D preview not available
          </p>
          <p className="text-xs text-muted-foreground mt-1">
            Download to view in a 3D application
          </p>
        </div>
      </div>

      <div className="p-4 border-t border-border bg-muted/30">
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-muted-foreground">Format</p>
            <p className="font-medium">{file.name.split('.').pop()?.toUpperCase()}</p>
          </div>
          <div>
            <p className="text-muted-foreground">Size</p>
            <p className="font-medium">{file.size || "Unknown"}</p>
          </div>
        </div>
      </div>
    </div>
  );
};
