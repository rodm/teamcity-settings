package GradleTeamCityPlugin

import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v10.Project
import jetbrains.buildServer.configs.kotlin.v10.vcs.GitVcsRoot

object Project : Project({
    uuid = "2c4c777e-bf5d-4eaf-8e46-eea999fdbd89"
    extId = "GradleTeamCityPlugin"
    parentId = "_Root"
    name = "Gradle TeamCity Plugin"
    description = "Gradle TeamCity Plugin project"

    vcsRoot(GitVcsRoot({
        uuid = "723408f3-cc0c-42da-b348-dedd4bc030ef"
        extId = "TeamcitySettings"
        name = "teamcity-settings"
        url = "https://github.com/rodm/teamcity-settings"
    }))
})
