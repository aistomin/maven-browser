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

/**
 * Simple implementation of the Maven group entity.
 *
 * @since 0.1
 */
public final class MavenGroup implements MvnGroup {

    /**
     * The Maven group's name.
     */
    private final String group;

    /**
     * Ctor.
     *
     * @param name The Maven group's name.
     */
    public MavenGroup(final String name) {
        this.group = name;
    }

    @Override
    public String name() {
        return this.group;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
            || obj != null
            && getClass() == obj.getClass()
            && this.group.equals(((MavenGroup) obj).group);
    }

    @Override
    public int hashCode() {
        return this.group.hashCode();
    }
}
