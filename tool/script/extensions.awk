BEGIN {
  delete extensions; split(EXTENSIONS, extensions, " ")

  for (i in extensions) {
    ext = extensions[i]
    split(ext, bits, "_")
    csc = ""
    for (j in bits) csc = csc toupper(substr(bits[j],1,1)) substr(bits[j],2)
    extensions[i] = csc
    printf "extern void collect%sFixups();\n", csc
    printf "extern void load%sClasses();\n", csc
  }

  print ""
  print "void init_extensions(void) {"
  for (i in extensions) {
    printf "  collect%sFixups();\n", extensions[i]
    printf "  load%sClasses();\n", extensions[i]
  }
  print "}"
}
