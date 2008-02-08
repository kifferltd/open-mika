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


package gnu.testlet.wonka.util.Calender;
import java.util.*;

/*
*  this class extends Calendar and is used to test java.uitl.Calender
*  (since it is an abstract class)
*  used by SMCalenderTest
*/
public class SMExCalender extends Calendar
{
	public SMExCalender(){
		super();
	}
	
	public SMExCalender (TimeZone tz, Locale l) {
		super();	
	}
/**
* Calendar has 5 protected fields.  SMExCalender can acces them, so we define
* methods to retieve their values.
*/	
    	public boolean get_areFieldsSet() {
    		return areFieldsSet;
    	}

/**
* Calendar has 5 protected fields.  SMExCalender can acces them, so we define
* methods to retieve their values.
*/	  	
    	public int[] get_fields() {
    		return fields;
    	}
    	
/**
* Calendar has 5 protected fields.  SMExCalender can acces them, so we define
* methods to retieve their values.
*/	
    	public boolean[] get_isSet() {
    		return isSet;
    	}
    	
/**
* Calendar has 5 protected fields.  SMExCalender can acces them, so we define
* methods to retieve their values.
*/	
    	public boolean get_isTimeSet() {
    		return isTimeSet;
    	}
    	
/**
* Calendar has 5 protected fields.  SMExCalender can acces them, so we define
* methods to retieve their values.
*/	
    	public long get_time() {
    		return time;
    	}
    	
/**
* Calendar is an abstract class following methods are declared abstract.
* note: not all of them are in wonka yet, but we implement an empty for them so
* the testclass will compile if those functions are added.
*/
    	public void add(int fld, int amount) {}
/**
* Calendar is an abstract class following methods are declared abstract.
* note: not all of them are in wonka yet, but we implement an empty for them so
* the testclass will compile if those functions are added.
*/
	protected void computeFields() {}    	
/**
* Calendar is an abstract class following methods are declared abstract.
* note: not all of them are in wonka yet, but we implement an empty for them so
* the testclass will compile if those functions are added.
*/
	protected void computeTime() {}
/**
* Calendar is an abstract class following methods are declared abstract.
* note: not all of them are in wonka yet, but we implement an empty for them so
* the testclass will compile if those functions are added.
*/
	public int getLeastMaximum(int fld) {
		return -1;
	}
/**
* Calendar is an abstract class following methods are declared abstract.
* note: not all of them are in wonka yet, but we implement an empty for them so
* the testclass will compile if those functions are added.
*/
	public int getMaximum(int fld) {
		return -1;
	}
/**
* Calendar is an abstract class following methods are declared abstract.
* note: not all of them are in wonka yet, but we implement an empty for them so
* the testclass will compile if those functions are added.
*/
	public int getMinimum(int fld) {
		return -1;
	}
/**
* Calendar is an abstract class following methods are declared abstract.
* note: not all of them are in wonka yet, but we implement an empty for them so
* the testclass will compile if those functions are added.
*/
	public int getGreatestMinimum(int fld) {
		return -1;
	}

/**
* Calendar is an abstract class following methods are declared abstract.
* note: not all of them are in wonka yet, but we implement an empty for them so
* the testclass will compile if those functions are added.
*/
	public void roll(int fld, boolean up){
	
	}

}	