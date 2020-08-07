// cheap-ruler-java is licensed under the BSD 3-Clause
// License, https://opensource.org/licenses/BSD-3-Clause
//
// Copyright (c) 2020, Ian Emmons. All rights reserved.

package mobi.emmons.cheap_ruler;

import java.util.Objects;

/**
 * A simple geospatial point class representing a (latitude, longitude) pair.
 */
public final class Point {
	private final double lat;
	private final double lon;

	public Point(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	@Override
	public int hashCode() {
		return Objects.hash(lat, lon);
	}

	@Override
	public boolean equals(Object rhs) {
		if (this == rhs) {
			return true;
		}
		if (!(rhs instanceof Point)) {
			return false;
		}
		Point other = (Point) rhs;
		return Double.doubleToLongBits(lat) == Double.doubleToLongBits(other.lat)
			&& Double.doubleToLongBits(lon) == Double.doubleToLongBits(other.lon);
	}

	@Override
	public String toString() {
		return String.format("(%1%g, %2$g)", lat, lon);
	}
}
