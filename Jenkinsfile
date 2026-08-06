pipeline {
    agent none

    environment {
        ARTIFACTORY_URL = "http://http://13.220.119.42:8081//artifactory/libs-release-local"
        WAR_NAME = "sample.war"
    }

    stages {

        stage('Checkout') {
            agent { label 'Agent-A' }

            steps {
                git branch: 'main',
                    url: 'https://github.com/huzefa-2/zubair-1-asgnmt.git'
            }
        }

        stage('Test') {
            agent { label 'Agent-A' }

            steps {
                sh 'mvn test'
            }
        }

        stage('Build WAR') {
            agent { label 'Agent-A' }

            steps {
                sh 'mvn clean package'
            }
        }

        stage('Upload WAR to Artifactory') {
            agent { label 'Agent-A' }

            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'artifactory-creds',
                    usernameVariable: 'USER',
                    passwordVariable: 'PASS'
                )]) {

                    sh '''
                    WAR=$(ls target/*.war | head -1)

                    curl -u $USER:$PASS \
                    -T $WAR \
                    ${ARTIFACTORY_URL}/${WAR_NAME}
                    '''
                }
            }
        }

        stage('Download WAR') {
            agent { label 'Agent-B' }

            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'artifactory-creds',
                    usernameVariable: 'USER',
                    passwordVariable: 'PASS'
                )]) {

                    sh '''
                    curl -u $USER:$PASS \
                    -o sample.war \
                    ${ARTIFACTORY_URL}/${WAR_NAME}
                    '''
                }
            }
        }

        stage('Deploy to Tomcat') {
            agent { label 'Agent-B' }

            steps {
                sh '''
                cp sample.war /opt/tomcat/webapps/
                '''
            }
        }
    }
}
