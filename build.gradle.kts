
plugins {
    id ("com.github.rodm.teamcity-environments") version "1.3.2"
}

extra["downloadsDir"] = project.findProperty("downloads.dir") as String? ?: "$rootDir/downloads"
extra["serversDir"] = project.findProperty("servers.dir") as String? ?: "$rootDir/servers"
extra["java8Home"] = project.findProperty("java8.home") as String? ?: "/opt/jdk1.8.0_202"

teamcity {
    environments {
        downloadsDir = extra["downloadsDir"] as String
        baseHomeDir = extra["serversDir"] as String

        register("teamcity2020.2") {
            version = "2020.2.1"
            javaHome = file(extra["java8Home"] as String)
        }
    }
}
