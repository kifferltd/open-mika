#ifndef _ENVIRONMENT_H
#define _ENVIRONMENT_H

/**************************************************************************
*                                                                         *
* Copyright (c) 2006 by Chris Gray,                                       *
* /k/ Embedded Java Solutions. All rights reserved.                       *
*                                                                         *
**************************************************************************/

extern char *getInstallationDir(void);
extern char *getExtensionDir(void);
extern char *getOSName(void);
extern char *getOSVersion(void);
extern char *getOSArch(void);
extern char *getUserName(void);
extern char *getUserHome(void);
extern char *getUserDir(void);
extern char *getUserLanguage(void);
extern char *getLibraryPath(void);

#endif /* _ENVIRONMENT_H */

