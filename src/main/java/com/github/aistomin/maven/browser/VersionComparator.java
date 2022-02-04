/*
 * Copyright (c) 2019-2022, Istomin Andrei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.aistomin.maven.browser;

import java.util.Arrays;
import java.util.List;

/**
 * The interface of the classes that compare two versions.
 *
 * @since 2.0
 */
public interface VersionComparator {

    /**
     * Check if the first version is bigger than the second one.
     *
     * @return True - first version is bigger than the second. False - first
     *  version is smaller or equal than the second one.
     */
    Boolean isFirstBiggerThanSecond();

    /**
     * Is this comparator applicable for the provided versions.
     *
     * @return True - the comparator is applicable; False - the comparator is
     * not applicable.
     */
    Boolean isApplicable();

    /**
     * Get an appropriate comparator of the two provided versions.
     *
     * @param first Version A.
     * @param second Version B.
     * @return The proper comparator.
     */
    static VersionComparator comparator(
        final MvnArtifactVersion first,
        final MvnArtifactVersion second
    ) {
        final List<VersionComparator> all = Arrays.asList(
            new DefaultVersionComparator(first, second),
            new OldStyleVersionComparator(first, second)
        );
        return all
            .stream()
            .filter(VersionComparator::isApplicable)
            .findFirst()
            .get();
    }
}
