/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: vsprintf.c,v 1.3 2006/02/17 10:53:19 cvs Exp $
*/

#include <stdarg.h>
#include <string.h>
#include <stdio.h>
#include <ctype.h>

#include "oswald.h"
#include "vsprintf.h"

/*
** Custom specifier array
*/

static x_fcb formatters[] = {
  NULL, /*  0 'A' */
  NULL, /*  1 'B' */
  NULL, /*  2 'C' */
  NULL, /*  3 'D' */
  NULL, /*  4 'E' */
  NULL, /*  5 'F' */
  NULL, /*  6 'G' */
  NULL, /*  7 'H' */
  NULL, /*  8 'I' */
  NULL, /*  9 'J' */
  NULL, /* 10 'K' */
  NULL, /* 11 'L' */
  NULL, /* 12 'M' */
  NULL, /* 13 'N' */
  NULL, /* 14 'O' */
  NULL, /* 15 'P' */
  NULL, /* 16 'Q' */
  NULL, /* 17 'R' */
  NULL, /* 18 'S' */
  NULL, /* 19 'T' */
  NULL, /* 20 'U' */
  NULL, /* 21 'V' */
  NULL, /* 22 'W' */
  NULL, /* 23 'X' */
  NULL, /* 24 'Y' */
  NULL, /* 25 'Z' */
  NULL, /* 26 ' ' */
  NULL, /* 27 ' ' */
  NULL, /* 28 ' ' */
  NULL, /* 29 ' ' */
  NULL, /* 30 ' ' */
  NULL, /* 31 ' ' */
  NULL, /* 32 'a' */
  NULL, /* 33 'b' */
  NULL, /* 34 'c' */
  NULL, /* 35 'd' */
  NULL, /* 36 'e' */
  NULL, /* 37 'f' */
  NULL, /* 38 'g' */
  NULL, /* 39 'h' */
  NULL, /* 40 'i' */
  NULL, /* 41 'j' */
  NULL, /* 42 'k' */
  NULL, /* 43 'l' */
  NULL, /* 44 'm' */
  NULL, /* 45 'n' */
  NULL, /* 46 'o' */
  NULL, /* 47 'p' */
  NULL, /* 48 'q' */
  NULL, /* 49 'r' */
  NULL, /* 50 's' */
  NULL, /* 51 't' */
  NULL, /* 52 'u' */
  NULL, /* 53 'v' */
  NULL, /* 54 'w' */
  NULL, /* 55 'x' */
  NULL, /* 56 'y' */
  NULL, /* 57 'z' */
};

#define TRUE 1
#define FALSE 0

x_boolean x_formatter(x_int specifier, x_fcb fcb) {

  x_int idx = specifier - 65; /* A = 65 Z = 90 a = 97 z = 122 */

  if (idx > 0 && idx < 58) {  
    formatters[idx] = fcb;
    return TRUE;
  }
  
  return FALSE;

}

/* 
** We use this so that we can do without the ctype library 
*/

inline static int is_digit(int c) {
  return (c >= '0' && c <= '9');
}

inline static int skip_atoi(const char **s) {

  x_int i = 0;

  while (is_digit(**s)) {
    i = (i * 10) + *((*s)++) - '0';
  }
  
  return i;

}

/*
** The different flag BITS
*/

#define FMT_PAD_ZERO   0x00000001 /* Pad field with zeros. */
#define FMT_SIGN       0x00000002 /*    */
#define FMT_PLUS       0x00000004 /* Show the sign, even if positive. */
#define FMT_SPACE      0x00000008 /* space if plus */
#define FMT_ALIGN_LEFT 0x00000010 /* left justified */
#define FMT_SPECIAL    0x00000020 /*    */
#define FMT_CAPITALS   0x00000040 /* Print hexadecimal in capital characters. */
#define FMT_LONG       0x00000200 /* Argument is a long argument. */

#define DO_DIV(n, base) ({                                   \
  int __res;                                                 \
  __res = ((unsigned long long) n) % (unsigned) base;        \
  n = ((unsigned long long) n) / (unsigned) base;            \
  __res; })

#define ADD_CHAR(buf, remain, c)                             \
if ((remain) > 1) {                                          \
  *(buf)++ = (c);                                            \
  (remain)--;                                                \
}

static char * number(char * str, x_size * bufsize, x_long arg_num, int base, int size, int precision, x_flags flags);

