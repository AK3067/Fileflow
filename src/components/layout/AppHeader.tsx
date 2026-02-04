import { useState } from "react";
import { Search, Upload, X, PanelLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

interface AppHeaderProps {
  onSearch: (query: string) => void;
  onToggleSidebar: () => void;
  isSidebarOpen: boolean;
}

export const AppHeader = ({ onSearch, onToggleSidebar, isSidebarOpen }: AppHeaderProps) => {
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [isLogoBroken, setIsLogoBroken] = useState(false);

  const handleSearchChange = (value: string) => {
    setSearchQuery(value);
    onSearch(value);
  };

  const clearSearch = () => {
    setSearchQuery("");
    onSearch("");
    setIsSearchOpen(false);
  };

  return (
    <header className="h-16 bg-card border-b border-border flex items-center justify-between px-4 gap-4">
      {/* Left: Toggle + Logo */}
      <div className="flex items-center gap-3">
        <Button
          variant="ghost"
          size="icon"
          onClick={onToggleSidebar}
          className="text-muted-foreground hover:text-foreground transition-colors"
        >
          <PanelLeft size={20} />
        </Button>
        <div className="flex items-center gap-2">
          {!isLogoBroken ? (
            <img
              src="/logo.png"
              alt="FileFlow logo"
              className="w-8 h-8 rounded-lg object-cover"
              onError={() => setIsLogoBroken(true)}
              draggable={false}
            />
          ) : (
            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
              <div className="w-4 h-4 border-2 border-primary-foreground rounded-sm relative">
                <div className="absolute inset-0.5 border border-primary-foreground rounded-[2px]"></div>
              </div>
            </div>
          )}
          <span className="font-semibold text-lg text-foreground">FileFlow</span>
        </div>
      </div>

      {/* Right: Search + Upload */}
      <div className="flex items-center gap-3">
        <div
          className={cn(
            "flex items-center transition-all duration-300 ease-out overflow-hidden",
            isSearchOpen ? "w-64" : "w-auto"
          )}
        >
          {isSearchOpen ? (
            <div className="flex items-center gap-2 w-full animate-fade-in">
              <div className="relative flex-1">
                <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
                <Input
                  type="text"
                  placeholder="Search files..."
                  value={searchQuery}
                  onChange={(e) => handleSearchChange(e.target.value)}
                  className="pl-9 pr-8 h-9 bg-secondary border-none focus-visible:ring-1 focus-visible:ring-primary"
                  autoFocus
                />
                {searchQuery && (
                  <button
                    onClick={clearSearch}
                    className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                  >
                    <X size={14} />
                  </button>
                )}
              </div>
              <button
                onClick={() => setIsSearchOpen(false)}
                className="text-muted-foreground hover:text-foreground transition-colors"
              >
                <X size={18} />
              </button>
            </div>
          ) : (
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setIsSearchOpen(true)}
              className="text-muted-foreground hover:text-foreground transition-colors"
            >
              <Search size={20} />
            </Button>
          )}
        </div>

        <Button className="gap-2 bg-primary hover:bg-primary/90 text-primary-foreground shadow-soft transition-all hover:shadow-medium">
          <Upload size={16} />
          <span className="font-medium">Upload File</span>
        </Button>
      </div>
    </header>
  );
};
