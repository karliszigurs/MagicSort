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
package com.zigurs.karlis.utils;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class MagicSortTestCases {

    @Test
    public void reverseSortSortedList() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 1_000; i++)
            doubles.add((double) i);

        assertEquals(doubles.get(0), 0.0, 0.0);

        /*
         * Reverse sorting a sorted list, this will trigger a
         * new top item for every visited entry. Worst case scenario from
         * O performance perspective.
         */
        assertEquals(sortFunction(doubles, 10, Comparator.reverseOrder()).get(0), 999, 0.0);
    }

    @Test
    public void sortAlreadySortedList() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 100_000; i++)
            doubles.add((double) i);

        assertEquals(doubles.get(0), 0.0, 0.0);

        /*
         * Sorting already sorted list. After first N visits
         * all remaining items will be early-discarded.
         */
        assertEquals(sortFunction(doubles, 10, Double::compareTo).get(0), 0.0, 0.0);
    }

    @Test
    public void correctResultListSize_1() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 10; i++)
            doubles.add((double) i);

        assertEquals(1, sortFunction(doubles, 1, Double::compareTo).size());
    }

    @Test
    public void correctResultListSize_0() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 10; i++)
            doubles.add((double) i);

        assertEquals(0, sortFunction(doubles, 0, Double::compareTo).size());
    }

    @Test
    public void correctResultListSize_10() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 10; i++)
            doubles.add((double) i);

        assertEquals(10, sortFunction(doubles, 10, Double::compareTo).size());
    }

    @Test
    public void correctResultListSize_11() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 10; i++)
            doubles.add((double) i);

        assertEquals(10, sortFunction(doubles, 11, Double::compareTo).size());
    }

    @Test
    public void correctResultListSize_MAX() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 10; i++)
            doubles.add((double) i);

        assertEquals(10, sortFunction(doubles, Integer.MAX_VALUE, Double::compareTo).size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void correctResultListSize__1() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 10; i++)
            doubles.add((double) i);

        assertEquals(0, sortFunction(doubles, -1, Double::compareTo).size());
    }

    @Test
    public void correctNaturalOrderSort() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            doubles.add((double) i);

        doubles.add(-50.0);
        doubles.add(50_000.0);

        Collections.shuffle(doubles);

        List<Double> result = sortFunction(doubles, 10, Double::compareTo);

        assertEquals(-50.0, result.get(0), 0);
        assertEquals(0.0, result.get(1), 0);
        assertEquals(1.0, result.get(2), 0);
        assertEquals(2.0, result.get(3), 0);
        assertEquals(8.0, result.get(9), 0);
    }

    @Test
    public void correctReverseOrderSort() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            doubles.add((double) i);

        doubles.add(-50.0);
        doubles.add(50_000.0);

        Collections.shuffle(doubles);

        List<Double> result = sortFunction(doubles, 10, Comparator.reverseOrder());

        assertEquals(50_000.0, result.get(0), 0);
        assertEquals(99.0, result.get(1), 0);
        assertEquals(98.0, result.get(2), 0);
        assertEquals(97.0, result.get(3), 0);
        assertEquals(91.0, result.get(9), 0);
    }

    @Test
    public void identicalElements() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            doubles.add(1.0);

        Collections.shuffle(doubles);

        List<Double> result = sortFunction(doubles, 10, Double::compareTo);

        assertEquals(1.0, result.get(0), 0.0);
        assertEquals(1.0, result.get(1), 0.0);
        assertEquals(1.0, result.get(2), 0.0);
        assertEquals(1.0, result.get(3), 0.0);
        assertEquals(1.0, result.get(9), 0.0);
        assertEquals(10, result.size());
    }

    @Test
    public void repeatingElements() {
        List<Double> doubles = new ArrayList<>();

        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 25; y++)
                doubles.add((double) y);

        Collections.shuffle(doubles);

        List<Double> result = sortFunction(doubles, 10, Double::compareTo);

        assertEquals(0.0, result.get(0), 0.0);
        assertEquals(0.0, result.get(1), 0.0);
        assertEquals(0.0, result.get(2), 0.0);
        assertEquals(0.0, result.get(3), 0.0);
        assertEquals(1.0, result.get(4), 0.0);
        assertEquals(1.0, result.get(5), 0.0);
        assertEquals(2.0, result.get(9), 0.0);
        assertEquals(10, result.size());
    }

    @Test
    public void nullsAndElement() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            doubles.add(null);

        doubles.add(1.0);

        Collections.shuffle(doubles);

        List<Double> result = sortFunction(doubles, 10, Double::compareTo);

        assertEquals(1.0, result.get(0), 0.0);
        assertEquals(1, result.size());
    }

    @Test
    public void oneNull() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            doubles.add((double) i);

        doubles.add(null);

        Collections.shuffle(doubles);

        List<Double> result = sortFunction(doubles, 10, Double::compareTo);

        assertEquals(0.0, result.get(0), 0.0);
        assertEquals(10, result.size());
    }

    @Test
    public void onlyNulls() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            doubles.add(null);

        Collections.shuffle(doubles);

        List<Double> result = sortFunction(doubles, 10, Double::compareTo);

        assertTrue(result.isEmpty());
    }

    @Test
    public void badComparatorMin() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            doubles.add((double) i);

        /* Technically the results here would be undefined */
        assertEquals(10, sortFunction(doubles, 10, (d1, d2) -> Integer.MIN_VALUE).size());
    }

    @Test
    public void badComparatorMax() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            doubles.add((double) i);

        /* Technically the results here would be undefined */
        assertEquals(10, sortFunction(doubles, 10, (d1, d2) -> Integer.MAX_VALUE).size());
    }

    @Test
    public void badComparatorZero() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            doubles.add((double) i);

        /* Technically the results here would be undefined */
        assertEquals(10, sortFunction(doubles, 10, (d1, d2) -> 0).size());
    }

    @Test
    public void badComparatorRandom() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 100_000; i++)
            doubles.add((double) i);

        Random random = new Random();

        /*
         * This takes undefined to a whole new level.
         *
         * What can I say, I was curious...
         */
        Comparator<Double> comparator = (d1, d2) -> random.nextInt(3) - 1;

        try {
            assertEquals(10, sortFunction(doubles, 10, comparator).size());
        } catch (IllegalArgumentException iae) {
            // Expected, but only from TimSort impl.
        }
    }

    @Test(expected = NullPointerException.class)
    public void nullSource() {
        List<Double> result = sortFunction(null, 10, Double::compareTo);

        assertTrue(result.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void nullComparatorNoEntries() {
        List<Double> result = sortFunction(new ArrayList<>(), 10, null);

        assertTrue(result.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void nullComparatorWithEntries() {
        List<Double> doubles = new ArrayList<>();

        for (int i = 0; i < 10; i++)
            doubles.add((double) i);

        List<Double> result = sortFunction(doubles, 10, null);

        assertTrue(result.isEmpty());
    }

    /*
     * Inherit and implement to verify behaviour of different sorting implementations.
     *
     * The test cases assume that the nulls in the input will be ignored (e.g. sorting a
     * collection consisting of only null entries will result in empty list as a result).
     */
    protected abstract <X> List<X> sortFunction(final Collection<? extends X> sourceToSort, final int limit, Comparator<? super X> comparator);
}