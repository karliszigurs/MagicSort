/*
 *                                     //
 * Copyright 2016 Karlis Zigurs (http://zigurs.com)
 *                                   //
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
import java.util.stream.Collectors;

/**
 * Sort functions returning top (or bottom) n items from the provided source {@code Collection}.
 * <p>
 * Useful in cases where only a small subset from the available items is required
 * (e.g. for user interaction) in a manner more efficient than JDK built-in sort
 * operations.
 * <p>
 * The overall contract of the sort functions available here is to ignore {@code null} elements
 * (e.g. any {@code null} elements in the source collection will be silently discarded) and allow
 * repeat and equals elements.
 */
public class MagicSort {

    private MagicSort() {
        // Not instantiable
    }

    /**
     * Catch-all sort function for {@code Comparable} object collections which may or
     * may not choose to use different sort implementation based on the size of {@code Collection}
     * and desired number of items requested.
     *
     * @param inputCollection collection to sort and select results from
     * @param limitResultsTo  maximum number of results to return
     * @param <X>             collection elements type
     * @return up to first n elements from a list sorted by a natural comparator for the type
     */
    public static <X extends Comparable<X>> List<X> sortAndLimit(final Collection<? extends X> inputCollection,
                                                                 final int limitResultsTo) {
        return sortAndLimit(inputCollection, limitResultsTo, Comparator.naturalOrder());
    }

    /**
     * Catch-all sort function for {@code Comparable} object collections which may or may not
     * choose to use different sort implementation based on the size of {@code Collection}
     * and desired number of items requested.
     *
     * @param inputCollection collection to sort and select results from
     * @param limitResultsTo  maximum number of results to return
     * @param <X>             collection elements type
     * @return up to first n elements from a list sorted by a reverse natural comparator for the type
     */
    public static <X extends Comparable<X>> List<X> sortReverseAndLimit(final Collection<? extends X> inputCollection,
                                                                        final int limitResultsTo) {
        return sortAndLimit(inputCollection, limitResultsTo, Comparator.reverseOrder());
    }

    /**
     * Catch-all sort function entry point which may or may not choose to use different sort
     * implementation based on the size of {@code Collection} and number of items requested.
     *
     * @param inputCollection collection to sort and select results from
     * @param limitResultsTo  maximum size of resulting ordered list
     * @param comparator      comparator for the specified collection
     * @param <X>             elements type
     * @return list sorted in the provided comparator order containing up to {@code limitResultsTo} elements
     */
    public static <X> List<X> sortAndLimit(final Collection<? extends X> inputCollection,
                                           final int limitResultsTo,
                                           final Comparator<? super X> comparator) {
        Objects.requireNonNull(inputCollection);

        /* If we are going to sort the whole array the native sort is
         * far faster than brute force insertions I use for collections of
         * any size.
         */
        if (limitResultsTo >= inputCollection.size() && inputCollection.size() > 1000) {
            return inputCollection.stream()
                    .filter(i -> i != null)
                    .sorted()
                    .collect(Collectors.toList());
        } else {
            return sortAndLimitBSearch(inputCollection, limitResultsTo, comparator);
        }
    }

