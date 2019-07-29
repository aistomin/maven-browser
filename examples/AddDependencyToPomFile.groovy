import com.github.aistomin.maven.browser.MavenCentral

/**
 * This example demonstrates how we can programmatically add certain dependency
 * to the pom file.
 */
@Grapes(
    @Grab(
        group = 'com.github.aistomin',
        module = 'maven-browser',
        version = '10.0-SNAPSHOT'
    )
)
final slurper = new XmlSlurper()
println('Parsing POM file .....')
final pomRoot = new XmlSlurper().parseText(new File('pom_sample.xml').text)
println()
println('Find Maven artifact com.github.aistomin:jenkins-sdk:0.0.2 .....')
final repository = new MavenCentral()
final version = repository.findVersions(
    repository.findArtifacts('aistomin')
        .find { it.name() == 'jenkins-sdk' }
).find { '0.0.2' == it.name() }
println()
println("Add jenkins-sdk dependency to the POM file.")
pomRoot.find { it.name() == 'dependencies'}
    .appendNode(slurper.parseText(version.dependency().forMaven()))
println()
println("Save modified POM file as pom_sample_modified.xml .....")
new File('pom_sample_modified.xml').write(groovy.xml.XmlUtil.serialize(pomRoot))
println()
println('Done.')
