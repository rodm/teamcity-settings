package GradleTeamCityPlugin

import jetbrains.buildServer.configs.kotlin.v2017_2.version
import jetbrains.buildServer.configs.kotlin.v2017_2.project
import jetbrains.buildServer.configs.kotlin.v2017_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2017_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2017_2.projectFeatures.versionedSettings
import jetbrains.buildServer.configs.kotlin.v2017_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2017_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2017_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2017_2.Template

/*
The settings script is an entry point for defining a single
TeamCity project. TeamCity looks for the 'settings.kts' file in a
project directory and runs it if it's found, so the script name
shouldn't be changed and its package should be the same as the
project's external id.

The script should contain a single call to the project() function
with a Project instance or an init function as an argument, you
can also specify both of them to refine the specified Project in
the init function.

VcsRoots, BuildTypes, and Templates of this project must be
registered inside project using the vcsRoot(), buildType(), and
template() methods respectively.

Subprojects can be defined either in their own settings.kts or by
calling the subProjects() method in this project.
*/

version = "2017.2"
project {
    uuid = "2c4c777e-bf5d-4eaf-8e46-eea999fdbd89"
    id = "GradleTeamCityPlugin"
    name = "Gradle TeamCity Plugin"
    description = "Gradle plugin for developing TeamCity plugins [master]"

    val settingsVcs = GitVcsRoot({
        uuid = "723408f3-cc0c-42da-b348-dedd4bc030ef"
        id = "TeamcitySettings"
        name = "teamcity-settings"
        url = "https://github.com/rodm/teamcity-settings"
    })
    vcsRoot(settingsVcs)

    features {
        versionedSettings {
            id = "PROJECT_EXT_1"
            mode = VersionedSettings.Mode.ENABLED
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
            rootExtId = "TeamcitySettings"
            showChanges = true
            settingsFormat = VersionedSettings.Format.KOTLIN
        }
        feature {
            id = "PROJECT_EXT_2"
            type = "JetBrains.SharedResources"
            param("name", "BuildLimit")
            param("type", "quoted")
            param("quota", "2")
        }
    }

    val vcs = GitVcsRoot({
        uuid = "ac063d49-90e5-4baf-84b3-7f307586ae0e"
        id = "GradleTeamcityPlugin"
        name = "gradle-teamcity-plugin"
        url = "https://github.com/rodm/gradle-teamcity-plugin.git"
    })
    vcsRoot(vcs)

    val buildTemplate = Template({
        uuid = "7f359c83-e4f3-4053-b9d6-d403a626b560"
        id = "GradleTeamCityPlugin_Build"
        name = "build"

        vcs {
            root(vcs)
        }

        steps {
            gradle {
                id = "RUNNER_1"
                tasks = "%gradle.tasks%"
                gradleParams = "%gradle.opts%"
                useGradleWrapper = true
                gradleWrapperPath = ""
                enableStacktrace = true
                jdkHome = "%java.home%"
            }
        }

        triggers {
            vcs {
                id = "TRIGGER_1"
                triggerRules = """
                    +:root=TeamcitySettings;:**
                    +:root=GradleTeamcityPlugin:**
                 """.trimIndent()
            }
        }

        failureConditions {
            executionTimeoutMin = 10
        }

        features {
            feature {
                id = "perfmon"
                type = "perfmon"
            }
            feature {
                id = "BUILD_EXT_2"
                type = "JetBrains.SharedResources"
                param("locks-param", "BuildLimit readLock")
            }
        }

        params {
            param("gradle.tasks", "clean build")
            param("gradle.opts", "")
            param("java.home", "%java7.home%")
        }
    })
    template(buildTemplate)

    buildType(BuildType({
        uuid = "b9b0cbf7-1665-4fe5-a24d-956280379ef0"
        id = "GradleTeamcityPlugin_BuildJava7"
        name = "Build - Java 7"
        template(buildTemplate)
    }))

    buildType(BuildType({
        uuid = "b9b0cbf7-1665-4fe5-a24d-956280379ef1"
        id = "GradleTeamcityPlugin_BuildJava8"
        name = "Build - Java 8"
        template(buildTemplate)
        params{
            param("java.home", "%java8.home%")
        }
        disableSettings("perfmon", "BUILD_EXT_2")
    }))

    buildType(BuildType({
        uuid = "b9b0cbf7-1665-4fe5-a24d-956280379ef2"
        id = "GradleTeamcityPlugin_FunctionalTestJava7"
        name = "Functional Test - Java 7"
        template(buildTemplate)
        failureConditions {
            executionTimeoutMin = 20
        }
        params{
            param("gradle.tasks", "clean functionalTest")
        }
    }))

    buildType(BuildType({
        uuid = "b9b0cbf7-1665-4fe5-a24d-956280379ef3"
        id = "GradleTeamcityPlugin_FunctionalTestJava8"
        name = "Functional Test - Java 8"
        template(buildTemplate)
        failureConditions {
            executionTimeoutMin = 20
        }
        params{
            param("gradle.tasks", "clean functionalTest")
            param("java.home", "%java8.home%")
        }
    }))

    buildType(BuildType({
        uuid = "b9b0cbf7-1665-4fe5-a24d-956280379ef4"
        id = "GradleTeamcityPlugin_SamplesTestJava7"
        name = "Samples Test - Java 7"
        template(buildTemplate)
        params{
            param("gradle.tasks", "clean samplesTest")
        }
    }))

    buildType(BuildType({
        uuid = "b9b0cbf7-1665-4fe5-a24d-956280379ef5"
        id = "GradleTeamcityPlugin_ReportCodeQuality"
        name = "Report - Code Quality"
        template(buildTemplate)
        params{
            param("gradle.tasks", "clean build sonarqube")
            param("gradle.opts", "%sonar.opts%")
            param("java.home", "%java8.home%")
        }
    }))
}
