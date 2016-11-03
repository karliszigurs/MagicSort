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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MagicSortTest extends MagicSortTestCases {

    protected <X> List<X> sortFunction(final Collection<? extends X> sourceToSort,
                                       final int limit,
                                       final Comparator<? super X> comparator) {
        return MagicSort.sortAndLimit(sourceToSort, limit, comparator);
    }

    @Test
    public void testNaturalComparator() {
        List<Double> doubles = new ArrayList<>();
        for (int i = 0; i < 100; i++)
            doubles.add((double) i);

        assertEquals(0.0, MagicSort.sortAndLimit(doubles, 10).get(0), 0.0);
    }

    @Test
    public void testReverseComparator() {
        List<Double> doubles = new ArrayList<>();
        for (int i = 0; i < 100; i++)
            doubles.add((double) i);

        assertEquals(99.0, MagicSort.sortReverseAndLimit(doubles, 10).get(0), 0.0);
    }
}
