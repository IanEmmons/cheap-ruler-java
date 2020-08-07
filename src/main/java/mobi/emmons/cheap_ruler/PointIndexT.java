// cheap-ruler-java is licensed under the BSD 3-Clause
// License, https://opensource.org/licenses/BSD-3-Clause
//
// Copyright (c) 2020, Ian Emmons. All rights reserved.

package mobi.emmons.cheap_ruler;

import java.util.Objects;

/**
 * A tuple used to return three values from the method CheapRuler.pointOnLine().
 * See the documentation of that method for details.
 */
public final class PointIndexT {
	private final Point point;
	private final int index;
	private final double t;

	PointIndexT(Point p, int index, double t) {
		this.point = p;
		this.index = index;
		this.t = t;
	}

	public Point getPoint() {
		return point;
	}

	public int getIndex() {
		return index;
	}

	public double getT() {
		return t;
	}

	@Override
	public int hashCode() {
		return Objects.hash(index, point, t);
	}

	@Override
	public boolean equals(Object rhs) {
		if (this == rhs) {
			return true;
		}
		if (!(rhs instanceof PointIndexT)) {
			return false;
		}
		PointIndexT other = (PointIndexT) rhs;
		return index == other.index && Objects.equals(point, other.point)
			&& Double.doubleToLongBits(t) == Double.doubleToLongBits(other.t);
	}

	@Override
	public String toString() {
		return String.format("PointIndexT [point=%1$s, index=%2$d, t=%3$g]", point, index, t);
	}
}
