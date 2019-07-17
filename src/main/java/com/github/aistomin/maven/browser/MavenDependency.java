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
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Simple implementation of the Maven artifact version's dependency entity.
 *
 * @todo: Issue #42. Let's solve the issue and remove this todo.
 * @todo: Issue #43. Let's solve the issue and remove this todo.
 * @todo: Issue #44. Let's solve the issue and remove this todo.
 * @todo: Issue #45. Let's solve the issue and remove this todo.
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
        try {
            return String.format(
                FileUtils.readFileToString(
                    new File(
                        Thread.currentThread().getContextClassLoader()
                            .getResource("maven_dependency.template").getFile()
                    ), "UTF-8"
                ),
                this.ver.artifact().group().name(),
                this.ver.artifact().name(),
                this.ver.name()
            );
        } catch (final IOException unexpected) {
            throw new IllegalStateException(
                "The error is totally unexpected.", unexpected
            );
        }
    }

    @Override
    public String forBuildr() {
        return String.format(
            "'%s:%s:jar:%s'",
            this.ver.artifact().group().name(),
            this.ver.artifact().name(),
            this.ver.name()
        );
    }

    @Override
    public String forIvy() {
        throw new NotImplementedException(
            "The method forIvy() is not implemented."
        );
    }

    @Override
    public String forGroovyGrape() {
        throw new NotImplementedException(
            "The method forGroovyGrape() is not implemented."
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
        throw new NotImplementedException(
            "The method forScala() is not implemented."
        );
    }

    @Override
    public String forLeiningen() {
        throw new NotImplementedException(
            "The method forLeiningen() is not implemented."
        );
    }
}
