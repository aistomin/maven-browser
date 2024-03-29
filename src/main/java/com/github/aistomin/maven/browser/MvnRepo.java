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

import java.util.List;

/**
 * The interface of classes which represent the Maven repository.
 *
 * @since 0.1
 */
public interface MvnRepo {

    /**
     * Search for the artifacts. Returns first 20 found artifacts.
     *
     * @param str The search string. It may be a part of group or artifact name.
     * @return The list of the found artifacts.
     * @throws MvnException If the problem occurred while reading from the repo.
     */
    List<MvnArtifact> findArtifacts(String str) throws MvnException;

    /**
     * Search for the artifacts.
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
     * version.
     *
     * @param version The version.
     * @return The list of the newer versions.
     * @throws MvnException If the problem occurred while reading from the repo.
     */
    List<MvnArtifactVersion> findVersionsNewerThan(
        MvnArtifactVersion version
    ) throws MvnException;

    /**
     * Search for all the versions of the artifact which are older than provided
     * version.
     *
     * @param version The version.
     * @return The list of the older versions.
     * @throws MvnException If the problem occurred while reading from the repo.
     */
    List<MvnArtifactVersion> findVersionsOlderThan(
        MvnArtifactVersion version
    ) throws MvnException;
}
