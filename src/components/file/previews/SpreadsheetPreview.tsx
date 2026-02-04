import { FileItem } from "@/types/file";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

interface SpreadsheetPreviewProps {
  file: FileItem;
}

// Mock spreadsheet data
const mockSpreadsheetData: Record<string, { headers: string[]; rows: string[][] }> = {
  "Budget 2024.xlsx": {
    headers: ["Category", "Q1", "Q2", "Q3", "Q4", "Total"],
    rows: [
      ["Marketing", "$25,000", "$30,000", "$28,000", "$35,000", "$118,000"],
      ["Development", "$80,000", "$85,000", "$90,000", "$95,000", "$350,000"],
      ["Operations", "$45,000", "$45,000", "$48,000", "$50,000", "$188,000"],
      ["Sales", "$35,000", "$40,000", "$42,000", "$45,000", "$162,000"],
      ["HR", "$20,000", "$22,000", "$22,000", "$25,000", "$89,000"],
      ["Infrastructure", "$15,000", "$18,000", "$20,000", "$22,000", "$75,000"],
      ["Research", "$30,000", "$32,000", "$35,000", "$38,000", "$135,000"],
      ["Training", "$8,000", "$10,000", "$10,000", "$12,000", "$40,000"],
      ["Total", "$258,000", "$282,000", "$295,000", "$322,000", "$1,157,000"],
    ],
  },
  "Financial-Report-Q2.xlsx": {
    headers: ["Month", "Revenue", "Expenses", "Profit", "Growth %"],
    rows: [
      ["April", "$125,000", "$98,000", "$27,000", "12%"],
      ["May", "$142,000", "$105,000", "$37,000", "14%"],
      ["June", "$158,000", "$112,000", "$46,000", "11%"],
      ["Q2 Total", "$425,000", "$315,000", "$110,000", "12.3%"],
    ],
  },
};

const defaultData = {
  headers: ["Column A", "Column B", "Column C", "Column D", "Column E"],
  rows: [
    ["Data 1", "Value 1", "100", "Active", "2024-01-15"],
    ["Data 2", "Value 2", "250", "Pending", "2024-02-20"],
    ["Data 3", "Value 3", "175", "Active", "2024-03-10"],
    ["Data 4", "Value 4", "320", "Inactive", "2024-04-05"],
    ["Data 5", "Value 5", "89", "Active", "2024-05-18"],
    ["Data 6", "Value 6", "445", "Pending", "2024-06-22"],
    ["Data 7", "Value 7", "210", "Active", "2024-07-30"],
    ["Data 8", "Value 8", "156", "Active", "2024-08-12"],
  ],
};

export const SpreadsheetPreview = ({ file }: SpreadsheetPreviewProps) => {
  const data = mockSpreadsheetData[file.name] || defaultData;

  return (
    <div className="w-full max-w-4xl bg-card border border-border rounded-xl shadow-lg overflow-hidden">
      <div className="bg-muted/50 px-6 py-3 border-b border-border flex items-center justify-between">
        <h3 className="font-medium text-sm">{file.name}</h3>
        <span className="text-xs text-muted-foreground">
          {data.rows.length} rows × {data.headers.length} columns
        </span>
      </div>
      <ScrollArea className="h-[60vh]">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/30">
              <TableHead className="w-12 text-center font-bold text-muted-foreground">#</TableHead>
              {data.headers.map((header, i) => (
                <TableHead key={i} className="font-semibold">
                  {header}
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {data.rows.map((row, rowIndex) => (
              <TableRow key={rowIndex} className={rowIndex % 2 === 0 ? "bg-background" : "bg-muted/20"}>
                <TableCell className="text-center text-muted-foreground font-mono text-xs">
                  {rowIndex + 1}
                </TableCell>
                {row.map((cell, cellIndex) => (
                  <TableCell key={cellIndex} className="font-mono text-sm">
                    {cell}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </ScrollArea>
    </div>
  );
};
