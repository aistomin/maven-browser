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

import org.json.simple.JSONObject;

/**
 * Simple implementation of the Maven artifact's version entity.
 *
 * @since 0.1
 */
public final class MavenArtifactVersion implements MvnArtifactVersion {

    /**
     * Magic number that we use for generating hash code.
     */
    private static final int MAGIC_NUMBER = 31;

    /**
     * Maven artifact.
     */
    private final MvnArtifact art;

    /**
     * Maven artifact version.
     */
    private final String ver;

    /**
     * The timestamp when the version was released.
     */
    private final Long release;

    /**
     * Ctor.
     *
     * @param artifact Maven artifact.
     * @param version Maven artifact version.
     * @param timestamp The timestamp when the version was released.
     */
    public MavenArtifactVersion(
        final MvnArtifact artifact, final String version,
        final Long timestamp
    ) {
        this.art = artifact;
        this.ver = version;
        this.release = timestamp;
    }

    /**
     * Ctor.
     *
     * @param json JSON object.
     */
    public MavenArtifactVersion(final JSONObject json) {
        this(
            new MavenArtifact(json),
            (String) json.get("v"), (Long) json.get("timestamp")
        );
    }

    @Override
    public MvnArtifact artifact() {
        return this.art;
    }

    @Override
    public String name() {
        return this.ver;
    }

    @Override
    public Long releaseTimestamp() {
        return this.release;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
            || obj != null
            && getClass() == obj.getClass()
            && this.art.equals(((MavenArtifactVersion) obj).art)
            && this.ver.equals(((MavenArtifactVersion) obj).ver);
    }

    @Override
    public int hashCode() {
        return MavenArtifactVersion.MAGIC_NUMBER * this.art.hashCode()
            + this.ver.hashCode();
    }
}
