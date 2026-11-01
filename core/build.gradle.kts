plugins { id("org.jetbrains.kotlin.jvm") }

kotlin { jvmToolchain(17) }

// Contract tests load the exact command dictionaries packaged by the app.
sourceSets.test { resources.srcDir(rootProject.file("app/src/main/assets")) }

dependencies { testImplementation("junit:junit:4.13.2") }
