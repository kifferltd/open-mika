#include "ts-mem.h"
#include "zutil.h"

const char * const z_errmsg[10] = {
	"need dictionary",     /* Z_NEED_DICT       2  */
	"stream end",          /* Z_STREAM_END      1  */
	"",                    /* Z_OK              0  */
	"file error",          /* Z_ERRNO         (-1) */
	"stream error",        /* Z_STREAM_ERROR  (-2) */
	"data error",          /* Z_DATA_ERROR    (-3) */
	"insufficient memory", /* Z_MEM_ERROR     (-4) */
	"buffer error",        /* Z_BUF_ERROR     (-5) */
	"incompatible version",/* Z_VERSION_ERROR (-6) */
	""};


voidpf zcalloc (voidpf opaque, unsigned items, unsigned size) {
  return allocClearedMem(items * size);
}

void  zcfree (voidpf opaque, voidpf ptr)
{
  return releaseMem(ptr);
}

