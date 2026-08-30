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

/**
 * The packaging types which a Maven artifact's version can have, plus
 * {@link MvnPackagingType#UNKNOWN} for the versions whose packaging the
 * repository did not tell us.
 *
 * @since 1.0
 */
public enum MvnPackagingType {

    /**
     * Packaging type "pom".
     */
    POM("pom"),

    /**
     * Packaging type "jar".
     */
    JAR("jar"),

    /**
     * Packaging type "maven-plugin".
     */
    MAVEN_PLUGIN("maven-plugin"),

    /**
     * Packaging type "ejb".
     */
    EJB("ejb"),

    /**
     * Packaging type "war".
     */
    WAR("war"),

    /**
     * Packaging type "ear".
     */
    EAR("ear"),

    /**
     * Packaging type "rar".
     */
    RAR("rar"),

    /**
     * Packaging type "par".
     */
    PAR("par"),

    /**
     * The packaging type is not known. The repository did not tell us which
     * one it is, which is what every version read out of maven-metadata.xml
     * looks like: that file carries no per-version packaging at all.
     */
    UNKNOWN("unknown");

    /**
     * Packaging type as it appears in pom.xml.
     */
    private final String type;

    /**
     * Ctor.
     *
     * @param str Packaging type as it appears in pom.xml.
     */
    MvnPackagingType(final String str) {
        this.type = str;
    }

    /**
     * Packaging type as it appears in pom.xml.
     *
     * @return Packaging type string.
     */
    public String packaging() {
        return this.type;
    }
}
