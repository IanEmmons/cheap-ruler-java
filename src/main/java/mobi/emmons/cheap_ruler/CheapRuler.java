// cheap-ruler-java is licensed under the BSD 3-Clause
// License, https://opensource.org/licenses/BSD-3-Clause
//
// Copyright (c) 2020, Ian Emmons. All rights reserved.

package mobi.emmons.cheap_ruler;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of very fast approximations to common geodesic measurements.
 * Useful for performance-sensitive code that measures things on a city scale.
 * Can be an order of magnitude faster than corresponding
 * <a href="http://turfjs.org/">Turf</a> methods.
 *
 * <P>The approximations are based on the
 * <a href="https://en.wikipedia.org/wiki/Earth_radius#Meridional">WGS84
 * ellipsoid model of the Earth</a>, projecting coordinates to a flat surface
 * that approximates the ellipsoid around a certain latitude. For distances
 * under 500 kilometers and not on the poles, the results are very precise
 * &mdash; within 0.1% margin of error compared to
 * <a href="https://en.wikipedia.org/wiki/Vincenty%27s_formulae">Vincenti</a>
 * formulas, and usually much less for shorter distances.
 *
 * <P>This library is a A Java port of the original
 * <a href="https://github.com/mapbox/cheap-ruler">JavaScript library</a>.
 */
public final class CheapRuler {
	// Values that define the WGS84 ellipsoid model of the Earth:
	private static double RE = 6378.137;				// equatorial radius
	private static double FE = 1.0 / 298.257223563;	// flattening

	private static double E2 = FE * (2 - FE);
	private static double RAD = Math.PI / 180.0;

	private final double ky;
	private final double kx;

	/**
	 * Creates a CheapRuler instance valid for geodesic computations near the given
	 * latitude.
	 *
	 * @param latitude The latitude of interest, expressed in decimal degrees
	 * @param unit     The distance unit to use in computations
	 * @return A CheapRuler instance
	 */
	public static CheapRuler fromLatitude(double latitude, Unit unit) {
		return new CheapRuler(latitude, unit);
	}

	/**
	 * Creates a CheapRuler instance valid for the given tile coordinates.
	 *
	 * @param y    Y parameter of the tile of interest
	 * @param z    Z parameter of the tile of interest
	 * @param unit The distance unit to use in computations
	 * @return A CheapRuler instance
	 */
	public static CheapRuler fromTile(int y, int z, Unit unit) {
		if (y < 0) {
			throw new IllegalArgumentException("y must be non-negative");
		}
		if (z < 0 || z >= 32) {
			throw new IllegalArgumentException(String.format(
				"z is out of the range [0, 32)", z));
		}

		double n = Math.PI * (1.0 - 2.0 * (y + 0.5) / (1 << z));
		double latitude = Math.atan(Math.sinh(n)) / RAD;

		return new CheapRuler(latitude, unit);
	}

	private CheapRuler(double latitude, Unit unit) {
		// Curvature formulas from https://en.wikipedia.org/wiki/Earth_radius#Meridional
		double mul = RAD * RE * unit.getMultiplier();
		double coslat = Math.cos(latitude * RAD);
		double w2 = 1 / (1 - E2 * (1 - coslat * coslat));
		double w = Math.sqrt(w2);

		// multipliers for converting longitude and latitude degrees into distance
		kx = mul * w * coslat;			// based on normal radius of curvature
		ky = mul * w * w2 * (1 - E2);	// based on meridonal radius of curvature
	}

	/**
	 * Computes the square of the distance between two points.
	 *
	 * @param a The first point
	 * @param b The second point
	 * @return The square of the distance between the two points
	 */
	public double squareDistance(Point a, Point b) {
		double dx = longDiff(a.getLat(), b.getLat()) * kx;
		double dy = (a.getLon() - b.getLon()) * ky;
		return dx * dx + dy * dy;
	}

	/**
	 * Computes the distance between two points.
	 *
	 * @param a The first point
	 * @param b The second point
	 * @return The distance between the two points
	 */
	public double distance(Point a, Point b) {
		return Math.sqrt(squareDistance(a, b));
	}

	/**
	 * Computes the bearing between two points in angles.
	 *
	 * @param a The first point
	 * @param b The second point
	 * @return The bearing between the two points
	 */
	public double bearing(Point a, Point b) {
		double dx = longDiff(b.getLat(), a.getLat()) * kx;
		double dy = (b.getLon() - a.getLon()) * ky;
		return Math.atan2(dx, dy) / RAD;
	}

	/**
	 * Computes a new point given distance and bearing from the starting point.
	 *
	 * @param origin  The point from which to start
	 * @param dist    The distance from the origin point
	 * @param bearing The bearing from the origin point
	 * @return A new point as indicated
	 */
	public Point destination(Point origin, double dist, double bearing) {
		double a = bearing * RAD;
		return offset(origin, Math.sin(a) * dist, Math.cos(a) * dist);
	}

