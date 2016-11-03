# Magic Sort

Fairly efficient sort implementation when you are only interested in top n items from the collection. This code was originally a part of [QuickSearch](https://github.com/karliszigurs/QuickSearch) library, however I have since found it quite handy in a general use as well.

Key notes:

- **Fast.** In general use it's about 10x as fast as functionally identical JDK classes implementation for most input collections and result set sizes. _(Not that JDK sorting implementations are a slouch to start with...)_
- **Fixed memory profile.** It can easily extract top n elements from an infinite stream without needing to buffer whole stream in memory.
- Provides both a static `Collection<T> -> List<T>` function and a Java 8 Streams `Collector` implementation.
- **Well behaved scaling** when collecting parallel streams.

Gotchas:

- I ignore any nulls in input collection and intend to continue doing so.
- I don't see a point on extending the code to cover primitives.

## Simple use example

```java
// Collections way
List<Double> doubles = new ArrayList<>(); // and fill it with n random elements
Collections.sort(doubles);                // sorts whole array
List<Double> sorted = doubles.subList(0, Math.min(10, doubles.size()));
```
```java
// MagicSort way
List<Double> doubles = new ArrayList<>(); // and fill it with n random elements
List<Double> sorted = MagicSort.sortAndLimit(doubles, 10, Comparator.naturalOrder());
```
[Benchmarked](https://github.com/karliszigurs/MagicSortBenchmarks) speed improvements (top n elements _vs._ source collection size):

|       |  100| 1,000| 10,000|100,000|1,000,000|
|-------|-----|------|-------|-------|---------|
|  **1**|6.90x|11.32x| 21.52x|  6.15x|   10.38x|
|  **5**|5.53x|11.57x| 20.14x| 10.78x|   10.46x|
| **10**|4.17x|10.66x| 19.38x| 12.18x|    9.19x|
|**100**|1.20x| 3.24x| 12.24x|  9.05x|   10.84x|
|**250**|     | 1.19x|  6.00x|  4.38x|   10.03x|

The larger the original collection top elements are desired from, the bigger the speedup. Speedups of over **100x** (e.g. from 15 seconds to 150ms) have been observed for real-life datasets.

For certain problems you might find `MagicSort` and a bit of scripting a far cheaper alternative than a 20 node elastic map reduce cluster ;)

## Streams support

```java
List<Double> doubles = new ArrayList<>(); // Take a very very large list

/* and find the top 10 entries spreading the work across available cpu cores */
doubles.parallelStream().collect(MagicSort.toList(10)).forEach(System.out::println);
```

_Note: Do take into account how well the source collection can split the work in chunks. e.g. `LinkedList` is notoriously bad at this, since splitting the list includes its traversal._

## Use it

```maven
<dependency>
    <groupId>com.zigurs.karlis.utils</groupId>
    <artifactId>magicsort</artifactId>
    <version>0.1</version>
</dependency>
```

### Credits

```
                              //
(C) 2016 Karlis Zigurs (http://zigurs.com)
                            //
```
