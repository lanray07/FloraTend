plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}
android {
    namespace = "com.floratend.app"
    compileSdk = 36
    // Opt-in device verification of the actual non-debuggable, signed candidate.
    testBuildType = providers.gradleProperty("floratend.testBuildType").getOrElse("debug")
    defaultConfig {
        applicationId = "com.floratend.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 15
        versionName = "0.1.14"
        // Empty until the privately configured, free-only identification server is verified.
        val identifierUrl = providers.gradleProperty("floratend.plantIdentifierUrl").getOrElse("")
        require(identifierUrl.isEmpty() || identifierUrl.matches(Regex("https://[a-zA-Z0-9.-]+/identify"))) { "Invalid plant identifier HTTPS URL" }
        buildConfigField("String", "PLANT_IDENTIFIER_URL", "\"$identifierUrl\"")
        val verifierUrl = providers.gradleProperty("floratend.receiptVerifierUrl")
            .orElse(providers.environmentVariable("FLORATEND_RECEIPT_VERIFIER_URL"))
            .getOrElse("https://floratend-receipt-verifier-production.lanraybanks.workers.dev/verify")
        require(verifierUrl.isEmpty() || verifierUrl.matches(Regex("https://[a-zA-Z0-9.-]+/[a-zA-Z0-9/_-]+"))) { "Invalid receipt verifier HTTPS URL" }
        buildConfigField("String", "RECEIPT_VERIFIER_URL", "\"$verifierUrl\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("store") {
            val path = System.getenv("FLORATEND_KEYSTORE")?.takeIf { it.isNotBlank() }
            if (path != null) {
                storeFile = file(path)
                storePassword = System.getenv("FLORATEND_STORE_PASSWORD")
                keyAlias = System.getenv("FLORATEND_KEY_ALIAS")
                keyPassword = System.getenv("FLORATEND_KEY_PASSWORD")
            }
        }
    }
    buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro"); testProguardFiles("test-proguard-rules.pro"); signingConfig = if (!System.getenv("FLORATEND_KEYSTORE").isNullOrBlank()) signingConfigs.getByName("store") else null } }
    buildTypes.create("qa") {
        initWith(buildTypes.getByName("debug"))
        applicationIdSuffix = ".qa"
        versionNameSuffix = "-qa"
        matchingFallbacks += "debug"
    }
    // Isolated, non-debuggable rendering verification; never the Amazon package.
    buildTypes.create("benchmark") {
        initWith(buildTypes.getByName("release"))
        // Recovery/diagnostics only; production release optimization cannot be disabled by this flag.
        isMinifyEnabled = providers.gradleProperty("floratend.perfOptimization").getOrElse("true").toBooleanStrict()
        applicationIdSuffix = ".perf"
        signingConfig = signingConfigs.getByName("debug")
        matchingFallbacks += "release"
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    testOptions { unitTests.isReturnDefaultValues = true }
    sourceSets.getByName("test").resources.srcDir("src/main/assets")
    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
}
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
dependencies {
    implementation(project(":core"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
    ksp("androidx.room:room-compiler:2.7.1")
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("com.amazon.device:amazon-appstore-sdk:3.0.9")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation("androidx.room:room-testing:2.7.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.8")
}

// Newly exported schemas must exist before test assets are snapshotted, including
// the first build after a schema increment (not just subsequent incremental builds).
tasks.configureEach {
    if (name.startsWith("merge") && name.endsWith("AndroidTestAssets")) {
        val variant = name.removePrefix("merge").removeSuffix("AndroidTestAssets")
        dependsOn("ksp${variant}Kotlin")
    }
}
