pipeline {

    agent any

    environment {
        IMAGE_NAME = "devops-demo"
        JFROG_URL = "http://artifactory:8081"
        JFROG_REPO = "docker-local"
        JFROG_IMAGE = "${JFROG_URL}/${JFROG_REPO}/${IMAGE_NAME}:${BUILD_NUMBER}"
    }

    stages {

        stage('Maven Build & Test') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                          -Dsonar.projectKey=devops-demo
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                      -t ${IMAGE_NAME}:${BUILD_NUMBER} .
                '''
            }
        }

        stage('Trivy Scan') {
            steps {
                sh '''
                    docker run --rm \
                      -v /var/run/docker.sock:/var/run/docker.sock \
                      -v trivy-cache:/root/.cache/ \
                      aquasec/trivy:latest \
                      image \
                      --severity HIGH,CRITICAL \
                      --exit-code 1 \
                      ${IMAGE_NAME}:${BUILD_NUMBER}
                '''
            }
        }

        stage('Push to JFrog') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'jfrog-credentials',
                        usernameVariable: 'JFROG_USER',
                        passwordVariable: 'JFROG_TOKEN'
                    )
                ]) {
                    sh '''
                        echo "$JFROG_TOKEN" | docker login ${JFROG_URL} \
                          -u "$JFROG_USER" \
                          --password-stdin

                        docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${JFROG_IMAGE}

                        docker push ${JFROG_IMAGE}

                        docker logout ${JFROG_URL}
                    '''
                }
            }
        }

        stage('Pull from JFrog') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'jfrog-credentials',
                        usernameVariable: 'JFROG_USER',
                        passwordVariable: 'JFROG_TOKEN'
                    )
                ]) {
                    sh '''
                        echo "$JFROG_TOKEN" | docker login ${JFROG_URL} \
                          -u "$JFROG_USER" \
                          --password-stdin

                        docker pull ${JFROG_IMAGE}

                        docker logout ${JFROG_URL}
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker rm -f devops-demo 2>/dev/null || true

                    docker run -d \
                      --name devops-demo \
                      --restart unless-stopped \
                      -p 8088:8080 \
                      ${JFROG_IMAGE}
                '''
            }
        }
    }

    post {
        success {
            echo '======================================'
            echo 'PIPELINE SUCCESSFUL'
            echo 'Application: http://<EC2-PUBLIC-IP>:8088'
            echo '======================================'
        }

        failure {
            echo '======================================'
            echo 'PIPELINE FAILED'
            echo '======================================'
        }
    }
}
