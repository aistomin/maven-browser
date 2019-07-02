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

import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The tests for {@link MavenArtifactVersion}.
 *
 * @since 0.1
 */
public final class MavenArtifactVersionTest {

    /**
     * Check that we assign and return the encapsulated fields properly.
     */
    @Test
    void testConstruct() {
        final MvnArtifact artifact = new MavenArtifact(
            UUID.randomUUID().toString(),
            new MavenGroup(UUID.randomUUID().toString())
        );
        final String name = UUID.randomUUID().toString();
        final MvnArtifactVersion version = new MavenArtifactVersion(
            artifact, name
        );
        Assertions.assertEquals(name, version.name());
        Assertions.assertEquals(artifact, version.artifact());
    }
}