	/**
	 * Computes a new point given easting and northing offsets from the starting
	 * point.
	 *
	 * @param origin The point from which to start
	 * @param dx     The easting offset
	 * @param dy     The northing offset
	 * @return A new point as indicated
	 */
	public Point offset(Point origin, double dx, double dy) {
		return new Point(origin.getLat() + dx / kx, origin.getLon() + dy / ky);
	}

	/**
	 * Computes the distance along a line.
	 *
	 * @param points The line (an array of points)
	 * @return The distance
	 */
	public double lineDistance(LineString points) {
		double total = 0;
		for (int i = 1; i < points.size(); ++i) {
			total += distance(points.get(i - 1), points.get(i));
		}
		return total;
	}

	/**
	 * Computes the area of a polygon.
	 *
	 * @param poly The polygon (an array of rings, each of which is an array of points)
	 * @return The area
	 */
	public double area(Polygon poly) {
		double sum = 0;
		for (int i = 0; i < poly.size(); ++i) {
			LinearRing ring = poly.get(i);
			int len = ring.size();
			for (int j = 0, k = len - 1; j < len; k = j++) {
				sum += longDiff(ring.get(j).getLat(), ring.get(k).getLat())
					* (ring.get(j).getLon() + ring.get(k).getLon()) * (i != 0 ? -1.0 : 1.0);
			}
		}
		return (Math.abs(sum) / 2.0) * kx * ky;
	}

	/**
	 * Computes a point at a specified distance along a line.
	 *
	 * @param line The line (an array of points)
	 * @param dist The distance along the line
	 * @return The indicated point
	 */
	public Point along(LineString line, double dist) {
		double sum = 0;

		if (line.isEmpty()) {
			return new Point(0, 0);
		}

		if (dist <= 0) {
			return line.get(0);
		}

		for (int i = 0; i < line.size() - 1; ++i) {
			Point p0 = line.get(i);
			Point p1 = line.get(i + 1);
			double d = distance(p0, p1);

			sum += d;

			if (sum > dist) {
				return interpolate(p0, p1, (dist - (sum - d)) / d);
			}
		}

		return line.get(line.size() - 1);
	}

	/**
	 * Computes the distance from a point p to the line segment between points a and b.
	 *
	 * @param p The point in question
	 * @param a One end of the line segment
	 * @param b The other end of the line segment
	 * @return The indicated distance
	 */
	public double pointToSegmentDistance(Point p, Point a, Point b) {
		double t = 0.0;
		double x = a.getLat();
		double y = a.getLon();
		double dx = longDiff(b.getLat(), x) * kx;
		double dy = (b.getLon() - y) * ky;

		if (dx != 0.0 || dy != 0.0) {
			t = (longDiff(p.getLat(), x) * kx * dx + (p.getLon() - y) * ky * dy)
				/ (dx * dx + dy * dy);
			if (t > 1.0) {
				x = b.getLat();
				y = b.getLon();
			} else if (t > 0.0) {
				x += (dx / kx) * t;
				y += (dy / ky) * t;
			}
		}
		return distance(p, new Point(x, y));
	}

	/**
	 * Computes a tuple of the form &lt;point, index, t&gt; where point is the
	 * closest point on the line from the given point, index is the start index of
	 * the segment with the closest point, and t is a parameter from 0 to 1 that
	 * indicates where the closest point is on that segment.
	 *
	 * @param line The line (an array of points)
	 * @param p    The point in question
	 * @return The indicated tuple
	 */
	public PointIndexT pointOnLine(LineString line, Point p) {
		double minDist = Double.POSITIVE_INFINITY;
		double minX = 0;
		double minY = 0;
		double minT = 0;
		int minI = 0;

		if (line.isEmpty()) {
			return new PointIndexT(new Point(0, 0), 0, 0);
		}

		for (int i = 0; i < line.size() - 1; ++i) {
			double t = 0.;
			double x = line.get(i).getLat();
			double y = line.get(i).getLon();
			double dx = longDiff(line.get(i + 1).getLat(), x) * kx;
			double dy = (line.get(i + 1).getLon() - y) * ky;

			if (dx != 0. || dy != 0.) {
				t = (longDiff(p.getLat(), x) * kx * dx
					+ (p.getLon() - y) * ky * dy) / (dx * dx + dy * dy);
				if (t > 1) {
					x = line.get(i + 1).getLat();
					y = line.get(i + 1).getLon();

				} else if (t > 0) {
					x += (dx / kx) * t;
					y += (dy / ky) * t;
				}
			}

			double sqDist = squareDistance(p, new Point(x, y));

			if (sqDist < minDist) {
				minDist = sqDist;
				minX = x;
				minY = y;
				minI = i;
				minT = t;
			}
		}

		return new PointIndexT(
			new Point(minX, minY),
			minI,
			Math.max(0.0, Math.min(1.0, minT)));
	}

