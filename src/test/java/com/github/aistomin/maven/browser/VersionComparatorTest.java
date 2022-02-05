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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The tests for {@link VersionComparator}.
 *
 * @since 0.2
 */
final class VersionComparatorTest {

    /**
     * Check that we can correctly detect the comparator for the provided
     * versions.
     */
    @Test
    void testIsApplicable() {
        final MvnArtifact fake = new MavenArtifact(
            new MavenGroup("org.fake"),
            "fake"
        );
        Assertions.assertInstanceOf(
            DefaultVersionComparator.class,
            VersionComparator.comparator(
                new MavenArtifactVersion(
                    fake, "2.17.1",
                    MvnPackagingType.JAR, System.currentTimeMillis()
                ),
                new MavenArtifactVersion(
                    fake, "2.3.2",
                    MvnPackagingType.JAR, System.currentTimeMillis()
                )
            )
        );
        Assertions.assertInstanceOf(
            DefaultVersionComparator.class,
            VersionComparator.comparator(
                new MavenArtifactVersion(
                    fake, "1.0",
                    MvnPackagingType.JAR, System.currentTimeMillis()
                ),
                new MavenArtifactVersion(
                    fake, "1.1",
                    MvnPackagingType.JAR, System.currentTimeMillis()
                )
            )
        );
        Assertions.assertInstanceOf(
            DefaultVersionComparator.class,
            VersionComparator.comparator(
                new MavenArtifactVersion(
                    fake, "1",
                    MvnPackagingType.JAR, System.currentTimeMillis()
                ),
                new MavenArtifactVersion(
                    fake, "1.1",
                    MvnPackagingType.JAR, System.currentTimeMillis()
                )
            )
        );
        Assertions.assertInstanceOf(
            DefaultVersionComparator.class,
            VersionComparator.comparator(
                new MavenArtifactVersion(
                    fake, "1",
                    MvnPackagingType.JAR, System.currentTimeMillis()
                ),
                new MavenArtifactVersion(
                    fake, "2",
                    MvnPackagingType.JAR, System.currentTimeMillis()
                )
            )
        );
        Assertions.assertInstanceOf(
            OldStyleVersionComparator.class,
            VersionComparator.comparator(
                new MavenArtifactVersion(
                    fake, "1.0.ALPHA1",
                    MvnPackagingType.JAR, System.currentTimeMillis()
                ),
                new MavenArtifactVersion(
                    fake, "1.0.ALPHA2",
                    MvnPackagingType.JAR, System.currentTimeMillis()
                )
            )
        );
    }
}
