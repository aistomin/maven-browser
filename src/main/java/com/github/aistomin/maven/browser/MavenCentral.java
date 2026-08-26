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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The class which works with Maven Central repository.
 * Uses <a href="https://repo1.maven.org/maven2/">repo1.maven.org</a> for
 * fetching artifact metadata (stable).
 * Uses <a href="https://search.maven.org/">search.maven.org</a> for artifact
 * search functionality.
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
     * Default time we wait until the connection to the repository is
     * established.
     */
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Default time we wait for the repository's response. Maven Central
     * usually answers in a fraction of a second, but it is known to stall for
     * a minute and more from time to time, that's why the default is that
     * generous. Use the parametrised ctor if your use case needs to give up
     * earlier.
     */
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(120);

    /**
     * The XML parser feature which forbids a DTD in the parsed document. A
     * legitimate maven-metadata.xml never contains one, so forbidding it costs
     * us nothing and takes away the ground under both XXE and entity
     * expansion attacks: neither works without a DTD.
     */
    private static final String NO_DOCTYPE =
        "http://apache.org/xml/features/disallow-doctype-decl";

    /**
     * The Maven repository base URL for fetching metadata.
     */
    private final String repo;

    /**
     * The Maven search API URL for artifact search.
     */
    private final String search;

    /**
     * The HTTP client which we use to talk to the repository.
     */
    private final HttpClient client;

    /**
     * The time we wait for the repository's response.
     */
    private final Duration response;

    /**
     * Parametrised ctor.
     *
     * @param repository The Maven repo base URL for fetching metadata.
     * @param searchApi The Maven search API URL.
     * @param connect The time we wait until the connection is established.
     * @param timeout The time we wait for the repository's response.
     */
    public MavenCentral(
        final String repository,
        final String searchApi,
        final Duration connect,
        final Duration timeout
    ) {
        this.repo = repository;
        this.search = searchApi;
        this.response = timeout;
        this.client = HttpClient.newBuilder()
            .connectTimeout(connect)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    /**
     * Parametrised ctor. The default timeouts are used.
     *
     * @param repository The Maven repo base URL for fetching metadata.
     * @param searchApi The Maven search API URL.
     */
    public MavenCentral(final String repository, final String searchApi) {
        this(repository, searchApi, CONNECT_TIMEOUT, REQUEST_TIMEOUT);
    }

    /**
     * Ctor.
     */
    public MavenCentral() {
        this(
            "https://repo1.maven.org/maven2",
            "https://search.maven.org/solrsearch/select"
        );
    }

    @Override
    public List<MvnArtifact> findArtifacts(
        final String str
    ) throws MvnException {
        return this.findArtifacts(str, 0, MAX_ROWS);
    }

    @Override
    public List<MvnArtifact> findArtifacts(
        final String str, final Integer start, final Integer rows
    ) throws MvnException {
        try {
            final String result = this.get(
                String.format(
                    "%s?q=%s&start=%d&rows=%d&wt=json",
                    this.search,
                    URLEncoder.encode(str, StandardCharsets.UTF_8),
                    start,
                    rows
                ),
                HttpResponse.BodyHandlers.ofString(Charset.forName(ENCODING))
            );
            return parseJsonResponse(result)
                .stream()
                .map(MavenArtifact::new)
                .collect(Collectors.toList());
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new MvnException(exception);
        } catch (final ParseException | IOException exception) {
            throw new MvnException(exception);
        }
    }

    @Override
    public List<MvnArtifactVersion> findVersions(
        final MvnArtifact artifact
    ) throws MvnException {
        return this.findVersions(artifact, 0, MAX_ROWS);
    }

    @Override
    public List<MvnArtifactVersion> findVersions(
        final MvnArtifact artifact, final Integer start, final Integer rows
    ) throws MvnException {
        try {
            final String url = String.format(
                "%s%s",
                this.repo,
                new URI(
                    null,
                    null,
                    String.format(
                        "/%s/%s/maven-metadata.xml",
                        artifact.group().name().replace('.', '/'),
                        artifact.name()
                    ),
                    null
                ).toASCIIString()
            );
            final List<String> allVersions = parseMetadataXml(
                this.get(url, HttpResponse.BodyHandlers.ofInputStream())
            );
            final int endIndex = Math.min(start + rows, allVersions.size());
            final List<String> pagedVersions =
                start < allVersions.size()
                    ? allVersions.subList(start, endIndex)
                    : new ArrayList<>();
            return pagedVersions.stream()
                .map(
                    ver -> new MavenArtifactVersion(
                        artifact, ver, MvnPackagingType.JAR, null
                    )
                )
                .collect(Collectors.toList());
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new MvnException(exception);
        } catch (final IOException | ParserConfigurationException
            | SAXException | URISyntaxException exception) {
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
                isFirstVersionBiggerThanSecondVersion(current, ver)
        );
    }

    /**
     * Send a GET request to the repository and read the response's body.
     *
     * @param url The URL we read from.
     * @param handler The handler which converts the response's body.
     * @param <T> The type of the response's body.
     * @return The response's body.
     * @throws IOException If the request failed or the repository answered
     *  with an unsuccessful status.
     * @throws InterruptedException If the request was interrupted.
     */
    private <T> T get(
        final String url, final HttpResponse.BodyHandler<T> handler
    ) throws IOException, InterruptedException {
        final HttpResponse<T> answer = this.client.send(
            HttpRequest.newBuilder(URI.create(url))
                .timeout(this.response)
                .GET()
                .build(),
            handler
        );
        final int status = answer.statusCode();
        if (status < HttpURLConnection.HTTP_OK
            || status >= HttpURLConnection.HTTP_MULT_CHOICE) {
            throw new IOException(
                String.format("Got HTTP %d reading %s.", status, url)
            );
        }
        return answer.body();
    }

    /**
     * Find all the versions of the artifact with a filter.
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
                    "%s %s was not found in Maven Central.",
                    current.artifact().name(),
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
     * Get the list of artifacts/versions from the Maven search API response.
     *
     * @param response Maven search API JSON response.
     * @return The list of JSON objects.
     * @throws ParseException If parsing wasn't successful.
     */
    @SuppressWarnings("unchecked")
    private static List<JSONObject> parseJsonResponse(
        final String response
    ) throws ParseException {
        return new ArrayList<JSONObject>(
            (JSONArray) (
                (JSONObject) ((JSONObject) new JSONParser().parse(response))
                    .get("response")
            ).get("docs")
        );
    }

    /**
     * Parse maven-metadata.xml to extract a versions list.
     * Returns versions in descending order (newest first).
     * The repository URL is a public constructor parameter, so the answer we
     * parse here is not necessarily trustworthy. The parser is therefore
     * hardened before it is used: a document with a DTD is rejected instead
     * of being parsed, and neither external entities nor XInclude are
     * resolved.
     *
     * @param input The input stream of maven-metadata.xml.
     * @return The list of version strings, newest first.
     * @throws ParserConfigurationException If XML parser config fails.
     * @throws SAXException If XML parsing fails.
     * @throws IOException If reading fails.
     */
    private static List<String> parseMetadataXml(
        final InputStream input
    ) throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        factory.setFeature(NO_DOCTYPE, true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        final Document doc = factory.newDocumentBuilder().parse(input);
        final NodeList versionNodes = doc.getElementsByTagName("version");
        final List<String> versions = new ArrayList<>(versionNodes.getLength());
        for (int i = 0; i < versionNodes.getLength(); i++) {
            versions.add(versionNodes.item(i).getTextContent());
        }
        Collections.reverse(versions);
        return versions;
    }

    /**
     * Check if one version is bigger than another. The versions are ordered
     * using Maven's own rules, so any version string can be compared and the
     * qualifiers are ranked the way Maven ranks them.
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
        return new ComparableVersion(first.name())
            .compareTo(new ComparableVersion(second.name())) > 0;
    }
}
