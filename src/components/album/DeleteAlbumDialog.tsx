import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

interface DeleteAlbumDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  albumName: string;
  onConfirm: () => void;
}

export const DeleteAlbumDialog = ({
  open,
  onOpenChange,
  albumName,
  onConfirm,
}: DeleteAlbumDialogProps) => {
  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent className="animate-scale-in">
        <AlertDialogHeader>
          <AlertDialogTitle>Delete Album</AlertDialogTitle>
          <AlertDialogDescription>
            Are you sure you want to delete "{albumName}"? This action cannot be
            undone. Files in this album will not be deleted.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Cancel</AlertDialogCancel>
          <AlertDialogAction
            onClick={onConfirm}
            className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
          >
            Delete
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};
