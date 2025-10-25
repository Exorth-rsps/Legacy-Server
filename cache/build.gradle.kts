repositories {
    mavenLocal()
}

dependencies {
    implementation("dev.or2:all:2.2.3")
    implementation("dev.or2:tools:2.2.3")
    implementation("dev.or2:server-utils:0.7")
}

tasks {
    register("buildCache",JavaExec::class) {
        group = "cache"
        description = "Build Cache"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("org.alter.CacheToolsKt")
        args = listOf("BUILD")
    }

    register("freshCache",JavaExec::class) {
        group = "cache"
        description = "Fresh Install Cache"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("org.alter.CacheToolsKt")
        args = listOf("FRESH_INSTALL")
    }

}