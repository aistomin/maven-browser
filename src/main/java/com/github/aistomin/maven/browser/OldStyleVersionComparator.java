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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
            new ArrayList<>(Arrays.asList(this.first.name().split(pattern)));
        final List<String> two =
            new ArrayList<>(Arrays.asList(this.second.name().split(pattern)));
        if (one.size() != two.size()) {
            final Optional<List<String>> smallest =
                Stream.of(one, two).min(Comparator.comparing(List::size));
            final Optional<List<String>> biggest =
                Stream.of(one, two).max(Comparator.comparing(List::size));
            IntStream.range(0, biggest.get().size()).forEach(
                index -> {
                    final int size = smallest.get().size();
                    if (size == index) {
                        smallest.get().add(index, "0");
                    }
                }
            );
        }
        final OptionalInt difference = IntStream.range(0, one.size()).filter(
            idx -> idx <= (two.size() - 1) && !one.get(idx).equals(two.get(idx))
        ).findFirst();
        final AtomicBoolean result = new AtomicBoolean();
        if (difference.isPresent()) {
            final int idx = difference.getAsInt();
            final String regex = "^\\d+(\\.\\d+)*$";
            if (one.get(idx).matches(regex) && two.get(idx).matches(regex)) {
                return Long.parseLong(one.get(idx))
                    > Long.parseLong(two.get(idx));
            } else {
                result.set(one.get(idx).compareTo(two.get(idx)) > 0);
            }
        } else {
            result.set(false);
        }
        return result.get();
    }

    @Override
    public Boolean isApplicable() {
        return true;
    }
}
