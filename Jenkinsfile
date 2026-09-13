pipeline {

    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Maven Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([
                        string(
                            credentialsId: 'sonar-token',
                            variable: 'SONAR_TOKEN'
                        )
                    ]) {
                        sh '''
                            mvn sonar:sonar \
                            -Dsonar.projectKey=devops-demo \
                            -Dsonar.token=$SONAR_TOKEN
                        '''
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t devops-demo:${BUILD_NUMBER} .'
            }
        }

        stage('Trivy Scan') {
            steps {
                sh '''
                    docker run --rm \
                    -v /var/run/docker.sock:/var/run/docker.sock \
                    -v trivy-cache:/root/.cache \
                    aquasec/trivy:latest \
                    image \
                    --severity HIGH,CRITICAL \
                    devops-demo:${BUILD_NUMBER}
                '''
            }
        }

        stage('Push to JFrog') {
            steps {
                echo 'Push Docker image to JFrog here'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploy Docker container here'
            }
        }
    }
}
