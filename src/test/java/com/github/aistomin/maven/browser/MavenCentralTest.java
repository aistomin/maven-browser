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
     * The size of the part which we load later to compare with total.
     */
    public static final int PART = 5;

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
            search, start, MavenCentralTest.PART
        );
        Assertions.assertEquals(MavenCentralTest.PART, part.size());
        part.forEach(
            artifact ->
                Assertions.assertEquals(
                    part.indexOf(artifact), artifacts.indexOf(artifact) - start
                )
        );
        final List<MvnArtifact> mine = mvn.findArtifacts("aistomin");
        Assertions.assertEquals(1, mine.size());
        Assertions.assertEquals("jenkins-sdk", mine.get(0).name());
        Assertions.assertEquals(
            "com.github.aistomin", mine.get(0).group().name()
        );
    }
}
