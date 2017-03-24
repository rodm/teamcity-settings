package GradleTeamCityPlugin

import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v10.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.versionedSettings
import jetbrains.buildServer.configs.kotlin.v10.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v10.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v10.BuildType

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

version = "10.0"
project {
    uuid = "2c4c777e-bf5d-4eaf-8e46-eea999fdbd89"
    extId = "GradleTeamCityPlugin"
    parentId = "_Root"
    name = "Gradle TeamCity Plugin"
    description = "Gradle plugin for developing TeamCity plugins"

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
        extId = "GradleTeamcityPlugin"
        name = "gradle-teamcity-plugin"
        url = "https://github.com/rodm/gradle-teamcity-plugin.git"
    })

    vcsRoot(vcs)

    val java7BuildType = createBuildType("Build - Java 7", "GradleTeamcityPlugin_BuildJava7", "b9b0cbf7-1665-4fe5-a24d-956280379ef0")
    configureBuildType(java7BuildType, vcs, "clean build", "%java7.home%")
    buildType(java7BuildType)

    val java8BuildType = createBuildType("Build - Java 8", "GradleTeamcityPlugin_BuildJava8", "b9b0cbf7-1665-4fe5-a24d-956280379ef1")
    configureBuildType(java8BuildType, vcs, "clean build", "%java8.home%")
    buildType(java8BuildType)

    val functionalTestJava7BuildType = createBuildType("Functional Test - Java 7", "GradleTeamcityPlugin_FunctionalTestJava7", "b9b0cbf7-1665-4fe5-a24d-956280379ef2")
    configureBuildType(functionalTestJava7BuildType, vcs, "clean functionalTest", "%java7.home%", 20)
    buildType(functionalTestJava7BuildType)

    val functionalTestJava8BuildType = createBuildType("Functional Test - Java 8", "GradleTeamcityPlugin_FunctionalTestJava8", "b9b0cbf7-1665-4fe5-a24d-956280379ef3")
    configureBuildType(functionalTestJava8BuildType, vcs, "clean functionalTest", "%java8.home%", 20)
    buildType(functionalTestJava8BuildType)

    val samplesBuildType = createBuildType("Samples Test - Java 7", "GradleTeamcityPlugin_SamplesTestJava7", "b9b0cbf7-1665-4fe5-a24d-956280379ef4")
    configureBuildType(samplesBuildType, vcs, "clean samplesTest", "%java7.home%")
    buildType(samplesBuildType)

    val sonarBuildType = createBuildType("Report - Code Quality", "GradleTeamcityPlugin_ReportCodeQuality", "b9b0cbf7-1665-4fe5-a24d-956280379ef5")
    configureBuildType(sonarBuildType, vcs, "clean build sonarqube", "%java8.home%")
    sonarBuildType.params {
        param("gradle.opts", "%sonar.opts%")
    }
    buildType(sonarBuildType)
}

fun createBuildType(name: String, extId: String, uuid: String): BuildType {
    return BuildType({
        this.uuid = uuid
        this.extId = extId
        this.name = name
    })
}

fun configureBuildType(buildType: BuildType, vcs: GitVcsRoot, gradleTasks: String, javaHome: String, timeout: Int = 10) {
    buildType.apply {
        vcs {
            root(vcs)
        }

        steps {
            gradle {
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
                triggerRules = "+:root=Settings_root_id;:*"
            }
        }

        failureConditions {
            executionTimeoutMin = timeout
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
            param("gradle.tasks", gradleTasks)
            param("gradle.opts", "")
            param("java.home", javaHome)
        }
    }
}