	/**
	 * Computes a part of the given line between the start and the stop points (or
	 * their closest points on the line).
	 *
	 * @param start The start point
	 * @param stop  The stop point
	 * @param line  The line (an array of points)
	 * @return The indicated portion of the line
	 */
	public LineString lineSlice(Point start, Point stop, LineString line) {
		PointIndexT p1 = pointOnLine(line, start);
		PointIndexT p2 = pointOnLine(line, stop);

		if (p1.getIndex() > p2.getIndex()
				|| (p1.getIndex() == p2.getIndex() && p1.getT() > p2.getT())) {
			PointIndexT tmp = p1;
			p1 = p2;
			p2 = tmp;
		}

		List<Point> slice = new ArrayList<>();
		slice.add(p1.getPoint());

		int l = p1.getIndex() + 1;
		int r = p2.getIndex();

		if (line.get(l) != slice.get(0) && l <= r) {
			slice.add(line.get(l));
		}

		for (int i = l + 1; i <= r; ++i) {
			slice.add(line.get(i));
		}

		if (line.get(r) != p2.getPoint()) {
			slice.add(p2.getPoint());
		}

		return new LineString(slice);
	}

	/**
	 * Computes the part of the given line between the start and the stop points as
	 * indicated by distances along the line.
	 *
	 * @param start The distance from the start of the line to the start point
	 * @param stop  The distance from the start of the line to the stop point
	 * @param line  The line (an array of points)
	 * @return The indicated portion of the line
	 */
	public LineString lineSliceAlong(double start, double stop, LineString line) {
		double sum = 0;
		List<Point> slice = new ArrayList<>();
		for (int i = 1; i < line.size(); ++i) {
			Point p0 = line.get(i - 1);
			Point p1 = line.get(i);
			double d = distance(p0, p1);
			sum += d;

			if (sum > start && slice.size() == 0) {
				slice.add(interpolate(p0, p1, (start - (sum - d)) / d));
			}

			if (sum >= stop) {
				slice.add(interpolate(p0, p1, (stop - (sum - d)) / d));
				return new LineString(slice);
			}

			if (sum > start) {
				slice.add(p1);
			}
		}

		return new LineString(slice);
	}

	/**
	 * Computes a bounding box object [w, s, e, n] centered on the given point and
	 * buffered by the given distance.
	 *
	 * @param p      The center point
	 * @param buffer The buffer distance
	 * @return The indicated bounding box
	 */
	public Box bufferPoint(Point p, double buffer) {
		double v = buffer / ky;
		double h = buffer / kx;

		return new Box(
			new Point(p.getLat() - h, p.getLon() - v),
			new Point(p.getLat() + h, p.getLon() + v));
	}

	/**
	 * Computes a bounding box object [w, s, e, n] centered on the given box and
	 * buffered by the given distance.
	 *
	 * @param box    The center box
	 * @param buffer The buffer distance
	 * @return The indicated bounding box
	 */
	public Box bufferBBox(Box box, double buffer) {
		double v = buffer / ky;
		double h = buffer / kx;

		return new Box(
			new Point(box.getMin().getLat() - h, box.getMin().getLon() - v),
			new Point(box.getMax().getLat() + h, box.getMax().getLon() + v));
	}

	/**
	 * Tests whether the given point is inside in the given bounding box.
	 *
	 * @param p    The given point
	 * @param bbox The bounding box
	 * @return True if the point is inside the box, false otherwise.
	 */
	public static boolean insideBBox(Point p, Box bbox) {
		return p.getLon() >= bbox.getMin().getLon()
			&& p.getLon() <= bbox.getMax().getLon()
			&& longDiff(p.getLat(), bbox.getMin().getLat()) >= 0
			&& longDiff(p.getLat(), bbox.getMax().getLat()) <= 0;
	}

	/**
	 * Computes the point along a line segment at a given distance from the first
	 * endpoint.
	 *
	 * @param a One end of the line segment
	 * @param b The other end of the line segment
	 * @param t The distance from a
	 * @return The indicated point
	 */
	public static Point interpolate(Point a, Point b, double t) {
		double dx = longDiff(b.getLat(), a.getLat());
		double dy = b.getLon() - a.getLon();

		return new Point(a.getLat() + dx * t, a.getLon() + dy * t);
	}

	private static double longDiff(double a, double b) {
		return Math.IEEEremainder(a - b, 360.0);
	}
}
