# Magic Sort

Fairly efficient sort implementation when you are only interested in top n items from the collection. Allows to easily find top elements in large (a few billion or so entries is not a problem) collections where JDK built in classes would give up long ago. Replaced a 20 node map-reduce cluster in one of my experiments.

Key notes:

- **Fast** - In general use it's about 10x as fast as functionally identical JDK classes implementation for most input collections and result set sizes
- **Fixed memory profile** - Only needs enough memory to accumulate an array for top n elements, capable of easily processing very large (or even infinite) data sets
- **Well behaved scaling** - when collecting parallel streams.
- Provides both a static `Collection<T> -> List<T>` function and a Java 8 Streams `Collector` implementation.

Gotchas:

- I ignore any nulls in input collections and intend to continue doing so
- I don't plan to extend the code to cover primitive collections
- My maths are rusty and I make no claims about O(x) performance

## Simple use example

```java
// Ten largest doubles in a list the JDK way
List<Double> doubles = new ArrayList<>();
for (double d = 0.0; d < 10_000_000.0; d += 1.0) doubles.add(d);
Collections.shuffle(doubles);

Collections.sort(doubles, Collections.reverseOrder()); // 5-10 seconds on a typical desktop
List<Double> topTenDoubles = doubles.subList(0, Math.min(10, doubles.size()));
```
```java
// Ten largest doubles in a list the MagicSort way
List<Double> doubles = new ArrayList<>();
for (double d = 0.0; d < 10_000_000.0; d += 1.0) doubles.add(d);
Collections.shuffle(doubles);

List<Double> topItems = MagicSort.sortReverseAndLimit(doubles, 10); // About 25 times faster
```
[Benchmarked](https://github.com/karliszigurs/MagicSortBenchmarks) speed improvements of about 5x-15x for various practically sized source collection / top n elements combinations ([full details](https://github.com/karliszigurs/MagicSortBenchmarks/blob/master/results/20161104-0.2-SNAPSHOT/summary.csv)).

In short - the larger the source collection is and the smaller count of top elements is desired (e.g. top 10 vs top 1000), the bigger the speedup. Speedups of over 100x have been observed in real-life datasets.

## Streams support

```java
List<Double> doubles = new ArrayList<>(); // Take a very very large list
for (double d = 0.0; d < 1_000_000.0; d += 1.0) doubles.add(d);
/* and find the top 10 entries spreading the work across available cpu cores */
doubles.parallelStream().collect(MagicSort.toList(10)).forEach(System.out::println);
```

## Use it in your project

```maven
<dependency>
    <groupId>com.zigurs.karlis.utils</groupId>
    <artifactId>magicsort</artifactId>
    <version>0.2</version>
</dependency>
```

### Credits

```
                              //
(C) 2016 Karlis Zigurs (http://zigurs.com)
                            //
```
