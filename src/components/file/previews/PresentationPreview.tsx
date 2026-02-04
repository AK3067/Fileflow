import { useState } from "react";
import { FileItem } from "@/types/file";
import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight } from "lucide-react";

interface PresentationPreviewProps {
  file: FileItem;
}

interface Slide {
  title: string;
  content: string[];
  type: "title" | "content" | "bullets" | "image";
}

// Mock presentation slides
const mockPresentations: Record<string, Slide[]> = {
  "Sales Presentation.pptx": [
    {
      title: "Q4 Sales Review",
      content: ["Annual Performance Overview", "December 2024"],
      type: "title",
    },
    {
      title: "Key Highlights",
      content: [
        "Revenue increased by 25% YoY",
        "New customer acquisition up 40%",
        "Customer retention rate: 92%",
        "Market share grew to 18%",
      ],
      type: "bullets",
    },
    {
      title: "Revenue Breakdown",
      content: [
        "Enterprise: $2.4M (45%)",
        "Mid-Market: $1.8M (34%)",
        "SMB: $1.1M (21%)",
        "Total: $5.3M",
      ],
      type: "bullets",
    },
    {
      title: "Top Performing Products",
      content: [
        "Product A: 156 units sold",
        "Product B: 142 units sold",
        "Product C: 98 units sold",
        "Product D: 87 units sold",
      ],
      type: "bullets",
    },
    {
      title: "Regional Performance",
      content: [
        "North America: +32%",
        "Europe: +28%",
        "Asia Pacific: +45%",
        "Latin America: +19%",
      ],
      type: "bullets",
    },
    {
      title: "Q1 2025 Goals",
      content: [
        "Increase revenue by 15%",
        "Launch 2 new product lines",
        "Expand into 3 new markets",
        "Hire 20 new sales representatives",
      ],
      type: "bullets",
    },
    {
      title: "Thank You",
      content: ["Questions?", "Contact: sales@company.com"],
      type: "title",
    },
  ],
};

const defaultSlides: Slide[] = [
  {
    title: "Presentation Title",
    content: ["Subtitle goes here", "Author Name"],
    type: "title",
  },
  {
    title: "Slide 2",
    content: [
      "First bullet point",
      "Second bullet point",
      "Third bullet point",
    ],
    type: "bullets",
  },
  {
    title: "Slide 3",
    content: [
      "Additional content here",
      "More information",
      "Final points",
    ],
    type: "bullets",
  },
];

export const PresentationPreview = ({ file }: PresentationPreviewProps) => {
  const slides = mockPresentations[file.name] || defaultSlides;
  const [currentSlide, setCurrentSlide] = useState(0);

  const goToPrev = () => {
    if (currentSlide > 0) setCurrentSlide(currentSlide - 1);
  };

  const goToNext = () => {
    if (currentSlide < slides.length - 1) setCurrentSlide(currentSlide + 1);
  };

  const slide = slides[currentSlide];

  return (
    <div className="w-full max-w-4xl">
      {/* Slide Display */}
      <div className="bg-gradient-to-br from-primary/10 via-background to-secondary/10 border border-border rounded-xl shadow-lg aspect-video flex flex-col items-center justify-center p-12 relative overflow-hidden">
        {/* Decorative elements */}
        <div className="absolute top-0 left-0 w-32 h-32 bg-primary/5 rounded-full -translate-x-1/2 -translate-y-1/2" />
        <div className="absolute bottom-0 right-0 w-48 h-48 bg-secondary/5 rounded-full translate-x-1/4 translate-y-1/4" />
        
        {slide.type === "title" ? (
          <div className="text-center z-10">
            <h1 className="text-4xl md:text-5xl font-bold mb-6 bg-gradient-to-r from-primary to-primary/70 bg-clip-text text-transparent">
              {slide.title}
            </h1>
            {slide.content.map((line, i) => (
              <p key={i} className="text-xl text-muted-foreground">
                {line}
              </p>
            ))}
          </div>
        ) : (
          <div className="w-full z-10">
            <h2 className="text-3xl font-bold mb-8 text-foreground">{slide.title}</h2>
            <ul className="space-y-4">
              {slide.content.map((item, i) => (
                <li key={i} className="flex items-start gap-3 text-lg">
                  <span className="w-2 h-2 rounded-full bg-primary mt-2.5 shrink-0" />
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {/* Navigation Controls */}
      <div className="flex items-center justify-between mt-4 px-2">
        <Button
          variant="outline"
          size="sm"
          onClick={goToPrev}
          disabled={currentSlide === 0}
        >
          <ChevronLeft size={18} className="mr-1" />
          Previous
        </Button>
        
        <div className="flex items-center gap-2">
          {slides.map((_, i) => (
            <button
              key={i}
              onClick={() => setCurrentSlide(i)}
              className={`w-2.5 h-2.5 rounded-full transition-colors ${
                i === currentSlide ? "bg-primary" : "bg-muted-foreground/30 hover:bg-muted-foreground/50"
              }`}
            />
          ))}
        </div>

        <Button
          variant="outline"
          size="sm"
          onClick={goToNext}
          disabled={currentSlide === slides.length - 1}
        >
          Next
          <ChevronRight size={18} className="ml-1" />
        </Button>
      </div>

      <p className="text-center text-sm text-muted-foreground mt-2">
        Slide {currentSlide + 1} of {slides.length}
      </p>
    </div>
  );
};
