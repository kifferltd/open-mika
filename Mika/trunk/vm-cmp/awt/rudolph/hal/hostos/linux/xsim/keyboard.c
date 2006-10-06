/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/* $Id: keyboard.c,v 1.3 2006/05/02 16:28:48 cvs Exp $ */

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/Xos.h>
#include <X11/keysym.h>
#include <X11/cursorfont.h>

#include "ts-mem.h"

#include "rudolph.h"
#include "Event.h"
#include "platform.h"

extern Display *display;
extern Window window;
extern x_monitor xlock;

/*
** These are coming from java.awt.event.KeyEvent.
** Whenever something changes there, it should be 
** changed here as well...
*/

#define KEY_FIRST        400
#define KEY_TYPED        400
#define KEY_PRESSED      401
#define KEY_RELEASED     402
#define KEY_LAST         402

#define CHAR_UNDEFINED     0
#define VK_UNDEFINED       0
#define VK_CANCEL          3
#define VK_BACK_SPACE      8
#define VK_TAB             9
#define VK_ENTER          10
#define VK_CLEAR          12
#define VK_SHIFT          16
#define VK_CONTROL        17
#define VK_ALT            18
#define VK_PAUSE          19
#define VK_CAPS_LOCK      20
#define VK_KANA           21
#define VK_FINAL          24
#define VK_KANJI          25
#define VK_ESCAPE         27
#define VK_CONVERT        28
#define VK_NONCONVERT     29
#define VK_ACCEPT         30
#define VK_MODECHANGE     31
#define VK_SPACE          32
#define VK_PAGE_UP        33
#define VK_PAGE_DOWN      34
#define VK_END            35
#define VK_HOME           36
#define VK_LEFT           37
#define VK_UP             38
#define VK_RIGHT          39
#define VK_DOWN           40
#define VK_COMMA          44
#define VK_PERIOD         46
#define VK_SLASH          47
  
#define VK_0              48
#define VK_1              49
#define VK_2              50
#define VK_3              51
#define VK_4              52
#define VK_5              53
#define VK_6              54
#define VK_7              55
#define VK_8              56
#define VK_9              57
#define VK_SEMICOLON      59

#define VK_A              65
#define VK_B              66
#define VK_C              67
#define VK_D              68
#define VK_E              69
#define VK_F              70
#define VK_G              71
#define VK_H              72
#define VK_I              73
#define VK_J              74
#define VK_K              75
#define VK_L              76
#define VK_M              77
#define VK_N              78
#define VK_O              79
#define VK_P              80
#define VK_Q              81
#define VK_R              82
#define VK_S              83
#define VK_T              84
#define VK_U              85
#define VK_V              86
#define VK_W              87
#define VK_X              88
#define VK_Y              89
#define VK_Z              90

#define VK_OPEN_BRACKET   91
#define VK_BACK_SLASH     92
#define VK_CLOSE_BRACKET  93

#define VK_NUMPAD0        96  
#define VK_NUMPAD1        97  
#define VK_NUMPAD2        98  
#define VK_NUMPAD3        99  
#define VK_NUMPAD4       100  
#define VK_NUMPAD5       101  
#define VK_NUMPAD6       102  
#define VK_NUMPAD7       103  
#define VK_NUMPAD8       104  
#define VK_NUMPAD9       105  

#define VK_MULTIPLY      106
#define VK_ADD           107
#define VK_SEPARATER     108
#define VK_SUBTRACT      109
#define VK_DECIMAL       110
#define VK_DIVIDE        111

#define VK_F1            112
#define VK_F2            113
#define VK_F3            114
#define VK_F4            115
#define VK_F5            116
#define VK_F6            117
#define VK_F7            118
#define VK_F8            119
#define VK_F9            120
#define VK_F10           121
#define VK_F11           122
#define VK_F12           123

#define VK_DELETE        127
#define VK_NUM_LOCK      144
#define VK_SCROLL_LOCK   145
#define VK_PRINTSCREEN   154
#define VK_INSERT        155
#define VK_HELP          156
#define VK_META          157
#define VK_BACK_QUOTE    192
#define VK_QUOTE         222

