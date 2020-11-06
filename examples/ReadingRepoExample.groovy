import com.github.aistomin.maven.browser.MavenCentral

/**
 * In this example the basic reporitory browsing functions are demonstrated.
 */
@Grapes(
    @Grab(
        group = 'com.github.aistomin',
        module = 'maven-browser',
        version = '1.4'
    )
)

final repository = new MavenCentral()
println(
    'Find the artifacts which contain "aistomin" string in it .....'
)
println()
final artifacts =
    repository.findArtifacts('aistomin')
println ('The following artifact were found:')
artifacts.each { artifact ->
    println(artifact.identifier())
}
final artifact = artifacts.find { it.name() == 'jenkins-sdk' }
println(
    "Artifact ${artifact.identifier()} has the following versions:"
)
final versions =
    repository.findVersions(artifact)
versions.each { version ->
    println(version.name())
}
println()
println(
    'Find all the artifact versions newer than 0.0.2 .....'
)
final ver002 = versions.find { '0.0.2' == it.name() }
repository.findVersionsNewerThan(ver002).each { version ->
    println(version.name())
}
println()
println(
    'Find all the artifact versions older than 0.0.2 .....'
)
repository.findVersionsOlderThan(ver002).each { version ->
    println(version.name())
}
println()
println('Done.')