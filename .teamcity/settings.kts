
import jetbrains.buildServer.configs.kotlin.v2018_2.version
import jetbrains.buildServer.configs.kotlin.v2018_2.project
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2018_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2018_2.Template
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.VcsTrigger.QuietPeriodMode.USE_DEFAULT

import com.github.rodm.teamcity.pipeline

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

version = "2019.1"

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
        id("Build")
        name = "build"

        vcs {
            root(vcs)
        }

        steps {
            gradle {
                id = "GRADLE_BUILD"
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
                quietPeriodMode = USE_DEFAULT
                triggerRules = """
                    +:root=${DslContext.projectId.absoluteId}_TeamcitySettings;:**
                    +:root=${DslContext.projectId.absoluteId}_GradleTeamcityPlugin:**
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
            param("java.home", "%java8.home%")
        }
    })
    template(buildTemplate)

    pipeline {
        stage ("Build") {
            build ({
                id("BuildJava8")
                name = "Build - Java 8"
                templates(buildTemplate)
                disableSettings("perfmon", "BUILD_EXT_2")
            })

            build ({
                id("ReportCodeQuality")
                name = "Report - Code Quality"
                templates(buildTemplate)
                params{
                    param("gradle.tasks", "clean build sonarqube")
                    param("gradle.opts", "%sonar.opts%")
                }

                features {
                    feature {
                        id = "gradle-init-scripts"
                        type = "gradle-init-scripts"
                        param("initScriptName", "sonarqube.gradle")
                    }
                }
            })
        }
        stage ("Functional tests") {
            defaults {
                failureConditions {
                    executionTimeoutMin = 20
                }
            }

            build ({
                id("FunctionalTestJava8")
                name = "Functional Test - Java 8"
                templates(buildTemplate)
                params {
                    param("gradle.tasks", "clean functionalTest")
                }
            })

            build ({
                id("FunctionalTestJava9")
                name = "Functional Test - Java 9"
                templates(buildTemplate)
                params {
                    param("gradle.tasks", "clean functionalTest")
                    param("java.home", "%java9.home%")
                }
            })

            build ({
                id("FunctionalTestJava10")
                name = "Functional Test - Java 10"
                templates(buildTemplate)
                params {
                    param("gradle.tasks", "clean functionalTest")
                    param("java.home", "%java10.home%")
                }
            })

            build ({
                id("SamplesTestJava8")
                name = "Samples Test - Java 8"
                templates(buildTemplate)
                params{
                    param("gradle.tasks", "clean samplesTest")
                }
            })
        }

        stage ("Publish") {
            build ({
                id("DummyPublish")
                name = "Publish to plugin repository"
            })
        }
    }
}
