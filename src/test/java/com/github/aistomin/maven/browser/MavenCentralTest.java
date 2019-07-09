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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The tests for {@link MavenCentral}.
 *
 * @since 0.1
 */
public final class MavenCentralTest {

    /**
     * Five.
     */
    public static final int FIVE = 5;

    /**
     * Three.
     */
    public static final int THREE = 3;

    /**
     * My previously created artifact which we can use for tests.
     */
    private final MvnArtifact mine = new MavenArtifact(
        "jenkins-sdk", new MavenGroup("com.github.aistomin")
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
        final MavenCentral mvn = new MavenCentral();
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
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(this.mine.name(), found.get(0).name());
        Assertions.assertEquals(
            this.mine.group().name(), found.get(0).group().name()
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
        final MavenCentral mvn = new MavenCentral();
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
}
