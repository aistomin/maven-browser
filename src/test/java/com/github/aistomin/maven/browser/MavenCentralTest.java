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
import java.io.IOException;
import java.net.ServerSocket;
import java.net.http.HttpTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXParseException;

/**
 * The tests for {@link MavenCentral}.
 * Almost all of them read from {@link FakeMavenCentral}, which serves the
 * fixtures in "src/test/resources/fixtures" instead of the real repository.
 * Maven Central is a live, mutable place: asserting on what it contains today
 * means the suite breaks the moment somebody publishes something, and it also
 * hides the answers which the real repository never gives - a 500, a broken
 * JSON, a metadata file without a single version. The two tests whose names
 * end with "InMavenCentral" are the exception: they do talk to the real
 * repository, because a suite which only ever talks to our own fake can not
 * notice that we stopped being able to talk to the real one. Their assertions
 * are deliberately loose - "not empty", "contains" - so that they check that
 * we can read Maven Central, not what Maven Central happens to hold.
 *
 * @since 0.1
 */
final class MavenCentralTest {

    /**
     * Two.
     */
    private static final int TWO = 2;

    /**
     * Three.
     */
    private static final int THREE = 3;

    /**
     * Five.
     */
    private static final int FIVE = 5;

    /**
     * The name of the fixture with the metadata of my artifact.
     */
    private static final String MINE_METADATA =
        "jenkins-sdk-maven-metadata.xml";

    /**
     * The name of the fixture with the metadata of Guava.
     */
    private static final String GUAVA_METADATA =
        "guava-maven-metadata.xml";

    /**
     * The name of the fixture with the metadata of the JUnit BOM.
     */
    private static final String BOM_METADATA =
        "junit-bom-maven-metadata.xml";

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
     * The JUnit BOM. Its packaging is "pom", not "jar", which is what makes it
     * the artifact to check that we do not fabricate a packaging.
     */
    private final MvnArtifact bom = new MavenArtifact(
        new MavenGroup("org.junit"), "junit-bom"
    );

    /**
     * The versions of my artifact, newest first, as the fixture holds them.
     */
    private final List<String> vers = Arrays.asList(
        "0.2.1",
        "0.2",
        "0.1",
        "0.0.2",
        "0.0.1"
    );