x_int x_vsnprintf(char * buf, x_size bufsize, const char *fmt, va_list args) {

  x_int len;
  x_long num;
  x_int i;
  x_int base;
  char * str;
  const char *s;
  x_flags flags;
  x_int field_width;
  x_int precision;		/* min. # of digits for integers; max number of chars for from string */
  x_int qualifier;		/* 'h', 'l', or 'L' for integer fields */
  
  /*
  ** We are on the safe side, let's reserve room for the trailing '\0'.
  */
  
  bufsize -= 1;

  for (str = buf; *fmt; fmt++) {
    if (*fmt != '%') {
      ADD_CHAR(str, bufsize, *fmt);
      continue;
    }
			
    /* 
    ** Set the appropriate flags...
    */

    flags = 0x00000000;

    repeat:
    fmt += 1; /* this also skips the '%' character. */
    switch (*fmt) {
      case '-': setFlag(flags, FMT_ALIGN_LEFT); goto repeat;
      case '+': setFlag(flags, FMT_PLUS); goto repeat;
      case ' ': setFlag(flags, FMT_SPACE); goto repeat;
      case '#': setFlag(flags, FMT_SPECIAL); goto repeat;
      case '0': setFlag(flags, FMT_PAD_ZERO); goto repeat;
    }
		
    /* 
    ** Get the field width 
    */

    field_width = -1;
    if (is_digit(*fmt)) {
      field_width = skip_atoi(&fmt);
    }
    else if (*fmt == '*') {
      fmt += 1;
      /* 
      ** It's the next argument 
      */
      field_width = va_arg(args, int);
      if (field_width < 0) {
        field_width = -field_width;
        setFlag(flags, FMT_ALIGN_LEFT);
      }
    }

    /* 
    ** Get the precision 
    */

    precision = -1;
    if (*fmt == '.') {
      fmt += 1;	
      if (is_digit(*fmt)) {
        precision = skip_atoi(&fmt);
      }
      else if (*fmt == '*') {
        fmt += 1;
        /* 
        ** It's the next argument 
        */
        precision = va_arg(args, int);
      }

      if (precision < 0) {
        precision = 0;
      }
    }

    /* 
    ** Get the conversion qualifier 
    */

    qualifier = -1;
    if (*fmt == 'h' || *fmt == 'l' || *fmt == 'L') {
      qualifier = *fmt;
      fmt += 1;
      if ((qualifier == 'l' || qualifier == 'L') && (*fmt == 'l' || *fmt == 'L')) {
        setFlag(flags, FMT_LONG);
        fmt += 1;
      }
    }

    /* 
    ** The default base 
    */

    base = 10;

    /*
    ** Now get the type of conversion required
    */ 

    switch (*fmt) {
      case 'c': {
        if (isNotSet(flags, FMT_ALIGN_LEFT)) {
          while (--field_width > 0) {
            ADD_CHAR(str, bufsize, ' ');
          }
        }

        ADD_CHAR(str, bufsize, (unsigned char) va_arg(args, int));

        while (--field_width > 0) {
          ADD_CHAR(str, bufsize, ' ');
        }
        
        /*
        ** Continue sliding over the format string...
        */
        
        continue;

      }
      
      case 's': {
        s = va_arg(args, char *);
        if (!s) {
          s = "<NULL>";
        }

        len = strlen(s);

        if (isNotSet(flags, FMT_ALIGN_LEFT)) {
          while (len < field_width--) {
            ADD_CHAR(str, bufsize, ' ');
          }
        }

        for (i = 0; i < len; i++) {
          ADD_CHAR(str, bufsize, *s++);
        }

        while (len < field_width--) {
          ADD_CHAR(str, bufsize, ' ');
        }

        /*
        ** Continue sliding over the format string...
        */

        continue;
        
      }

      case 'p': {
        x_long pointer;
        if (field_width == -1) {
          field_width = 2 * sizeof(void *);
          setFlag(flags, FMT_PAD_ZERO);
        }

        ADD_CHAR(str, bufsize, '0');
        ADD_CHAR(str, bufsize, 'x');
        pointer = (x_long)(x_int)va_arg(args, void *);
        str = number(str, &bufsize, pointer, 16, field_width, precision, flags);

        /*
        ** Continue sliding over the format string...
        */

        continue;
      
      }


      case 'n': {
        if (qualifier == 'l') {
          long * ip = va_arg(args, long *);
          *ip = (str - buf);
        } 
        else {
          int * ip = va_arg(args, int *);
          *ip = (str - buf);
        }

        /*
        ** Continue sliding over the format string...
        */

        continue;
        
      }

      /*
      ** Integer number formats - set up the flags and "break" 
      */

      case 'o': {
        base = 8;
        break;
      }

      case 'X': {
        setFlag(flags, FMT_CAPITALS);

        /*
        ** Yes, we fall through to the case 'x'...
        */

      }
      
      case 'x': {
        base = 16;
        break;
      }

      case 'd':
      case 'i': {
        setFlag(flags, FMT_SIGN);

        /*
        ** Yes, we fall through to the case 'u'...
        */

      }
      
      case 'u': {
        break;
      }

      default: {
      
        /*
        ** See if the conversion specifier is one of the custom ones...
        */

        i = (*fmt) - 65;
        if (i > 0 && i < 58 && formatters[i]) {
          str = formatters[i](str, &bufsize, va_arg(args, void *), field_width == -1 ? 0 : field_width, precision, flags);
          continue;
        }

        if (*fmt != '%') {
          ADD_CHAR(str, bufsize, '%');
        }

        if (*fmt) {
          ADD_CHAR(str, bufsize, *fmt);
        }
        else {
          fmt -= 1;
        }

        /*
        ** Continue sliding over the format string...
        */

        continue;
        
      }

    }
    
    /*
    ** OK, we're out of the switch, now we can proceed the conversion...
    */
    
    if (qualifier == 'l') {
      if (isSet(flags, FMT_LONG)) {
        num = (x_long)va_arg(args, unsigned long long);
      }
      else {
        num = (x_long)va_arg(args, unsigned long);
      }
    }
    else if (qualifier == 'h') {
      if (isSet(flags, FMT_SIGN)) {
        num = (x_long)va_arg(args, int);
      }
      else {
        num = (x_long)va_arg(args, unsigned int);
      }
    }
    else if (isSet(flags, FMT_SIGN)) {
      num = (x_long)va_arg(args, int);
    }
    else {
      num = (x_long)va_arg(args, unsigned int);
    }
    str = number(str, &bufsize, (signed long long)num, base, field_width, precision, flags);
  }

  *str = '\0';

  return (str - buf);

}

