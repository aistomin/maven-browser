/*
 * Copyright (c) 2019-2021, Istomin Andrei
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
 * The tests for {@link MavenDependency}.
 *
 * @since 1.0
 */
final class MavenDependencyTest {

    /**
     * My previously created artifact which we can use for tests.
     */
    private final MvnArtifact mine = new MavenArtifact(
        new MavenGroup("com.github.aistomin"), "jenkins-sdk"
    );

    /**
     * Check that we correctly convert dependency to the Grails/Gradle
     * dependency format.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testForGradle() throws Exception {
        Assertions.assertEquals(
            "compile 'com.github.aistomin:jenkins-sdk:0.2.1'",
            this.dependency().forGradle()
        );
    }

    /**
     * Check that we correctly convert dependency to the Apache Buildr
     * dependency format.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testForBuildr() throws Exception {
        Assertions.assertEquals(
            "'com.github.aistomin:jenkins-sdk:jar:0.2.1'",
            this.dependency().forBuildr()
        );
    }

    /**
     * Check that we correctly convert dependency to the Apache Maven
     * dependency format.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testForMaven() throws Exception {
        Assertions.assertEquals(
            String.format(
                "<dependency>%n%s%n%s%n%s%n</dependency>",
                "  <groupId>com.github.aistomin</groupId>",
                "  <artifactId>jenkins-sdk</artifactId>",
                "  <version>0.2.1</version>"
            ), this.dependency().forMaven()
        );
    }

    /**
     * Check that we correctly convert dependency to the Apache Ivy
     * dependency format.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testForIvy() throws Exception {
        Assertions.assertEquals(
            String.format(
                "<dependency org=%s name=%s rev=%s />",
                "\"com.github.aistomin\"", "\"jenkins-sdk\"", "\"0.2.1\""
            ),
            this.dependency().forIvy()
        );
    }

    /**
     * Check that we correctly convert dependency to the Groovy Grape
     * dependency format.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testForGroovyGrape() throws Exception {
        Assertions.assertEquals(
            String.format(
                "@Grapes(\n  @Grab(group=%s, module=%s, version=%s)\n)",
                "'com.github.aistomin'", "'jenkins-sdk'", "'0.2.1'"
            ),
            this.dependency().forGroovyGrape()
        );
    }

    /**
     * Check that we correctly convert dependency to the Scala SBT
     * dependency format.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testForScala() throws Exception {
        Assertions.assertEquals(
            String.format(
                "%s%s",
                "libraryDependencies += \"com.github.aistomin\"",
                " % \"jenkins-sdk\" % \"0.2.1\""
            ),
            this.dependency().forScala()
        );
    }

    /**
     * Check that we correctly convert dependency to the Leiningen
     * dependency format.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testForLeiningen() throws Exception {
        Assertions.assertEquals(
            "[com.github.aistomin/jenkins-sdk \"0.2.1\"]",
            this.dependency().forLeiningen()
        );
    }

    /**
     * Load test dependency.
     *
     * @return Test dependency.
     * @throws Exception If something went wrong.
     */
    private MvnDependency dependency() throws Exception {
        return new MavenCentral()
            .findVersions(this.mine)
            .stream()
            .findFirst()
            .get()
            .dependency();
    }
}
