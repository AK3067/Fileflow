import { useState, useRef, useEffect, useCallback } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { FileItem } from "@/types/file";
import { 
  X, 
  Bold,
  Italic,
  Underline,
  Strikethrough,
  AlignLeft,
  AlignCenter,
  AlignRight,
  AlignJustify,
  List,
  ListOrdered,
  Table2,
  Image as ImageIcon,
  Droplets,
  Undo,
  Redo,
  Save,
  Download,
  Type,
  Highlighter,
  Link,
  Minus,
  Plus,
} from "lucide-react";
import { toast } from "@/hooks/use-toast";

interface FileEditorModalProps {
  file: FileItem | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSave?: (file: FileItem, editedData: string) => void;
}

const fontFamilies = [
  { value: "Arial", label: "Arial" },
  { value: "Times New Roman", label: "Times New Roman" },
  { value: "Georgia", label: "Georgia" },
  { value: "Verdana", label: "Verdana" },
  { value: "Courier New", label: "Courier New" },
  { value: "Trebuchet MS", label: "Trebuchet MS" },
  { value: "Comic Sans MS", label: "Comic Sans MS" },
];

const fontSizes = [8, 9, 10, 11, 12, 14, 16, 18, 20, 24, 28, 32, 36, 48, 72];

// Mock document content for editing
const mockDocumentContent: Record<string, string> = {
  "Project Brief.docx": `PROJECT BRIEF

Project Name: Website Redesign 2024
Date: December 15, 2024
Author: John Smith

EXECUTIVE SUMMARY
This document outlines the comprehensive plan for redesigning our corporate website to improve user experience, modernize the visual design, and enhance mobile responsiveness.

OBJECTIVES
• Improve user engagement by 40%
• Reduce bounce rate by 25%
• Increase mobile traffic conversion by 50%
• Modernize brand presentation

SCOPE OF WORK
1. User Research & Analysis
   - Conduct user interviews
   - Analyze current analytics data
   - Create user personas

2. Design Phase
   - Wireframing
   - High-fidelity mockups
   - Prototype development

3. Development
   - Frontend implementation
   - Backend integration
   - Performance optimization

4. Testing & Launch
   - QA testing
   - User acceptance testing
   - Phased rollout

TIMELINE
Phase 1: Research (2 weeks)
Phase 2: Design (4 weeks)
Phase 3: Development (6 weeks)
Phase 4: Testing (2 weeks)

BUDGET
Total estimated budget: $75,000

STAKEHOLDERS
- Marketing Team
- IT Department
- Executive Leadership
- External Design Agency`,

  "Design Specs.docx": `DESIGN SPECIFICATIONS

Document Version: 1.2
Last Updated: December 10, 2024

COLOR PALETTE
Primary: #3B82F6 (Blue)
Secondary: #10B981 (Green)
Accent: #F59E0B (Amber)
Background: #F8FAFC
Text: #1E293B

TYPOGRAPHY
Headings: Inter Bold
  - H1: 48px / 56px line-height
  - H2: 36px / 44px line-height
  - H3: 24px / 32px line-height

Body: Inter Regular
  - Large: 18px / 28px line-height
  - Normal: 16px / 24px line-height
  - Small: 14px / 20px line-height

SPACING SYSTEM
Base unit: 4px
- xs: 4px
- sm: 8px
- md: 16px
- lg: 24px
- xl: 32px
- 2xl: 48px

COMPONENT SPECIFICATIONS

Buttons
- Height: 40px (default), 32px (small), 48px (large)
- Border radius: 8px
- Padding: 16px horizontal

Cards
- Border radius: 12px
- Shadow: 0 4px 6px rgba(0,0,0,0.1)
- Padding: 24px

Input Fields
- Height: 40px
- Border: 1px solid #E2E8F0
- Border radius: 8px
- Focus: 2px blue outline`,
};

// Convert plain text to HTML paragraphs
const textToHtml = (text: string): string => {
  return text
    .split('\n')
    .map(line => {
      if (line.trim() === '') return '<p><br></p>';
      // Check if it's a heading (all caps line)
      if (line === line.toUpperCase() && line.trim().length > 0 && !line.startsWith('-') && !line.startsWith('•')) {
        return `<h2 style="font-weight: bold; font-size: 18px; margin: 16px 0 8px 0;">${line}</h2>`;
      }
      return `<p style="margin: 4px 0;">${line}</p>`;
    })
    .join('');
};

const getDocumentContent = (fileName: string): string => {
  const content = mockDocumentContent[fileName];
  if (content) {
    return textToHtml(content);
  }
  return `<p>Start editing "${fileName}"...</p><p><br></p>`;
};

