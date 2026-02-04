export type FileType = 'image' | 'document' | 'spreadsheet' | 'presentation' | 'video' | 'audio' | '3d' | 'apk' | 'folder';

export interface FileItem {
  id: string;
  name: string;
  type: FileType;
  thumbnail?: string;
  url?: string;
  starred?: boolean;
  size?: string;
  modifiedAt?: Date;
  albumId?: string;
  deletedAt?: Date;
  editedContent?: string; // Stores edited HTML content for documents
}

export interface Album {
  id: string;
  name: string;
  icon?: string;
  fileCount: number;
}

export interface QuickAccessItem {
  id: string;
  name: string;
  type: FileType;
}

export interface Folder {
  id: string;
  name: string;
  parentId?: string;
}

export interface FileBreakdown {
  type: FileType;
  label: string;
  count: number;
}
