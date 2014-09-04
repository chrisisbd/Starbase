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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.toolkit.jupiter;

/** Static methods to do common mathematical calculations. The trigonometrical methods are for ease of working
 * in degrees rather than radians. java.lang.StrictMath methods are always used.
 */
public class Maths
{
	private Maths () { /* Not instantiable */ }

	/** Find the mean of a set of angles, taking into account periodicity. Uses angleStatistics (). */
	public static double angleMean (double... anglesInDegrees)
	{
		Maths.Statistics stats = angleStatistics (anglesInDegrees);
		return stats.mean;
	} // angleMean

	/** Find the difference (a1 - a2) between 2 angles in degrees, taking into account periodicity.
		* The result is in the range -180..+180. */
	public static double angleDifference (double a1degrees, double a2degrees)
	{
		double diff = a1degrees - a2degrees;

		if (diff > 180.0) return diff - 360;

		if (diff < -180.0) return diff + 360;

		return diff;
	} // angleDifference

	/** Calculate the min, max, mean and stdDev of a set of angles, taking into account periodicity.
		* Uses angleDifference () from a working mean of the first angle. Does NOT use the erroneous
		* method you can find on the web that averages sin and cos and then takes atan2. */
	public static Maths.Statistics angleStatistics (double... anglesInDegrees)
	{
		final double NOT_SET = Double.POSITIVE_INFINITY;
		Maths.Statistics stats = new Maths.Statistics ();
		double minDiff = 0.0;
		double maxDiff = 0.0;
		double angle0degrees = NOT_SET;
		double sumDiff = 0.0;
		double sumDiffSq = 0.0;
		int n = 0;

		for (double angle : anglesInDegrees)
		{
			if (NOT_SET == angle0degrees) angle0degrees = angle;

			n++;
			double diff = angleDifference (angle, angle0degrees);
			sumDiff += diff;
			sumDiffSq += diff * diff;

			if (diff < minDiff) minDiff = diff;
			else if (diff > maxDiff) maxDiff = diff;
		}

		stats.n = n;
		stats.min = in360 (angle0degrees + minDiff);
		stats.max = in360 (angle0degrees + maxDiff);
		stats.mean = in360 (angle0degrees + sumDiff / n);
		stats.stdDev = StrictMath.sqrt (sumDiffSq / n - stats.mean * stats.mean);
		return stats;
	} // angleStatistics

	/** Calculate the min, max, mean and stdDev of a set of angles, taking into account periodicity.
		* Uses angleDifference () from a working mean of the first angle. Does NOT use the erroneous
		* method you can find on the web that averages sin and cos and then takes atan2. */
	public static Maths.Statistics angleStatistics (java.util.List <Double> anglesInDegrees)
	{
		double [] a = new double [anglesInDegrees.size ()];

		for (int i = 0; i < a.length; i++)
		{
			a [i] = anglesInDegrees.get (i);
		}

		return angleStatistics (a);
	} // angleStatistics

	/** Convert angle x (degrees) to lie in range 0..360.<br>
		* 10.4.7 deprecated - use synonymous in360 () instead. */
	@Deprecated
	public static double angleNormalise (double xDegs)
	{
		return in360 (xDegs);
	} // angleNormalise

	/** Convert angle x (degrees) to lie in range 0..360 */
	public static double in360 (double xDegs)
	{
		double x360 = xDegs % 360.0;

		if (x360 >= 0.0) return x360;

		return x360 + 360.0;
	} // in360

	/** Get arccos in degrees. */
	public static double acos (double cos)
	{
		return StrictMath.toDegrees (StrictMath.acos (cos));
	} // acos

	/** Get arcsin in degrees. */
	public static double asin (double sin)
	{
		return StrictMath.toDegrees (StrictMath.asin (sin));
	} // asin

	/** Get atan2 in degrees. */
	public static double atan2 (double sin, double cos)
	{
		return StrictMath.toDegrees (StrictMath.atan2 (sin, cos));
	} // atan2

	/** Get sine of an angle given in degrees. */
	public static double sin (double xDegs)
	{
		return StrictMath.sin (StrictMath.toRadians (xDegs));
	} // sin

	/** Get cosine of an angle given in degrees. */
	public static double cos (double xDegs)
	{
		return StrictMath.cos (StrictMath.toRadians (xDegs));
	} // cos

	/** Get tangent of an angle in degrees. */
	public static double tan (double xDegs)
	{
		return StrictMath.tan (StrictMath.toRadians (xDegs));
	} // tan

	/** Just a record to hold public values.
		* Differs from net.jupiter.grip.Statistics because min and max are not int and there is no mode. */
	public static class Statistics
	{
		public int n;
		public double min, mean, max, stdDev;
	} // Statistics

} // Maths
