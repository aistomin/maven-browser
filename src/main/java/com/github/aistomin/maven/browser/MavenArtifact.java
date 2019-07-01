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

/**
 * Simple implementation of the Maven artifact entity.
 *
 * @since 0.1
 */
public final class MavenArtifact implements MvnArtifact {

    /**
     * The artifact's name.
     */
    private final String artifact;

    /**
     * The artifact's group.
     */
    private final MvnGroup grp;

    /**
     * The latest artifact version.
     */
    private final MvnArtifactVersion latest;

    /**
     * Ctor.
     * @param name The artifact's name.
     * @param group The artifact's group.
     * @param version The latest artifact version.
     */
    public MavenArtifact(
        final String name, final MvnGroup group,
        final MvnArtifactVersion version
    ) {
        this.artifact = name;
        this.grp = group;
        this.latest = version;
    }

    @Override
    public MvnGroup group() {
        return this.grp;
    }

    @Override
    public String name() {
        return this.artifact;
    }

    @Override
    public MvnArtifactVersion latestVersion() {
        return this.latest;
    }
}
