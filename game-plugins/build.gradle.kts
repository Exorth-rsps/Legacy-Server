description = "Alter Servers Plugins"
val lib = rootProject.project.libs

dependencies {
    implementation(project(":cache"))
    implementation(projects.gameServer)
    implementation(projects.util)
    implementation(project(":game-api"))
    implementation(rootProject.project.libs.rsprot)
    implementation(lib.routefinder)
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}