// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
// Laurence Newell
// starbase@ukraa.com
// radio.telescope@btinternet.com
//
// This file is part of Starbase.
//
// Starbase is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Starbase is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Starbase.  If not, see http://www.gnu.org/licenses.

/** A trackball.  Allows interactive rotation of 3d views or objects  */


package org.lmn.fc.ui.trackerball;
/*
 * Trackball code:
 *
 * Implementation of a virtual trackball.
 * Implemented by Gavin Bell, lots of ideas from Thant Tessman and
 *   the August '88 issue of Siggraph's "Computer Graphics," pp. 121-129.
 *
 * Vector manip code:
 *
 * Original code from:
 * David M. Ciemiewicz, Mark Grossman, Henry Moreton, and Paul Haeberli
 *
 * Much mucking with by:
 * Gavin Bell
 *
 * Java version by Tim Lambert, following the method names used
 * in Magician, and adding some more methods to make it easier to use
 */

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public final class TrackerBall extends MouseAdapter
                         implements MouseMotionListener
    {
    private final float trackballSize;
    private int prevX = 0;
    private int prevY = 0;
    private int startX = 0;
    private int startY = 0;
    private float[] curQuat = buildQuaternion(0.0f, 0.0f, 0.0f, 0.0f);

    private float[] lastQuat = curQuat;
    private boolean spin = false;

    public TrackerBall()
        {
        trackballSize = 0.8f;
        }

    /**
     * specify component virtual trackball is in
     */
    public void listen(final Component component)
        {
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
        }

    /**
     * return rotation Matrix representing current rotation of trackball
     */
    public float[] getRotMatrix()
        {
        final float[] rotMat = buildMatrix(curQuat);
        if (spin)
            {
            curQuat = addQuats(lastQuat, curQuat);
            }
        return rotMat;
        }

    // deal with Mouse events
    private final static int EPS2 = 25;  //only spin if mouse moved this far

    public void mouseReleased(final MouseEvent evt)
        {
        final int dx = startX - evt.getX();
        final int dy = startY - evt.getY();
        spin = (dx * dx + dy * dy > EPS2);
        }

    public void mousePressed(final MouseEvent evt)
        {
        startX = prevX = evt.getX();
        startY = prevY = evt.getY();
        spin = false;
        }

    public void mouseMoved(final MouseEvent evt)
        {
        }

    public void mouseDragged(final MouseEvent evt)
        {
        final int aWidth = evt.getComponent().getSize().width;
        final int aHeight = evt.getComponent().getSize().height;
        final int currX = evt.getX();
        final int currY = evt.getY();

        lastQuat =
            buildQuaternion((2.0f * prevX - aWidth) / (float) aWidth,
                            (aHeight - 2.0f * prevY) / (float) aHeight,
                            (2.0f * currX - aWidth) / (float) aWidth,
                            (aHeight - 2.0f * currY) / (float) aHeight);
        curQuat = addQuats(lastQuat, curQuat);
        prevX = currX;
        prevY = currY;

        }


/*
 * Ok, simulate a track-ball.  Project the points onto the virtual
 * trackball, then figure out the axis of rotation, which is the cross
 * product of P1 P2 and O P1 (O is the center of the ball, 0,0,0)
 * Note:  This is a deformed trackball-- is a trackball in the center,
 * but is deformed into a hyperbolic sheet of rotation away from the
 * center.  This particular function was chosen after trying out
 * several variations.
 *
 * It is assumed that the arguments to this routine are in the range
 * (-1.0 ... 1.0)
 */
    private float[] buildQuaternion(final float p1x, final float p1y, final float p2x, final float p2y)
        {
        final float[] a = new float[3]; /* Axis of rotation */
        final float phi;  /* how much to rotate about axis */
        final float[] p1 = new float[3];
        final float[] p2 = new float[3];
        final float[] d = new float[3];
        float t;

        if (p1x == p2x && p1y == p2y)
            {
            /* Zero rotation */
            final float[] q = {0.0f, 0.0f, 0.0f, 1.0f};
            return q;
            }

        /*
         * First, figure out z-coordinates for projection of P1 and P2 to
         * deformed sphere
         */
        vset(p1, p1x, p1y, projectToSphere(trackballSize, p1x, p1y));
        vset(p2, p2x, p2y, projectToSphere(trackballSize, p2x, p2y));

        /*
         *  Now, we want the cross product of P1 and P2
         */
        vcross(p2, p1, a);

        /*
         *  Figure out how much to rotate around that axis.
         */
        vsub(p1, p2, d);
        t = vlength(d) / (2.0f * trackballSize);

        /*
         * Avoid problems with out-of-control values...
         */
        if (t > 1.0) t = 1.0f;
        if (t < -1.0) t = -1.0f;
        phi = 2.0f * (float) Math.asin(t);

        return axisToQuat(a, phi);
        }

    /**
     * Create a unit quaternion that represents the rotation about axis by theta
     */
    private static float[] axisToQuat(final float[] axis, final float theta)
        {
        final float[] q = new float[4];
        q[3] = (float) Math.cos(theta / 2.0f); //scalar part
        vnormal(axis);
        vcopy(axis, q);
        vscale(q, (float) Math.sin(theta / 2.0));
        return q;
        }

    private static float[] renormalizeQuat(final float[] q)
        {
        float len = 0.0f;
        for (int i = 0; i < q.length; i++)
            {
            len += q[i] * q[i];
            }
        len = (float) Math.sqrt(len);
        final float[] ans = new float[q.length];
        for (int i = 0; i < q.length; i++)
            {
            ans[i] = q[i] / len;
            }
        return ans;
        }

    /**
     * Given two rotations, e1 and e2, expressed as quaternion rotations, figure out the equivalent
     * single rotation and stuff it into dest.
     * <p/>
     * This routine also normalizes the result every RENORMCOUNT times it is called, to keep error
     * from creeping in.
     * <p/>
     * NOTE: This routine is written so that q1 or q2 may be the same as dest (or each other).
     */

    private static final int RENORMCOUNT = 97;

    private int count = 0;

    private float[] addQuats(final float[] q1, final float[] q2)
        {
        final float[] ans = new float[4];
        ans[3] = q2[3] * q1[3] - q2[0] * q1[0] - q2[1] * q1[1] - q2[2] * q1[2];
        ans[0] = q2[3] * q1[0] + q2[0] * q1[3] + q2[1] * q1[2] - q2[2] * q1[1];
        ans[1] = q2[3] * q1[1] + q2[1] * q1[3] + q2[2] * q1[0] - q2[0] * q1[2];
        ans[2] = q2[3] * q1[2] + q2[2] * q1[3] + q2[0] * q1[1] - q2[1] * q1[0];
        if (++count > RENORMCOUNT)
            {
            count = 0;
            renormalizeQuat(ans);
            }
        return ans;
        }

    /**
     * Project an x,y pair onto a sphere of radius r OR a hyperbolic sheet if we are away from the
     * center of the sphere.
     */

    private static float projectToSphere(final float r, final float x, final float y)
        {
        final float z;
        final float d = (float) Math.sqrt(x * x + y * y);
        if (d < r * 0.70710678118654752440f)
            {    /* Inside sphere */
            z = (float) Math.sqrt(r * r - d * d);
            }
        else
            {           /* On hyperbola */
            final float t = r / 1.41421356237309504880f;
            z = t * t / d;
            }
        return z;
        }


    /*
     * Build a rotation matrix, given a quaternion rotation.
     *
     */
    private static float[] buildMatrix(final float[] q)
        {
        final float[] m = new float[16];
        m[0] = 1.0f - 2.0f * (q[1] * q[1] + q[2] * q[2]);
        m[1] = 2.0f * (q[0] * q[1] - q[2] * q[3]);
        m[2] = 2.0f * (q[2] * q[0] + q[1] * q[3]);
        m[3] = 0.0f;

        m[4] = 2.0f * (q[0] * q[1] + q[2] * q[3]);
        m[5] = 1.0f - 2.0f * (q[2] * q[2] + q[0] * q[0]);
        m[6] = 2.0f * (q[1] * q[2] - q[0] * q[3]);
        m[7] = 0.0f;

        m[8] = 2.0f * (q[2] * q[0] - q[1] * q[3]);
        m[9] = 2.0f * (q[1] * q[2] + q[0] * q[3]);
        m[10] = 1.0f - 2.0f * (q[1] * q[1] + q[0] * q[0]);
        m[11] = 0.0f;

        m[12] = 0.0f;
        m[13] = 0.0f;
        m[14] = 0.0f;
        m[15] = 1.0f;
        return m;
        }

    /* our own collectio of 3D vector functions */

    public static void vzero(final float[] v)
        {
        v[0] = 0.0f;
        v[1] = 0.0f;
        v[2] = 0.0f;
        }

    private static void vset(final float[] v, final float x, final float y, final float z)
        {
        v[0] = x;
        v[1] = y;
        v[2] = z;
        }

    private static void vsub(final float[] src1, final float[] src2, final float[] dst)
        {
        dst[0] = src1[0] - src2[0];
        dst[1] = src1[1] - src2[1];
        dst[2] = src1[2] - src2[2];
        }

    private static void vcopy(final float[] v1, final float[] v2)
        {
        for (int i = 0; i < 3; i++)
            v2[i] = v1[i];
        }

    private static void vcross(final float[] v1, final float[] v2, final float[] cross)
        {
        final float[] temp = new float[3];

        temp[0] = (v1[1] * v2[2]) - (v1[2] * v2[1]);
        temp[1] = (v1[2] * v2[0]) - (v1[0] * v2[2]);
        temp[2] = (v1[0] * v2[1]) - (v1[1] * v2[0]);
        vcopy(temp, cross);
        }

    private static float vlength(final float[] v)
        {
        return (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        }

    private static void vscale(final float[] v, final float div)
        {
        v[0] *= div;
        v[1] *= div;
        v[2] *= div;
        }

    private static void vnormal(final float[] v)
        {
        vscale(v, 1.0f / vlength(v));
        }

    public static float vdot(final float[] v1, final float[] v2)
        {
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
        }

    public static void vadd(final float[] src1, final float[] src2, final float[] dst)
        {
        dst[0] = src1[0] + src2[0];
        dst[1] = src1[1] + src2[1];
        dst[2] = src1[2] + src2[2];
        }

    }

