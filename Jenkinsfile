pipeline {

    agent any

    environment {
        IMAGE_NAME  = "myapp"
        JFROG_IMAGE = "artifactory:8082/docker-local/myapp:${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Maven Test') {
            steps {
                sh '''
                    docker run --rm \
                    -v "$WORKSPACE:/workspace" \
                    -w /workspace \
                    -v maven-cache:/root/.m2 \
                    maven:3.9-eclipse-temurin-17 \
                    mvn clean test
                '''
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
                            docker run --rm \
                            -v "$WORKSPACE:/workspace" \
                            -w /workspace \
                            -v maven-cache:/root/.m2 \
                            -e SONAR_TOKEN \
                            maven:3.9-eclipse-temurin-17 \
                            mvn sonar:sonar \
                            -Dsonar.projectKey=devops-demo \
                            -Dsonar.host.url=http://sonarqube:9000 \
                            -Dsonar.token=$SONAR_TOKEN
                        '''
                    }
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
                    -v trivy-cache:/root/.cache \
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
                        echo "$JFROG_TOKEN" | docker login \
                        artifactory:8082 \
                        -u "$JFROG_USER" \
                        --password-stdin

                        docker tag \
                        ${IMAGE_NAME}:${BUILD_NUMBER} \
                        ${JFROG_IMAGE}

                        docker push ${JFROG_IMAGE}
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
                        echo "$JFROG_TOKEN" | docker login \
                        artifactory:8082 \
                        -u "$JFROG_USER" \
                        --password-stdin

                        docker pull ${JFROG_IMAGE}
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker rm -f myapp 2>/dev/null || true

                    docker run -d \
                    --name myapp \
                    -p 8081:8080 \
                    ${JFROG_IMAGE}
                '''
            }
        }
    }

    post {
        success {
            echo '======================================'
            echo 'PIPELINE COMPLETED SUCCESSFULLY'
            echo '======================================'
            echo 'Application: http://EC2-PUBLIC-IP:8081'
            echo 'Container: myapp'
        }

        failure {
            echo '======================================'
            echo 'PIPELINE FAILED'
            echo '======================================'
        }
    }
}
