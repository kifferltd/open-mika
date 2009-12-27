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


package com.acunia.wonka.test.awt.Graphics.dottedtext;

public class DotArray

{

  boolean[][] array;

  public DotArray(int width, int height)

  {

    array = new boolean[height][width];

  }



  public void fill(boolean b)

  {

    for(int r=0; r<array.length; r++)

    {

      for(int c=0; c<array[r].length; c++)

      {

        array[r][c]=b;

      }

    }

  }



  public void drawString(String string, int x, int y, FontTypeInterface font)

  {

    int xPos = x;

    int yPos = y;

    for(int i=0; i<string.length(); i++)

    {

      int[] oneChar = font.getChar(string.charAt(i));

      int cols = oneChar[0];

      int rows = font.getHeight();

      int bitMask = 1;

      int useCol=1;

      for(int r=0; r<rows; r++)

      {

        for(int c=0; c<cols; c++)

        {

            if(bitMask==0)

            {

              useCol++;

              bitMask=1;

            }

            try

            {

              array[yPos+r][xPos+c]=((oneChar[useCol]&bitMask)!=0);

            }

            catch(Exception e)

            {}

            bitMask = bitMask<<1;

        }

      }

      xPos += cols;

    }

  }



  public boolean[][] getArray()

  {

    return array;

  }



  public void drawLine(int x0, int y0, int x1, int y1)

  {

    this.drawLine(x0, y0, x1, y1, true);

  }



  public void drawLine(int x0, int y0, int x1, int y1, boolean b)

  {

    int dy = y1 - y0;

    int dx = x1 - x0;

    int stepx, stepy;



    if (dy < 0)

    {

      dy = -dy;

      stepy = -array[0].length;

    }

    else

    {

      stepy = array[0].length;

    }

    if (dx < 0)

    {

      dx = -dx;

      stepx = -1;

    }

    else

    {

      stepx = 1;

    }

    dy <<= 1;

    dx <<= 1;



    y0 *= array[0].length;

    y1 *= array[0].length;

    array[(x0+y0)/array.length][(x0+y0)%array.length] = b;

    if (dx > dy)

    {

      int fraction = dy - (dx >> 1);

      while (x0 != x1)

      {

        if (fraction >= 0)

        {

          y0 += stepy;

          fraction -= dx;

        }

        x0 += stepx;

        fraction += dy;

        array[(x0+y0)/array.length][(x0+y0)%array.length] = b;

      }

    }

    else

    {

      int fraction = dx - (dy >> 1);

      while (y0 != y1)

      {

        if (fraction >= 0)

        {

          x0 += stepx;

          fraction -= dy;

        }

        y0 += stepy;

        fraction += dx;

        array[(x0+y0)/array.length][(x0+y0)%array.length] = b;

      }

    }

  }



  public void drawOval(int start_X, int start_Y, int axis_A, int axis_B)

  {

    this.drawOval(start_X, start_Y, axis_A, axis_B, true);

  }



  public void drawOval(int start_X, int start_Y, int axis_A, int axis_B, boolean b)

  {

    int active_X;

    int active_Y;

    int axis_A_Squared;

    int axis_B_Squared;

    int axis_A_Squared_Times_2;

    int axis_B_Squared_Times_2;

    long derivitive_X, derivitive_Y, dd;



    active_X = axis_A;

    active_Y = 0;

    axis_A_Squared = axis_A*axis_A;

    axis_B_Squared = axis_B*axis_B;

    axis_A_Squared_Times_2 = axis_A_Squared + axis_A_Squared;

    axis_B_Squared_Times_2 = axis_B_Squared + axis_B_Squared;

    derivitive_X = axis_B_Squared_Times_2*axis_A;

    derivitive_Y = 0L;

    dd = (axis_B_Squared / 4L) - (axis_B_Squared * axis_A) + axis_A_Squared;



    while ( derivitive_X > derivitive_Y )

    {

      try

      {

        array[start_X-active_X][start_Y-active_Y] = b;

      }

      catch(Exception e)

      {}

      try

      {

        array[start_X-active_X][start_Y+active_Y] = b;

      }

      catch(Exception e)

      {}

      try

      {

        array[start_X+active_X][start_Y-active_Y] = b;

      }

      catch(Exception e)

      {}

      try

      {

        array[start_X+active_X][start_Y+active_Y] = b;

      }

      catch(Exception e)

      {}

      active_Y +=1;

      derivitive_Y += axis_A_Squared_Times_2;

      if (dd <= 0L )

      {

        dd  += (derivitive_Y + axis_A_Squared);

      }

      else

      {

        derivitive_X -= axis_B_Squared_Times_2;

        active_X -= 1L;

        dd += (derivitive_Y + axis_A_Squared - derivitive_X );

      }

    }

    dd += ( ( ((3L *(axis_B_Squared-axis_A_Squared)) / 2L) - ( derivitive_X + derivitive_Y ) ) / 2L);



    while ( active_X > 0L )

    {

      try

      {

        array[start_X-active_X][start_Y-active_Y] = b;

      }

      catch(Exception e)

      {}

      try

      {

        array[start_X-active_X][start_Y+active_Y] = b;

      }

      catch(Exception e)

      {}

      try

      {

        array[start_X+active_X][start_Y-active_Y] = b;

      }

      catch(Exception e)

      {}

      try

      {

        array[start_X+active_X][start_Y+active_Y] = b;

      }

      catch(Exception e)

      {}

      active_X -=1;

      derivitive_X -= axis_B_Squared_Times_2;



      if (dd > 0L )

      {

        dd  += (axis_B_Squared - derivitive_X);

      }

      else

      {

        derivitive_Y += axis_A_Squared_Times_2;

        active_Y += 1L;

        dd += (derivitive_Y + axis_B_Squared - derivitive_X );

      }

    }

  }

