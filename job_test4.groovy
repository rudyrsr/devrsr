def url_repo = "https://github.com/andresmerida/academic-management.git"
def low_vp = ""
def high_vp = ""
def medium_vp = ""
def critical_vp = ""
pipeline{
    agent
    {
      label 'slave1'        
    }
    tools{
        jdk 'Java17_slave1'
        maven 'Maven-399'
    }
    parameters{
        string defaultValue: 'dev', description: 'Colocar el branch a ejecutar', name: 'BRANCH', trim: false
        choice (name: 'SCAN_GRYPE', choices: ['NO','YES'],description: 'Activar si desea escanear con grype')
        choice (name: 'SCAN_SONARQ', choices: ['YES','NO'],description: 'Activar si desea escanear con Sonar Qube')
    }
    stages{
        stage("Limpiar Workspace")
        {
            steps{
                cleanWs()
            }
        }
        stage("Colocar nombre de build"){
            steps{
                  script{
                     currentBuild.displayName= "service_back-"+ currentBuild.number
                  }
            }
        }
        stage("Descargar Proyecto")
        {
            steps{
                git credentialsId: 'git-hub_cred',branch: "${params.BRANCH}", url: "${url_repo}"
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
            when {equals expected: 'YES', actual: SCAN_GRYPE}
            agent { label 'grype_test'}
            steps{
                script {
                  unstash 'backartifact'
                  sh "/grype /home/workspace/DEV/APP-DEV/job_test4/am-core-web-service/target/app.jar > Informe-scan.txt"
                  archiveArtifacts artifacts: 'Informe-scan.txt', onlyIfSuccessful: true
                  low_vp = sh(returnStdout: true, script: "cat Informe-scan.txt | grep 'Low' | wc -l").trim()
                  medium_vp = sh(returnStdout: true, script: "cat Informe-scan.txt | grep 'Medium' | wc -l").trim()
                  high_vp = sh(returnStdout: true, script: "cat Informe-scan.txt | grep 'High' | wc -l").trim()
                  critical_vp = sh(returnStdout: true, script: "cat Informe-scan.txt | grep 'Critical' | wc -l").trim()
                  sh "echo 'vulnerabilidades: low_vp->${low_vp}, medium_vp->${medium_vp}, high_vp->${high_vp}, critical_vp->${critical_vp}'"
                } 
            }
        }
    }
}