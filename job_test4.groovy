pipeline{
    agent
    {
      label 'slave1'        
    }
    tools{
        jdk 'Java17_slave1'
        maven 'Maven-399'
    }
    stages{
        stage("Limpiar Workspace")
        {
            steps{
                cleanWs()
            }
        }
        stage("Descargar Proyecto")
        {
            steps{
                git credentialsId: 'git-hub_cred',branch: "dev", url: "https://github.com/andresmerida/academic-management.git"
            }
        }
        stage("Realizar Build")
        {
            steps{
                sh "mvn -v"
                sh "pwd"
                sh "mvn clean compile package"
            }
        }
        stage("archivar artefacto")
        {
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
                sh "/grype /home/workspace/DEV/APP-DEV/job_test4/am-core-web-service/target/app.jar > Informe-scan.txt"
                 archiveArtifacts artifacts: 'Informe-scan.txt', onlyIfSuccessful: true
            }
        }
    }
}