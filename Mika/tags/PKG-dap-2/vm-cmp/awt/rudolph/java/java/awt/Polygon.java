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


// Author: n.oberfeld
// Created: 2001/07/17
package java.awt;

/****************************************************************************************************************************************/
/**
* Polygon class
* Note that the actual drawing of the polygon is done in the functions drawPolygon and fillPolygon of java.awt.Graphics
*/
/****************************************************************************************************************************************/
public class Polygon implements Shape, java.io.Serializable {

  private static final long serialVersionUID = -6460061437900069969L;

/**  Protected variable fectangle bounds: accessible through getBounds */
  protected Rectangle bounds;
  
/**
* Public variables
*/

  public int xpoints[];
  public int ypoints[];
  public int npoints;

/**  Access Polygon.bounds. For more security send a copy of the value to avoid manipulation of the boundaries from outside */
  public Rectangle getBounds() {return new Rectangle(bounds);}
/**  deprecated */
  public Rectangle getBoundingBox() {return new Rectangle(bounds);}

  private boolean[] isPeak;

/****************************************************************************************************************************************/
/**
* full constructor
*/
  public Polygon(int[] x, int[] y, int size) {
    if (size < 0)
      throw new NegativeArraySizeException();
    else if (size ==0){
      npoints=0;
      xpoints = new int[1];
      ypoints = new int[1];
      bounds = new Rectangle();
      isPeak = new boolean[1];
    }
    else if (size ==1){
      npoints=0;
      xpoints = new int[1];
      ypoints = new int[1];
      bounds = new Rectangle();
      if(x.length>=1)
      {
        xpoints[0]=x[0];
        bounds.x=x[0];
      }
      else
        xpoints[0]=0;
      
      if(y.length>=1)
      {
        ypoints[0]=y[0];
        bounds.y=y[0];
      }
      else
        ypoints[0]=0;
      
      isPeak = new boolean[1];
      isPeak[0]=true;
    }
    else  {
      //variables
      npoints=size;
      xpoints = new int[size];
      ypoints = new int[size];
       isPeak = new boolean[size];
      bounds = new Rectangle(x[0],y[0],x[0],y[0]);
      
      xpoints[0]=x[0];
      ypoints[0]=y[0];
      //assign x-values and calculate horizontal boundaries
      for(int i=1; i<npoints && i<x.length; i++)
      {
        xpoints[i]=x[i];
        if(x[i]<bounds.x)
          bounds.x = x[i];
        else if(x[i]>bounds.width)
          bounds.width = x[i];
      }
      //in calculations above, we used 'width' as an absolute position for simplicity => convert to true width
      bounds.width-=bounds.x;
      //assign y-values and calculate vertical boundaries
      for(int i=1; i<npoints && i<y.length; i++)
      {
        ypoints[i]=y[i];
        if(y[i]<bounds.y)
          bounds.y= y[i];
        else if(y[i]>bounds.height)
          bounds.height = y[i];
      }
      //idem as above: 'height' value to true height
      bounds.height-=bounds.y;
      
      // calculate peeks
      size--; //int lastsize =npoint-1;
      //first point
       isPeak[0]= ((ypoints[0]<ypoints[size] && ypoints[0]<ypoints[1]) || (ypoints[0]>ypoints[size] && ypoints[0]>ypoints[1]));
        //next&previous are both bigger or both smaller
      //in between
      for(int i=1; i<size; i++)
        isPeak[i]= ((ypoints[i]<ypoints[i-1] && ypoints[i]<ypoints[i+1]) || (ypoints[i]>ypoints[i-1] && ypoints[i]>ypoints[i+1]));
      //last point
      isPeak[size]=((ypoints[size]<ypoints[size-1] && ypoints[size]<ypoints[0])||(ypoints[size]>ypoints[size-1] && ypoints[size]>ypoints[0]));
      
    }
      
  }
    
/**
*  Default constructor
*/
  public Polygon()  { this(null,null,0); }

/****************************************************************************************************************************************/
/**
* Add a point to the polygon point array
*/
  public void addPoint(int x, int y) {
    if(npoints==0)
    {
      //xpoints = new int[1]; //already done above
      xpoints[0]=x;
      //ypoints = new int[1]; //already done above
      ypoints[0]=y;
      //isPeak = new boolean[1];
      isPeak[0]=true;
      bounds.setBounds(x,y,0,0);
      npoints=1;
    }
    else if(npoints==1)
    {
      //bounds.x= xpoints[0] //already done above
      //bounds.y= ypoints[0] //already done above
      xpoints = new int[2];
      xpoints[0]=bounds.x;
      xpoints[1]=x;
      ypoints = new int[2];
      ypoints[0]=bounds.y;
      ypoints[1]=y;
      isPeak = new boolean[2];
      isPeak[0]=true;
      isPeak[1]=true;
      //bounds
      if(x<bounds.x)
      {
        bounds.width=bounds.x-x;
        bounds.x=x;
      }
      else
        bounds.width=x-bounds.x;

      if(y<bounds.y)
      {
        bounds.height=bounds.y-y;
        bounds.y=y;
      }
      else
        bounds.height=y-bounds.y;

      npoints=2;
    }
    else
    {
      //new arrays of size npoints+1
      int[] swapx = new int[npoints + 1];
      int[] swapy = new int[npoints + 1];
      boolean[] peaks = new boolean[npoints + 1];

      //bounds
      if(x<bounds.x)
      {
        bounds.width=bounds.width+bounds.x-x;
        bounds.x=x;
      }
      else if(x>(bounds.x+bounds.width) )
        bounds.width=x-bounds.x;
        
      if(y<bounds.y)
      {
        bounds.height=bounds.width+bounds.y-y;
        bounds.y=y;
      }
      else if(y>(bounds.y+bounds.height) )
        bounds.height=y-bounds.y;

      // copy values into new array
      for(int i=0;i<npoints;i++)
      {
        swapx[i]=xpoints[i];
        swapy[i]=ypoints[i];
        peaks[i]=isPeak[i];
      }
      //add new values and swap
      swapx[npoints]=x;
      swapy[npoints]=y;
      // calculate if... new point makes previous last point now become a peek
      peaks[npoints-1]=((swapy[npoints-2]>swapy[npoints-1] && y>swapy[npoints-1])||(swapy[npoints-2]<swapy[npoints-1] && y<swapy[npoints-1]));
      // calculate if... new point is a peek in itselves
      peaks[npoints]=((y>swapy[npoints-1] && y>swapy[0]) || (y<swapy[npoints-1] && y<swapy[0]));
      // calculate if... new point makes first point now become a peek
      peaks[0]=((swapy[1]>swapy[0] && y>swapy[0]) || (swapy[1]<swapy[0] && y<swapy[0]));

      xpoints = swapx;
      ypoints = swapy;
      isPeak = peaks;
      npoints++;
    }
//System.out.println("added ("+x+","+y+"), new size "+npoints+", new bounds "+bounds);
  }




/****************************************************************************************************************************************/
/**
* Translate all points by the given amount
*/
  public void translate(int offsetx, int offsety) {
    for (int i = 0; i < npoints; i++) {
      xpoints[i] += offsetx;
      ypoints[i] += offsety;
    }
    bounds.translate(offsetx, offsety);
  }

/****************************************************************************************************************************************/
/**
* Check if given point inside polygon
* TODO: call native check in analogy to g.fillPolygon algorithm
*/
  public boolean contains(Point p) { return contains(p.x, p.y); }

