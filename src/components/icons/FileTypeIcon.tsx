import { FileType } from "@/types/file";
import { Box, FileText, Sheet, Presentation, Video, Image, Package, Folder, Music } from "lucide-react";

interface FileTypeIconProps {
  type: FileType;
  className?: string;
  size?: number;
}

export const FileTypeIcon = ({ type, className = "", size = 32 }: FileTypeIconProps) => {
  const iconProps = { size, className };

  switch (type) {
    case 'document':
      return (
        <div className={`flex items-center justify-center ${className}`}>
          <div className="relative">
            <div className="w-8 h-10 bg-file-doc/10 rounded-sm border-2 border-file-doc flex items-center justify-center">
              <div className="w-4 h-0.5 bg-file-doc absolute top-2"></div>
              <div className="w-3 h-0.5 bg-file-doc absolute top-3.5"></div>
              <div className="absolute -top-0.5 -right-0.5 w-3 h-3">
                <div className="absolute right-0 top-0 w-0 h-0 border-l-[12px] border-l-transparent border-t-[12px] border-t-file-doc"></div>
              </div>
            </div>
          </div>
        </div>
      );
    case 'spreadsheet':
      return (
        <div className={`flex items-center justify-center ${className}`}>
          <div className="w-8 h-10 bg-file-sheet/10 rounded-sm border-2 border-file-sheet flex items-center justify-center p-1">
            <div className="grid grid-cols-2 gap-0.5 w-full h-full">
              <div className="bg-file-sheet rounded-[1px]"></div>
              <div className="bg-file-sheet rounded-[1px]"></div>
              <div className="bg-file-sheet rounded-[1px]"></div>
              <div className="bg-file-sheet rounded-[1px]"></div>
            </div>
          </div>
        </div>
      );
    case 'presentation':
      return (
        <div className={`flex items-center justify-center ${className}`}>
          <div className="w-8 h-10 bg-file-ppt/10 rounded-sm border-2 border-file-ppt flex items-center justify-center">
            <div className="w-5 h-3 bg-file-ppt/30 rounded-[2px] border border-file-ppt"></div>
          </div>
        </div>
      );
    case '3d':
      return (
        <div className={`flex items-center justify-center ${className}`}>
          <Box size={size} className="text-file-3d" />
        </div>
      );
    case 'video':
      return (
        <div className={`flex items-center justify-center ${className}`}>
          <div className="w-10 h-10 bg-file-video rounded-lg flex items-center justify-center">
            <div className="w-0 h-0 border-l-[10px] border-l-white border-y-[6px] border-y-transparent ml-1"></div>
          </div>
        </div>
      );
    case 'image':
      return (
        <div className={`flex items-center justify-center ${className}`}>
          <Image size={size} className="text-file-image" />
        </div>
      );
    case 'audio':
      return (
        <div className={`flex items-center justify-center ${className}`}>
          <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-pink-500 rounded-lg flex items-center justify-center">
            <Music size={20} className="text-white" />
          </div>
        </div>
      );
    case 'apk':
      return (
        <div className={`flex items-center justify-center ${className}`}>
          <Package size={size} className="text-file-apk" />
        </div>
      );
    case 'folder':
      return (
        <div className={`flex items-center justify-center ${className}`}>
          <Folder size={size} className="text-amber-500 fill-amber-100" />
        </div>
      );
    default:
      return <FileText {...iconProps} className="text-muted-foreground" />;
  }
};
