
import jetbrains.buildServer.configs.kotlin.v2018_1.version
import jetbrains.buildServer.configs.kotlin.v2018_1.project
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_1.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2018_1.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_1.Template
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.script

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

version = "2018.1"
project {
    description = "Gradle plugin for developing TeamCity plugins [master]"

    val settingsVcs = GitVcsRoot({
        id("TeamcitySettings")
        name = "teamcity-settings"
        url = "https://github.com/rodm/teamcity-settings"
    })
    vcsRoot(settingsVcs)

    features {
//        versionedSettings {
//            id = "PROJECT_EXT_1"
//            mode = VersionedSettings.Mode.ENABLED
//            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
//            rootExtId = "TeamcitySettings"
//            showChanges = true
//            settingsFormat = VersionedSettings.Format.KOTLIN
//        }
        feature {
            id = "PROJECT_EXT_2"
            type = "JetBrains.SharedResources"
            param("name", "BuildLimit")
            param("type", "quoted")
            param("quota", "2")
        }
    }

    val vcs = GitVcsRoot({
        id("GradleTeamcityPlugin")
        name = "gradle-teamcity-plugin"
        url = "https://github.com/rodm/gradle-teamcity-plugin.git"
    })
    vcsRoot(vcs)

    val buildTemplate = Template({
        id("GradleTeamCityPlugin_Build")
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
        id("GradleTeamcityPlugin_BuildJava7")
        name = "Build - Java 7"
        templates(buildTemplate)
    }))

    buildType(BuildType({
        id("GradleTeamcityPlugin_BuildJava8")
        name = "Build - Java 8"
        templates(buildTemplate)
        params{
            param("java.home", "%java8.home%")
        }
        disableSettings("perfmon", "BUILD_EXT_2")
    }))

    buildType(BuildType({
        id("GradleTeamcityPlugin_FunctionalTestJava7")
        name = "Functional Test - Java 7"
        templates(buildTemplate)
        failureConditions {
            executionTimeoutMin = 20
        }
        params{
            param("gradle.tasks", "clean functionalTest")
        }
    }))

    buildType(BuildType({
        id("GradleTeamcityPlugin_FunctionalTestJava8")
        name = "Functional Test - Java 8"
        templates(buildTemplate)
        failureConditions {
            executionTimeoutMin = 20
        }
        params{
            param("gradle.tasks", "clean functionalTest")
            param("java.home", "%java8.home%")
        }
    }))

    buildType(BuildType({
        id("GradleTeamCityPlugin_FunctionalTestJava9")
        name = "Functional Test - Java 9"
        templates(buildTemplate)
        failureConditions {
            executionTimeoutMin = 20
        }
        params{
            param("gradle.tasks", "clean functionalTest")
            param("gradle.version", "4.3")
            param("java.home", "%java9.home%")
        }
        steps {
            script {
                id = "RUNNER_2"
                scriptContent = """
                #!/bin/sh
                JAVA_HOME=%java8.home% ./gradlew wrapper --gradle-version=%gradle.version%
                JAVA_HOME=%java.home% ./gradlew --version
                """.trimIndent()
            }
            stepsOrder = arrayListOf("RUNNER_2", "RUNNER_1")
        }
    }))

    buildType(BuildType({
        id("GradleTeamcityPlugin_SamplesTestJava7")
        name = "Samples Test - Java 7"
        templates(buildTemplate)
        params{
            param("gradle.tasks", "clean samplesTest")
        }
    }))

    buildType(BuildType({
        id("GradleTeamcityPlugin_ReportCodeQuality")
        name = "Report - Code Quality"
        templates(buildTemplate)
        params{
            param("gradle.tasks", "clean build sonarqube")
            param("gradle.opts", "%sonar.opts%")
            param("java.home", "%java8.home%")
        }
    }))
}
