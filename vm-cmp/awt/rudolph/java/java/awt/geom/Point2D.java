/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Denis M. Kishenko
 */
package java.awt.geom;

public abstract class Point2D implements Cloneable {

    public static class Float extends Point2D {

        public float x;
        public float y;

        public Float() {
        }

        public Float(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setLocation(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void setLocation(double x, double y) {
            this.x = (float)x;
            this.y = (float)y;
        }

        public String toString() {
            return getClass().getName() + "[x=" + x + ",y=" + y + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    public static class Double extends Point2D {

        public double x;
        public double y;

        public Double() {
        }

        public Double(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setLocation(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return getClass().getName() + "[x=" + x + ",y=" + y + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    protected Point2D() {
    }

    public abstract double getX();

    public abstract double getY();

    public abstract void setLocation(double x, double y);

    public void setLocation(Point2D p) {
        setLocation(p.getX(), p.getY());
    }

    public static double distanceSq(double x1, double y1, double x2, double y2) {
        x2 -= x1;
        y2 -= y1;
        return x2 * x2 + y2 * y2;
    }

    public double distanceSq(double px, double py) {
        return Point2D.distanceSq(getX(), getY(), px, py);
    }

    public double distanceSq(Point2D p) {
        return Point2D.distanceSq(getX(), getY(), p.getX(), p.getY());
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(distanceSq(x1, y1, x2, y2));
    }

    public double distance(double px, double py) {
        return Math.sqrt(distanceSq(px, py));
    }

    public double distance(Point2D p) {
        return Math.sqrt(distanceSq(p));
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    // [CG 201208018] my crappy code not Kishenko's
    public int hashCode() {
      long hash = java.lang.Double.doubleToLongBits(getX()) * 123456789123456789L;
      hash += java.lang.Double.doubleToLongBits(getY());
      return (int) (hash * 987654321987654321L);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Point2D) {
            Point2D p = (Point2D) obj;
            return getX() == p.getX() && getY() == p.getY();
        }
        return false;
    }
}


