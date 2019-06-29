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

import org.apache.commons.lang3.NotImplementedException;

/**
 * Simple implementation of the Maven group entity.
 *
 * @since 0.1
 * @todo: Issue #8. Let's implement it and remove this todo.
 */
public final class MavenGroup implements MvnGroup {

    @Override
    public String name() {
        throw new NotImplementedException(
            "The method MavenGroup.name() is not implemented."
        );
    }
}
