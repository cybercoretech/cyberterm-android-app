plugins { id("com.android.application") }

val cybertermCode = providers.gradleProperty("cybertermVersionCode").get().toInt()
val cybertermName = providers.gradleProperty("cybertermVersionName").get()
val releaseStoreFile = providers.environmentVariable("CYBERTERM_RELEASE_STORE_FILE").orNull
val releaseStorePassword = providers.environmentVariable("CYBERTERM_RELEASE_STORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("CYBERTERM_RELEASE_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("CYBERTERM_RELEASE_KEY_PASSWORD").orNull
val releaseValues = listOf(releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword)
val releaseSigningConfigured = releaseValues.all { !it.isNullOrBlank() }
if (releaseValues.any { !it.isNullOrBlank() } && !releaseSigningConfigured) {
    throw GradleException("Release signing requires all CYBERTERM_RELEASE_* environment variables.")
}
val rustDir = rootProject.file("rust")
val ndkDir = providers.environmentVariable("ANDROID_NDK_HOME")
    .orElse(providers.environmentVariable("ANDROID_HOME").map { "$it/ndk/30.0.16248370" })
val rustTargetDir = rootProject.file("build/rust")
val jniDir = file("src/main/jniLibs")
val abis = mapOf("arm64-v8a" to "aarch64-linux-android", "x86_64" to "x86_64-linux-android")

android {
    namespace = "dev.cybercore.cyberterm"
    compileSdk = 36
    buildToolsVersion = "36.0.0"
    ndkVersion = "30.0.16248370"
    defaultConfig {
        applicationId = "dev.cybercore.cyberterm"
        minSdk = 26
        targetSdk = 35
        versionCode = cybertermCode
        versionName = cybertermName
        ndk { abiFilters += abis.keys }
    }
    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                storeType = "PKCS12"
            }
        }
    }
    buildTypes {
        debug { applicationIdSuffix = ".debug"; versionNameSuffix = "-debug" }
        release {
            isMinifyEnabled = false
            if (releaseSigningConfigured) signingConfig = signingConfigs.getByName("release")
        }
    }
    packaging { jniLibs { useLegacyPackaging = false } }
}

val rustBuildTasks = abis.map { (abi, target) ->
    tasks.register<Exec>("buildRust${abi.split('-').joinToString("") { it.replaceFirstChar(Char::uppercase) }}") {
        group = "rust"
        workingDir = rustDir
        environment("CARGO_TARGET_DIR", rustTargetDir.absolutePath)
        val targetKey = target.replace('-', '_')
        environment("CC_$targetKey", "${ndkDir.get()}/toolchains/llvm/prebuilt/linux-x86_64/bin/${target}26-clang")
        environment("AR_$targetKey", "${ndkDir.get()}/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ar")
        environment("CARGO_TARGET_${targetKey.uppercase()}_LINKER", "${ndkDir.get()}/toolchains/llvm/prebuilt/linux-x86_64/bin/${target}26-clang")
        commandLine("cargo", "build", "--locked", "--release", "--target", target)
    }
}

val buildRust = tasks.register("buildRust") {
    group = "rust"
    description = "Build the Cyberterm NativeActivity library for supported ABIs."
    inputs.dir(rustDir.resolve("src"))
    inputs.files(rustDir.resolve("Cargo.toml"), rustDir.resolve("Cargo.lock"))
    outputs.dir(jniDir)
    dependsOn(rustBuildTasks)
    doLast {
        abis.forEach { (abi, target) ->
            val source = rustTargetDir.resolve("$target/release/libmain.so")
            check(source.isFile) { "Rust build did not produce $source" }
            val destination = jniDir.resolve(abi)
            destination.mkdirs()
            source.copyTo(destination.resolve("libmain.so"), overwrite = true)
        }
    }
}

tasks.named("preBuild") { dependsOn(buildRust) }
