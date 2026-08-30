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

import java.util.List;

/**
 * The interface of classes which represent the Maven repository.
 *
 * @since 0.1
 */
public interface MvnRepo {

    /**
     * Search for the artifacts. Returns first 20 found artifacts.
     * The search string is URL-encoded before it is sent to the repository, so
     * it may safely contain spaces and other special characters.
     *
     * @param str The search string. It may be a part of group or artifact name.
     * @return The list of the found artifacts.
     * @throws MvnException If the problem occurred while reading from the repo.
     */
    List<MvnArtifact> findArtifacts(String str) throws MvnException;

    /**
     * Search for the artifacts.
     * The search string is URL-encoded before it is sent to the repository, so
     * it may safely contain spaces and other special characters.
     *
     * @param str The search string. It may be a part of group or artifact name.
     * @param start The start index of the search.
     * @param rows The max amount of results.
     * @return The list of the found artifacts.
     * @throws MvnException If the problem occurred while reading from the repo.
     */
    List<MvnArtifact> findArtifacts(
        String str, Integer start, Integer rows
    ) throws MvnException;

    /**
     * Search for the versions of the artifact. Returns first 20 found versions.
     * A repository which lists the versions out of maven-metadata.xml learns
     * nothing but their names from it, so the versions come back with
     * {@link MvnPackagingType#UNKNOWN} packaging and an empty release
     * timestamp.
     *
     * @param artifact The artifact.
     * @return The list of the found versions of the artifact.
     * @throws MvnException If the problem occurred while reading from the repo.
     */
    List<MvnArtifactVersion> findVersions(
        MvnArtifact artifact
    ) throws MvnException;

    /**
     * Search for the versions of the artifact.
     * The range is clamped to the amount of the versions which the repository
     * has, so {@code rows} may be {@link Integer#MAX_VALUE} to ask for
     * everything starting from {@code start}.
     * The versions carry as much as the repository told us about them - see
     * {@link #findVersions(MvnArtifact)}.
     *
     * @param artifact The artifact.
     * @param start Indent of the search.
     * @param rows The max amount of results.
     * @return The list of the found versions of the artifact.
     * @throws MvnException If the problem occurred while reading from the repo.
     */
    List<MvnArtifactVersion> findVersions(
        MvnArtifact artifact, Integer start, Integer rows
    ) throws MvnException;

    /**
     * Search for all the versions of the artifact which are newer than provided
     * version. The versions carry as much as the repository told us about
     * them - see {@link #findVersions(MvnArtifact)}.
     *
     * @param version The version.
     * @return The list of the newer versions.
     * @throws MvnException If the problem occurred while reading from the repo,
     *  or if the version is not found in the repository.
     */
    List<MvnArtifactVersion> findVersionsNewerThan(
        MvnArtifactVersion version
    ) throws MvnException;

    /**
     * Search for all the versions of the artifact which are older than provided
     * version. The versions carry as much as the repository told us about
     * them - see {@link #findVersions(MvnArtifact)}.
     *
     * @param version The version.
     * @return The list of the older versions.
     * @throws MvnException If the problem occurred while reading from the repo,
     *  or if the version is not found in the repository.
     */
    List<MvnArtifactVersion> findVersionsOlderThan(
        MvnArtifactVersion version
    ) throws MvnException;
}
