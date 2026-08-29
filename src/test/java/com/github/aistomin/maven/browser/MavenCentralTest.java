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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXParseException;

/**
 * The tests for {@link MavenCentral}.
 *
 * @since 0.1
 */
final class MavenCentralTest {

    /**
     * Three.
     */
    private static final int THREE = 3;

    /**
     * Five.
     */
    private static final int FIVE = 5;

    /**
     * Ten.
     */
    private static final int TEN = 10;

    /**
     * My previously created artifact which we can use for tests.
     */
    private final MvnArtifact mine = new MavenArtifact(
        new MavenGroup("com.github.aistomin"), "jenkins-sdk"
    );

    /**
     * Guava. Its version history contains both non-numeric versions
     * (r03 ... r09) and qualified ones (10.0-rc1), which is exactly what the
     * version comparison needs to be checked against.
     */
    private final MvnArtifact guava = new MavenArtifact(
        new MavenGroup("com.google.guava"), "guava"
    );

    /**
     * The list of the versions of my artifact which we use for this test.
     */
    private final List<String> vers = Arrays.asList(
        "0.2.1",
        "0.2",
        "0.1",
        "0.0.2",
        "0.0.1"
    );

    /**
     * Check that we correctly find the artifacts with start and rows
     * parameters.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifacts() throws Exception {
        final MvnRepo mvn = new MavenCentral();
        final String search = "guice";
        final List<MvnArtifact> artifacts = mvn.findArtifacts(
            search, 0, MavenCentral.MAX_ROWS
        );
        Assertions.assertEquals(
            MavenCentral.MAX_ROWS, artifacts.size()
        );
        final List<MvnArtifact> def = mvn.findArtifacts(search);
        Assertions.assertEquals(
            artifacts.size(), def.size()
        );
        def.forEach(
            artifact ->
                Assertions.assertEquals(
                    def.indexOf(artifact), artifacts.indexOf(artifact)
                )
        );
        final int start = 2;
        final List<MvnArtifact> part = mvn.findArtifacts(
            search, start, MavenCentralTest.FIVE
        );
        Assertions.assertEquals(MavenCentralTest.FIVE, part.size());
        part.forEach(
            artifact ->
                Assertions.assertEquals(
                    part.indexOf(artifact), artifacts.indexOf(artifact) - start
                )
        );
        final List<MvnArtifact> found = mvn.findArtifacts("aistomin");
        Assertions.assertEquals(MavenCentralTest.FIVE, found.size());
        Assertions.assertNotNull(
            found.stream()
                .filter(artifact -> artifact.equals(this.mine))
                .findFirst()
                .get()
        );
    }

    /**
     * Check that we can search using a string which contains a space.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsWithSpace() throws Exception {
        Assertions.assertFalse(
            new MavenCentral().findArtifacts("guice inject").isEmpty()
        );
    }

    /**
     * Check that we can search using the Maven search API field syntax, which
     * contains characters that are not allowed in a URL.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsWithFieldQuery() throws Exception {
        final String group = this.mine.group().name();
        final List<MvnArtifact> found = new MavenCentral().findArtifacts(
            String.format("g:\"%s\"", group)
        );
        Assertions.assertFalse(found.isEmpty());
        Assertions.assertTrue(found.contains(this.mine));
        found.forEach(
            artifact ->
                Assertions.assertEquals(group, artifact.group().name())
        );
    }

    /**
     * Check that the request parameters can not be injected via the search
     * string.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsIgnoresInjectedParameters() throws Exception {
        Assertions.assertTrue(
            new MavenCentral()
                .findArtifacts("a&rows=1000", 0, MavenCentralTest.FIVE)
                .size() <= MavenCentralTest.FIVE
        );
    }

    /**
     * Check that the artifacts are read out of a search answer which has the
     * shape the real search API produces: the documents sit in "docs" inside
     * "response", and each of them carries its group in "g" and its name
     * in "a".
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsParsesSearchAnswer() throws Exception {
        final HttpServer server = MavenCentralTest.serving(
            String.join(
                "",
                "{\"responseHeader\":{\"status\":0,\"QTime\":1},",
                "\"response\":{\"numFound\":2,\"start\":0,\"docs\":[",
                "{\"id\":\"com.github.aistomin:jenkins-sdk\",",
                "\"g\":\"com.github.aistomin\",\"a\":\"jenkins-sdk\",",
                "\"latestVersion\":\"0.2.1\",\"p\":\"maven-plugin\",",
                "\"timestamp\":1479480474000,\"versionCount\":5},",
                "{\"id\":\"com.google.guava:guava\",",
                "\"g\":\"com.google.guava\",\"a\":\"guava\",",
                "\"latestVersion\":\"33.0.0\",\"p\":\"bundle\",",
                "\"timestamp\":1700000000000,\"versionCount\":99}",
                "]},\"spellcheck\":{\"suggestions\":[]}}"
            )
        );
        try {
            Assertions.assertEquals(
                Arrays.asList(this.mine, this.guava),
                MavenCentralTest.talkingTo(server).findArtifacts("whatever")
            );
        } finally {
            server.stop(0);
        }
    }

    /**
     * Check that a search answer which contains no documents at all gives an
     * empty list rather than breaking the search. The answer is navigated
     * field by field, so every one of those fields has to tolerate being
     * absent.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsParsesAnswerWithoutDocuments() throws Exception {
        final HttpServer server = MavenCentralTest.serving(
            "{\"responseHeader\":{\"status\":0}}"
        );
        try {
            Assertions.assertTrue(
                MavenCentralTest.talkingTo(server)
                    .findArtifacts("whatever")
                    .isEmpty()
            );
        } finally {
            server.stop(0);
        }
    }

    /**
     * Check that an answer which is not a valid JSON fails with
     * {@link MvnException} rather than with an unchecked exception.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsRejectsBrokenJson() throws Exception {
        final HttpServer server = MavenCentralTest.serving("{\"response\":");
        try {
            final MvnRepo mvn = MavenCentralTest.talkingTo(server);
            Assertions.assertInstanceOf(
                JsonProcessingException.class,
                Assertions.assertThrows(
                    MvnException.class,
                    () -> mvn.findArtifacts("whatever")
                ).getCause()
            );
        } finally {
            server.stop(0);
        }
    }

    /**
     * Check that an artifact whose coordinates contain a character which is not
     * allowed in a URL fails with {@link MvnException} rather than with an
     * unchecked exception.
     */
    @Test
    void testFindVersionsWithSpaceInCoordinates() {
        Assertions.assertThrows(
            MvnException.class,
            () -> new MavenCentral().findVersions(
                new MavenArtifact(
                    new MavenGroup("com.github aistomin"), "jenkins sdk"
                )
            )
        );
    }

