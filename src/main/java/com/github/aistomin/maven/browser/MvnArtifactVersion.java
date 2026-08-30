/*
 * Copyright (c) 2019 Andrej Istomin
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

import java.util.Optional;

/**
 * The interface of classes which represent the Maven artifact's version.
 * Not every repository knows everything about a version: the packaging and
 * the release timestamp are only available from the search API, so a version
 * which was read out of maven-metadata.xml says so instead of pretending -
 * see {@link MvnArtifactVersion#packaging()} and
 * {@link MvnArtifactVersion#releaseTimestamp()}.
 *
 * @since 0.1
 */
public interface MvnArtifactVersion {

    /**
     * Maven artifact.
     *
     * @return The artifact.
     */
    MvnArtifact artifact();

    /**
     * Version's name.
     *
     * @return The name of the version.
     */
    String name();

    /**
     * The timestamp when the version was released.
     *
     * @return The timestamp, or an empty {@link Optional} if the repository
     *  did not tell us when the version was released. The versions which
     *  {@link MvnRepo#findVersions(MvnArtifact)} returns are always empty
     *  here: maven-metadata.xml carries no per-version timestamp.
     */
    Optional<Long> releaseTimestamp();

    /**
     * Artifact's version identifier. Normally it looks like
     * "group_name:artifact_name:version".
     *
     * @return The version's identifier.
     */
    String identifier();

    /**
     * Artifact's version dependency.
     *
     * @return The dependency.
     */
    MvnDependency dependency();

    /**
     * Artifact's packaging.
     *
     * @return Packaging type, or {@link MvnPackagingType#UNKNOWN} if the
     *  repository did not tell us which one it is. The versions which
     *  {@link MvnRepo#findVersions(MvnArtifact)} returns are always UNKNOWN
     *  here: maven-metadata.xml carries no per-version packaging.
     */
    MvnPackagingType packaging();
}
