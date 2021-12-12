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
 * Simple implementation of the Maven artifact version's dependency entity.
 *
 * @since 1.0
 */
public final class MavenDependency implements MvnDependency {

    /**
     * Maven artifact's version.
     */
    private final MvnArtifactVersion ver;

    /**
     * Ctor.
     *
     * @param version Maven artifact's version.
     */
    public MavenDependency(final MvnArtifactVersion version) {
        this.ver = version;
    }

    @Override
    public String forMaven() {
        final MvnArtifact artifact = this.ver.artifact();
        return String.format(
            "<dependency>%n%s%n%s%n%s%n</dependency>",
            String.format("  <groupId>%s</groupId>", artifact.group().name()),
            String.format("  <artifactId>%s</artifactId>", artifact.name()),
            String.format("  <version>%s</version>", this.ver.name())
        );
    }

    @Override
    public String forBuildr() {
        final MvnArtifact artifact = this.ver.artifact();
        return String.format(
            "'%s:%s:jar:%s'",
            artifact.group().name(),
            artifact.name(),
            this.ver.name()
        );
    }

    @Override
    public String forIvy() {
        final MvnArtifact artifact = this.ver.artifact();
        return String.format(
            "<dependency org=\"%s\" name=\"%s\" rev=\"%s\" />",
            artifact.group().name(),
            artifact.name(),
            this.ver.name()
        );
    }

    @Override
    public String forGroovyGrape() {
        final MvnArtifact artifact = this.ver.artifact();
        return String.format(
            "@Grapes(%n  @Grab(group='%s', module='%s', version='%s')%n)",
            artifact.group().name(),
            artifact.name(),
            this.ver.name()
        );
    }

    @Override
    public String forGradle() {
        final MvnArtifact artifact = this.ver.artifact();
        return String.format(
            "compile '%s:%s:%s'",
            artifact.group().name(),
            artifact.name(),
            this.ver.name()
        );
    }

    @Override
    public String forScala() {
        final MvnArtifact artifact = this.ver.artifact();
        return String.format(
            "libraryDependencies += \"%s\" %% \"%s\" %% \"%s\"",
            artifact.group().name(),
            artifact.name(),
            this.ver.name()
        );
    }

    @Override
    public String forLeiningen() {
        final MvnArtifact artifact = this.ver.artifact();
        return String.format(
            "[%s/%s \"%s\"]", artifact.group().name(), artifact.name(),
            this.ver.name()
        );
    }
}