#define MOD_SHIFT          1
#define MOD_CTRL           2
#define MOD_META           4
#define MOD_ALT            8

#define X_SHIFT            1
#define X_CAPS             2
#define X_CTRL             4
#define X_ALT              8
#define X_META            16

typedef struct x_XK2VK {
  KeySym  XK;
  w_int   VK;
} x_XK2VK;

typedef x_XK2VK  *x_xk2vk;

/*
** This table converts X events to Java events.
** The symbol itself is provided by X.
*/

static x_XK2VK  x_keymap[] = {
  { XK_Cancel,     VK_CANCEL         },
  { XK_BackSpace,  VK_BACK_SPACE     },
  { XK_Tab,        VK_TAB            },
  { XK_Return,     VK_ENTER          },
  { XK_Linefeed,   VK_ENTER          },
  { XK_Clear,      VK_CLEAR          },
  { XK_Shift_L,    VK_SHIFT          },
  { XK_Shift_R,    VK_SHIFT          },

  { XK_Control_L,  VK_CONTROL        },
  { XK_Control_R,  VK_CONTROL        },
  { XK_Alt_L,      VK_ALT            },
  { XK_Alt_R,      VK_ALT            },
  { XK_Pause,      VK_PAUSE          },
  { XK_Caps_Lock,  VK_CAPS_LOCK      },
  { XK_Shift_Lock, VK_CAPS_LOCK      },
  { 0,             VK_KANA           },
  { 0,             VK_FINAL          },
  { XK_Kanji,      VK_KANJI          },
  { XK_Escape,     VK_ESCAPE         },
  { XK_Henkan_Mode,VK_CONVERT        },
  { XK_Muhenkan,   VK_NONCONVERT     },
  { 0,             VK_ACCEPT         },
  { 0,             VK_MODECHANGE     },
  { XK_space,      VK_SPACE          },
  { XK_Page_Up,    VK_PAGE_UP        },
  { XK_Page_Down,  VK_PAGE_DOWN      },
  { XK_End,        VK_END            },
  { XK_Home,       VK_HOME           },
  { XK_Left,       VK_LEFT           },
  { XK_Up,         VK_UP             },
  { XK_Right,      VK_RIGHT          },
  { XK_Down,       VK_DOWN           },
  { XK_comma,      VK_COMMA          },
  { XK_period,     VK_PERIOD         },
  { XK_slash,      VK_SLASH          },

  { XK_KP_Space,   VK_SPACE          },
  { XK_KP_Tab,     VK_TAB            },
  { XK_KP_Enter,   VK_ENTER          },
  { XK_KP_Home,    VK_HOME           },
  { XK_KP_Left,    VK_LEFT           },
  { XK_KP_Up,      VK_UP             },
  { XK_KP_Right,   VK_RIGHT          },
  { XK_KP_Down,    VK_DOWN           },
  { XK_KP_Page_Up, VK_PAGE_UP        },
  { XK_KP_Page_Down,VK_PAGE_DOWN     },
  { XK_KP_End,     VK_END            },
  { XK_KP_Delete,  VK_DELETE         },
  { XK_KP_Insert,  VK_INSERT         },

  { XK_0,          VK_0              },
  { XK_1,          VK_1              },
  { XK_2,          VK_2              },
  { XK_3,          VK_3              },
  { XK_4,          VK_4              },
  { XK_5,          VK_5              },
  { XK_6,          VK_6              },
  { XK_7,          VK_7              },
  { XK_8,          VK_8              },
  { XK_9,          VK_9              },
  { XK_semicolon,  VK_SEMICOLON      },

  { XK_A,          VK_A              },
  { XK_B,          VK_B              },
  { XK_C,          VK_C              },
  { XK_D,          VK_D              },
  { XK_E,          VK_E              },
  { XK_F,          VK_F              },
  { XK_G,          VK_G              },
  { XK_H,          VK_H              },
  { XK_I,          VK_I              },
  { XK_J,          VK_J              },
  { XK_K,          VK_K              },
  { XK_L,          VK_L              },
  { XK_M,          VK_M              },
  { XK_N,          VK_N              },
  { XK_O,          VK_O              },
  { XK_P,          VK_P              },
  { XK_Q,          VK_Q              },
  { XK_R,          VK_R              },
  { XK_S,          VK_S              },
  { XK_T,          VK_T              },
  { XK_U,          VK_U              },
  { XK_V,          VK_V              },
  { XK_W,          VK_W              },
  { XK_X,          VK_X              },
  { XK_Y,          VK_Y              },
  { XK_Z,          VK_Z              },

  { XK_a,          VK_A              },
  { XK_b,          VK_B              },
  { XK_c,          VK_C              },
  { XK_d,          VK_D              },
  { XK_e,          VK_E              },
  { XK_f,          VK_F              },
  { XK_g,          VK_G              },
  { XK_h,          VK_H              },
  { XK_i,          VK_I              },
  { XK_j,          VK_J              },
  { XK_k,          VK_K              },
  { XK_l,          VK_L              },
  { XK_m,          VK_M              },
  { XK_n,          VK_N              },
  { XK_o,          VK_O              },
  { XK_p,          VK_P              },
  { XK_q,          VK_Q              },
  { XK_r,          VK_R              },
  { XK_s,          VK_S              },
  { XK_t,          VK_T              },
  { XK_u,          VK_U              },
  { XK_v,          VK_V              },
  { XK_w,          VK_W              },
  { XK_x,          VK_X              },
  { XK_y,          VK_Y              },
  { XK_z,          VK_Z              },

  { XK_bracketleft,  VK_OPEN_BRACKET },
  { XK_backslash,    VK_BACK_SLASH   },
  { XK_bracketright, VK_CLOSE_BRACKET},

  { XK_KP_0,       VK_NUMPAD0        },
  { XK_KP_1,       VK_NUMPAD1        },
  { XK_KP_2,       VK_NUMPAD2        },
  { XK_KP_3,       VK_NUMPAD3        },
  { XK_KP_4,       VK_NUMPAD4        },
  { XK_KP_5,       VK_NUMPAD5        },
  { XK_KP_6,       VK_NUMPAD6        },
  { XK_KP_7,       VK_NUMPAD7        },
  { XK_KP_8,       VK_NUMPAD8        },
  { XK_KP_9,       VK_NUMPAD9        },

  { XK_KP_Multiply,  VK_MULTIPLY     },
  { XK_KP_Add,       VK_ADD          },
  { XK_KP_Separator, VK_SEPARATER    },
  { XK_KP_Subtract,  VK_SUBTRACT     },
  { XK_KP_Decimal,   VK_DECIMAL      },
  { XK_KP_Divide,    VK_DIVIDE       },

  { XK_F1,         VK_F1             },
  { XK_F2,         VK_F2             },
  { XK_F3,         VK_F3             },
  { XK_F4,         VK_F4             }, 
  { XK_F5,         VK_F5             },
  { XK_F6,         VK_F6             },
  { XK_F7,         VK_F7             },
  { XK_F8,         VK_F8             },
  { XK_F9,         VK_F9             },
  { XK_F10,        VK_F10            },
  { XK_F11,        VK_F11            },
  { XK_F12,        VK_F12            },

  { XK_Delete,     VK_DELETE         },
  { XK_Num_Lock,   VK_NUM_LOCK       },
  { XK_Scroll_Lock,VK_SCROLL_LOCK    },
  { 0,             VK_PRINTSCREEN    },
  { XK_Insert,     VK_INSERT         },
  { XK_Help,       VK_HELP           },
  { XK_Meta_L,     VK_META           },
  { XK_Meta_L,     VK_META           },
  { XK_apostrophe, VK_BACK_QUOTE     },
  { XK_quoteright, VK_QUOTE          },
  { XK_grave,      VK_BACK_QUOTE     },
  
  { XK_ISO_Left_Tab, VK_TAB          },
  
  { 0xFFFFFFFF,    VK_UNDEFINED      }
};

