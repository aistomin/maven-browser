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

import java.util.List;
import org.apache.commons.lang3.NotImplementedException;

/**
 * The class which works with Maven Central repository.
 * URL: https://search.maven.org/
 *
 * @since 0.1
 * @todo: Issue #9. Let's implement it and remove this todo.
 * @todo: Issue #10. Let's implement it and remove this todo.
 * @todo: Issue #11. Let's implement it and remove this todo.
 * @todo: Issue #12. Let's implement it and remove this todo.
 */
public final class MavenCentral implements MvnRepo {

    @Override
    public List<MvnArtifact> findArtifacts(
        final String str, final Integer indent, final Integer rows
    ) {
        throw new NotImplementedException(
            "The method MavenCentral.findArtifacts() is not implemented."
        );
    }

    @Override
    public List<MvnArtifactVersion> findVersions(
        final MvnArtifact artifact, final Integer indent, final Integer rows
    ) {
        throw new NotImplementedException(
            "The method MavenCentral.findVersions() is not implemented."
        );
    }

    @Override
    public List<MvnArtifactVersion> findVersionsNewerThan(
        final MvnArtifactVersion version
    ) {
        throw new NotImplementedException(
            "The method MavenCentral.findVersionsNewerThan is not implemented."
        );
    }

    @Override
    public List<MvnArtifactVersion> findVersionsOlderThan(
        final MvnArtifactVersion version
    ) {
        throw new NotImplementedException(
            "The method MavenCentral.findVersionsOlderThan is not implemented."
        );
    }
}