  public boolean contains(int x, int y) {
    if(!bounds.contains(x,y))
      return false;
    //else
    int intersections=0;
    int last=npoints-1;  //for ease of calculation
    
    // count intersections bigger then x0
    // line from <this> to next point
    for(int i=0; i<last;i++)
    {
      if ((y==ypoints[i] && !isPeak[i] && xpoints[i]>x)
            || (ypoints[i]<y && ypoints[i+1]>y && x<(xpoints[i]+(xpoints[i+1]-xpoints[i])*(y-ypoints[i])/(ypoints[i+1]-ypoints[i])) )
              || (ypoints[i]>y && ypoints[i+1]<y && x<(xpoints[i]+(xpoints[i+1]-xpoints[i])*(y-ypoints[i])/(ypoints[i+1]-ypoints[i])) ) )
        intersections++;
    }
    // last line from last to first point
    if ((y==ypoints[last] && !isPeak[last] && xpoints[last]>x)
          ||(ypoints[last]<y && ypoints[0]>y && x<(xpoints[last]+(xpoints[0]-xpoints[last])*(y-ypoints[last])/(ypoints[0]-ypoints[last])) )
            ||(ypoints[last]>y && ypoints[0]<y && x<(xpoints[last]+(xpoints[0]-xpoints[last])*(y-ypoints[last])/(ypoints[0]-ypoints[last]))))
        intersections++;
    
    return (intersections%2>0);
  }

  public boolean inside(int x, int y) {
    return contains(x, y);
  }
  
  public String toString() {
    return getClass().getName() +" number of points: " + npoints; 
  }

}
