// cheap-ruler-java is licensed under the BSD 3-Clause
// License, https://opensource.org/licenses/BSD-3-Clause
//
// Copyright (c) 2020, Ian Emmons. All rights reserved.

package mobi.emmons.cheap_ruler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CheapRulerTest {
	private static CheapRuler kmRuler;
	private static CheapRuler miRuler;

	@BeforeAll
	public static void beforeAll() {
		kmRuler = CheapRuler.fromLatitude(32.8351, Unit.KILOMETERS);
		miRuler = CheapRuler.fromLatitude(32.8351, Unit.MILES);
	}

	private static void assertErr(double expected, double actual, double maxError) {
		// Add a negligible fraction to make sure we don't divide by zero:
		double error = Math.abs((actual - expected)
			/ ((expected == 0.0) ? expected + 0.000001 : expected));

		assertTrue(error <= maxError,
			String.format("Expected is %1$g but got %2$g", expected, actual));
	}

	private static IntStream pointsIndexRangeMinusOne() {
		return IntStream.range(0, CheapRulerTestData.POINTS.length - 1);
	}

	private static IntStream pointsIndexRange() {
		return IntStream.range(0, CheapRulerTestData.POINTS.length);
	}

	private static IntStream linesIndexRange() {
		return IntStream.range(0, CheapRulerTestData.LINES.length);
	}

	@SuppressWarnings("static-method")
	@ParameterizedTest
	@MethodSource("pointsIndexRangeMinusOne")
	public void testDistanceKm(int i) {
		double expected = CheapRulerTestData.TURF_DISTANCE[i];
		double actual = kmRuler.distance(
			CheapRulerTestData.POINTS[i],
			CheapRulerTestData.POINTS[i + 1]);
		assertErr(expected, actual, .003);
	}

	@SuppressWarnings("static-method")
	@Test
	public void testDistanceMi() {
		double d = kmRuler.distance(new Point(30.5, 32.8351), new Point(30.51, 32.8451));
		double d2 = miRuler.distance(new Point(30.5, 32.8351), new Point(30.51, 32.8451));
		assertErr(d / d2, 1.609344, 1e-12);
	}

	@SuppressWarnings("static-method")
	@ParameterizedTest
	@MethodSource("pointsIndexRangeMinusOne")
	public void testBearing(int i) {
		double expected = CheapRulerTestData.TURF_BEARING[i];
		double actual = kmRuler.bearing(
			CheapRulerTestData.POINTS[i],
			CheapRulerTestData.POINTS[i + 1]);
		assertErr(expected, actual, .005);
	}

	@SuppressWarnings("static-method")
	@ParameterizedTest
	@MethodSource("pointsIndexRange")
	public void testDestination(int i) {
		double bearing = (i % 360) - 180.0;
		Point expected = CheapRulerTestData.TURF_DESTINATION[i];
		Point actual = kmRuler.destination(CheapRulerTestData.POINTS[i], 1.0, bearing);

		assertErr(expected.getLat(), actual.getLat(), 1e-6);	// longitude
		assertErr(expected.getLon(), actual.getLon(), 1e-6);	// latitude
	}

	@SuppressWarnings("static-method")
	@Test
	public void testEmptyLineDistance() {
		LineString emptyLine = new LineString();
		double expected = 0.0;
		double actual = kmRuler.lineDistance(emptyLine);
		assertErr(expected, actual, 0.0);
	}

	@SuppressWarnings("static-method")
	@ParameterizedTest
	@MethodSource("linesIndexRange")
	public void testLineDistance(int i) {
		double expected = CheapRulerTestData.TURF_LINE_DISTANCE[i];
		double actual = kmRuler.lineDistance(CheapRulerTestData.LINES[i]);
		assertErr(expected, actual, 0.003);
	}

	@SuppressWarnings("static-method")
	@ParameterizedTest
	@MethodSource("linesIndexRange")
	public void testArea(int i) {
		LineString line = CheapRulerTestData.LINES[i];
		double expectedArea = CheapRulerTestData.TURF_AREA[i];
		if (line.size() < 3 && expectedArea < 0.0) {
			// Do nothing: Skip these because they have no area
		} else if (line.size() < 3) {
			fail(String.format("Valid area but invalid polygon for i = %1%d%n", i));
		} else if (expectedArea < 0.0) {
			fail(String.format("Valid polygon but invalid area for i = %1%d%n", i));
		} else {
			Stream<Point> pointStream = Stream.concat(
				line.stream(),
				Stream.of(line.get(0)));
			LinearRing ring = new LinearRing(pointStream.collect(Collectors.toList()));
			double actualArea = kmRuler.area(new Polygon(ring));
			assertErr(expectedArea, actualArea, 0.003);
		}
	}

	@SuppressWarnings("static-method")
	@Test
	public void testAlongEmptyLine() {
		Point emptyPoint = new Point(0, 0);
		LineString emptyLine = new LineString();
		Point expected = emptyPoint;
		Point actual = kmRuler.along(emptyLine, 0.0);

		assertErr(expected.getLat(), actual.getLat(), 0.0);
		assertErr(expected.getLon(), actual.getLon(), 0.0);
	}

	@SuppressWarnings("static-method")
	@ParameterizedTest
	@MethodSource("linesIndexRange")
	public void testAlong(int i) {
		Point expected = CheapRulerTestData.TURF_ALONG[i];
		Point actual = kmRuler.along(CheapRulerTestData.LINES[i],
			CheapRulerTestData.TURF_ALONG_DIST[i]);

		assertErr(expected.getLat(), actual.getLat(), 1e-6); // along longitude
		assertErr(expected.getLon(), actual.getLon(), 1e-6); // along latitude
	}

	@SuppressWarnings("static-method")
	@Test
	public void testAlongWithDist() {
		LineString line0 = CheapRulerTestData.LINES[0];
		assertEquals(kmRuler.along(line0, -5), line0.get(0));
	}

	@SuppressWarnings("static-method")
	@Test
	public void testAlongWithDistGreaterThanLength() {
		LineString line0 = CheapRulerTestData.LINES[0];
		assertEquals(kmRuler.along(line0, 1000), line0.get(line0.size() - 1));
	}

	@SuppressWarnings("static-method")
	@Test
	public void testPointOnLine() {
		// not using a Turf comparison because pointOnLine is buggy. See
		// https://github.com/Turfjs/turf/issues/344
		LineString line = new LineString(
			new Point(-77.031669, 38.878605),
			new Point(-77.029609, 38.881946));
		PointIndexT result = kmRuler.pointOnLine(line, new Point(-77.034076, 38.882017));

		assertErr(result.getPoint().getLat(), -77.03052689033436, 1e-6);
		assertErr(result.getPoint().getLon(), 38.880457324462576, 1e-6);
		assertEquals(result.getIndex(), 0);
		assertErr(result.getT(), 0.5544221677861756, 1e-6);

		assertEquals(kmRuler.pointOnLine(line, new Point(-80.0, 38.0)).getT(), 0,
			"t is not less than 0");
		assertEquals(kmRuler.pointOnLine(line, new Point(-75.0, 38.0)).getT(), 1,
			"t is not bigger than 1");
	}

	@SuppressWarnings("static-method")
	@Test
	public void testPointToSegmentDistance() {
		Point p = new Point(-77.034076, 38.882017);
		Point p0 = new Point(-77.031669, 38.878605);
		Point p1 = new Point(-77.029609, 38.881946);
		double distance = kmRuler.pointToSegmentDistance(p, p0, p1);
		assertErr(0.37461484020420416, distance, 1e-6);
	}

	@SuppressWarnings("static-method")
	@ParameterizedTest
	@MethodSource("linesIndexRange")
	public void testLineSlice(int i) {
		LineString line = CheapRulerTestData.LINES[i];
		double dist = kmRuler.lineDistance(line);
		Point start = kmRuler.along(line, dist * 0.3);
		Point stop = kmRuler.along(line, dist * 0.7);
		double expected = CheapRulerTestData.TURF_LINE_SLICE[i];
		double actual = kmRuler.lineDistance(kmRuler.lineSlice(start, stop, line));

		//TODO: Should update CheapRulerTestData.TURF_LINE_SLICE and revert maxError back
		assertErr(expected, actual, 1e-4);
	}

	@SuppressWarnings("static-method")
	@Test
	public void testLineSliceAlongEmptyLine() {
		LineString emptyLine = new LineString();
		double expected = kmRuler.lineDistance(emptyLine);
		double actual = kmRuler.lineDistance(kmRuler.lineSliceAlong(0.0, 0.0, emptyLine));
		assertErr(expected, actual, 0.0);
	}

	@SuppressWarnings("static-method")
	@ParameterizedTest
	@MethodSource("linesIndexRange")
	public void testLineSliceAlong(int i) {
		if (i == 46) {	// skip due to Turf bug https://github.com/Turfjs/turf/issues/351
			return;
		}

		LineString line = CheapRulerTestData.LINES[i];
		double dist = kmRuler.lineDistance(line);
		double expected = CheapRulerTestData.TURF_LINE_SLICE[i];
		double actual = kmRuler.lineDistance(
			kmRuler.lineSliceAlong(dist * 0.3, dist * 0.7, line));

		//TODO: Should update CheapRulerTestData.TURF_LINE_SLICE and revert maxError back
		assertErr(expected, actual, 1e-4);
	}

	@SuppressWarnings("static-method")
	@Test
	public void testLineSliceReverse() {
		LineString line = CheapRulerTestData.LINES[0];
		double dist = kmRuler.lineDistance(line);
		Point start = kmRuler.along(line, dist * 0.7);
		Point stop = kmRuler.along(line, dist * 0.3);
		double actual = kmRuler.lineDistance(kmRuler.lineSlice(start, stop, line));

		assertErr(0.018676476689649835, actual, 1e-6);
	}

	@SuppressWarnings("static-method")
	@ParameterizedTest
	@MethodSource("pointsIndexRange")
	public void testBufferPoint(int i) {
		Box expected = CheapRulerTestData.TURF_BUFFER_POINT[i];
		Box actual = miRuler.bufferPoint(CheapRulerTestData.POINTS[i], 0.1);

		assertErr(expected.getMin().getLat(), actual.getMin().getLat(), 2e-7);
		assertErr(expected.getMin().getLat(), actual.getMin().getLat(), 2e-7);
		assertErr(expected.getMax().getLon(), actual.getMax().getLon(), 2e-7);
		assertErr(expected.getMax().getLon(), actual.getMax().getLon(), 2e-7);
	}

	@SuppressWarnings("static-method")
	@Test
	public void testBufferBBox() {
		Box bbox = new Box(new Point(30, 38), new Point(40, 39));
		Box bbox2 = kmRuler.bufferBBox(bbox, 1);

		assertErr(bbox2.getMin().getLat(), 29.989319515875376, 1e-6);
		assertErr(bbox2.getMin().getLon(), 37.99098271225711, 1e-6);
		assertErr(bbox2.getMax().getLat(), 40.01068048412462, 1e-6);
		assertErr(bbox2.getMax().getLon(), 39.00901728774289, 1e-6);
	}

	@SuppressWarnings("static-method")
	@Test
	public void testInsideBBox() {
		Box bbox = new Box(new Point(30, 38), new Point(40, 39));

		assertTrue(CheapRuler.insideBBox(new Point(35, 38.5), bbox));
		assertFalse(CheapRuler.insideBBox(new Point(45, 45), bbox));
	}

	@SuppressWarnings("static-method")
	@Test
	public void testFromTile() {
		CheapRuler ruler1 = CheapRuler.fromLatitude(50.5, Unit.KILOMETERS);
		CheapRuler ruler2 = CheapRuler.fromTile(11041, 15, Unit.KILOMETERS);

		Point p1 = new Point(30.5, 50.5);
		Point p2 = new Point(30.51, 50.51);

		assertErr(ruler1.distance(p1, p2), ruler2.distance(p1, p2), 2e-5);
	}

	@SuppressWarnings("static-method")
	@Test
	public void testLongitudeWrap() {
		CheapRuler r = CheapRuler.fromLatitude(50.5, Unit.KILOMETERS);
		List<Point> points = new ArrayList<>();
		Point origin = new Point(0, 50.5);	// Greenwich
		double radius = 1000.0;
		// construct a regular dodecagon
		for (int i = -180; i <= 180; i += 30) {
			Point p = r.destination(origin, radius, i);
			// shift randomly east/west to the international date line
			double shift = ThreadLocalRandom.current().nextBoolean() ? 180.0 : -180.0;
			Point pShifted = new Point(p.getLat() + shift, p.getLon());
			points.add(pShifted);
		}
		LineString line = new LineString(points);
		Polygon poly = new Polygon(new LinearRing(points));

		double p = r.lineDistance(line);
		double a = r.area(poly);
		// cheap_ruler does planar calculations, so the perimeter and area of a
		// planar regular dodecagon with circumradius rad are used in these checks.
		// For the record, the results for rad = 1000 km are:
		//        perimeter    area
		// planar 6211.657082  3000000
		// WGS84  6187.959236  2996317.6328
		// error  0.38%        0.12%
		assertErr(12 * radius / Math.sqrt(2 + Math.sqrt(3.0)), p, 1e-12);
		assertErr(3 * radius * radius, a, 1e-12);
		for (int j = 1; j < line.size(); ++j) {
			double azi = r.bearing(line.get(j - 1), line.get(j));
			// offset expect and actual by 1 to make err criterion absolute
			assertErr(1, Math.IEEEremainder(270 - 15 + 30 * j - azi, 360) + 1, 1e-12);
		}
	}
}
