import { useState, useEffect } from "react";
import { Info } from "lucide-react";
import { FileBreakdown, FileType } from "@/types/file";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";

interface FileBreakdownPopoverProps {
  breakdown: FileBreakdown[];
  totalFiles: number;
}

const getTypeColor = (type: FileType): string => {
  switch (type) {
    case 'image':
      return 'text-file-image';
    case 'document':
      return 'text-file-doc';
    case 'spreadsheet':
      return 'text-file-sheet';
    case 'presentation':
      return 'text-file-ppt';
    case 'video':
      return 'text-file-video';
    case 'audio':
      return 'text-purple-500';
    case 'apk':
      return 'text-file-apk';
    case '3d':
      return 'text-file-3d';
    default:
      return 'text-muted-foreground';
  }
};

export const FileBreakdownPopover = ({ breakdown, totalFiles }: FileBreakdownPopoverProps) => {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (open) {
      const timer = setTimeout(() => {
        setOpen(false);
      }, 4000);
      return () => clearTimeout(timer);
    }
  }, [open]);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <button className="flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors">
          <span className="text-sm font-medium text-foreground">{totalFiles} files</span>
          <Info size={14} className="text-muted-foreground" />
        </button>
      </PopoverTrigger>
      <PopoverContent align="start" className="w-64 p-4 animate-scale-in">
        <div className="space-y-3">
          <div>
            <h4 className="font-semibold text-foreground">File Breakdown</h4>
            <p className="text-xs text-muted-foreground">Total files you have stored.</p>
          </div>
          <div className="space-y-2">
            {breakdown.map((item) => (
              <div
                key={item.type}
                className="flex items-center justify-between text-sm"
              >
                <span className="text-foreground">{item.label}</span>
                <span className={getTypeColor(item.type)}>{item.count}</span>
              </div>
            ))}
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
};
