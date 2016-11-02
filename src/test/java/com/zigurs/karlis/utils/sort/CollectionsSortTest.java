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
         * Special case. Collections sort is happy to proceed with null comparator.
         */
        if (comparator == null)
            throw new NullPointerException("Collections.sort() proceeds without one");

        return tempList.subList(0, Math.min(limit, tempList.size()));
    }

}
