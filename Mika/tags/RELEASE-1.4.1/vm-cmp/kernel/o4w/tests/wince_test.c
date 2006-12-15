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
