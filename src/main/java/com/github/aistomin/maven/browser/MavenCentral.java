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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * The class which works with Maven Central repository.
 * URL: https://search.maven.org/
 *
 * @todo: Issue #10. Let's implement it and remove this todo.
 * @todo: Issue #11. Let's implement it and remove this todo.
 * @todo: Issue #12. Let's implement it and remove this todo.
 * @since 0.1
 */
public final class MavenCentral implements MvnRepo {

    /**
     * Default max amount of rows that query will return.
     */
    public static final int MAX_ROWS = 20;

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
        this("https://search.maven.org");
    }

    @Override
    public List<MvnArtifact> findArtifacts(final String str) throws Exception {
        return this.findArtifacts(str, 0, MavenCentral.MAX_ROWS);
    }

    @Override
    public List<MvnArtifact> findArtifacts(
        final String str, final Integer start, final Integer rows
    ) throws Exception {
        final String result = IOUtils.toString(
            URI.create(
                String.format(
                    "%s/solrsearch/select?q=%s&start=%d&rows=%d&wt=json",
                    this.repo, str, start, rows
                )
            ),
            "UTF-8"
        );
        final JSONArray docs = (JSONArray) (
            (JSONObject) ((JSONObject) new JSONParser().parse(result))
                .get("response")
        ).get("docs");
        return new ArrayList<JSONObject>(docs)
            .stream()
            .map(MavenArtifact::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<MvnArtifactVersion> findVersions(
        final MvnArtifact artifact, final Integer indent, final Integer rows
    ) {
        throw new NotImplementedException(
            "The method MavenCentral.findVersions() is not implemented."
        );
    }

    @Override
    public List<MvnArtifactVersion> findVersionsNewerThan(
        final MvnArtifactVersion version
    ) {
        throw new NotImplementedException(
            "The method MavenCentral.findVersionsNewerThan is not implemented."
        );
    }

    @Override
    public List<MvnArtifactVersion> findVersionsOlderThan(
        final MvnArtifactVersion version
    ) {
        throw new NotImplementedException(
            "The method MavenCentral.findVersionsOlderThan is not implemented."
        );
    }
}