    /**
     * Check that we correctly throw exceptions if something went wrong.
     */
    @Test
    void testExceptions() {
        final String invalid = "http://not.existing.mvn/";
        final MvnRepo repo = new MavenCentral(invalid, invalid);
        Assertions.assertThrows(
            MvnException.class,
            () -> repo.findArtifacts("someartifact")
        );
        Assertions.assertThrows(
            MvnException.class,
            () -> repo.findVersions(this.mine)
        );
    }

    /**
     * Check that we correctly find the versions of the artifact.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersions() throws Exception {
        final List<MvnArtifactVersion> versions =
            new MavenCentral().findVersions(this.mine);
        Assertions.assertEquals(MavenCentralTest.FIVE, versions.size());
        for (final MvnArtifactVersion ver : versions) {
            Assertions.assertEquals(this.mine.name(), ver.artifact().name());
            Assertions.assertEquals(
                this.mine.group().name(), ver.artifact().group().name()
            );
            Assertions.assertTrue(this.vers.contains(ver.name()));
        }
        final List<MvnArtifactVersion> reduced =
            new MavenCentral().findVersions(this.mine, 1, 2);
        Assertions.assertEquals(2, reduced.size());
        Assertions.assertEquals(
            this.vers.get(1), reduced.get(0).name()
        );
        Assertions.assertEquals(
            this.vers.get(2), reduced.get(1).name()
        );
    }

    /**
     * Check that asking for everything from a non-zero start works. The amount
     * of the rows is not added to the start index in {@code int} arithmetic,
     * so {@link Integer#MAX_VALUE} rows do not overflow into a negative end
     * index.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsWithMaxRows() throws Exception {
        final List<MvnArtifactVersion> versions = new MavenCentral()
            .findVersions(this.mine, 1, Integer.MAX_VALUE);
        Assertions.assertEquals(this.vers.size() - 1, versions.size());
        Assertions.assertEquals(this.vers.get(1), versions.get(0).name());
        Assertions.assertEquals(
            this.vers.get(this.vers.size() - 1),
            versions.get(versions.size() - 1).name()
        );
    }

    /**
     * Check that we correctly find the versions which are newer than provided
     * one.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsNewerThan() throws Exception {
        final MvnRepo mvn = new MavenCentral();
        Assertions.assertThrows(
            MvnException.class,
            () -> {
                mvn.findVersionsNewerThan(
                    new MavenArtifactVersion(
                        this.mine,
                        "not-existing",
                        MvnPackagingType.JAR,
                        System.currentTimeMillis()
                    )
                );
            }
        );
        final MvnArtifactVersion version = mvn.findVersions(this.mine)
            .stream()
            .filter(
                ver ->
                    this.vers.get(this.vers.size() - 2).equals(ver.name())
            ).findFirst().get();
        final List<MvnArtifactVersion> newer =
            mvn.findVersionsNewerThan(version);
        Assertions.assertEquals(MavenCentralTest.THREE, newer.size());
        Assertions.assertEquals(this.vers.get(0), newer.get(0).name());
        Assertions.assertEquals(this.vers.get(1), newer.get(1).name());
        Assertions.assertEquals(this.vers.get(2), newer.get(2).name());
    }

    /**
     * Check that we correctly find the versions which are older than provided
     * one.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsOlderThan() throws Exception {
        final MvnRepo mvn = new MavenCentral();
        Assertions.assertThrows(
            MvnException.class,
            () -> {
                mvn.findVersionsOlderThan(
                    new MavenArtifactVersion(
                        this.mine,
                        "wrong-version",
                        MvnPackagingType.JAR,
                        System.currentTimeMillis()
                    )
                );
            }
        );
        final MvnArtifactVersion version = mvn.findVersions(this.mine)
            .stream()
            .filter(
                ver ->
                    this.vers.get(2).equals(ver.name())
            ).findFirst().get();
        final List<MvnArtifactVersion> older =
            mvn.findVersionsOlderThan(version);
        Assertions.assertEquals(2, older.size());
        Assertions.assertEquals(
            this.vers.get(this.vers.size() - 2), older.get(0).name()
        );
        Assertions.assertEquals(
            this.vers.get(this.vers.size() - 1), older.get(1).name()
        );
    }

    /**
     * Check that a version which does not start with a digit does not break
     * the search of the newer versions.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsNewerThanNonNumericVersion() throws Exception {
        final List<String> newer = names(
            new MavenCentral().findVersionsNewerThan(this.guavaVersion("r09"))
        );
        Assertions.assertFalse(newer.isEmpty());
        Assertions.assertTrue(newer.contains("10.0"));
        Assertions.assertFalse(newer.contains("r03"));
        Assertions.assertFalse(newer.contains("r09"));
    }

    /**
     * Check that the versions are ordered using Maven's rules: a release
     * candidate is older than the release, and a non-numeric version is older
     * than a numeric one.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsOlderThanQualifiedVersion() throws Exception {
        final List<String> older = names(
            new MavenCentral().findVersionsOlderThan(this.guavaVersion("10.0"))
        );
        Assertions.assertTrue(older.contains("10.0-rc1"));
        Assertions.assertTrue(older.contains("r09"));
        Assertions.assertFalse(older.contains("10.0.1"));
    }

    /**
     * Check that the search does not hang forever if the repository accepts
     * the connection but never answers.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsTimesOut() throws Exception {
        try (ServerSocket silent = new ServerSocket(0)) {
            final MvnRepo mvn = MavenCentralTest.impatient(silent);
            Assertions.assertInstanceOf(
                HttpTimeoutException.class,
                Assertions.assertThrows(
                    MvnException.class,
                    () -> mvn.findArtifacts("guice")
                ).getCause()
            );
        }
    }

    /**
     * Check that the versions search does not hang forever if the repository
     * accepts the connection but never answers.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsTimesOut() throws Exception {
        try (ServerSocket silent = new ServerSocket(0)) {
            final MvnRepo mvn = MavenCentralTest.impatient(silent);
            Assertions.assertInstanceOf(
                HttpTimeoutException.class,
                Assertions.assertThrows(
                    MvnException.class,
                    () -> mvn.findVersions(this.mine)
                ).getCause()
            );
        }
    }

    /**
     * Check that a metadata file which declares a DTD is rejected instead of
     * being parsed. Such a file is the vehicle for XXE: the entity below
     * points at a local file, and an unhardened parser would happily read it
     * and hand its content back as a version name.
     *
     * @param dir The temporary directory with the "secret" file.
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsRejectsMaliciousDoctype(
        @TempDir final Path dir
    ) throws Exception {
        final Path secret = dir.resolve("secret.txt");
        Files.writeString(secret, "top-secret");
        final HttpServer server = MavenCentralTest.serving(
            String.join(
                "",
                "<?xml version=\"1.0\"?>",
                "<!DOCTYPE metadata [",
                String.format("<!ENTITY xxe SYSTEM \"%s\">", secret.toUri()),
                "]>",
                "<metadata><versioning><versions>",
                "<version>&xxe;</version>",
                "</versions></versioning></metadata>"
            )
        );
        try {
            final MvnRepo mvn = MavenCentralTest.talkingTo(server);
            final Throwable cause = Assertions.assertThrows(
                MvnException.class,
                () -> mvn.findVersions(this.mine)
            ).getCause();
            Assertions.assertInstanceOf(SAXParseException.class, cause);
            Assertions.assertTrue(
                cause.getMessage().contains("DOCTYPE"), cause.getMessage()
            );
        } finally {
            server.stop(0);
        }
    }

    /**
     * Check that hardening the parser did not break the parsing of an
     * ordinary metadata file.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsParsesPlainMetadata() throws Exception {
        final HttpServer server = MavenCentralTest.serving(
            String.join(
                "",
                "<?xml version=\"1.0\"?>",
                "<metadata><versioning><versions>",
                "<version>1.0</version><version>2.0</version>",
                "</versions></versioning></metadata>"
            )
        );
        try {
            Assertions.assertEquals(
                Arrays.asList("2.0", "1.0"),
                MavenCentralTest.names(
                    MavenCentralTest.talkingTo(server).findVersions(this.mine)
                )
            );
        } finally {
            server.stop(0);
        }
    }

    /**
     * Check that we correctly find the versions which are newer than provided
     * one.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testCompareVersionsByNumber() throws Exception {
        final MvnRepo mvn = new MavenCentral();
        final MavenArtifactVersion current = new MavenArtifactVersion(
            new MavenArtifact(
                new MavenGroup("org.junit.jupiter"),
                "junit-jupiter-api"
            ),
            "5.12.2",
            MvnPackagingType.JAR,
            System.currentTimeMillis()
        );
        final List<MvnArtifactVersion> newer =
            mvn.findVersionsNewerThan(current);
        for (final MvnArtifactVersion version : newer) {
            Assertions.assertTrue(
                this.calculateNumberFromVersion(version.name())
                    > this.calculateNumberFromVersion(current.name()),
                String.format(
                    "Version %s is not newer than %s.",
                    version.name(), current.name()
                )
            );
        }
    }

    /**
     * Create a repository which talks to the given socket and gives up almost
     * immediately. The socket is never accepted, so the connection is
     * established by the OS and the "server" stays silent forever.
     *
     * @param silent The socket which never answers.
     * @return The repository.
     */
    private static MvnRepo impatient(final ServerSocket silent) {
        final String url = String.format(
            "http://127.0.0.1:%d", silent.getLocalPort()
        );
        return new MavenCentral(
            url, url, Duration.ofSeconds(1), Duration.ofSeconds(1)
        );
    }

