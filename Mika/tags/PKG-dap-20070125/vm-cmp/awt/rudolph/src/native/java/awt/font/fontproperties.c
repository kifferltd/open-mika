#include <stdio.h>

#ifndef WINNT
#define EOL 10
#define LF  10
#else
#define EOL 13
#define LF  10
#endif

static char*
getInt(char* ptr, int *val) {
    if (*ptr == '*') {
        *val = -1;
        ptr++;
    }
    else {
      if (*ptr == '%' && *(ptr+1) == 'd') {
        *val = -1;
        ptr+=2;
      }
      else {
        for (*val = 0; *ptr >= '0' && *ptr <= '9';) {
          *val = *val * 10 + *ptr++ - '0';
        }
      }
    }
    if (*ptr == '-')
      return ptr;
    else
      return (char *) 0;
}

int
parseXLFDName(char* fname){

  register char *ptr;
  register char *ptr0;
  register char *ptr1;
  register char *ptr2;
  register char *ptr3;
  register char *ptr4;
  register char *ptr5;
  register char *ptr6;
  register char *ptr7;
  register char *ptr8;
  register char *ptr9;
  register char *ptr10;
  register char *ptr11;
  register char *ptr12;
  register char *ptr13;

  int         INT;

  if (!(*(ptr0 = ptr = fname) == '-' || *ptr++ == '*' && *ptr == '-') ||  /* fndry */
          !(ptr1 = ptr = (char*)strchr(ptr + 1, '-')) ||    /* family_name */
          !(ptr2 = ptr = (char*)strchr(ptr + 1, '-')) ||    /* weight_name */
          !(ptr3 = ptr = (char*)strchr(ptr + 1, '-')) ||    /* slant */
          !(ptr4 = ptr = (char*)strchr(ptr + 1, '-')) ||    /* setwidth_name */
          !(ptr5 = ptr = (char*)strchr(ptr + 1, '-')) ||    /* add_style_name */
          !(ptr6 = ptr = (char*)strchr(ptr + 1, '-')) ||    /* pixel_size */
          !(ptr7 = ptr = getInt(ptr + 1, &INT)) ||
          !(ptr8 = ptr = getInt(ptr + 1, &INT)) ||
          !(ptr9 = ptr = getInt(ptr + 1, &INT)) ||          /* resolution_x */
          !(ptr10 = ptr = getInt(ptr + 1, &INT)) ||         /* resolution_y */
          !(ptr11 = ptr = (char*)strchr(ptr + 1, '-')) ||   /* spacing */
          !(ptr12 = ptr = getInt(ptr + 1, &INT)) ||         /* average_width */
          !(ptr13 = ptr = (char*)strchr(ptr + 1, '-')) ||   /* charset_registry */
          (char*)strchr(ptr + 1, '-') )                     /* charset_encoding */ {

    return 0;
  }
  else {
    return 1;
  }
}


/******************************************************************************/
/*
 * read in buffer, the next line(s) of inFile that
 *  - is not completely blank (ignoring possible continuation mark '\'
 *  - does not start with '#' (ignoring leading white spaces and possible '\')
 *  - does not start with line feed (or CRLF for win32)
 * a data line may be continued by a '\' character, in which case the next line
 * is also read in the buffer (skipping LF and '\')
 * the buffer is ended with a '\0' after the last character that was copied to it.
 * On return of the function the file pointer is positioned after the
 * line feed of the last line read.
 * the last character entered in the file must be a LF such that
 * a read after that LF returns EOF
 */
int
readLine(FILE* inFile, char* buffer, int buffSize, int* lineNbr){


  int c;
  int cprev = LF;

  int i = 0;
  int line = 0;
  int err = EOF-1;

  if (inFile == NULL)
    return err;
  if (buffer == NULL)
    return err;

  c = getc(inFile);

  while ((c == ' ' || c == '#' || c == EOL ) && i < buffSize-1  && c != EOF){
    if (c == EOL) {
      i = 0;
      cprev=c;
      c = getc(inFile);
#ifdef WINNT
      if (c == LF) {
        cprev=c;
        c = getc(inFile);
      }
#endif
      if (c != EOF) {
        line++;
      }
    }
    else {
      if (c == '#'){
        cprev=c;
        c = getc(inFile);
        while (c != EOL && c != EOF){
          if (c == '\\'){
            fprintf(stderr, "readLine: warning unexpected character '\\' on line %d\n", *lineNbr+line);
          }
          cprev=c;
          c = getc(inFile);
        }
      }
      else {
        buffer[i++] = c;
        cprev=c;
        c = getc(inFile);
        if (c == '\\'){
          fprintf(stderr, "readLine: warning unexpected character '\\' on line %d\n", *lineNbr+line);
          cprev=c;
          c = getc(inFile);
        }
      }
    }
  }

  while (i < buffSize-1 && c != EOL && c != EOF) {
    buffer[i++] = c;
    cprev=c;
    c = getc(inFile);
    if (c == '#'){
      fprintf(stderr, "readLine: warning unexpected character '#' on line %d\n", *lineNbr+line);
      cprev=c;
      c = getc(inFile);
      while (c != EOL && c != EOF) {
        cprev=c;
        c = getc(inFile);
      }
    }
    if (c == '\\'){
      cprev=c;
      c = getc(inFile);
      while (c != EOL && c != EOF) {
        cprev=c;
        c = getc(inFile);
      }
      if (c == EOL) {
        cprev=c;
        c = getc(inFile);
#ifdef WINNT
        if (c == LF) {
          cprev=c;
          c = getc(inFile);
        }
#endif
      }
      if (c != EOF){
        line++;
      }
    }
  }

  if (i == buffSize-1 && c != EOL && c != EOF){
    fprintf(stderr, "readLine: inputbuffer full reading line %d\n", *lineNbr+line);
    while (c != EOL && c != EOF) {   //inputbuffer full, rest of line ignored
      cprev=c;
      c = getc(inFile);
    }
  }

#ifdef WINNT
  if (c == EOL){
    cprev=c;
    c = getc(inFile);
  }
#endif

  buffer[i] = '\0';
  *lineNbr += line;
  if (c == EOF){
    if (cprev != LF) {
      fprintf(stderr,"missing newline at end of file\n");
      return err;
    }
    else
      return EOF;
  }
  else
    return 1;

}