export const FileEditorModal = ({
  file,
  open,
  onOpenChange,
  onSave,
}: FileEditorModalProps) => {
  const editorRef = useRef<HTMLDivElement>(null);
  const [isReady, setIsReady] = useState(false);
  const [content, setContent] = useState("");
  const [fontFamily, setFontFamily] = useState("Arial");
  const [fontSize, setFontSize] = useState("14");
  const [textColor, setTextColor] = useState("#000000");
  const [highlightColor, setHighlightColor] = useState("#ffff00");
  const [watermarkText, setWatermarkText] = useState("");
  const [showWatermark, setShowWatermark] = useState(false);
  const [tableRows, setTableRows] = useState(3);
  const [tableCols, setTableCols] = useState(3);

  // Reset ready state when dialog closes
  useEffect(() => {
    if (!open) {
      setIsReady(false);
    }
  }, [open]);

  // Initialize editor content when ready
  useEffect(() => {
    if (isReady && file && editorRef.current) {
      // Check if file has previously edited content
      if (file.editedContent) {
        editorRef.current.innerHTML = file.editedContent;
      } else if (file.type === "document") {
        editorRef.current.innerHTML = getDocumentContent(file.name);
      } else if (file.type === "image" && file.thumbnail) {
        editorRef.current.innerHTML = `<p>Image: ${file.name}</p><img src="${file.thumbnail}" style="max-width: 100%; height: auto;" /><p><br></p>`;
      } else {
        editorRef.current.innerHTML = getDocumentContent(file.name);
      }
      editorRef.current.focus();
    }
  }, [isReady, file]);

  // Callback ref to detect when editor is mounted
  const setEditorRef = useCallback((node: HTMLDivElement | null) => {
    editorRef.current = node;
    if (node && open && file) {
      setIsReady(true);
    }
  }, [open, file]);

  // Execute formatting command
  const execCommand = useCallback((command: string, value: string | undefined = undefined) => {
    document.execCommand(command, false, value);
    editorRef.current?.focus();
  }, []);

  // Format handlers
  const handleBold = () => execCommand("bold");
  const handleItalic = () => execCommand("italic");
  const handleUnderline = () => execCommand("underline");
  const handleStrikethrough = () => execCommand("strikeThrough");
  
  const handleAlignLeft = () => execCommand("justifyLeft");
  const handleAlignCenter = () => execCommand("justifyCenter");
  const handleAlignRight = () => execCommand("justifyRight");
  const handleAlignJustify = () => execCommand("justifyFull");
  
  const handleBulletList = () => execCommand("insertUnorderedList");
  const handleNumberedList = () => execCommand("insertOrderedList");
  
  const handleFontFamily = (family: string) => {
    setFontFamily(family);
    execCommand("fontName", family);
  };
  
  const handleFontSize = (size: string) => {
    setFontSize(size);
    // Use a span with inline style for precise font size
    const selection = window.getSelection();
    if (selection && selection.rangeCount > 0) {
      const range = selection.getRangeAt(0);
      if (!range.collapsed) {
        const span = document.createElement("span");
        span.style.fontSize = `${size}px`;
        range.surroundContents(span);
      }
    }
    editorRef.current?.focus();
  };
  
  const handleTextColor = (color: string) => {
    setTextColor(color);
    execCommand("foreColor", color);
  };
  
  const handleHighlight = (color: string) => {
    setHighlightColor(color);
    execCommand("hiliteColor", color);
  };

  const handleUndo = () => execCommand("undo");
  const handleRedo = () => execCommand("redo");

  // Insert table
  const handleInsertTable = () => {
    if (!editorRef.current) return;
    
    let tableHtml = `<table style="border-collapse: collapse; width: 100%; margin: 10px 0;">`;
    for (let i = 0; i < tableRows; i++) {
      tableHtml += "<tr>";
      for (let j = 0; j < tableCols; j++) {
        tableHtml += `<td style="border: 1px solid #ccc; padding: 8px; min-width: 60px;" contenteditable="true">&nbsp;</td>`;
      }
      tableHtml += "</tr>";
    }
    tableHtml += "</table><p><br></p>";
    
    execCommand("insertHTML", tableHtml);
    toast({ title: "Table Inserted", description: `${tableRows}x${tableCols} table added.` });
  };

  // Insert image from URL
  const handleInsertImage = () => {
    const url = prompt("Enter image URL:");
    if (url) {
      execCommand("insertHTML", `<img src="${url}" style="max-width: 100%; height: auto; margin: 10px 0;" />`);
      toast({ title: "Image Inserted" });
    }
  };

  // Insert link
  const handleInsertLink = () => {
    const url = prompt("Enter URL:");
    if (url) {
      execCommand("createLink", url);
      toast({ title: "Link Added" });
    }
  };

  // Insert horizontal line
  const handleInsertLine = () => {
    execCommand("insertHorizontalRule");
  };

  // Apply watermark
  const handleApplyWatermark = () => {
    if (!watermarkText.trim()) {
      toast({ title: "Enter watermark text", variant: "destructive" });
      return;
    }
    setShowWatermark(true);
    toast({ title: "Watermark Applied" });
  };

  // Save document
  const handleSave = () => {
    if (!editorRef.current || !file) return;
    const htmlContent = editorRef.current.innerHTML;
    onSave?.(file, htmlContent);
    toast({ title: "Saved", description: "Document saved successfully." });
    onOpenChange(false);
  };

  // Download as HTML
  const handleDownload = () => {
    if (!editorRef.current || !file) return;
    
    const htmlContent = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>${file.name}</title>
  <style>
    body { font-family: ${fontFamily}; padding: 40px; max-width: 800px; margin: 0 auto; }
    table { border-collapse: collapse; width: 100%; }
    td { border: 1px solid #ccc; padding: 8px; }
    img { max-width: 100%; }
    ${showWatermark ? `
    body::before {
      content: "${watermarkText}";
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%) rotate(-45deg);
      font-size: 80px;
      color: rgba(0,0,0,0.1);
      pointer-events: none;
      z-index: 1000;
    }
    ` : ""}
  </style>
</head>
<body>
${editorRef.current.innerHTML}
</body>
</html>`;

    const blob = new Blob([htmlContent], { type: "text/html" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `${file.name.replace(/\.[^/.]+$/, "")}.html`;
    link.click();
    URL.revokeObjectURL(url);
    toast({ title: "Downloaded", description: "Document exported as HTML." });
  };

  if (!file) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-6xl h-[90vh] flex flex-col p-0 gap-0">
        {/* Header */}
        <DialogHeader className="px-4 py-3 border-b border-border flex flex-row items-center justify-between shrink-0">
          <DialogTitle className="text-base font-medium">Edit: {file.name}</DialogTitle>
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" onClick={handleDownload}>
              <Download className="h-4 w-4 mr-1" />
              Export
            </Button>
            <Button size="sm" onClick={handleSave}>
              <Save className="h-4 w-4 mr-1" />
              Save
            </Button>
            <button
              onClick={() => onOpenChange(false)}
              className="p-1 rounded-full hover:bg-secondary transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </DialogHeader>

        {/* Toolbar */}
        <div className="px-4 py-2 border-b border-border bg-muted/30 flex flex-wrap items-center gap-1 shrink-0">
          {/* Undo/Redo */}
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleUndo} title="Undo">
            <Undo className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleRedo} title="Redo">
            <Redo className="h-4 w-4" />
          </Button>
          
          <Separator orientation="vertical" className="h-6 mx-1" />
          
          {/* Font Family */}
          <Select value={fontFamily} onValueChange={handleFontFamily}>
            <SelectTrigger className="w-32 h-8 text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {fontFamilies.map((font) => (
                <SelectItem key={font.value} value={font.value} style={{ fontFamily: font.value }}>
                  {font.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          
          {/* Font Size */}
          <Select value={fontSize} onValueChange={handleFontSize}>
            <SelectTrigger className="w-16 h-8 text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {fontSizes.map((size) => (
                <SelectItem key={size} value={size.toString()}>
                  {size}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Separator orientation="vertical" className="h-6 mx-1" />

          {/* Text Formatting */}
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleBold} title="Bold">
            <Bold className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleItalic} title="Italic">
            <Italic className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleUnderline} title="Underline">
            <Underline className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleStrikethrough} title="Strikethrough">
            <Strikethrough className="h-4 w-4" />
          </Button>

          <Separator orientation="vertical" className="h-6 mx-1" />

          {/* Text Color */}
          <Popover>
            <PopoverTrigger asChild>
              <Button variant="ghost" size="icon" className="h-8 w-8 relative" title="Text Color">
                <Type className="h-4 w-4" />
                <div 
                  className="absolute bottom-1 left-1 right-1 h-1 rounded"
                  style={{ backgroundColor: textColor }}
                />
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-2">
              <Input
                type="color"
                value={textColor}
                onChange={(e) => handleTextColor(e.target.value)}
                className="w-20 h-8 cursor-pointer"
              />
            </PopoverContent>
          </Popover>

          {/* Highlight */}
          <Popover>
            <PopoverTrigger asChild>
              <Button variant="ghost" size="icon" className="h-8 w-8 relative" title="Highlight">
                <Highlighter className="h-4 w-4" />
                <div 
                  className="absolute bottom-1 left-1 right-1 h-1 rounded"
                  style={{ backgroundColor: highlightColor }}
                />
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-2">
              <div className="flex gap-1">
                {["#ffff00", "#00ff00", "#00ffff", "#ff00ff", "#ff0000", "#ffffff"].map((color) => (
                  <button
                    key={color}
                    className="w-6 h-6 rounded border border-border"
                    style={{ backgroundColor: color }}
                    onClick={() => handleHighlight(color)}
                  />
                ))}
              </div>
            </PopoverContent>
          </Popover>

          <Separator orientation="vertical" className="h-6 mx-1" />

          {/* Alignment */}
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleAlignLeft} title="Align Left">
            <AlignLeft className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleAlignCenter} title="Align Center">
            <AlignCenter className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleAlignRight} title="Align Right">
            <AlignRight className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleAlignJustify} title="Justify">
            <AlignJustify className="h-4 w-4" />
          </Button>

          <Separator orientation="vertical" className="h-6 mx-1" />

          {/* Lists */}
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleBulletList} title="Bullet List">
            <List className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleNumberedList} title="Numbered List">
            <ListOrdered className="h-4 w-4" />
          </Button>

          <Separator orientation="vertical" className="h-6 mx-1" />

          {/* Insert Table */}
          <Popover>
            <PopoverTrigger asChild>
              <Button variant="ghost" size="icon" className="h-8 w-8" title="Insert Table">
                <Table2 className="h-4 w-4" />
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-3">
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <span className="text-xs">Rows:</span>
                  <Input
                    type="number"
                    value={tableRows}
                    onChange={(e) => setTableRows(Math.max(1, parseInt(e.target.value) || 1))}
                    className="w-16 h-7 text-xs"
                    min={1}
                    max={20}
                  />
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs">Cols:</span>
                  <Input
                    type="number"
                    value={tableCols}
                    onChange={(e) => setTableCols(Math.max(1, parseInt(e.target.value) || 1))}
                    className="w-16 h-7 text-xs"
                    min={1}
                    max={20}
                  />
                </div>
                <Button size="sm" className="w-full" onClick={handleInsertTable}>
                  Insert
                </Button>
              </div>
            </PopoverContent>
          </Popover>

          {/* Insert Image */}
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleInsertImage} title="Insert Image">
            <ImageIcon className="h-4 w-4" />
          </Button>

          {/* Insert Link */}
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleInsertLink} title="Insert Link">
            <Link className="h-4 w-4" />
          </Button>

          {/* Horizontal Line */}
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={handleInsertLine} title="Horizontal Line">
            <Minus className="h-4 w-4" />
          </Button>

          <Separator orientation="vertical" className="h-6 mx-1" />

          {/* Watermark */}
          <Popover>
            <PopoverTrigger asChild>
              <Button variant="ghost" size="icon" className="h-8 w-8" title="Watermark">
                <Droplets className="h-4 w-4" />
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-3">
              <div className="space-y-2">
                <Input
                  placeholder="Watermark text..."
                  value={watermarkText}
                  onChange={(e) => setWatermarkText(e.target.value)}
                  className="h-8 text-xs"
                />
                <Button size="sm" className="w-full" onClick={handleApplyWatermark}>
                  Apply Watermark
                </Button>
                {showWatermark && (
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="w-full"
                    onClick={() => setShowWatermark(false)}
                  >
                    Remove Watermark
                  </Button>
                )}
              </div>
            </PopoverContent>
          </Popover>
        </div>

        {/* Editor Area */}
        <div className="flex-1 overflow-auto bg-muted/10 p-8 flex justify-center">
          <div className="relative w-full max-w-4xl">
            {/* Watermark Overlay */}
            {showWatermark && watermarkText && (
              <div 
                className="absolute inset-0 flex items-center justify-center pointer-events-none z-10"
                style={{
                  transform: "rotate(-45deg)",
                }}
              >
                <span 
                  className="text-7xl font-bold whitespace-nowrap"
                  style={{ 
                    color: "rgba(0, 0, 0, 0.08)",
                    userSelect: "none",
                  }}
                >
                  {watermarkText}
                </span>
              </div>
            )}

            {/* Editable Content */}
            <div
              ref={setEditorRef}
              contentEditable
              suppressContentEditableWarning
              className="min-h-[600px] bg-white dark:bg-zinc-900 p-12 rounded-lg shadow-lg outline-none prose prose-sm dark:prose-invert max-w-none"
              style={{
                fontFamily: fontFamily,
                fontSize: `${fontSize}px`,
                lineHeight: 1.6,
              }}
              onInput={(e) => setContent((e.target as HTMLDivElement).innerHTML)}
            />
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};
