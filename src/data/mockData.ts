import { FileItem, Album, QuickAccessItem, Folder, FileBreakdown } from "@/types/file";

export const mockAlbums: Album[] = [
  { id: "ak", name: "AK", fileCount: 12 },
];

export const mockQuickAccess: QuickAccessItem[] = [
  { id: "qa1", name: "Financial-Report-Q2.xlsx", type: "spreadsheet" },
];

export const mockFiles: FileItem[] = [
  { id: "1", name: "3D Model.obj", type: "3d", size: "12.4 MB" },
  { id: "2", name: "Project Brief.docx", type: "document", size: "245 KB" },
  { id: "3", name: "Sales Presentation.pptx", type: "presentation", size: "5.2 MB" },
  { id: "4", name: "Budget 2024.xlsx", type: "spreadsheet", starred: true, size: "1.1 MB" },
  { id: "5", name: "Design Specs.docx", type: "document", size: "320 KB" },
  { id: "6", name: "Iceland Cliff.jpg", type: "image", thumbnail: "https://images.unsplash.com/photo-1490108108823-dea4e4a59d0c?w=400&h=300&fit=crop", size: "2.3 MB" },
  { id: "7", name: "Product Demo.mp4", type: "video", size: "45.8 MB", url: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" },
  { id: "8", name: "Winter Bench.jpg", type: "image", thumbnail: "https://images.unsplash.com/photo-1516655855035-d5215bcb5604?w=400&h=300&fit=crop", size: "1.8 MB" },
  { id: "9", name: "Background Music.mp3", type: "audio", size: "4.2 MB", url: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3" },
];

export const mockFolders: Folder[] = [
  { id: "android", name: "Android" },
  { id: "dcim", name: "DCIM" },
  { id: "documents", name: "Documents" },
  { id: "download", name: "Download" },
  { id: "movies", name: "Movies" },
  { id: "music", name: "Music" },
];

export const mockBreakdown: FileBreakdown[] = [
  { type: "image", label: "Image", count: 2 },
  { type: "document", label: "Document", count: 2 },
  { type: "spreadsheet", label: "Spreadsheet", count: 1 },
  { type: "presentation", label: "Presentation", count: 1 },
  { type: "video", label: "Video", count: 1 },
  { type: "audio", label: "Audio", count: 1 },
  { type: "apk", label: "Apk", count: 1 },
];