static XComposeStatus status_in_out;

w_int keyboard_isMod(w_int VK) {
  return (VK == VK_SHIFT) || (VK == VK_ALT) || (VK == VK_META) || (VK == VK_CONTROL) || ((VK >= VK_F1) && (VK <= VK_F12)) || (VK == VK_LEFT) || (VK == VK_RIGHT) || (VK == VK_UP) || (VK == VK_DOWN);
}

w_int keyboard_poll(w_int *VK, w_char *keychar, w_int *mod, w_int *pressed) {
  XEvent  e;
  KeySym  keysym;
  x_xk2vk iter;
  w_ubyte buffer[5];
  w_int   i;
  
  x_monitor_enter(xlock, x_eternal);
  
  if(XCheckWindowEvent(display, window, KeyPressMask | KeyReleaseMask, &e)) {

    /*
    ** Lookup the keysym and the string it represents.
    */
    
    i = XLookupString(&e.xkey, (char *)&buffer, 5, &keysym, &status_in_out);

    if(i == 1) {
      *keychar = buffer[0];
    }
    else {
      woempa(5, "Too many keychars ?!?\n");
    }
   
    /*
    ** Get the modifiers.
    */ 
    
    *mod = 0;
	
	woempa(9, "e.xkey.state: %d\n", e.xkey.state);

    if((e.xkey.state & X_SHIFT) == X_SHIFT) *mod |= MOD_SHIFT;
    if((e.xkey.state & X_CAPS)  == X_CAPS)  *mod ^= MOD_SHIFT;
    if((e.xkey.state & X_CTRL)  == X_CTRL)  *mod |= MOD_CTRL;
    if((e.xkey.state & X_META)  == X_META)  *mod |= MOD_META;
    if((e.xkey.state & X_ALT)   == X_ALT)   *mod |= MOD_ALT;

    /*
    ** Translate XK to VK.
    */

    *VK = VK_UNDEFINED;
    iter = (x_xk2vk)&x_keymap;  
    while(*VK == VK_UNDEFINED && iter->XK != 0xFFFFFFFF) {
      if(iter->XK == keysym) {
        *VK = iter->VK;
      }
      iter++;
    }
      
    if (e.type == KeyPress) {

		woempa(9, "test for %d: %d\n", VK_CONTROL, *VK);

		if(*VK == VK_SHIFT)
		{
			*mod |= MOD_SHIFT;
		}
		else if(*VK == VK_ALT)
		{
			*mod |= MOD_ALT;
		}
		else if(*VK == VK_META)
		{
			*mod |= MOD_META;
		}
		else if(*VK == VK_CONTROL)      
		{	
			*mod |= MOD_CTRL;
		}

	  woempa(9, "Key Pressed : %d, %d, %s\n", keysym, *mod, &buffer);
      *pressed = 1;
    }
    else if (e.type == KeyRelease) {
      woempa(9, "Key Released : %d, %d, %s\n", keysym, *mod, &buffer);
      *pressed = 0;
    }

	  if(*VK == VK_ENTER)
	  {
		*keychar = 10;
	  }
	  else if((*VK >= VK_F1) && (*VK <= VK_F10))
	  {
		 *keychar = 65535;
	  }
	  else if((*VK >= VK_LEFT) && (*VK <= VK_DOWN))
	  {
		 *keychar = 65535;
	  }
	  else if((*VK == VK_SHIFT) || (*VK == VK_ALT) || (*VK == VK_CONTROL))
	  {
		 *keychar = 65535;
	  }

    x_monitor_exit(xlock);
    
    return 1;
  }

  x_monitor_exit(xlock);
  
  return 0;
}

w_int keyboard_init() {
  return 0;
}

void keyboard_shutdown() {
}
