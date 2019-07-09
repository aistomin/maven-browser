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
     * @throws Exception If the problem occurred while reading from the repo.
     */
    List<MvnArtifact> findArtifacts(String str) throws Exception;

    /**
     * Search for the artifacts.
     *
     * @param str The search string. It may be a part of group or artifact name.
     * @param start The start index of the search.
     * @param rows The max amount of results.
     * @return The list of the found artifacts.
     * @throws Exception If the problem occurred while reading from the repo.
     */
    List<MvnArtifact> findArtifacts(
        String str, Integer start, Integer rows
    ) throws Exception;

    /**
     * Search for the versions of the artifact. Returns first 20 found versions.
     *
     * @param artifact The artifact.
     * @return The list of the found versions of the artifact.
     * @throws Exception If the problem occurred while reading from the repo.
     */
    List<MvnArtifactVersion> findVersions(MvnArtifact artifact) throws Exception;

    /**
     * Search for the versions of the artifact.
     *
     * @param artifact The artifact.
     * @param start Indent of the search.
     * @param rows The max amount of results.
     * @return The list of the found versions of the artifact.
     * @throws Exception If the problem occurred while reading from the repo.
     */
    List<MvnArtifactVersion> findVersions(
        MvnArtifact artifact, Integer start, Integer rows
    ) throws Exception;

    /**
     * Search for all the versions of the artifact which are newer than provided
     * version.
     *
     * @param version The version.
     * @return The list of the newer versions.
     * @throws Exception If the problem occurred while reading from the repo.
     */
    List<MvnArtifactVersion> findVersionsNewerThan(
        MvnArtifactVersion version
    ) throws Exception;

    /**
     * Search for all the versions of the artifact which are older than provided
     * version.
     *
     * @param version The version.
     * @return The list of the older versions.
     * @throws Exception If the problem occurred while reading from the repo.
     */
    List<MvnArtifactVersion> findVersionsOlderThan(
        MvnArtifactVersion version
    ) throws Exception;
}
