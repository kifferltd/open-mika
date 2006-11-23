#
# $Id: cvs-tex.awk,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
#

BEGIN {

  month2name[0]  = "(unchecked)";
  month2name[1]  = "January";
  month2name[2]  = "February";
  month2name[3]  = "March";
  month2name[4]  = "April";
  month2name[5]  = "May";
  month2name[6]  = "June";
  month2name[7]  = "July";
  month2name[8]  = "August";
  month2name[9]  = "September";
  month2name[10] = "October";
  month2name[11] = "November";
  month2name[12] = "December";

  printf("\\lhead[\\small \\thechapter. Version Information]{\\small \\textsc{Acunia}}\n");
  printf("\\chead[]{}\n");
  printf("\\rhead[{\\footnotesize\\bfseries \\textsc{Oswald}}]{{\\footnotesize\\bfseries \\textsc{Oswald}}}\n");
  printf("\\chapter{Version Information}\n\n");
  printf("This document is made up from different files, of which the versions,\n");
  printf("the date of the last change and the author that committed the last change\n");
  printf("can be found back in table \\ref{versioninformation}.\n\n");
  printf("\n");
  printf("Each file contains a chapter of this document and has a corresponding source file.\n");
  printf("\n");
  printf("\\begin{table}[h]\n");
  printf("  \\begin{center}\n");
  printf("    \\begin{tabular}{|| l | c | l | l ||} \\hline\n");
  printf("      \\textbf{File} & \\textbf{Version} & \\textbf{Committed} & \\textbf{Author}  \\\\\n");
  printf("      \\hline\n");

}

function datemangle(date) {

  year = substr(date, 0, 4);
  month = substr(date, 6, 2);
  month += 0;
  day = substr(date, 9, 2);

  result = month2name[month]" "day", "year;

  return result;

}

/^\%[ ]*\$Id/ {
  file = $3;
  if (file == "") {
    file = FILENAME;
  }
  gsub(",v", "", file);
  version = $4;
  if (version == "") {
    version = "(unchecked)";
  }
  date = $5;
  if (date == "") {
    date = "2001/00/00";
  }
  date = datemangle(date);
  author = $7;
  if (author == "") {
    author = "(unchecked)";
  }

  printf("        %20s & %5s & %20s & %15s \\\\\n", file, version, date, author);

}

END {
  printf("      \\hline\n");
  printf("    \\end{tabular}\n");
  printf("    \\caption{File version information}\n");
  printf("    \\label{versioninformation}\n");
  printf("  \\end{center}\n");
  printf("\\end{table}\n");
}