  public void fillOval(int start_X, int start_Y, int axis_A, int axis_B)

  {

    this.fillOval(start_X, start_Y, axis_A, axis_B, true);

  }



  public void fillOval(int start_X, int start_Y, int axis_A, int axis_B, boolean b)

  {

    int active_X;

    int active_Y;

    int axis_A_Squared;

    int axis_B_Squared;

    int axis_A_Squared_Times_2;

    int axis_B_Squared_Times_2;

    long derivitive_X, derivitive_Y, dd;



    active_X = axis_A;

    active_Y = 0;

    axis_A_Squared = axis_A*axis_A;

    axis_B_Squared = axis_B*axis_B;

    axis_A_Squared_Times_2 = axis_A_Squared + axis_A_Squared;

    axis_B_Squared_Times_2 = axis_B_Squared + axis_B_Squared;

    derivitive_X = axis_B_Squared_Times_2*axis_A;

    derivitive_Y = 0L;

    dd = (axis_B_Squared / 4L) - (axis_B_Squared * axis_A) + axis_A_Squared;



    while ( derivitive_X > derivitive_Y )

    {

      drawLine((start_X - active_X),(start_Y - active_Y),(start_X - active_X)+(active_X << 1) +1,(start_Y - active_Y));

      drawLine((start_X - active_X),(start_Y + active_Y),(start_X - active_X)+(active_X << 1) +1,(start_Y + active_Y));



      active_Y +=1;

      derivitive_Y += axis_A_Squared_Times_2;

      if (dd <= 0L )

      {

        dd  += (derivitive_Y + axis_A_Squared);

      }

      else

      {

        derivitive_X -= axis_B_Squared_Times_2;

        active_X -= 1L;

        dd += (derivitive_Y + axis_A_Squared - derivitive_X );

      }

    }

    dd += ( ( ((3L *(axis_B_Squared-axis_A_Squared)) / 2L) - ( derivitive_X + derivitive_Y ) ) / 2L);



    while ( active_X > 0L )

    {

      drawLine((start_X - active_X),(start_Y - active_Y),(start_X - active_X)+(active_X << 1) +1,(start_Y - active_Y));

      drawLine((start_X - active_X),(start_Y + active_Y),(start_X - active_X)+(active_X << 1) +1,(start_Y + active_Y));



      active_X -=1;

      derivitive_X -= axis_B_Squared_Times_2;



      if (dd > 0L )

      {

        dd  += (axis_B_Squared - derivitive_X);

      }

      else

      {

        derivitive_Y += axis_A_Squared_Times_2;

        active_Y += 1L;

        dd += (derivitive_Y + axis_B_Squared - derivitive_X );

      }

    }

  }



  public static void main(String[] args)

  {

    DotArray da = new DotArray(50,50);

    da.drawString("Koules...",1,1,new Arial_10_0());

    boolean[][] b = da.getArray();

    for(int r=0; r<b.length; r++)

    {

      for(int c=0; c<b[r].length; c++)

      {

        System.out.print(b[r][c]?"#":".");

      }

      System.out.println();

    }

    System.exit(1);

  }

}
