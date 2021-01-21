
import com.github.rodm.teamcity.TeamCityEnvironment

plugins {
    id ("com.github.rodm.teamcity-environments") version "1.2.2"
}

extra["downloadsDir"] = project.findProperty("downloads.dir") as String? ?: "$rootDir/downloads"
extra["serversDir"] = project.findProperty("servers.dir") as String? ?: "$rootDir/servers"
extra["java8Home"] = project.findProperty("java8.home") as String? ?: "/opt/jdk1.8.0_202"

teamcity {
    environments {
        downloadsDir = extra["downloadsDir"] as String
        baseHomeDir = extra["serversDir"] as String

        operator fun String.invoke(block: TeamCityEnvironment.() -> Unit) = environments.create(this, closureOf(block))

        "teamcity2020.1" {
            version = "2020.1.5"
            javaHome = file(extra["java8Home"] as String)
        }
    }
}
