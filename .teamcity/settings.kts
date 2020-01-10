
import jetbrains.buildServer.configs.kotlin.v2019_2.version
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger.QuietPeriodMode.USE_DEFAULT

import com.github.rodm.teamcity.pipeline
import com.github.rodm.teamcity.gradle.switchGradleBuildStep
import com.github.rodm.teamcity.project.githubIssueTracker

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

version = "2019.2"

project {
    description = "Gradle plugin for developing TeamCity plugins [master]"

    val settingsVcs = GitVcsRoot {
        id("TeamcitySettings")
        name = "teamcity-settings"
        url = "https://github.com/rodm/teamcity-settings"
    }
    vcsRoot(settingsVcs)

    features {
        githubIssueTracker {
            displayName = "GradleTeamCityPlugin"
            repository = "https://github.com/rodm/gradle-teamcity-plugin"
            pattern = """#(\d+)"""
        }
    }

    params {
        param("teamcity.ui.settings.readOnly", "true")
    }

    val vcs = GitVcsRoot {
        id("GradleTeamcityPlugin")
        name = "gradle-teamcity-plugin"
        url = "https://github.com/rodm/gradle-teamcity-plugin.git"
    }
    vcsRoot(vcs)

    val buildTemplate = template {
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

        failureConditions {
            executionTimeoutMin = 10
        }

        features {
            feature {
                id = "perfmon"
                type = "perfmon"
            }
        }

        params {
            param("gradle.tasks", "clean build")
            param("gradle.opts", "")
            param("java.home", "%java8.home%")
        }
    }

    pipeline {
        stage ("Build") {
            build {
                id("BuildJava8")
                name = "Build - Java 8"
                templates(buildTemplate)
            }

            build {
                id("BuildJava11")
                name = "Build - Java 11"
                templates(buildTemplate)

                params {
                    param("java.home", "%java11.home%")
                }
            }

            build {
                id("ReportCodeQuality")
                name = "Report - Code Quality"
                templates(buildTemplate)
                params {
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
            }
        }
        stage ("Functional tests") {
            defaults {
                failureConditions {
                    executionTimeoutMin = 20
                }
            }

            build {
                id("FunctionalTestJava8")
                name = "Functional Test - Java 8"
                templates(buildTemplate)
                params {
                    param("gradle.tasks", "clean functionalTest")
                }
            }

            build {
                id("FunctionalTestJava9")
                name = "Functional Test - Java 9"
                templates(buildTemplate)
                params {
                    param("gradle.tasks", "clean functionalTest")
                    param("java.home", "%java9.home%")
                }
            }

            build {
                id("FunctionalTestJava10")
                name = "Functional Test - Java 10"
                templates(buildTemplate)
                params {
                    param("gradle.tasks", "clean functionalTest")
                    param("java.home", "%java10.home%")
                }
            }

            build {
                id("FunctionalTestJava11")
                name = "Functional Test - Java 11"
                templates(buildTemplate)
                params {
                    param("gradle.tasks", "clean functionalTest")
                    param("java.home", "%java11.home%")
                }
            }

            build {
                id("FunctionalTestJava12")
                name = "Functional Test - Java 12"
                templates(buildTemplate)
                params {
                    param("gradle.tasks", "clean functionalTest")
                    param("gradle.version", "5.4")
                    param("java.home", "%java12.home%")
                }
                steps {
                    switchGradleBuildStep()
                    stepsOrder = arrayListOf("SWITCH_GRADLE", "GRADLE_BUILD")
                }
            }

            build {
                id("FunctionalTestJava13")
                name = "Functional Test - Java 13"
                templates(buildTemplate)
                params {
                    param("gradle.tasks", "clean functionalTest")
                    param("gradle.version", "6.0")
                    param("java.home", "%java13.home%")
                }
                steps {
                    switchGradleBuildStep()
                    stepsOrder = arrayListOf("SWITCH_GRADLE", "GRADLE_BUILD")
                }
            }

            build {
                id("SamplesTestJava8")
                name = "Samples Test - Java 8"
                templates(buildTemplate)
                params{
                    param("gradle.tasks", "clean samplesTest")
                }
            }
        }

        stage ("Publish") {
            build {
                id("DummyPublish")
                name = "Publish to repository"
                templates(buildTemplate)

                params {
                    param("gradle.tasks", "clean build publishPluginPublicationToMavenLocal")
                }

                triggers {
                    vcs {
                        quietPeriodMode = USE_DEFAULT
                        branchFilter = ""
//                        triggerRules = "-:.teamcity/**"
                    }
                }
            }
        }
    }
}
