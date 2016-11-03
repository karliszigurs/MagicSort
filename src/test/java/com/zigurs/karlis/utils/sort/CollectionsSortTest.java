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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionsSortTest extends MagicSortTestCases {

    protected <X> List<X> sortFunction(final Collection<? extends X> sourceToSort,
                                       final int limit,
                                       final Comparator<? super X> comparator) {
        // Convert to list and filter nulls
        List<X> tempList = sourceToSort.stream().filter(i -> i != null).collect(Collectors.toList());

        Collections.sort(tempList, comparator);

        /*
         * Special case. Collections sort is happy to proceed with null comparator if
         * the type is Comparable.
         */
        if (comparator == null)
            throw new NullPointerException("Collections.sort() proceeds without one");

        return tempList.subList(0, Math.min(limit, tempList.size()));
    }

}
