// ──────────────────────────────────────────────────────────────────────────────
// Jenkinsfile — Declarative Pipeline
// Framework: Selenium Java BDD
// Author:    Nishant Jaiswal
// Triggers:  SCM push, nightly schedule, manual (parameterised)
// ──────────────────────────────────────────────────────────────────────────────

pipeline {

    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-17'
            args '--shm-size=2g -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    // ── Parameters ────────────────────────────────────────────────────────
    parameters {
        choice(
            name:        'BROWSER',
            choices:     ['chrome', 'firefox', 'edge'],
            description: 'Target browser for test execution'
        )
        choice(
            name:        'ENVIRONMENT',
            choices:     ['qa', 'staging', 'prod'],
            description: 'Target environment'
        )
        string(
            name:        'TAGS',
            defaultValue:'@smoke',
            description: 'Cucumber tags to run (e.g. @regression, @smoke, @sanity)'
        )
        booleanParam(
            name:        'HEADLESS',
            defaultValue: true,
            description: 'Run in headless mode'
        )
        booleanParam(
            name:        'SEND_EMAIL',
            defaultValue: false,
            description: 'Send email report after execution'
        )
    }

    // ── Environment Variables ─────────────────────────────────────────────
    environment {
        MAVEN_OPTS  = '-Xmx1024m -XX:MaxMetaspaceSize=512m'
        ALLURE_HOME = tool name: 'allure-2.25', type: 'allure'
        TIMESTAMP   = sh(script: "date '+%Y%m%d_%H%M%S'", returnStdout: true).trim()
        BUILD_LABEL = "Build-${env.BUILD_NUMBER}_${env.TIMESTAMP}"
    }

    // ── Triggers ─────────────────────────────────────────────────────────
    triggers {
        // Nightly full regression at 1 AM IST
        cron('0 19 * * *')
        // Poll SCM every 5 minutes
        pollSCM('H/5 * * * *')
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        disableConcurrentBuilds()
        ansiColor('xterm')
        timestamps()
    }

    // ── Stages ───────────────────────────────────────────────────────────
    stages {

        stage('Checkout') {
            steps {
                echo "Checking out branch: ${env.BRANCH_NAME}"
                checkout scm
            }
        }

        stage('Validate & Compile') {
            steps {
                sh 'mvn validate compile -q'
                echo "Compilation successful"
            }
        }

        stage('Setup Test Environment') {
            steps {
                script {
                    // Pull Selenium Grid if remote execution configured
                    if (params.ENVIRONMENT != 'local') {
                        sh 'docker-compose -f docker/docker-compose.yml up -d --scale chrome=3'
                        sh 'sleep 10'
                        echo "Selenium Grid started"
                    }
                }
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    def mvnCmd = """
                        mvn test \
                        -Dbrowser=${params.BROWSER} \
                        -Denv=${params.ENVIRONMENT} \
                        -Dtags="${params.TAGS}" \
                        -Dheadless=${params.HEADLESS} \
                        -Dremote=false \
                        -Dallure.results.directory=target/allure-results \
                        -Dsurefire.failIfNoSpecifiedTests=false \
                        -Dmaven.test.failure.ignore=true
                    """.stripIndent().trim()

                    echo "Executing: ${mvnCmd}"
                    sh mvnCmd
                }
            }
        }

        stage('Generate Allure Report') {
            steps {
                allure([
                    includeProperties: true,
                    jdk: '',
                    properties: [],
                    reportBuildPolicy: 'ALWAYS',
                    results: [[path: 'target/allure-results']]
                ])
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/cucumber-reports/**/*', allowEmptyArchive: true
                archiveArtifacts artifacts: 'target/screenshots/**/*',      allowEmptyArchive: true
                archiveArtifacts artifacts: 'target/logs/**/*',             allowEmptyArchive: true
                junit testResults: 'target/surefire-reports/*.xml',         allowEmptyResults: true
            }
        }

        stage('Send Email Notification') {
            when {
                expression { return params.SEND_EMAIL }
            }
            steps {
                script {
                    def status     = currentBuild.currentResult
                    def statusIcon = status == 'SUCCESS' ? '✅' : '❌'
                    emailext(
                        subject: "${statusIcon} Test Report | ${params.ENVIRONMENT.toUpperCase()} | ${params.TAGS} | ${env.BUILD_LABEL}",
                        body: """
                            <h2>Test Execution Summary</h2>
                            <table border="1" cellpadding="5">
                                <tr><td><b>Status</b></td><td>${status}</td></tr>
                                <tr><td><b>Environment</b></td><td>${params.ENVIRONMENT}</td></tr>
                                <tr><td><b>Browser</b></td><td>${params.BROWSER}</td></tr>
                                <tr><td><b>Tags</b></td><td>${params.TAGS}</td></tr>
                                <tr><td><b>Build</b></td><td>${env.BUILD_URL}</td></tr>
                                <tr><td><b>Allure Report</b></td><td>${env.BUILD_URL}allure/</td></tr>
                            </table>
                        """,
                        to:          '$DEFAULT_RECIPIENTS',
                        mimeType:    'text/html',
                        attachLog:   false,
                        compressLog: true
                    )
                }
            }
        }
    }

    // ── Post Actions ──────────────────────────────────────────────────────
    post {

        always {
            script {
                // Tear down Grid if used
                sh 'docker-compose -f docker/docker-compose.yml down --remove-orphans || true'
            }
            cleanWs(cleanWhenNotBuilt: false, deleteDirs: true, disableDeferredWipeout: true)
        }

        success {
            echo "✅ Build ${env.BUILD_NUMBER} PASSED — ${params.TAGS} on ${params.ENVIRONMENT}"
        }

        failure {
            echo "❌ Build ${env.BUILD_NUMBER} FAILED — Check Allure report: ${env.BUILD_URL}allure/"
        }

        unstable {
            echo "⚠️ Build ${env.BUILD_NUMBER} UNSTABLE — Some tests failed"
        }
    }
}
