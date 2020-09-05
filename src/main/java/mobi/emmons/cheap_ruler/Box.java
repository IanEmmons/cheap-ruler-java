// cheap-ruler-java is licensed under the BSD 3-Clause
// License, https://opensource.org/licenses/BSD-3-Clause
//
// Copyright (c) 2020, Ian Emmons. All rights reserved.

package mobi.emmons.cheap_ruler;

import java.util.Objects;

/**
 * An immutable class representing a geospatial box.
 */
public final class Box {
	private final Point min;
	private final Point max;

	public Box(Point min, Point max) {
		this.min = min;
		this.max = max;
	}

	public Point getMin() {
		return min;
	}

	public Point getMax() {
		return max;
	}

	@Override
	public int hashCode() {
		return Objects.hash(max, min);
	}

	@Override
	public boolean equals(Object rhs) {
		if (this == rhs) {
			return true;
		}
		if (!(rhs instanceof Box)) {
			return false;
		}
		Box other = (Box) rhs;
		return Objects.equals(max, other.max) && Objects.equals(min, other.min);
	}

	@Override
	public String toString() {
		return String.format("Box [min=%1$s, max=%2$s]", min, max);
	}
}
