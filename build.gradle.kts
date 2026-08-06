plugins {
    kotlin("jvm") version "2.3.20-Beta1"
    id("java-library")
    id("io.freefair.lombok") version "9.4.0"
    id("com.gradleup.shadow") version "9.4.1"
}

group = "me.croabeast"
version = "2.0.0"

val vncVersion = "1.2.1"

val embedded by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

repositories {
    mavenCentral()

    maven("https://croabeast.github.io/repo/")

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.viaversion.com")
}

dependencies {
    // Transitive on purpose: the published VNC jar is a fat jar with an empty pom, so this changes
    // nothing there, but when settings.gradle.kts substitutes it for the local build the classes
    // live in :core, :bukkit and friends, which only reach the compile classpath through :bootstrap.
    implementation("me.croabeast.vnc:VNC:$vncVersion")
    
    // Stays non-transitive: :bootstrap already inlines every module, one jar is all that is embedded.
    embedded("me.croabeast.vnc:VNC:$vncVersion") {
        isTransitive = false
    }

    compileOnly("org.jetbrains:annotations:26.0.2-1")
    annotationProcessor("org.jetbrains:annotations:26.0.2-1")

    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")

    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")

    compileOnlyApi("net.kyori:adventure-api:4.21.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.21.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.21.0")

    compileOnly("com.viaversion:viaversion-api:5.8.1") {
        isTransitive = false
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Javadoc>().configureEach {
    isFailOnError = false

    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        encoding = "UTF-8"
        charSet = "UTF-8"
        docEncoding = "UTF-8"

        if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_1_9))
            addBooleanOption("html5", true)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.compilerArgs.add("-Xlint:-options")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from({
        embedded
            .filter { it.extension == "jar" }
            .map(::zipTree)
    }) {
        exclude("META-INF/**", "**.json", "plugin.yml")
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
