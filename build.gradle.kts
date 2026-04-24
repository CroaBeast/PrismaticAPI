plugins {
    kotlin("jvm") version "2.3.20-Beta1"
    id("java-library")
    id("io.freefair.lombok") version "9.4.0"
    id("com.gradleup.shadow") version "9.4.1"
}

group = "me.croabeast"
version = "1.4.0"

val vncProjectDir = listOf("../VNC", "VNC")
    .map(::file)
    .firstOrNull { candidate ->
        candidate.resolve("build.gradle.kts").exists() && candidate.resolve("settings.gradle.kts").exists()
    }
    ?: error("VNC project not found. Clone CroaBeast/VNC next to PrismaticAPI or into PrismaticAPI/VNC.")

val vncBuildScript = vncProjectDir.resolve("build.gradle.kts").readText()
val vncSettingsScript = vncProjectDir.resolve("settings.gradle.kts").readText()

val vncVersion = Regex("""(?m)^\s*version\s*=\s*"([^"]+)"""")
    .find(vncBuildScript)
    ?.groupValues
    ?.getOrNull(1)
    ?: error("Could not resolve VNC version from ${vncProjectDir.resolve("build.gradle.kts")}.")

val vncArtifactId: String? = Regex("""(?m)^\s*rootProject\.name\s*=\s*"([^"]+)"""")
    .find(vncSettingsScript)
    ?.groupValues
    ?.getOrNull(1)
    ?: vncProjectDir.name

val VNC = vncProjectDir.resolve("build/libs/$vncArtifactId-$vncVersion.jar")

val embedded by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.viaversion.com")
}

dependencies {
    implementation(files(VNC))
    embedded(files(VNC))

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

val buildVncJar by tasks.registering(Exec::class) {
    group = "build"
    description = "Builds the local VNC artifacts used by PrismaticAPI without deleting existing jars."
    workingDir = vncProjectDir

    if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        commandLine("cmd", "/c", "gradlew.bat", "jar", "sourcesJar")
    } else {
        commandLine("bash", "gradlew", "jar", "sourcesJar")
    }
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(buildVncJar)
}

tasks.withType<Javadoc>().configureEach {
    dependsOn(buildVncJar)
}

tasks.jar {
    dependsOn(buildVncJar)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from({
        embedded
            .filter { it.extension == "jar" }
            .map(::zipTree)
    })

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
