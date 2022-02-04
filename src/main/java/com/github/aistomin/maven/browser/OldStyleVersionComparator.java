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
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * The comparator that compares two versions in the old style manner that we had
 * before version 2.0.
 *
 * @since 2.0
 */
public final class OldStyleVersionComparator implements VersionComparator {

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
    public OldStyleVersionComparator(
        final MvnArtifactVersion one, final MvnArtifactVersion two
    ) {
        this.first = one;
        this.second = two;
    }

    @Override
    public Boolean isFirstBiggerThanSecond() {
        final String pattern = "\\.";
        final List<String> one =
            Arrays.asList(this.first.name().split(pattern));
        final List<String> two =
            Arrays.asList(this.second.name().split(pattern));
        final OptionalInt difference = IntStream.range(0, one.size()).filter(
            idx -> idx <= (two.size() - 1) && !one.get(idx).equals(two.get(idx))
        ).findFirst();
        final AtomicBoolean result = new AtomicBoolean();
        if (difference.isPresent()) {
            final int idx = difference.getAsInt();
            result.set(one.get(idx).compareTo(two.get(idx)) > 0);
        } else {
            result.set(one.size() > two.size());
        }
        return result.get();
    }

    @Override
    public Boolean isApplicable() {
        return true;
    }
}
