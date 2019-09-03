def scmVars

pipeline {
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '5', artifactNumToKeepStr: '5', daysToKeepStr: '5', numToKeepStr: '5')
        durabilityHint 'PERFORMANCE_OPTIMIZED'
        timeout(5)
    }
    libraries {
        lib('jpl-core@master') // https://github.com/joostvdg/jpl-core
        lib('jpl-maven@master') // https://github.com/joostvdg/jpl-maven
    }
    agent {
        kubernetes {
            label 'mypod'
            defaultContainer 'jnlp'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    some-label: some-label-value
spec:
  containers:
  - name: maven
    image: maven:3-jdk-8
    command:
    - cat
    tty: true
    volumeMounts:
      - name: maven-cache
        mountPath: /root/.m2/repository
  volumes:
    - name: maven-cache
      hostPath:
        path: /tmp
        type: Directory
"""
        }
    }
    stages {
        stage('Test versions') {
            steps {
                container('maven') {
                    sh 'uname -a'
                    sh 'mvn -version'
                }
            }
        }
        stage('Checkout') {
            steps {
                script {
                    // use this if used within Multibranch or Org Job
                    scmVars = checkout scm
                    // use this if used within a Pipeline Job
                    // scmVars = git('https://github.com/joostvdg/maven-demo-lib.git')
                }
                echo "scmVars=${scmVars}"
                gitRemoteConfigByUrl(scmVars.GIT_URL, 'githubtoken')
                sh '''
                git config --global user.email "jenkins@jenkins.io"
                git config --global user.name "Jenkins"
                '''
                //sh 'env'
            }
        }
        stage('Build') {
            steps {
                container('maven') {
                    sh 'mvn clean verify -C -e --show-version'
                }
            }
        }
        stage('Version & Analysis') {
            parallel {
                stage('Version Bump') {
                    // disable when {} when used in a Pipelone
                    when { branch 'master' }
                    // requires: https://plugins.jenkins.io/pipeline-utility-steps
                    environment {
                        NEW_VERSION = gitNextSemverTagMaven('pom.xml')
                    }
                    steps {
                        container('maven') {
                            sh 'mvn versions:set -DnewVersion=${NEW_VERSION}'
                        }
                        gitTag("v${NEW_VERSION}")
                    }
                }
                stage('Sonar Analysis') {
                    // disable when {} when used in a Pipelone
                    when {branch 'master'}
                    // environment {
                    //     SONAR_HOST="http://messy-vulture-sonarqube.cicd:9000"
                    //     SONAR_TOKEN=credentials('sonar')
                    // }
                    steps {
                        container('maven') {
                            // sh 'mvn sonar:sonar -Dsonar.host.url=${SONAR_HOST} -Dsonar.login=${SONAR_TOKEN}'
                            withSonarQubeEnv('mysonar') {
                                sh 'mvn -e org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar'
                            }
                        }
                    }
                }
            }
        }
        stage('Publish Artifact') {
            // disable when {} when used in a Pipelone
            when { branch 'master' }
            steps {
                container('maven') {
                    // #1 = credentialsId for artifactory
                    // #2 = distributionManagement.id
                    generateMavenSettings('nexus', 'nexus')
                    sh 'mvn deploy -s jenkins-settings.xml'
                }
            }
            post {
                always {
                    cleanMavenSettings()
                }
            }
        }
    }
}