    /**
     * Create a repository which reads its metadata from the given local
     * server.
     *
     * @param server The server which answers the requests.
     * @return The repository.
     */
    private static MvnRepo talkingTo(final HttpServer server) {
        final String url = String.format(
            "http://127.0.0.1:%d", server.getAddress().getPort()
        );
        return new MavenCentral(url, url);
    }

    /**
     * Start a local HTTP server which answers every request with the given
     * body. The caller has to stop it.
     *
     * @param body The body of the answer.
     * @return The started server.
     * @throws IOException If the server can not be started.
     */
    private static HttpServer serving(final String body) throws IOException {
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        final HttpServer server = HttpServer.create(
            new InetSocketAddress("127.0.0.1", 0), 0
        );
        server.createContext(
            "/",
            exchange -> {
                exchange.sendResponseHeaders(
                    HttpURLConnection.HTTP_OK, bytes.length
                );
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            }
        );
        server.start();
        return server;
    }

    /**
     * Create a version of Guava.
     *
     * @param name The version's name.
     * @return The version.
     */
    private MvnArtifactVersion guavaVersion(final String name) {
        return new MavenArtifactVersion(
            this.guava, name, MvnPackagingType.JAR, System.currentTimeMillis()
        );
    }

    /**
     * Extract the names of the versions.
     *
     * @param versions The versions.
     * @return The names of the versions.
     */
    private static List<String> names(
        final List<MvnArtifactVersion> versions
    ) {
        return versions.stream().map(MvnArtifactVersion::name).toList();
    }

    /**
     * Calculate a numeric representation of the version.
     * Strips pre-release identifiers (like -M1, -RC1, -SNAPSHOT) before
     * calculating, and uses a large multiplier to handle multi-digit parts.
     *
     * @param version The version.
     * @return The numeric representation.
     */
    private Double calculateNumberFromVersion(final String version) {
        final String base = version.split("-")[0];
        final String clean = base.replaceAll("[^\\d.]", "");
        final List<String> nums = Arrays.asList(clean.split("\\."));
        final int multiplier = 1000;
        return IntStream
            .range(0, nums.size())
            .mapToDouble(
                index ->
                Integer.parseInt(nums.get(index))
                    * Math.pow(multiplier, nums.size() - index - 1)
            )
            .sum();
    }
}
