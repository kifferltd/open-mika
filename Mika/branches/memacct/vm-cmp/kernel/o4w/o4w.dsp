# Microsoft Developer Studio Project File - Name="o4w" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Static Library" 0x0104

CFG=o4w - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "o4w.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "o4w.mak" CFG="o4w - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "o4w - Win32 Release" (based on "Win32 (x86) Static Library")
!MESSAGE "o4w - Win32 Debug" (based on "Win32 (x86) Static Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName "o4w"
# PROP Scc_LocalPath "."
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "o4w - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "../../build-x86-winnt/kernel/o4w/bin"
# PROP Intermediate_Dir "../../build-x86-winnt/kernel/o4w/bin"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /YX /FD /c
# ADD CPP /nologo /W3 /GX /O2 /I "include" /D "NDEBUG" /D "WIN32" /D "_MBCS" /D "_LIB" /D "WINNT" /YX /FD /I../common/include /I../../wonka/include /c
# ADD BASE RSC /l 0x813 /d "NDEBUG"
# ADD RSC /l 0x813 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ELSEIF  "$(CFG)" == "o4w - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "../../build-x86-winnt/kernel/o4w/bin"
# PROP Intermediate_Dir "../../build-x86-winnt/kernel/o4w/bin"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /YX /FD /GZ /c
# ADD CPP /nologo /W3 /Gm /GX /ZI /Od /I "include" /D "_DEBUG" /D "WIN32" /D "_MBCS" /D "_LIB" /D "WINNT" /YX /FD /I../common/include /GZ /I../../wonka/include /c
# ADD BASE RSC /l 0x813 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ENDIF 

# Begin Target

# Name "o4w - Win32 Release"
# Name "o4w - Win32 Debug"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\src\cond.c
# End Source File
# Begin Source File

SOURCE=.\src\ext.c
# End Source File
# Begin Source File

SOURCE=.\src\loempa.c
# End Source File
# Begin Source File

SOURCE=.\src\memory.c
# End Source File
# Begin Source File

SOURCE=.\src\monitor.c
# End Source File
# Begin Source File

SOURCE=.\src\mutex.c
# End Source File
# Begin Source File

SOURCE=.\src\o4w.c
# End Source File
# Begin Source File

SOURCE=.\src\queue.c
# End Source File
# Begin Source File

SOURCE=.\src\scheduler.c
# End Source File
# Begin Source File

SOURCE=.\src\semaphore.c
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\include\loempa.h
# End Source File
# Begin Source File

SOURCE=.\include\o4w.h
# End Source File
# Begin Source File

SOURCE=.\include\oswald.h
# End Source File
# Begin Source File

SOURCE=.\include\types.h
# End Source File
# End Group
# End Target
# End Project