    /**
     * Check that we can still search the real Maven Central. Nothing is
     * asserted about which artifacts come back, only that some do and that
     * they are whole, because the answer changes whenever somebody publishes
     * something named like the search string.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsInMavenCentral() throws Exception {
        final MvnRepo mvn = new MavenCentral();
        final List<MvnArtifact> artifacts = mvn.findArtifacts("guice");
        Assertions.assertFalse(artifacts.isEmpty());
        Assertions.assertTrue(artifacts.size() <= MavenCentral.MAX_ROWS);
        artifacts.forEach(
            artifact -> {
                Assertions.assertFalse(artifact.name().isBlank());
                Assertions.assertFalse(artifact.group().name().isBlank());
            }
        );
        Assertions.assertTrue(
            mvn.findArtifacts("guice", 0, MavenCentralTest.FIVE).size()
                <= MavenCentralTest.FIVE
        );
    }

    /**
     * Check that we can still read the versions out of the real Maven
     * Central. My artifact was last released in 2016, but the assertions
     * still allow it to grow: what is checked is that the versions we know
     * are there are among the ones we get, not that they are the only ones.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsInMavenCentral() throws Exception {
        final List<MvnArtifactVersion> versions =
            new MavenCentral().findVersions(this.mine);
        Assertions.assertTrue(versions.size() >= this.vers.size());
        Assertions.assertTrue(
            MavenCentralTest.names(versions).contains("0.2.1")
        );
        for (final MvnArtifactVersion ver : versions) {
            Assertions.assertEquals(this.mine.name(), ver.artifact().name());
            Assertions.assertEquals(
                this.mine.group().name(), ver.artifact().group().name()
            );
        }
    }

    /**
     * Check that the artifacts are read out of a search answer which has the
     * shape the real search API produces: the documents sit in "docs" inside
     * "response", and each of them carries its own group in "g" and its own
     * name in "a". The fixture holds two documents from two different groups,
     * so a reader which picks the group up once for the whole answer instead
     * of once per document can not pass.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsParsesSearchAnswer() throws Exception {
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withSearch(
                FakeMavenCentral.fixture("search-mixed-groups.json")
            );
            Assertions.assertEquals(
                Arrays.asList(
                    new MavenArtifact(
                        new MavenGroup("org.openidentityplatform.commons"),
                        "guice"
                    ),
                    new MavenArtifact(
                        new MavenGroup("io.github.replay-framework"), "guice"
                    )
                ),
                fake.browser().findArtifacts("guice")
            );
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
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withSearch("{\"responseHeader\":{\"status\":0}}");
            Assertions.assertTrue(
                fake.browser().findArtifacts("whatever").isEmpty()
            );
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
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withSearch("{\"response\":");
            final MvnRepo mvn = fake.browser();
            Assertions.assertInstanceOf(
                JsonProcessingException.class,
                Assertions.assertThrows(
                    MvnException.class,
                    () -> mvn.findArtifacts("whatever")
                ).getCause()
            );
        }
    }

    /**
     * Check that the search without paging asks for the first page of
     * {@link MavenCentral#MAX_ROWS} rows.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsAsksForTheFirstPage() throws Exception {
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withSearch(
                FakeMavenCentral.fixture("search-single-group.json")
            );
            fake.browser().findArtifacts("guice");
            Assertions.assertEquals(
                String.format(
                    "q=guice&start=0&rows=%d&wt=json", MavenCentral.MAX_ROWS
                ),
                fake.lastQuery()
            );
        }
    }

    /**
     * Check that a search string which contains a space reaches the
     * repository in one piece. The answer can not show this - our own fake
     * would answer whatever is asked - so the request itself is asserted.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsEncodesSpace() throws Exception {
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withSearch(
                FakeMavenCentral.fixture("search-single-group.json")
            );
            Assertions.assertFalse(
                fake.browser().findArtifacts("guice inject").isEmpty()
            );
            Assertions.assertEquals(
                String.format(
                    "q=guice+inject&start=0&rows=%d&wt=json",
                    MavenCentral.MAX_ROWS
                ),
                fake.lastQuery()
            );
        }
    }

    /**
     * Check that the Maven search API field syntax survives the trip: the
     * quotes and the colon of {@code g:"..."} are not allowed in a URL, so
     * they have to arrive encoded rather than mangled or dropped.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsEncodesFieldQuery() throws Exception {
        final String group = this.mine.group().name();
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withSearch(
                FakeMavenCentral.fixture("search-single-group.json")
            );
            final List<MvnArtifact> found = fake.browser().findArtifacts(
                String.format("g:\"%s\"", group)
            );
            Assertions.assertEquals(
                String.format(
                    "q=g%%3A%%22%s%%22&start=0&rows=%d&wt=json",
                    group, MavenCentral.MAX_ROWS
                ),
                fake.lastQuery()
            );
            Assertions.assertEquals(MavenCentralTest.THREE, found.size());
            Assertions.assertTrue(found.contains(this.mine));
            found.forEach(
                artifact ->
                    Assertions.assertEquals(group, artifact.group().name())
            );
        }
    }

    /**
     * Check that the request parameters can not be injected via the search
     * string. A search string which carries its own "rows" has to end up
     * encoded inside "q", so that the "rows" the caller asked for stays the
     * only one in the request.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsIgnoresInjectedParameters() throws Exception {
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withSearch(
                FakeMavenCentral.fixture("search-single-group.json")
            );
            fake.browser().findArtifacts(
                "a&rows=1000", 0, MavenCentralTest.FIVE
            );
            Assertions.assertEquals(
                String.format(
                    "q=a%%26rows%%3D1000&start=0&rows=%d&wt=json",
                    MavenCentralTest.FIVE
                ),
                fake.lastQuery()
            );
        }
    }

    /**
     * Check that a repository which answers the search with an error status
     * fails with {@link MvnException} instead of the error page being parsed
     * as if it were an answer.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindArtifactsRejectsErrorStatus() throws Exception {
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withSearchStatus(500);
            final MvnRepo mvn = fake.browser();
            Assertions.assertInstanceOf(
                IOException.class,
                Assertions.assertThrows(
                    MvnException.class,
                    () -> mvn.findArtifacts("whatever")
                ).getCause()
            );
        }
    }

    /**
     * Check that we correctly find the versions of the artifact, and that
     * asking for a page of them gives that page.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersions() throws Exception {
        try (FakeMavenCentral fake = this.serving()) {
            final MvnRepo mvn = fake.browser();
            final List<MvnArtifactVersion> versions =
                mvn.findVersions(this.mine);
            Assertions.assertEquals(
                this.vers, MavenCentralTest.names(versions)
            );
            for (final MvnArtifactVersion ver : versions) {
                Assertions.assertEquals(
                    this.mine.name(), ver.artifact().name()
                );
                Assertions.assertEquals(
                    this.mine.group().name(), ver.artifact().group().name()
                );
            }
            Assertions.assertEquals(
                this.vers.subList(1, MavenCentralTest.THREE),
                MavenCentralTest.names(
                    mvn.findVersions(this.mine, 1, MavenCentralTest.TWO)
                )
            );
        }
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
        try (FakeMavenCentral fake = this.serving()) {
            Assertions.assertEquals(
                this.vers.subList(1, this.vers.size()),
                MavenCentralTest.names(
                    fake.browser().findVersions(
                        this.mine, 1, Integer.MAX_VALUE
                    )
                )
            );
        }
    }

    /**
     * Check that asking for a page which starts past the last version gives
     * an empty list instead of failing on the index.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsPastTheLastPage() throws Exception {
        try (FakeMavenCentral fake = this.serving()) {
            Assertions.assertTrue(
                fake.browser()
                    .findVersions(this.mine, 99, MavenCentral.MAX_ROWS)
                    .isEmpty()
            );
        }
    }

    /**
     * Check that the versions we read out of maven-metadata.xml admit what
     * that file does not say. It carries the version names and nothing else,
     * so claiming a packaging or a release date would be making it up. The
     * artifact here is a BOM, whose packaging is "pom": before, every one of
     * its versions came back calling itself a jar.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsDoesNotFabricateMetadata() throws Exception {
        try (FakeMavenCentral fake = this.serving()) {
            final List<MvnArtifactVersion> versions =
                fake.browser().findVersions(this.bom);
            Assertions.assertFalse(versions.isEmpty());
            for (final MvnArtifactVersion ver : versions) {
                Assertions.assertEquals(
                    MvnPackagingType.UNKNOWN, ver.packaging()
                );
                Assertions.assertTrue(ver.releaseTimestamp().isEmpty());
            }
        }
    }

    /**
     * Check that we correctly find the versions which are newer than provided
     * one.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsNewerThan() throws Exception {
        try (FakeMavenCentral fake = this.serving()) {
            final MvnRepo mvn = fake.browser();
            Assertions.assertThrows(
                MvnException.class,
                () -> mvn.findVersionsNewerThan(this.version("not-existing"))
            );
            final String from =
                this.vers.get(this.vers.size() - MavenCentralTest.TWO);
            Assertions.assertEquals(
                this.vers.subList(0, MavenCentralTest.THREE),
                MavenCentralTest.names(
                    mvn.findVersionsNewerThan(this.version(from))
                )
            );
        }
    }

    /**
     * Check that we correctly find the versions which are older than provided
     * one.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsOlderThan() throws Exception {
        try (FakeMavenCentral fake = this.serving()) {
            final MvnRepo mvn = fake.browser();
            Assertions.assertThrows(
                MvnException.class,
                () -> mvn.findVersionsOlderThan(this.version("wrong-version"))
            );
            Assertions.assertEquals(
                this.vers.subList(MavenCentralTest.THREE, this.vers.size()),
                MavenCentralTest.names(
                    mvn.findVersionsOlderThan(
                        this.version(this.vers.get(MavenCentralTest.TWO))
                    )
                )
            );
        }
    }

    /**
     * Check that a version which does not start with a digit does not break
     * the search of the newer versions: everything Guava released after r09
     * comes back, and neither r09 itself nor the older r03 does.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsNewerThanNonNumericVersion() throws Exception {
        try (FakeMavenCentral fake = this.serving()) {
            Assertions.assertEquals(
                Arrays.asList("33.7.1-jre", "10.0.1", "10.0", "10.0-rc1"),
                MavenCentralTest.names(
                    fake.browser().findVersionsNewerThan(
                        this.guavaVersion("r09")
                    )
                )
            );
        }
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
        try (FakeMavenCentral fake = this.serving()) {
            Assertions.assertEquals(
                Arrays.asList("10.0-rc1", "r09", "r03"),
                MavenCentralTest.names(
                    fake.browser().findVersionsOlderThan(
                        this.guavaVersion("10.0")
                    )
                )
            );
        }
    }

    /**
     * Check that a metadata file which has no versions in it gives an empty
     * list, and that looking for something newer than a version of such an
     * artifact says that the version is not there rather than saying that
     * nothing is newer.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsParsesEmptyMetadata() throws Exception {
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withMetadata(
                this.mine,
                String.join(
                    "",
                    "<?xml version=\"1.0\"?>",
                    "<metadata><versioning><versions/></versioning></metadata>"
                )
            );
            final MvnRepo mvn = fake.browser();
            Assertions.assertTrue(mvn.findVersions(this.mine).isEmpty());
            Assertions.assertThrows(
                MvnException.class,
                () -> mvn.findVersionsNewerThan(this.version("0.2.1"))
            );
        }
    }

    /**
     * Check that a metadata file which is not a well formed XML fails with
     * {@link MvnException} rather than with an unchecked exception.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsRejectsMalformedXml() throws Exception {
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withMetadata(
                this.mine, "<metadata><versioning><versions>"
            );
            final MvnRepo mvn = fake.browser();
            Assertions.assertInstanceOf(
                SAXParseException.class,
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
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withMetadata(
                this.mine,
                String.join(
                    "",
                    "<?xml version=\"1.0\"?>",
                    "<!DOCTYPE metadata [",
                    String.format(
                        "<!ENTITY xxe SYSTEM \"%s\">", secret.toUri()
                    ),
                    "]>",
                    "<metadata><versioning><versions>",
                    "<version>&xxe;</version>",
                    "</versions></versioning></metadata>"
                )
            );
            final MvnRepo mvn = fake.browser();
            final Throwable cause = Assertions.assertThrows(
                MvnException.class,
                () -> mvn.findVersions(this.mine)
            ).getCause();
            Assertions.assertInstanceOf(SAXParseException.class, cause);
            Assertions.assertTrue(
                cause.getMessage().contains("DOCTYPE"), cause.getMessage()
            );
        }
    }

    /**
     * Check that the metadata is looked up under the path which the artifact's
     * coordinates spell out. The fake only answers the path of my artifact, so
     * asking it for Guava has to end up on a path it does not know, and a
     * repository which does not have an artifact answers 404, not silence.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsRejectsUnknownArtifact() throws Exception {
        try (FakeMavenCentral fake = new FakeMavenCentral()) {
            fake.withMetadata(
                this.mine,
                FakeMavenCentral.fixture(MavenCentralTest.MINE_METADATA)
            );
            final MvnRepo mvn = fake.browser();
            Assertions.assertFalse(mvn.findVersions(this.mine).isEmpty());
            Assertions.assertInstanceOf(
                IOException.class,
                Assertions.assertThrows(
                    MvnException.class,
                    () -> mvn.findVersions(this.guava)
                ).getCause()
            );
        }
    }

    /**
     * Check that an artifact whose coordinates contain a character which is
     * not allowed in a URL fails with {@link MvnException} rather than with an
     * unchecked exception.
     *
     * @throws Exception If something went wrong.
     */
    @Test
    void testFindVersionsWithSpaceInCoordinates() throws Exception {
        try (FakeMavenCentral fake = this.serving()) {
            final MvnRepo mvn = fake.browser();
            Assertions.assertThrows(
                MvnException.class,
                () -> mvn.findVersions(
                    new MavenArtifact(
                        new MavenGroup("com.github aistomin"), "jenkins sdk"
                    )
                )
            );
        }
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
     * Create a fake which serves the metadata of both artifacts the tests
     * use.
     *
     * @return The started fake. The caller has to close it.
     * @throws IOException If the fake can not be started or a fixture can not
     *  be read.
     */
    private FakeMavenCentral serving() throws IOException {
        return new FakeMavenCentral()
            .withMetadata(
                this.mine,
                FakeMavenCentral.fixture(MavenCentralTest.MINE_METADATA)
            )
            .withMetadata(
                this.guava,
                FakeMavenCentral.fixture(MavenCentralTest.GUAVA_METADATA)
            )
            .withMetadata(
                this.bom,
                FakeMavenCentral.fixture(MavenCentralTest.BOM_METADATA)
            );
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
     * Create a version of my artifact.
     *
     * @param name The version's name.
     * @return The version.
     */
    private MvnArtifactVersion version(final String name) {
        return new MavenArtifactVersion(
            this.mine, name, MvnPackagingType.JAR, System.currentTimeMillis()
        );
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
}
