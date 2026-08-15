pipeline {
    agent any

    environment {
        SONAR_PROJECT_KEY = 'my-app'
    }

    stages {

        stage('SonarQube Analysis') {
            steps {
                echo '===== SonarQube Code Analysis ====='

                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                          -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                          -Dsonar.projectName=${SONAR_PROJECT_KEY}
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo '===== Checking SonarQube Quality Gate ====='

                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
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

        stage('Trivy Scan') {
            steps {
                echo '===== Trivy Filesystem Vulnerability Scan ====='

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
            echo '''
            ==========================================
              PIPELINE COMPLETED SUCCESSFULLY
            ==========================================
            '''
        }

        failure {
            echo '''
            ==========================================
              PIPELINE FAILED
            ==========================================
            '''
        }

        always {
            echo "Build Number: ${BUILD_NUMBER}"
        }
    }
}
