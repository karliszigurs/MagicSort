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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class MagicSortCollectorTest extends MagicSortTestCases {

    protected <X> List<X> sortFunction(final Collection<? extends X> sourceToSort,
                                       final int limit,
                                       final Comparator<? super X> comparator) {
        return sourceToSort.stream().collect(MagicSort.toList(limit, comparator));
    }

}