    /**
     * Sort function using binary search on accumulating array to insert the elements
     * it determines to be part of desired result set during sort and selection.
     *
     * @param inputCollection collection to sort and select results from
     * @param limitResultsTo  maximum size of resulting ordered list
     * @param comparator      comparator for the specified collection
     * @param <X>             elements type
     * @return list sorted in the provided comparator order containing up to {@code limitResultsTo} elements
     */
    private static <X> List<X> sortAndLimitBSearch(final Collection<? extends X> inputCollection,
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

            if (populated < array.length) {

                array[populated++] = entry;

                if (populated == array.length) { // population finished, sort in-place and proceed with boundary checks
                    Arrays.parallelSort(array, comparator);
                    lastEntry = array[array.length - 1];
                }

            } else if (comparator.compare(entry, lastEntry) < 0) {

                int insertionPoint = Arrays.binarySearch(array, entry, comparator);

                // Catch bad comparators that lie.
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

        if (populated < array.length) { /* Special case - the working array is only partially filled and therefore still unsorted as well */
            X[] subArray = Arrays.copyOfRange(array, 0, populated);
            Arrays.parallelSort(subArray, comparator);
            return Arrays.asList(subArray);
        } else {
            return Arrays.asList(array);
        }
    }

    /**
     * Returns a {@code Collector} that accumulates the top n {@code Comparable} (as determined
     * by natural {@code Comparator}) encountered elements into a new sorted {@code List}.
     *
     * @param limitResultsTo number of top elements to accumulate
     * @param <T>            type
     * @return list of 0 to limitResultsTo elements sorted in natural order
     */
    public static <T extends Comparable<T>> Collector<T, ?, List<T>> toList(final int limitResultsTo) {
        return toList(limitResultsTo, Comparator.naturalOrder());
    }

    /**
     * Returns a {@code Collector} that accumulates the top n {@code Comparable} (as determined
     * by natural reverse {@code Comparator}) encountered elements into a new sorted {@code List}.
     *
     * @param limitResultsTo number of top elements to accumulate
     * @param <T>            type
     * @return list of 0 to limitResultsTo elements sorted in reversed natural order
     */
    public static <T extends Comparable<T>> Collector<T, ?, List<T>> toListReverseOrder(final int limitResultsTo) {
        return toList(limitResultsTo, Comparator.reverseOrder());
    }

    /**
     * Returns a {@code Collector} that accumulates the top n (as determined by the
     * supplied {@code Comparator}) encountered elements into a new sorted {@code List}.
     *
     * @param limitResultsTo number of top elements to accumulate
     * @param comparator     comparator to use to determine top elements
     * @param <T>            type
     * @return list of 0 to limitResultsTo elements sorted by the provided comparator
     */
    public static <T> Collector<T, MagicSortCollector<T>, List<T>> toList(final int limitResultsTo,
                                                                          final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);

        if (limitResultsTo < 0)
            throw new IllegalArgumentException(String.valueOf(limitResultsTo));

        /*
         * This is an annoying one. As we can't bound the size of the target array by examining
         * source collection size we have to put _some_ limit on (otherwise it's going to be an OOM
         * on every instance creation if the limit is ... out there. Not to mention the
         * efficiency of inserting into an array that size.
         *
         * If you read this you probably know what you are doing and a hat off
         * to your dataset that justifies it. Ping me if you need help sorting it out.
         *
         * But for now you are going to get an exception.
         */
        if (limitResultsTo > 100_000)
            throw new IllegalArgumentException(
                    String.format("Requested limit of %d is too large for reliable operation.", limitResultsTo)
            );

        return Collector.of(
                () -> new MagicSortCollector<>(limitResultsTo, comparator),
                MagicSortCollector::add,
                MagicSortCollector::merge,
                MagicSortCollector::toList
        );
    }

    /**
     * Basic collector to perform top-n sorting. Uses the same logic as the binary tree
     * accumulator above, but wrapped in a stateful class to comply with {@link Collector}
     * semantics.
     *
     * @param <T> type of items to accumulate
     */
    private static class MagicSortCollector<T> {

        /* Configuration */
        private final Comparator<? super T> comparator;

        /* Working array where results are accumulated */
        private final T[] array;

        /* Working state tracking */
        private T lastEntry;
        private int populated;

        private MagicSortCollector(final int limitResultsTo, final Comparator<? super T> comparator) {
            this.comparator = comparator;

            //noinspection unchecked
            array = (T[]) new Object[limitResultsTo];
        }

        /*
         * Handle accumulating new entries
         */
        private void add(T entry) {
            if (entry == null || array.length == 0)
                return;

            if (populated < array.length) {

                array[populated++] = entry;

                if (populated == array.length) { // population finished, sort in-place and proceed with boundary checks
                    Arrays.parallelSort(array, comparator);
                    lastEntry = array[array.length - 1];
                }

            } else if (comparator.compare(entry, lastEntry) < 0) {
                /*
                 * And after filling the initial array start discarding new entries less than
                 * the last element or inserting (and shifting existing set) them in their
                 * rightful place.
                 *
                 * The early discard bit is the speedup here.
                 */
                int insertionPoint = Arrays.binarySearch(array, entry, comparator);

                // Catch bad comparators. Yes, they exist...
                if (insertionPoint == -(array.length + 1))
                    return;

                int pos = insertionPoint;

                if (insertionPoint < 0)
                    pos = (-insertionPoint) - 1;

                System.arraycopy(array, pos, array, pos + 1, array.length - (pos + 1));
                array[pos] = entry;

                lastEntry = array[array.length - 1];
            }
        }

        /*
         * Handle joining multiple results together in case of
         * parallel streams.
         */
        private MagicSortCollector<T> merge(MagicSortCollector<T> right) {
            for (int i = 0; i < right.populated; i++)
                add(right.array[i]);

            return this;
        }

        /*
         * Finalizer to wrap the working array into list
         */
        private List<T> toList() {
            /* Special case - the working array is only partially filled and therefore still unsorted as well */
            if (populated < array.length) {
                T[] subArray = Arrays.copyOfRange(array, 0, populated);
                Arrays.parallelSort(subArray, comparator);
                return Arrays.asList(subArray);
            } else {
                return Arrays.asList(array);
            }
        }
    }
}
