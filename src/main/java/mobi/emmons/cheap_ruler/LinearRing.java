// cheap-ruler-java is licensed under the BSD 3-Clause
// License, https://opensource.org/licenses/BSD-3-Clause
//
// Copyright (c) 2020, Ian Emmons. All rights reserved.

package mobi.emmons.cheap_ruler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An immutable class representing a linear ring as a list of points, where the last
 * point in the list is assumed to be connected to the first.
 */
public final class LinearRing implements Iterable<Point> {
	private final List<Point> points;

	public LinearRing(Point... points) {
		this.points = Collections.unmodifiableList(
			Arrays.asList(points));
	}

	public LinearRing(Collection<Point> points) {
		this.points = Collections.unmodifiableList(
			points.stream().collect(Collectors.toList()));
	}

	public Point get(int i) {
		return points.get(i);
	}

	public int size() {
		return points.size();
	}

	public boolean isEmpty() {
		return points.isEmpty();
	}

	@Override
	public Iterator<Point> iterator() {
		return points.iterator();
	}

	public Stream<Point> stream() {
		return points.stream();
	}

	@Override
	public int hashCode() {
		return Objects.hash(points);
	}

	@Override
	public boolean equals(Object rhs) {
		if (this == rhs) {
			return true;
		}
		if (!(rhs instanceof LinearRing)) {
			return false;
		}
		LinearRing other = (LinearRing) rhs;
		return Objects.equals(points, other.points);
	}

	@Override
	public String toString() {
		return points.stream()
			.map(Point::toString)
			.collect(Collectors.joining(", ", "LinearRing [", "]"));
	}
}
