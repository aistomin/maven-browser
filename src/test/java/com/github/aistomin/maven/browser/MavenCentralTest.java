/*
 * Copyright (c) 2019, Istomin Andrei
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
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The tests for {@link MavenCentral}.
 *
 * @since 0.1
 */
final class MavenCentralTest {

    /**
     * Three.
     */
    private static final int THREE = 3;

    /**
     * Four.
     */
    private static final int FOUR = 4;

    /**
     * Five.
     */
    private static final int FIVE = 5;

    /**
     * Ten.
     */
    private static final int TEN = 10;

    /**
     * My previously created artifact which we can use for tests.
     */
    private final MvnArtifact mine = new MavenArtifact(
        new MavenGroup("com.github.aistomin"), "jenkins-sdk"
    );

    /**
     * The list of the versions of my artifact which we use for this test.
     */
    private final List<String> vers = Arrays.asList(
        "0.2.1",
        "0.2",
        "0.1",
        "0.0.2",
        "0.0.1"
    );

    /**
     * Check that we correctly find the artifacts with start and rows
     * parameters.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifacts() throws Exception {
        final MvnRepo mvn = new MavenCentral();
        final String search = "guice";
        final List<MvnArtifact> artifacts = mvn.findArtifacts(
            search, 0, MavenCentral.MAX_ROWS
        );
        Assertions.assertEquals(
            MavenCentral.MAX_ROWS, artifacts.size()
        );
        final List<MvnArtifact> def = mvn.findArtifacts(search);
        Assertions.assertEquals(
            artifacts.size(), def.size()
        );
        def.forEach(
            artifact ->
                Assertions.assertEquals(
                    def.indexOf(artifact), artifacts.indexOf(artifact)
                )
        );
        final int start = 2;
        final List<MvnArtifact> part = mvn.findArtifacts(
            search, start, MavenCentralTest.FIVE
        );
        Assertions.assertEquals(MavenCentralTest.FIVE, part.size());
        part.forEach(
            artifact ->
                Assertions.assertEquals(
                    part.indexOf(artifact), artifacts.indexOf(artifact) - start
                )
        );
        final List<MvnArtifact> found = mvn.findArtifacts("aistomin");
        Assertions.assertEquals(MavenCentralTest.FOUR, found.size());
        Assertions.assertNotNull(
            found.stream()
                .filter(artifact -> artifact.equals(this.mine))
                .findFirst()
                .get()
        );
    }

    /**
     * Check that we correctly throw exceptions if something went wrong.
     */
    @Test
    void testExceptions() {
        final MvnRepo repo = new MavenCentral("http://not.existing.mvn/");
        Assertions.assertThrows(
            MvnException.class,
            () -> repo.findArtifacts("someartifact")
        );
        Assertions.assertThrows(
            MvnException.class,
            () -> repo.findVersions(this.mine)
        );
    }

    /**
     * Check that we correctly find the versions of the artifact.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersions() throws Exception {
        final List<MvnArtifactVersion> versions =
            new MavenCentral().findVersions(this.mine);
        Assertions.assertEquals(MavenCentralTest.FIVE, versions.size());
        for (final MvnArtifactVersion ver : versions) {
            Assertions.assertEquals(this.mine.name(), ver.artifact().name());
            Assertions.assertEquals(
                this.mine.group().name(), ver.artifact().group().name()
            );
            Assertions.assertTrue(this.vers.contains(ver.name()));
        }
        final List<MvnArtifactVersion> reduced =
            new MavenCentral().findVersions(this.mine, 1, 2);
        Assertions.assertEquals(2, reduced.size());
        Assertions.assertEquals(
            this.vers.get(1), reduced.get(0).name()
        );
        Assertions.assertEquals(
            this.vers.get(2), reduced.get(1).name()
        );
    }

    /**
     * Check that we correctly find the versions which are newer than provided
     * one.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsNewerThan() throws Exception {
        final MvnRepo mvn = new MavenCentral();
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> {
                mvn.findVersionsNewerThan(
                    new MavenArtifactVersion(
                        this.mine,
                        "not-existing",
                        MvnPackagingType.JAR,
                        System.currentTimeMillis()
                    )
                );
            }
        );
        final MvnArtifactVersion version = mvn.findVersions(this.mine)
            .stream()
            .filter(
                ver ->
                    this.vers.get(this.vers.size() - 2).equals(ver.name())
            ).findFirst().get();
        final List<MvnArtifactVersion> newer =
            mvn.findVersionsNewerThan(version);
        Assertions.assertEquals(MavenCentralTest.THREE, newer.size());
        Assertions.assertEquals(this.vers.get(0), newer.get(0).name());
        Assertions.assertEquals(this.vers.get(1), newer.get(1).name());
        Assertions.assertEquals(this.vers.get(2), newer.get(2).name());
    }

    /**
     * Check that we correctly find the versions which are older than provided
     * one.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsOlderThan() throws Exception {
        final MvnRepo mvn = new MavenCentral();
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> {
                mvn.findVersionsNewerThan(
                    new MavenArtifactVersion(
                        this.mine,
                        "wrong-version",
                        MvnPackagingType.JAR,
                        System.currentTimeMillis()
                    )
                );
            }
        );
        final MvnArtifactVersion version = mvn.findVersions(this.mine)
            .stream()
            .filter(
                ver ->
                    this.vers.get(2).equals(ver.name())
            ).findFirst().get();
        final List<MvnArtifactVersion> older =
            mvn.findVersionsOlderThan(version);
        Assertions.assertEquals(2, older.size());
        Assertions.assertEquals(
            this.vers.get(this.vers.size() - 2), older.get(0).name()
        );
        Assertions.assertEquals(
            this.vers.get(this.vers.size() - 1), older.get(1).name()
        );
    }

    /**
     * Check that we correctly find the versions which are newer than provided
     * one.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testCompareVersionsByNumber() throws Exception {
        final MvnRepo mvn = new MavenCentral();
        final MavenArtifactVersion current = new MavenArtifactVersion(
            new MavenArtifact(
                new MavenGroup("org.junit.jupiter"),
                "junit-jupiter-api"
            ),
            "5.7.0",
            MvnPackagingType.JAR,
            System.currentTimeMillis()
        );
        final List<MvnArtifactVersion> newer =
            mvn.findVersionsNewerThan(current);
        for (final MvnArtifactVersion version : newer) {
            Assertions.assertTrue(
                this.calculateNumberFromVersion(version.name())
                    > this.calculateNumberFromVersion(current.name()),
                String.format(
                    "Version %s is not newer than %s.",
                    version.name(), current.name()
                )
            );
        }
    }

    /**
     * Calculate a numeric representation of the version.
     *
     * @param version The version.
     * @return The numeric representation.
     */
    private Double calculateNumberFromVersion(final String version) {
        final String clean = version.replaceAll("[^\\d.]", "");
        final List<String> nums = Arrays.asList(clean.split("\\."));
        return IntStream
            .range(0, nums.size())
            .mapToDouble(
                index ->
                Integer.parseInt(nums.get(index))
                    * Math.pow(MavenCentralTest.TEN, nums.size() - index - 1)
            )
            .sum();
    }
}
