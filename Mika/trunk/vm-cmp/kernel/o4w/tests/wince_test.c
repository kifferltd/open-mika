/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

/*
** $Id: wince_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <windows.h>
#include "oswald.h"
#include "main_test.h"

int WINAPI WinMain (HINSTANCE hInstance, HINSTANCE hPrevInstance,LPWSTR lpszCmdLine,int nCmdShow)
{

	WNDCLASS wc;
	MSG msg;
    LPWSTR groot = TEXT("gfffkvjl");

	wc.style=CS_HREDRAW | CS_VREDRAW;
	wc.lpfnWndProc = (WNDPROC) WindowProc;
	wc.cbClsExtra = 0;
	wc.cbWndExtra = 0;
	wc.hInstance=hInstance;
	wc.hIcon=LoadIcon(NULL, NULL);
	wc.hCursor=LoadCursor(NULL, IDC_ARROW);
	wc.hbrBackground=(HBRUSH) (COLOR_WINDOW +1);
	wc.lpszMenuName=NULL;
	wc.lpszClassName =TEXT("MyWnd");
  
	RegisterClass (&wc);

	//Create the file
	fp_1 = fopen("My Documents/l4w.txt","w"); 

	//Create the window
	ListBox= CreateWindow
	(
		TEXT("LISTBOX"),
		TEXT("o4w - Tests"),
		WS_VISIBLE|WS_HSCROLL|WS_VSCROLL|LBS_DISABLENOSCROLL,
		CW_USEDEFAULT,  
		CW_USEDEFAULT,
		CW_USEDEFAULT,  
		CW_USEDEFAULT,
		NULL ,
		NULL,
		hInstance,
		NULL
	);
	
	SendMessage(ListBox,LB_SETHORIZONTALEXTENT,(WPARAM)1000,0);

	//Start the tests
	x_oswald_init(30*1024*1024, 50);
   	
	while (GetMessage (&msg, ListBox,0,0))
	{
		TranslateMessage(&msg); 
		DispatchMessage(&msg); 
	}

	return msg.wParam;
}


LRESULT CALLBACK WindowProc (HWND wnd, UINT message, WPARAM wParam,LPARAM lParam) {
	switch (message)
	{	
	case LB_ADDSTRING:
		SendMessage(ListBox,LB_ADDSTRING,-1,(LPARAM)lParam); 
		UpdateWindow(ListBox);  
		return 0; 
	case WM_CREATE:
		SendMessage(ListBox,LB_ADDSTRING,-1,(LPARAM)lParam); 
		UpdateWindow(ListBox);  
		return 0; 
	case WM_DESTROY:
		PostQuitMessage (0);
		return 0;
	}
	return DefWindowProc (wnd, message, wParam, lParam);
}
