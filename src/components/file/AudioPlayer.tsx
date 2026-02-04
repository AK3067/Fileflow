import { useState, useRef, useEffect } from "react";
import { FileItem } from "@/types/file";
import { FileTypeIcon } from "@/components/icons/FileTypeIcon";
import { Play, Pause, SkipBack, SkipForward, Volume2, VolumeX } from "lucide-react";
import { Slider } from "@/components/ui/slider";

interface AudioPlayerProps {
  audioSrc: string;
  file: FileItem;
}

const formatTime = (seconds: number) => {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins}:${secs.toString().padStart(2, "0")}`;
};

export const AudioPlayer = ({ audioSrc, file }: AudioPlayerProps) => {
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [volume, setVolume] = useState(1);
  const [isMuted, setIsMuted] = useState(false);
  const audioRef = useRef<HTMLAudioElement>(null);

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;

    const updateTime = () => setCurrentTime(audio.currentTime);
    const updateDuration = () => setDuration(audio.duration);
    const handleEnded = () => setIsPlaying(false);

    audio.addEventListener("timeupdate", updateTime);
    audio.addEventListener("loadedmetadata", updateDuration);
    audio.addEventListener("ended", handleEnded);

    return () => {
      audio.removeEventListener("timeupdate", updateTime);
      audio.removeEventListener("loadedmetadata", updateDuration);
      audio.removeEventListener("ended", handleEnded);
    };
  }, []);

  const togglePlay = () => {
    const audio = audioRef.current;
    if (!audio) return;

    if (isPlaying) {
      audio.pause();
    } else {
      audio.play();
    }
    setIsPlaying(!isPlaying);
  };

  const handleSeek = (value: number[]) => {
    const audio = audioRef.current;
    if (!audio) return;
    audio.currentTime = value[0];
    setCurrentTime(value[0]);
  };

  const handleVolumeChange = (value: number[]) => {
    const audio = audioRef.current;
    if (!audio) return;
    const newVolume = value[0];
    audio.volume = newVolume;
    setVolume(newVolume);
    setIsMuted(newVolume === 0);
  };

  const toggleMute = () => {
    const audio = audioRef.current;
    if (!audio) return;
    if (isMuted) {
      audio.volume = volume || 1;
      setIsMuted(false);
    } else {
      audio.volume = 0;
      setIsMuted(true);
    }
  };

  const skip = (seconds: number) => {
    const audio = audioRef.current;
    if (!audio) return;
    audio.currentTime = Math.max(0, Math.min(audio.currentTime + seconds, duration));
  };

  return (
    <div className="w-full max-w-lg bg-card border border-border rounded-2xl p-6 flex flex-col items-center gap-5 shadow-xl">
      {/* Hidden audio element */}
      <audio ref={audioRef} src={audioSrc} />

      {/* Rotating disc */}
      <div 
        className={`w-36 h-36 rounded-full bg-gradient-to-br from-zinc-900 via-zinc-700 to-zinc-900 flex items-center justify-center shadow-xl relative overflow-hidden ${
          isPlaying ? "animate-spin" : ""
        }`}
        style={{ 
          animationDuration: isPlaying ? "3s" : "0s",
          animationTimingFunction: "linear",
          animationIterationCount: "infinite"
        }}
      >
        {/* Vinyl grooves effect */}
        <div className="absolute inset-2 rounded-full border border-zinc-600/30" />
        <div className="absolute inset-4 rounded-full border border-zinc-600/20" />
        <div className="absolute inset-6 rounded-full border border-zinc-600/30" />
        <div className="absolute inset-8 rounded-full border border-zinc-600/20" />
        
        {/* Center label/icon */}
        <div className="w-14 h-14 rounded-full bg-gradient-to-br from-primary/30 to-primary/10 flex items-center justify-center z-10 border-2 border-primary/20">
          {file.thumbnail ? (
            <img 
              src={file.thumbnail} 
              alt={file.name} 
              className="w-full h-full rounded-full object-cover"
            />
          ) : (
            <FileTypeIcon type={file.type} size={28} />
          )}
        </div>
      </div>

      {/* Track title */}
      <p className="text-sm font-medium text-foreground truncate max-w-full">{file.name}</p>

      {/* Playback controls - centered */}
      <div className="flex items-center gap-4">
        <button
          onClick={() => skip(-10)}
          className="p-2 rounded-full hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
        >
          <SkipBack size={20} />
        </button>
        
        <button
          onClick={togglePlay}
          className="w-14 h-14 rounded-full bg-primary text-primary-foreground flex items-center justify-center shadow-lg hover:bg-primary/90 transition-all hover:scale-105"
        >
          {isPlaying ? <Pause size={24} /> : <Play size={24} className="ml-1" />}
        </button>
        
        <button
          onClick={() => skip(10)}
          className="p-2 rounded-full hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
        >
          <SkipForward size={20} />
        </button>
      </div>

      {/* Timeline at bottom */}
      <div className="w-full flex flex-col gap-2">
        <Slider
          value={[currentTime]}
          max={duration || 100}
          step={0.1}
          onValueChange={handleSeek}
          className="w-full cursor-pointer"
        />
        <div className="flex justify-between text-xs text-muted-foreground">
          <span>{formatTime(currentTime)}</span>
          <span>{formatTime(duration)}</span>
        </div>
      </div>

      {/* Volume control */}
      <div className="flex items-center gap-2 w-full max-w-[180px]">
        <button
          onClick={toggleMute}
          className="p-1.5 rounded-full hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
        >
          {isMuted ? <VolumeX size={18} /> : <Volume2 size={18} />}
        </button>
        <Slider
          value={[isMuted ? 0 : volume]}
          max={1}
          step={0.01}
          onValueChange={handleVolumeChange}
          className="flex-1 cursor-pointer"
        />
      </div>
    </div>
  );
};
