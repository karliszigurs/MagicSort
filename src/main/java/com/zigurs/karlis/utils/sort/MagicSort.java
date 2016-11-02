/*
 * Copyright 2016 Karlis Zigurs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zigurs.karlis.utils.sort;

import java.util.*;
import java.util.stream.Collector;

/**
 * Purpose built sort discarding known beyond-the-cut elements early.
 * Trades the cost of manual insertion against the cost of having to sort whole array.
 * <p>
 * Compares well to built in sort functions on smaller datasets (&lt;1000 elements), but
 * significantly outperforms built-in sort algorithms on larger datasets.
 */
public class MagicSort {

    /**
     * MagicSort function which delegates the sorting to likely more efficient
     * internal sorting implementation for the given input size and desired
     * results set size
     *
     * @param inputCollection collection to select elements from
     * @param limitResultsTo  maximum size of generated ordered list, negative or 0 will return empty list
     * @param comparator      comparator to use (or use Comparator.naturalOrder())
     * @param <X>             type of objects to sort
     * @return sorted list consisting of first (up to limitResultsTo) elements in specified comparator order
     */
    public static <X> List<X> sortAndLimit(final Collection<? extends X> inputCollection,
                                           final int limitResultsTo,
                                           final Comparator<? super X> comparator) {
        Objects.requireNonNull(inputCollection);
        Objects.requireNonNull(comparator);

        // Keep consistent with the native API
        if (limitResultsTo < 0)
            throw new IllegalArgumentException(String.valueOf(limitResultsTo));

        if (limitResultsTo < 1 || inputCollection.isEmpty())
            return Collections.emptyList();

        return sortAndLimitWithArrayAndBinarySearch(inputCollection, limitResultsTo, comparator);
    }

    public static <X> List<X> sortAndLimitWithArray(final Collection<? extends X> inputCollection,
                                                    final int limitResultsTo,
                                                    final Comparator<? super X> comparator) {
        Objects.requireNonNull(inputCollection);
        Objects.requireNonNull(comparator);

        // Keep consistent with the native API
        if (limitResultsTo < 0)
            throw new IllegalArgumentException(String.valueOf(limitResultsTo));

        if (limitResultsTo < 1 || inputCollection.isEmpty())
            return Collections.emptyList();


        //noinspection unchecked
        X[] array = (X[]) new Object[Math.min(inputCollection.size(), limitResultsTo)];

        X lastEntry = null;
        int populated = 0;

        for (X entry : inputCollection) {
            if (entry == null)
                continue;

            if (lastEntry == null) { //handle initial population
                for (int pos = 0; pos < array.length; pos++) {
                    if (array[pos] == null || comparator.compare(entry, array[pos]) < 0) {
                        System.arraycopy(array, pos, array, pos + 1, array.length - (pos + 1));
                        array[pos] = entry;
                        break;
                    }
                }
                populated++;
                lastEntry = array[array.length - 1];
            } else if (comparator.compare(entry, lastEntry) < 0) {
                for (int pos = 0; pos < array.length; pos++) {
                    if (comparator.compare(entry, array[pos]) < 0) {
                        System.arraycopy(array, pos, array, pos + 1, array.length - (pos + 1));
                        array[pos] = entry;
                        break;
                    }
                }
                lastEntry = array[array.length - 1];
            }
        }

        if (populated == limitResultsTo)
            return Arrays.asList(array);
        else
            return Arrays.asList(Arrays.copyOfRange(array, 0, populated));
    }

