package mobi.emmons.cheap_ruler;

/** An enumeration of distance units supported by CheapRuler. */
public enum Unit {
	KILOMETERS(1.0),
	MILES(1000.0 / 1609.344),
	NAUTICAL_MILES(1000.0 / 1852.0),
	METERS(1000.0),
	METRES(1000.0),
	YARDS(1000.0 / 0.9144),
	FEET(1000.0 / 0.3048),
	INCHES(1000.0 / 0.0254);

	private final double multiplier;

	Unit(double multiplier) {
		this.multiplier = multiplier;
	}

	public double getMultiplier() {
		return multiplier;
	}
}
