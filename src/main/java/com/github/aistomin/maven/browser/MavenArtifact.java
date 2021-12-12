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

import org.json.simple.JSONObject;

/**
 * Simple implementation of the Maven artifact entity.
 *
 * @since 0.1
 */
public final class MavenArtifact implements MvnArtifact {

    /**
     * Magic number that we use for generating hash code.
     */
    private static final int MAGIC_NUMBER = 31;

    /**
     * The artifact's name.
     */
    private final String artifact;

    /**
     * The artifact's group.
     */
    private final MvnGroup grp;

    /**
     * Ctor.
     *
     * @param group The artifact's group.
     * @param name The artifact's name.
     */
    public MavenArtifact(final MvnGroup group, final String name) {
        this.artifact = name;
        this.grp = group;
    }

    /**
     * Ctor.
     *
     * @param json JSON object.
     */
    public MavenArtifact(final JSONObject json) {
        this(new MavenGroup(json), (String) json.get("a"));
    }

    @Override
    public MvnGroup group() {
        return this.grp;
    }

    @Override
    public String name() {
        return this.artifact;
    }

    @Override
    public String identifier() {
        return String.format("%s:%s", this.group().name(), this.name());
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
            || obj != null
            && getClass() == obj.getClass()
            && this.artifact.equals(((MavenArtifact) obj).artifact)
            && this.grp.equals(((MavenArtifact) obj).grp);
    }

    @Override
    public int hashCode() {
        return MavenArtifact.MAGIC_NUMBER * this.artifact.hashCode()
            + this.grp.hashCode();
    }

    @Override
    public String toString() {
        return this.identifier();
    }
}
