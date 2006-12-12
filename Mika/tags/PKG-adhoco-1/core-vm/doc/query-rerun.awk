BEGIN {
  rerun = 0;
}

/LaTeX Warning/{
  if ($3 == "Label(s)" && $7 == "Rerun") {
    printf("Rerunning LaTeX to get labels right...\n");
    rerun = 1;
  }
}

END {
  if (rerun == 1) {
    exit(0);
  }
  else {
    exit(1);
  }
}