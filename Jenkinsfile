pipeline {

    agent any

    environment {

        IMAGE_NAME = "devops-demo"

        // JFrog Docker registry URL from Set Me Up
        JFROG_URL = "98.87.166.51:8082"

        // JFrog Docker local repository
        JFROG_REPO = "docker-local"

        // Final image name in JFrog
        JFROG_IMAGE = "${JFROG_URL}/${JFROG_REPO}/${IMAGE_NAME}:${BUILD_NUMBER}"
    }

    stages {

        stage('Maven Build & Test') {
            steps {
                sh '''
                    mvn clean package -U
                '''
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

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
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
                        --scanners vuln \
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

                        docker tag \
                            ${IMAGE_NAME}:${BUILD_NUMBER} \
                            ${JFROG_IMAGE}

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
            echo '''
==========================================
        PIPELINE SUCCESSFUL
==========================================

Application:
http://<EC2-PUBLIC-IP>:8088

JFrog Image:
98.87.166.51:8082/docker-local/devops-demo:${BUILD_NUMBER}

==========================================
'''
        }

        failure {
            echo '''
==========================================
        PIPELINE FAILED
==========================================

Check the failed stage above.

==========================================
'''
        }
    }
}
