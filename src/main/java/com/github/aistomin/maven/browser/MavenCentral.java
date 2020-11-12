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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * The class which works with Maven Central repository.
 * URL: https://search.maven.org/
 *
 * @since 0.1
 */
public final class MavenCentral implements MvnRepo {

    /**
     * Default max amount of rows that query will return.
     */
    public static final int MAX_ROWS = 20;

    /**
     * HTTP request encoding.
     */
    public static final String ENCODING = "UTF-8";

    /**
     * The Maven repo base URL.
     */
    private final String repo;

    /**
     * Ctor.
     *
     * @param repository The Maven repo base URL.
     */
    public MavenCentral(final String repository) {
        this.repo = repository;
    }

    /**
     * Ctor.
     */
    public MavenCentral() {
        this("https://search.maven.org/solrsearch/select");
    }

    @Override
    public List<MvnArtifact> findArtifacts(final String str) throws MvnException {
        return this.findArtifacts(str, 0, MavenCentral.MAX_ROWS);
    }

    @Override
    public List<MvnArtifact> findArtifacts(
        final String str, final Integer start, final Integer rows
    ) throws MvnException {
        try {
            final String result = IOUtils.toString(
                URI.create(
                    String.format(
                        "%s?q=%s&start=%d&rows=%d&wt=json",
                        this.repo, str, start, rows
                    )
                ),
                MavenCentral.ENCODING
            );
            return new ArrayList<JSONObject>(MavenCentral.parseResponse(result))
                .stream()
                .map(MavenArtifact::new)
                .collect(Collectors.toList());
        } catch (final ParseException | IOException exception) {
            throw new MvnException(exception);
        }
    }

    @Override
    public List<MvnArtifactVersion> findVersions(
        final MvnArtifact artifact
    ) throws MvnException {
        return this.findVersions(artifact, 0, MavenCentral.MAX_ROWS);
    }

    @Override
    public List<MvnArtifactVersion> findVersions(
        final MvnArtifact artifact, final Integer start, final Integer rows
    ) throws MvnException {
        try {
            final String res = IOUtils.toString(
                URI.create(
                    String.format(
                        "%s?q=g:%s+AND+a:%s&core=gav&start=%d&rows=%d&wt=json",
                        this.repo,
                        artifact.group().name(),
                        artifact.name(),
                        start,
                        rows
                    )
                ),
                MavenCentral.ENCODING
            );
            return new ArrayList<JSONObject>(MavenCentral.parseResponse(res))
                .stream()
                .map(MavenArtifactVersion::new)
                .collect(Collectors.toList());
        } catch (final ParseException | IOException exception) {
            throw new MvnException(exception);
        }
    }

    @Override
    public List<MvnArtifactVersion> findVersionsNewerThan(
        final MvnArtifactVersion version
    ) throws MvnException {
        return this.findArtifactVersionsWithFilter(
            version,
            MavenCentral::isFirstVersionBiggerThanSecondVersion
        );
    }

    @Override
    public List<MvnArtifactVersion> findVersionsOlderThan(
        final MvnArtifactVersion version
    ) throws MvnException {
        return this.findArtifactVersionsWithFilter(
            version,
            (ver, current) ->
                MavenCentral.isFirstVersionBiggerThanSecondVersion(current, ver)
        );
    }

    /**
     * Find all the versions of the artifact with filter.
     *
     * @param current Current version.
     * @param predicate The filter.
     * @return The list of the found versions.
     * @throws MvnException If the problem occurred while reading from the repo.
     */
    private List<MvnArtifactVersion> findArtifactVersionsWithFilter(
        final MvnArtifactVersion current,
        final BiPredicate<MvnArtifactVersion, MvnArtifactVersion> predicate
    ) throws MvnException {
        final List<MvnArtifactVersion> versions =
            this.findVersions(current.artifact(), 0, Integer.MAX_VALUE);
        final MvnArtifactVersion loaded = versions
            .stream()
            .filter(ver -> ver.name().equals(current.name()))
            .findFirst()
            .orElse(null);
        if (loaded == null) {
            throw new IllegalStateException(
                String.format(
                    "Version %s was not found in Maven Central.",
                    current.name()
                )
            );
        }
        return versions
            .stream()
            .filter(ver -> predicate.test(ver, loaded))
            .collect(Collectors.toList());
    }

    /**
     * Get the list of artifacts/versions from the Maven repo response.
     *
     * @param response Maven repo response.
     * @return The list of JSON objects.
     * @throws ParseException If parsing wasn't successful.
     */
    private static JSONArray parseResponse(
        final String response
    ) throws ParseException {
        return (JSONArray) (
            (JSONObject) ((JSONObject) new JSONParser().parse(response))
                .get("response")
        ).get("docs");
    }

    /**
     * Check if one version is bigger than another.
     *
     * @param first Version A.
     * @param second Version B.
     * @return True - version A is bigger than version B. False - version A is
     *  smaller or equal to version B.
     */
    private static Boolean isFirstVersionBiggerThanSecondVersion(
        final MvnArtifactVersion first,
        final MvnArtifactVersion second
    ) {
        final String pattern = "\\.";
        final List<String> one =
            Arrays.asList(first.name().split(pattern));
        final List<String> two =
            Arrays.asList(second.name().split(pattern));
        final OptionalInt difference = IntStream.range(0, one.size()).filter(
            idx -> idx <= (two.size() - 1) && !one.get(idx).equals(two.get(idx))
        ).findFirst();
        final AtomicBoolean result = new AtomicBoolean();
        if (difference.isPresent()) {
            final int idx = difference.getAsInt();
            result.set(one.get(idx).compareTo(two.get(idx)) > 0);
        } else {
            result.set(one.size() > two.size());
        }
        return result.get();
    }
}
