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
    }

    val vcs = GitVcsRoot({
        uuid = "ac063d49-90e5-4baf-84b3-7f307586ae0e"
        extId = "GradleTeamcityPlugin"
        name = "gradle-teamcity-plugin"
        url = "https://github.com/rodm/gradle-teamcity-plugin.git"
    })

    vcsRoot(vcs)

    val java7BuildType = BuildType({
        uuid = "b9b0cbf7-1665-4fe5-a24d-956280379ef0"
        extId = "GradleTeamcityPlugin_BuildJava7"
        name = "Build - Java 7"

        vcs {
            root(vcs)
        }

        steps {
            gradle {
                tasks = "%gradle.tasks%"
                useGradleWrapper = true
                gradleWrapperPath = ""
                enableStacktrace = true
                jdkHome = "%java.home%"
            }
        }

        params {
            param("gradle.tasks", "clean build")
            param("java.home", "%java7.home%")
        }

        triggers {
            vcs {
            }
        }
    })
    buildType(java7BuildType)

    val java8BuildType = BuildType({
        uuid = "b9b0cbf7-1665-4fe5-a24d-956280379ef1"
        extId = "GradleTeamcityPlugin_BuildJava8"
        name = "Build - Java 8"

        vcs {
            root(vcs)
        }

        steps {
            gradle {
                tasks = "%gradle.tasks%"
                useGradleWrapper = true
                gradleWrapperPath = ""
                enableStacktrace = true
                jdkHome = "%java.home%"
            }
        }

        triggers {
            vcs {
            }
        }

        params {
            param("gradle.tasks", "clean build")
            param("java.home", "%java8.home%")
        }
    })
    buildType(java8BuildType)

    val functionalTestJava7BuildType = BuildType({
        uuid = "b9b0cbf7-1665-4fe5-a24d-956280379ef2"
        extId = "GradleTeamcityPlugin_FunctionalTestJava7"
        name = "Functional Test - Java 7"

        vcs {
            root(vcs)
        }

        steps {
            gradle {
                tasks = "%gradle.tasks%"
                useGradleWrapper = true
                gradleWrapperPath = ""
                enableStacktrace = true
                jdkHome = "%java.home%"
            }
        }

        triggers {
            vcs {
            }
        }

        params {
            param("gradle.tasks", "clean functionalTest")
            param("java.home", "%java7.home%")
        }
    })
    buildType(functionalTestJava7BuildType)

    val functionalTestJava8BuildType = BuildType({
        uuid = "b9b0cbf7-1665-4fe5-a24d-956280379ef3"
        extId = "GradleTeamcityPlugin_FunctionalTestJava8"
        name = "Functional Test - Java 8"

        vcs {
            root(vcs)
        }

        steps {
            gradle {
                tasks = "%gradle.tasks%"
                useGradleWrapper = true
                gradleWrapperPath = ""
                enableStacktrace = true
                jdkHome = "%java.home%"
            }
        }

        triggers {
            vcs {
            }
        }

        params {
            param("gradle.tasks", "clean functionalTest")
            param("java.home", "%java8.home%")
        }
    })
    buildType(functionalTestJava8BuildType)
}