    public static <X> List<X> sortAndLimitWithArrayAndBinarySearch(final Collection<? extends X> inputCollection,
                                                                   final int limitResultsTo,
                                                                   final Comparator<? super X> comparator) {
        Objects.requireNonNull(inputCollection);
        Objects.requireNonNull(comparator);

        // Keep consistent with the native API
        if (limitResultsTo < 0)
            throw new IllegalArgumentException(String.valueOf(limitResultsTo));

        if (limitResultsTo < 1 || inputCollection.isEmpty())
            return Collections.emptyList();

        //noinspection unchecked
        X[] array = (X[]) new Object[Math.min(inputCollection.size(), limitResultsTo)];

        X lastEntry = null;
        int populated = 0;

        for (X entry : inputCollection) {
            if (entry == null)
                continue;

            if (lastEntry == null) { //handle initial population
                for (int pos = 0; pos < array.length; pos++) {
                    if (array[pos] == null || comparator.compare(entry, array[pos]) < 0) {
                        System.arraycopy(array, pos, array, pos + 1, array.length - (pos + 1));
                        array[pos] = entry;
                        break;
                    }
                }
                populated++;
                lastEntry = array[array.length - 1];

            } else if (comparator.compare(entry, lastEntry) < 0) {
                int insertionPoint = Arrays.binarySearch(array, entry, comparator);

                // Catch bad comparators
                if (insertionPoint == -(array.length + 1)) {
                    continue;
                }

                int pos = insertionPoint;

                if (insertionPoint < 0)
                    pos = (-insertionPoint) - 1;

                System.arraycopy(array, pos, array, pos + 1, array.length - (pos + 1));
                array[pos] = entry;

                lastEntry = array[array.length - 1];
            }
        }

        if (populated == limitResultsTo)
            return Arrays.asList(array);
        else
            return Arrays.asList(Arrays.copyOfRange(array, 0, populated));
    }

    public static <T extends Comparable<T>> Collector<T, ?, List<T>> toListNaturalOrder(final int limitResultsTo) {
        return toList(limitResultsTo, Comparator.naturalOrder());
    }

    public static <T extends Comparable<T>> Collector<T, ?, List<T>> toListReverseOrder(final int limitResultsTo) {
        return toList(limitResultsTo, Comparator.reverseOrder());
    }

    public static <T> Collector<T, MagicSortCollector<T>, List<T>> toList(final int limitResultsTo,
                                                                          final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        if (limitResultsTo < 0)
            throw new IllegalArgumentException(String.valueOf(limitResultsTo));

        return Collector.of(
                () -> new MagicSortCollector<>(limitResultsTo, comparator),
                MagicSortCollector::add,
                MagicSortCollector::merge,
                MagicSortCollector::toList
        );
    }

    private static class MagicSortCollector<T> {

        private final int limitResultsTo;
        private final Comparator<? super T> comparator;

        private final T[] array;

        private T lastEntry;
        private int populated;

        private MagicSortCollector(final int limitResultsTo, final Comparator<? super T> comparator) {
            this.limitResultsTo = limitResultsTo;
            this.comparator = comparator;

            /*
             * TODO - Can't limit the top limit as we don't know the input array size.
             * Need to come up with a cunning resizing plan...
             *
             * Capping at arbitrary large, but feasible size for now.
             */
            //noinspection unchecked
            array = (T[]) new Object[Math.min(limitResultsTo, 100_000)];
        }

        private void add(T entry) {
            if (entry == null || array.length == 0)
                return;

            if (lastEntry == null) { //handle initial population
                for (int pos = 0; pos < array.length; pos++) {
                    if (array[pos] == null || comparator.compare(entry, array[pos]) < 0) {
                        System.arraycopy(array, pos, array, pos + 1, array.length - (pos + 1));
                        array[pos] = entry;
                        break;
                    }
                }
                populated++;
                lastEntry = array[array.length - 1];

            } else if (comparator.compare(entry, lastEntry) < 0) {
                int insertionPoint = Arrays.binarySearch(array, entry, comparator);

                // Catch bad comparators. Yes, they exist...
                if (insertionPoint == -(array.length + 1)) {
                    return;
                }

                int pos = insertionPoint;

                if (insertionPoint < 0)
                    pos = (-insertionPoint) - 1;

                System.arraycopy(array, pos, array, pos + 1, array.length - (pos + 1));
                array[pos] = entry;

                lastEntry = array[array.length - 1];
            }
        }

        private MagicSortCollector<T> merge(MagicSortCollector<T> right) {
            for (int i = 0; i < right.populated; i++)
                if (right.array[i] != null)
                    add(right.array[i]);

            return this;
        }

        private List<T> toList() {
            if (populated == limitResultsTo)
                return Arrays.asList(array);
            else
                return Arrays.asList(Arrays.copyOfRange(array, 0, populated));
        }
    }
}
