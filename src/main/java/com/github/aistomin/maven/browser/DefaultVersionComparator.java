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

/**
 * The comparator that compares two standard numeric versions like 1.0.1
 * and 2.0.
 *
 * @since 2.0
 * @todo Implement #185 and remove this TODO.
 * @todo Implement #186 and remove this TODO.
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
        return null;
    }

    @Override
    public Boolean isApplicable() {
        return false;
    }
}
