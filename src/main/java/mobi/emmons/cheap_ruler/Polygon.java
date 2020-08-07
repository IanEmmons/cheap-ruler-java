// cheap-ruler-java is licensed under the BSD 3-Clause
// License, https://opensource.org/licenses/BSD-3-Clause
//
// Copyright (c) 2020, Ian Emmons. All rights reserved.

package mobi.emmons.cheap_ruler;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple representation of polygon as a list of linear rings.
 */
public final class Polygon implements Iterable<LinearRing> {
	private final List<LinearRing> linearRings;

	public Polygon(LinearRing... linearRings) {
		this.linearRings = Arrays.asList(linearRings);
	}

	public LinearRing get(int i) {
		return linearRings.get(i);
	}

	public int size() {
		return linearRings.size();
	}

	public boolean isEmpty() {
		return linearRings.isEmpty();
	}

	@Override
	public Iterator<LinearRing> iterator() {
		return linearRings.iterator();
	}

	public Stream<LinearRing> stream() {
		return linearRings.stream();
	}

	@Override
	public int hashCode() {
		return Objects.hash(linearRings);
	}

	@Override
	public boolean equals(Object rhs) {
		if (this == rhs) {
			return true;
		}
		if (!(rhs instanceof Polygon)) {
			return false;
		}
		Polygon other = (Polygon) rhs;
		return Objects.equals(linearRings, other.linearRings);
	}

	@Override
	public String toString() {
		return linearRings.stream()
			.map(LinearRing::toString)
			.collect(Collectors.joining(", ", "Polygon [", "]"));
	}
}
