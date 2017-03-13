package GradleTeamCityPlugin

import jetbrains.buildServer.configs.kotlin.v10.BuildType
import jetbrains.buildServer.configs.kotlin.v10.Project
import jetbrains.buildServer.configs.kotlin.v10.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.versionedSettings
import jetbrains.buildServer.configs.kotlin.v10.triggers.vcs
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

    features {
        versionedSettings {
            id = "PROJECT_EXT_1"
            mode = VersionedSettings.Mode.ENABLED
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
            rootExtId = "TeamcitySettings"
            showChanges = true
            settingsFormat = VersionedSettings.Format.KOTLIN
        }
    }

    val vcs = GitVcsRoot({
        uuid = "ac063d49-90e5-4baf-84b3-7f307586ae0e"
        extId = "GradleTeamcityPlugin"
        name = "gradle-teamcity-plugin"
        url = "https://github.com/rodm/gradle-teamcity-plugin.git"
    })

    vcsRoot(vcs)

    val buildType = BuildType({
        uuid = "b9b0cbf7-1665-4fe5-a24d-956280379ef0"
        extId = "GradleTeamcityPlugin_Build"
        name = "Build"

        vcs {
            root(vcs)
        }

        steps {
            gradle {
                tasks = "%gradle.tasks%"
                useGradleWrapper = true
                gradleWrapperPath = ""
                enableStacktrace = true
            }
        }

        params {
            param("gradle.tasks", "clean build")
        }

        triggers {
            vcs {
            }
        }
    })
    buildType(buildType)
})
