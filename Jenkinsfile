pipeline {
    agent any

    environment {
        SONAR_PROJECT_KEY = 'my-app'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Maven Build') {
            steps {
                echo '===== Maven Build ====='

                sh '''
                    mvn clean package -DskipTests
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo '===== SonarQube Analysis ====='

                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                          -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                          -Dsonar.projectName=${SONAR_PROJECT_KEY}
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo '===== SonarQube Quality Gate ====='

                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Trivy Filesystem Scan') {
            steps {
                echo '===== Trivy Filesystem Scan ====='

                sh '''
                    trivy \
                      --config /dev/null \
                      --scanners vuln \
                      --severity HIGH,CRITICAL \
                      fs .
                '''
            }
        }
    }

    post {
        success {
            echo '======================================'
            echo 'PIPELINE COMPLETED SUCCESSFULLY'
            echo '======================================'
        }

        failure {
            echo '======================================'
            echo 'PIPELINE FAILED'
            echo 'Check the Console Output'
            echo '======================================'
        }

        always {
            echo "Build Number: ${BUILD_NUMBER}"
        }
    }
}
