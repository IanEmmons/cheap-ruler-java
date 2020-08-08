# cheap-ruler-java

Port to Java of [Cheap Ruler](https://github.com/mapbox/cheap-ruler), a collection
of very fast approximations to common geodesic measurements.  Useful for
performance-sensitive code that measures things on a city scale.  Can be an order
of magnitude faster than corresponding [Turf](http://turfjs.org/) methods.

The approximations are based on the [WGS84 ellipsoid model of the
Earth](https://en.wikipedia.org/wiki/Earth_radius#Meridional), projecting coordinates
to a flat surface that approximates the ellipsoid around a certain latitude.  For
distances under 500 kilometers and not on the poles, the results are very precise —
within [0.1% margin of error](#precision) compared to [Vincenti
formulas](https://en.wikipedia.org/wiki/Vincenty%27s_formulae), and usually much less
for shorter distances.

cheap-ruler-java is licensed under the [BSD 3-Clause
License](https://opensource.org/licenses/BSD-3-Clause).
Copyright (c) 2020, Ian Emmons. All rights reserved.

## Usage

See the javadocs.

## Install

### From Source

To build from source:

* Clone the repository
* In your working copy, run `./gradlew build` (or `gradlew build` on Windows)
* The jars can be found in `build/libs`

### Via Dependency Resolution

Gradle:

```
implementation 'mobi.emmons.cheap_ruler:cheap-ruler-java:1.0.0'
```

Apache Maven:

```
<dependency>
  <groupId>mobi.emmons.cheap_ruler</groupId>
  <artifactId>cheap-ruler-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

Apache Ivy:

```
<dependency org="mobi.emmons.cheap_ruler" name="cheap-ruler-java" rev="1.0.0"/>
```

## Precision

The following table shows the margin of error for `CheapRuler.distance()` compared
to `node-vincenty` (a state of the art distance formula). These results are taken
from the original JavaScript implementation, but should hold for this Java port
given that all of the original JavaScript tests have been ported as well.

|  lat   | 0&deg; | 10&deg; | 20&deg; | 30&deg; | 40&deg; | 50&deg; | 60&deg; | 70&deg; | 80&deg; |
| ------ | ------ | ------- | ------- | ------- | ------- | ------- | ------- | ------- | ------- |
|    1km |     0% |      0% |      0% |      0% |      0% |      0% |      0% |      0% |      0% |
|  100km |     0% |      0% |      0% |      0% |      0% |      0% |      0% |   0.01% |   0.03% |
|  500km |  0.01% |   0.01% |   0.01% |   0.01% |   0.02% |   0.04% |   0.08% |   0.2%  |   0.83% |
| 1000km |  0.03% |   0.03% |   0.04% |   0.06% |   0.1%  |   0.17% |   0.33% |   0.8%  |   3.38% |

Errors for all other methods are similar.

## Related

- [cheap-ruler](https://github.com/mapbox/cheap-ruler) – The original JavaScript from which this library was ported
- [cheap-ruler-cpp](https://github.com/mapbox/cheap-ruler-cpp) – C++ port of the cheap-ruler JavaScript library
