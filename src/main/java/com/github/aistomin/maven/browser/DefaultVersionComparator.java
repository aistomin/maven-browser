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
import java.util.stream.Collectors;

/**
 * The comparator that compares two standard numeric versions like 1.0.1
 * and 2.0.
 *
 * @since 2.0
 */
public final class DefaultVersionComparator implements VersionComparator {

    /**
     * The first version.
     */
    private final MvnArtifactVersion first;

    /**
     * The second version.
     */
    private final MvnArtifactVersion second;

    /**
     * Ctor.
     *
     * @param one The first version.
     * @param two The second version.
     */
    public DefaultVersionComparator(
        final MvnArtifactVersion one, final MvnArtifactVersion two
    ) {
        this.first = one;
        this.second = two;
    }

    @Override
    public Boolean isFirstBiggerThanSecond() {
        if (!this.isApplicable()) {
            throw new IllegalStateException(
                "The comparator is not applicable to the provided versions."
            );
        }
        final String dot = "\\.";
        final List<Long> one = Arrays
            .stream(this.first.name().split(dot))
            .map(Long::valueOf)
            .collect(Collectors.toList());
        final List<Long> two = Arrays
            .stream(this.second.name().split(dot))
            .map(Long::valueOf)
            .collect(Collectors.toList());
        for (int index = 0; index < Math.min(one.size(), two.size()); index++) {
            if (one.get(index) != two.get(index)) {
                return one.get(index) > two.get(index);
            }
        }
        return false;
    }

    @Override
    public Boolean isApplicable() {
        final String regex = "^\\d+(\\.\\d+)*$";
        return this.first.name().matches(regex)
            && this.second.name().matches(regex);
    }
}