/******************************************************************************/
int
readFontsDir(char* pathName){

  int c;
  int err = EOF-1;
  int i, nbrLines;
  char buff[1024];
  char left[512];
  char right[512];

  int line;
  char* p;
  char* pe;
  char* q;
  char* qe;

  FILE *fontsDirFile;

  if ((fontsDirFile = fopen(pathName, "r")) == NULL)
    return err;

  line = 1;
  c = readLine(fontsDirFile, buff, 1024, &line);
  if (c >= 0 && c != EOF) {
    p = buff;
    nbrLines = 0;
    while (*p != '\0'){
      if (!(*p >= '0' && *p <= '9')) {
        fprintf(stderr, "readFontsDir: illegal character in count string, file %s, line %d\n", pathName, line);
        return err;
      }
      nbrLines = nbrLines * 10 + *p++ - '0';
    }
  }

printf("%3d: %d\n", line, nbrLines);

  line++;
  c = readLine(fontsDirFile, buff, 1024, &line);
  i = 0;
  while (i < nbrLines && c >= 0 && c != EOF) {
//printf("%3d: %s@\n", line, buff);
    q = buff;
    while (*q == ' '){ q++;}
    p = (char*)strchr(q, '-');
    while (p != NULL && *(p-1) != ' ') {
      p = (char*)strchr(p+1, '-');
    }
    if (p == NULL || p == q || *(p-1) != ' '){
      fprintf(stderr, "readFontsDir: file %s, line %d, no xlfd found; line ignored\n", pathName, line);
    }
    else {
      if (!parseXLFDName(p)){
        fprintf(stderr, "readFontsDir: file %s, line %d, illegal xlfd name; line ignored\n", pathName, line);
      }
      else{
        qe = p-1;
        while (*qe == ' '){qe--;}
        *(qe+1) = '\0';
        pe = (char*)strchr(p, '\0');
        pe--;
        while (*pe == ' '){pe--;}
        *(pe+1) = '\0';
        strcpy(left, q);
        strcpy(right,p);
        i++;
printf("%3d@%s@%s@\n", line, left, right);
      }
    }
    line++;
    c = readLine(fontsDirFile, buff, 1024, &line);
  }

  if (i < nbrLines){
    fprintf(stderr, "readFontsDir: %d font definition(s) missing in file %s\n", nbrLines-i, pathName);
    return err;
  }

  if (c < 0 && c != EOF)
    return c;

  if (fclose(fontsDirFile) == EOF)
    return EOF;

  return 0;
}

/******************************************************************************/
int
readFontProperties(char* pathName){


  int c;

  char buff[1024];
  char left[512];
  char right[512];

  int line;
  char* p;

  FILE *fontPropsFile;

  if ((fontPropsFile = fopen(pathName, "r")) == NULL)
    return -1;


  line = 1;
  c = readLine(fontPropsFile, buff, 1024, &line);
  while (c >= 0 && c != EOF) {
//printf("%3d: %s\n", line, buff);
    p = (char*)strchr(buff, '=');
    if (p == NULL){
      fprintf(stderr, "syntax error in file %s, line %d: no '=' character found. ignoring line.\n", pathName, line);
    }
    else {
      *p = '\0';
      strcpy(left, buff);
      strcpy(right, p+1);
printf("%3d: %s=%s\n", line, left, right);
    }
    line++;
    c = readLine(fontPropsFile, buff, 1024, &line);
  }


  if (fclose(fontPropsFile) == EOF)
    return EOF;
  if (c < 0 && c != EOF)
    return c;
  return 0;
}

/****** test ************************************************************************/
/*
int
main(int argc, char** argv){

  if (argc != 2){
    fprintf(stderr,"usage : %s <fonts.dir> <font.properties>\n", argv[0]);
  }
  else{
    printf("processing file %s\n", argv[1]);
    printf("return of readFontsDir : %d\n\n", readFontsDir(argv[1]));

//    printf("processing file %s\n", argv[2]);
//    printf("return of readFontProperties : %d\n", readFontProperties(argv[2]));
  }
}

*/