x_int x_snprintf(char * buf, x_size bufsize, const char *fmt, ...) {

  va_list args;
  x_int i;

  va_start(args, fmt);
  i = x_vsnprintf(buf, bufsize, fmt, args);
  va_end(args);

  return i;

}

static char * number(char * str, x_size * bufsize, x_long arg_num, int base, int size, int precision, x_flags flags) {

  char c;
  char sign;
  char tmp[66];
  const char *digits = "0123456789abcdefghijklmnopqrstuvwxyz";
  x_int i;
  x_long num = arg_num;

  if (isSet(flags, FMT_CAPITALS)) {
    digits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  }

  if (isSet(flags, FMT_ALIGN_LEFT)) {
    unsetFlag(flags, FMT_PAD_ZERO);
  }

  if (base < 2 || base > 36) {
    return 0;
  }

  c = (isSet(flags, FMT_PAD_ZERO)) ? '0' : ' ';
  sign = 0;
  if (isSet(flags, FMT_SIGN)) {
    if (num < 0) {
      sign = '-';
      num = -num;
      size--;
    } 
    else if (isSet(flags, FMT_PLUS)) {
      sign = '+';
      size--;
    } 
    else if (isSet(flags, FMT_SPACE)) {
      sign = ' ';
      size--;
    }
  }

  if (isSet(flags, FMT_SPECIAL)) {
    if (base == 16) {
      size -= 2;
    }
    else if (base == 8) {
      size--;
    }
  }

  i = 0;
  if (num == 0) {
    tmp[i++] = '0';
  }
  else {
    while (num != 0) {
      tmp[i++] = digits[DO_DIV(num, base)];
    }
  }

  if (i > precision) {
    precision = i;
  }
  size -= precision;
  if (isNotSet(flags, FMT_PAD_ZERO | FMT_ALIGN_LEFT)) {
    while (size-- > 0) {
      ADD_CHAR(str, *bufsize, ' ');
    }
  }

  if (sign) {
    ADD_CHAR(str, *bufsize, sign);
  }

  if (isSet(flags, FMT_SPECIAL)) {
    if (base == 8) {
      ADD_CHAR(str, *bufsize, '0');
    }
    else if (base == 16) {
      ADD_CHAR(str, *bufsize, '0');
      ADD_CHAR(str, *bufsize, digits[33]);
    }
  }		

  if (isNotSet(flags, FMT_ALIGN_LEFT)) {
    while (size-- > 0) {
      ADD_CHAR(str, *bufsize, c);
    }
  }			

  while (i < precision--) {
    ADD_CHAR(str, *bufsize, '0');
  }

  while (i-- > 0) {
    ADD_CHAR(str, *bufsize, tmp[i]);
  }

  while (size-- > 0) {
    ADD_CHAR(str, *bufsize, ' ');
  }

  return str;

}
