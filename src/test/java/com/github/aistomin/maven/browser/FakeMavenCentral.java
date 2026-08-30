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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Maven Central which runs on localhost and answers out of the fixtures
 * instead of out of the real repository.
 * Only the paths which were explicitly registered are answered; everything
 * else gets a 404, the same way the real repository reacts to an artifact
 * which it does not have. That is what makes the tests prove that
 * {@link MavenCentral} builds its URLs correctly: an answer only arrives if
 * the URL is the one we expect.
 *
 * @since 5.2
 */
final class FakeMavenCentral implements AutoCloseable {

    /**
     * The path under which the fake serves the repository, mirroring the
     * layout of the real repo1.maven.org.
     */
    private static final String REPO = "/maven2";

    /**
     * The path under which the fake serves the search API, mirroring the
     * layout of the real search.maven.org.
     */
    private static final String SEARCH = "/solrsearch/select";

    /**
     * The answers which the fake gives, by the path they are registered for.
     */
    private final Map<String, Answer> answers;

    /**
     * The query string of the request which arrived last, or null if the fake
     * was not asked anything yet.
     */
    private final AtomicReference<String> last;

    /**
     * The server which does the talking.
     */
    private final HttpServer server;

    /**
     * Ctor. Starts the server on a free port of the loopback interface.
     *
     * @throws IOException If the server can not be started.
     */
    FakeMavenCentral() throws IOException {
        this.answers = new ConcurrentHashMap<>();
        this.last = new AtomicReference<>();
        this.server = HttpServer.create(
            new InetSocketAddress("127.0.0.1", 0), 0
        );
        this.server.createContext("/", this::answer);
        this.server.start();
    }

    /**
     * Read a fixture out of the test classpath.
     *
     * @param name The name of the fixture file.
     * @return The content of the fixture.
     * @throws IOException If the fixture can not be read.
     */
    static String fixture(final String name) throws IOException {
        final String path = String.format("/fixtures/%s", name);
        try (InputStream stream =
            FakeMavenCentral.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException(
                    String.format("The fixture %s does not exist.", path)
                );
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Answer the metadata of the given artifact with the given body.
     *
     * @param artifact The artifact whose metadata is asked for.
     * @param body The body of the answer.
     * @return This fake, so that the calls can be chained.
     */
    FakeMavenCentral withMetadata(
        final MvnArtifact artifact, final String body
    ) {
        return this.withAnswer(
            FakeMavenCentral.metadataPath(artifact),
            HttpURLConnection.HTTP_OK,
            body
        );
    }

    /**
     * Answer the search requests with the given body.
     *
     * @param body The body of the answer.
     * @return This fake, so that the calls can be chained.
     */
    FakeMavenCentral withSearch(final String body) {
        return this.withAnswer(
            FakeMavenCentral.SEARCH, HttpURLConnection.HTTP_OK, body
        );
    }

    /**
     * Answer the search requests with the given HTTP status.
     *
     * @param status The status of the answer.
     * @return This fake, so that the calls can be chained.
     */
    FakeMavenCentral withSearchStatus(final int status) {
        return this.withAnswer(FakeMavenCentral.SEARCH, status, "");
    }

    /**
     * Create the browser which reads from this fake.
     *
     * @return The browser.
     */
    MvnRepo browser() {
        final String url = String.format(
            "http://127.0.0.1:%d", this.server.getAddress().getPort()
        );
        return new MavenCentral(
            String.format("%s%s", url, FakeMavenCentral.REPO),
            String.format("%s%s", url, FakeMavenCentral.SEARCH)
        );
    }

    /**
     * The query string of the request which arrived last. The tests which are
     * about how a request is built assert on it, because the body of the
     * answer can not tell them whether the search string reached the fake in
     * one piece.
     *
     * @return The query string, or null if nothing was asked yet.
     */
    String lastQuery() {
        return this.last.get();
    }

    @Override
    public void close() {
        this.server.stop(0);
    }

    /**
     * The path under which the metadata of the given artifact is served.
     *
     * @param artifact The artifact.
     * @return The path.
     */
    private static String metadataPath(final MvnArtifact artifact) {
        return String.format(
            "%s/%s/%s/maven-metadata.xml",
            FakeMavenCentral.REPO,
            artifact.group().name().replace('.', '/'),
            artifact.name()
        );
    }

    /**
     * Register an answer for the given path.
     *
     * @param path The path which is answered.
     * @param status The status of the answer.
     * @param body The body of the answer.
     * @return This fake, so that the calls can be chained.
     */
    private FakeMavenCentral withAnswer(
        final String path, final int status, final String body
    ) {
        this.answers.put(path, new Answer(status, body));
        return this;
    }

    /**
     * Answer one request: remember its query and write back whatever is
     * registered for its path, or a 404 if nothing is.
     *
     * @param exchange The request which arrived.
     * @throws IOException If the answer can not be written.
     */
    private void answer(final HttpExchange exchange) throws IOException {
        this.last.set(exchange.getRequestURI().getRawQuery());
        final Answer registered = this.answers.get(
            exchange.getRequestURI().getPath()
        );
        final Answer given;
        if (registered == null) {
            given = new Answer(HttpURLConnection.HTTP_NOT_FOUND, "");
        } else {
            given = registered;
        }
        final byte[] bytes = given.body().getBytes(StandardCharsets.UTF_8);
        final long length;
        if (bytes.length == 0) {
            length = -1L;
        } else {
            length = bytes.length;
        }
        exchange.sendResponseHeaders(given.status(), length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /**
     * One answer of the fake.
     *
     * @param status The HTTP status of the answer.
     * @param body The body of the answer.
     * @since 5.2
     */
    private record Answer(int status, String body) {
    }
}
