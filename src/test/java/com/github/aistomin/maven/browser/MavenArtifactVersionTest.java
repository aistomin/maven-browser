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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The tests for {@link MavenArtifactVersion}.
 *
 * @since 0.1
 */
final class MavenArtifactVersionTest {

    /**
     * Expected test timestamp.
     */
    private static final Long TIMESTAMP = 1_479_480_474_000L;

    /**
     * Check that we assign and return the encapsulated fields properly.
     *
     * @throws Exception If something goes wrong.
     */
    @Test
    void testConstruct() throws Exception {
        final MvnArtifact artifact = new MavenArtifact(
            new MavenGroup(UUID.randomUUID().toString()),
            UUID.randomUUID().toString()
        );
        final String name = UUID.randomUUID().toString();
        final MvnArtifactVersion old = new MavenArtifactVersion(
            artifact, name, System.currentTimeMillis()
        );
        Assertions.assertEquals(name, old.name());
        Assertions.assertEquals(artifact, old.artifact());
        final MvnPackagingType[] types = MvnPackagingType.values();
        final MvnPackagingType type = types[new Random().nextInt(types.length)];
        final MvnArtifactVersion modern = new MavenArtifactVersion(
            artifact, name, type, System.currentTimeMillis()
        );
        Assertions.assertEquals(name, modern.name());
        Assertions.assertEquals(artifact, modern.artifact());
        Assertions.assertEquals(type, modern.packaging());
        final MavenArtifactVersion json = new MavenArtifactVersion(
            (JSONObject) new JSONParser().parse(
                FileUtils.readFileToString(
                    new File(
                        Thread.currentThread().getContextClassLoader()
                            .getResource("sample.json").getFile()
                    ), "UTF-8"
                )
            )
        );
        Assertions.assertEquals("0.2.1", json.name());
        Assertions.assertEquals(
            MavenArtifactVersionTest.TIMESTAMP, json.releaseTimestamp()
        );
        Assertions.assertEquals(
            MvnPackagingType.MAVEN_PLUGIN, json.packaging()
        );
    }

    /**
     * Check that we correctly override equals and hashCode methods.
     */
    @Test
    void testEqualsAndHashCode() {
        final String group = UUID.randomUUID().toString();
        final String artifact = UUID.randomUUID().toString();
        final String version = UUID.randomUUID().toString();
        final MvnArtifactVersion first = new MavenArtifactVersion(
            new MavenArtifact(
                new MavenGroup(group), artifact
            ), version, MvnPackagingType.JAR, System.currentTimeMillis()
        );
        final MvnArtifactVersion second = new MavenArtifactVersion(
            new MavenArtifact(
                new MavenGroup(group), artifact
            ), version, MvnPackagingType.JAR, System.currentTimeMillis()
        );
        Assertions.assertEquals(first, second);
        Assertions.assertEquals(
            1, new HashSet<>(Arrays.asList(first, second)).size()
        );
    }

    /**
     * Check that we properly convert the entity to string.
     */
    @Test
    void testToString() {
        final String group = UUID.randomUUID().toString();
        final String artifact = UUID.randomUUID().toString();
        final String version = UUID.randomUUID().toString();
        Assertions.assertEquals(
            String.format("%s:%s:%s", group, artifact, version),
            new MavenArtifactVersion(
                new MavenArtifact(
                    new MavenGroup(group),
                    artifact
                ), version, MvnPackagingType.JAR, System.currentTimeMillis()
            ).toString()
        );
    }

    /**
     * Check that we properly assign and return the packaging of the artifact.
     */
    @Test
    void testPackaging() throws Exception {
        final List<MvnArtifactVersion> versions =
            new MavenCentral().findVersions(
                new MavenArtifact(
                    new MavenGroup("org.apache.maven.plugins"),
                    "maven-plugin-plugin"
                )
            );
        for (final MvnArtifactVersion version : versions) {
            Assertions.assertEquals(
                MvnPackagingType.MAVEN_PLUGIN, version.packaging()
            );
        }
    }
}
