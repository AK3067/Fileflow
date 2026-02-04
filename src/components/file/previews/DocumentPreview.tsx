import { FileItem } from "@/types/file";
import { ScrollArea } from "@/components/ui/scroll-area";

interface DocumentPreviewProps {
  file: FileItem;
}

// Mock document content for demo
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

export const DocumentPreview = ({ file }: DocumentPreviewProps) => {
  const content = mockDocumentContent[file.name] || `Document: ${file.name}

This is a preview of the document content.

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.

Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.

Section 1: Introduction
The purpose of this document is to provide an overview of the project requirements and specifications.

Section 2: Requirements
- Requirement 1: System must handle 1000 concurrent users
- Requirement 2: Response time under 200ms
- Requirement 3: 99.9% uptime guarantee

Section 3: Conclusion
This document serves as the foundation for the project development phase.`;

  return (
    <div className="w-full max-w-3xl bg-card border border-border rounded-xl shadow-lg overflow-hidden">
      <div className="bg-muted/50 px-6 py-3 border-b border-border">
        <h3 className="font-medium text-sm">{file.name}</h3>
      </div>
      <ScrollArea className="h-[60vh]">
        <div className="p-8 whitespace-pre-wrap font-mono text-sm leading-relaxed">
          {content}
        </div>
      </ScrollArea>
    </div>
  );
};
