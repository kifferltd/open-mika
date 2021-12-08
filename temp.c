static void dumpDir(const char *path, int level) {
  woempa(7, "%*sScanning directory %s", level * 4, "", path);
  FF_FindData_t *findData = allocMem(sizeof(FF_FindData_t));
  int rc = ff_findfirst(path, findData );
  while (rc == 0) {
    woempa(7, "%*s%s [%s %s] [size=%d]", level * 4, "", findData->pcFileName, (findData->ucAttributes & FF_FAT_ATTR_DIR) ? "DIR" : "", (findData->ucAttributes && FF_FAT_ATTR_DIR) ? "RO" : "", findData->ulFileSize);
    if ((findData->ucAttributes & FF_FAT_ATTR_DIR) && strcmp(findData->pcFileName, ".") && strcmp(findData->pcFileName, "..")) {
      char *pathbuf = allocMem(strlen(path) + strlen(findData->pcFileName) + 2);
      sprintf(pathbuf, "%s%s/", path, findData->pcFileName);
      dumpDir(pathbuf, level + 1);
      releaseMem(pathbuf);
    }
    rc =  ff_findnext( findData ) == 0;
  }
  releaseMem(findData);
  woempa(7, "%*sEnd of directory %s", level * 4, "", path);
}

