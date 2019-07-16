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
 * The interface which is responsible for manipulating the Maven artifact's
 * version as a Maven/Ant/Gradle dependency.
 *
 * @since 1.0
 */
public interface MvnDependency {

    /**
     * Converts the dependency to Apache Maven dependency string.
     *
     * @return Apache Maven dependency string.
     */
    String forMaven();

    /**
     * Converts the dependency to Apache Buildr dependency string.
     *
     * @return Apache Buildr dependency string.
     */
    String forBuildr();

    /**
     * Converts the dependency to Apache Ivy dependency string.
     *
     * @return Apache Ivy dependency string.
     */
    String forIvy();

    /**
     * Converts the dependency to Groovy Grape dependency string.
     *
     * @return Groovy Grape dependency string.
     */
    String forGroovyGrape();

    /**
     * Converts the dependency to Gradle/Grails dependency string.
     *
     * @return Gradle/Grails dependency string.
     */
    String forGradle();

    /**
     * Converts the dependency to Scala SBT dependency string.
     *
     * @return Scala SBT dependency string.
     */
    String forScala();

    /**
     * Converts the dependency to Leiningen dependency string.
     *
     * @return Leiningen dependency string.
     */
    String forLeiningen();
}
