rootProject.name = "PrismaticAPI"

// Builds VNC from source and substitutes it for me.croabeast.vnc:VNC, so changes on both sides can
// be developed together before VNC is published. Locally the checkout is a sibling folder;
// publish.yml checks it out inside this repository instead. When neither path exists the build
// falls back to the published artifact.
listOf("../VNC", "VNC")
    .map(::file)
    .firstOrNull { it.resolve("settings.gradle.kts").isFile }
    ?.let {
        includeBuild(it) {
            // The published artifact is me.croabeast.vnc:VNC but the producing project is :bootstrap
            // (its jar is named VNC and bundles every module), so the substitution is explicit.
            dependencySubstitution {
                substitute(module("me.croabeast.vnc:VNC")).using(project(":bootstrap"))
            }
        }
    }
