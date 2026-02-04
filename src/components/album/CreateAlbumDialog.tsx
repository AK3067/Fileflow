import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface CreateAlbumDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onCreateAlbum: (name: string) => void;
  mode?: "create" | "rename";
  initialName?: string;
}

export const CreateAlbumDialog = ({
  open,
  onOpenChange,
  onCreateAlbum,
  mode = "create",
  initialName = "",
}: CreateAlbumDialogProps) => {
  const [albumName, setAlbumName] = useState(initialName);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (albumName.trim()) {
      onCreateAlbum(albumName.trim());
      setAlbumName("");
      onOpenChange(false);
    }
  };

  const handleOpenChange = (open: boolean) => {
    if (open) {
      setAlbumName(initialName);
    }
    onOpenChange(open);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-[400px] animate-scale-in">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>
              {mode === "create" ? "Create New Album" : "Rename Album"}
            </DialogTitle>
            <DialogDescription>
              {mode === "create"
                ? "Enter a name for your new album."
                : "Enter a new name for the album."}
            </DialogDescription>
          </DialogHeader>
          <div className="py-4">
            <Label htmlFor="album-name" className="text-sm font-medium">
              Album Name
            </Label>
            <Input
              id="album-name"
              value={albumName}
              onChange={(e) => setAlbumName(e.target.value)}
              placeholder="My Album"
              className="mt-2"
              autoFocus
            />
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={!albumName.trim()}>
              {mode === "create" ? "Create" : "Rename"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
