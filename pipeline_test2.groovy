def url_repo = "https://github.com/andresmerida/academic-management.git"
pipeline{
    agent {
       label 'slave1'
    }
    tools{
        jdk 'jdk_17'
        maven 'Maven-399'
    }
    parameters{
        string defaultValue: 'dev', description: 'Colocar el branch a ejecutar', name: 'BRANCH', trim: false
    }
    stages{
        stage("Limpiar Workspace"){
            steps{
                cleanWs()
            }
        }
        stage("Colocar nombre de build")
        {
            steps{
                 script{
                    currentBuild.displayName= "service_back-"+ currentBuild.number
                 }
            }
        }
        stage("Descargar Proyecto")
        {
            steps{
                git credentialsId: 'git_credentials',branch: "${params.BRANCH}", url:"${url_repo}"
            }
        }
        stage("Realizar Build")
        {
            steps{
                sh "mvn -v"
                sh "pwd"
                sh "mvn clean compile package"
                sh "pwd"
               
            }
        }
        stage("archivar artefacto"){
            steps{
                sh "mv am-core-web-service/target/am-core-web-service-1.0.0.jar am-core-web-service/target/app.jar"
                stash includes: 'am-core-web-service/target/app.jar', name: 'backartifact'
                archiveArtifacts artifacts: 'am-core-web-service/target/app.jar', onlyIfSuccessful: true
            }
        }
        stage("Test de vulnerabilidades de seguridad"){
            agent { label 'grype_test'}
            steps{
                unstash 'backartifact'
                sh "/grype /home/workspace/APP-DEV/job_test2/am-core-web-service/target/app.jar > Informe-scan.txt"
                 archiveArtifacts artifacts: 'Informe-scan.txt', onlyIfSuccessful: true
            }

        }
    }
}