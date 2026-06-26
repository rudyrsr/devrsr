pipeline{
    agent{
        label 'slave1'
    }
    tools{
        jdk 'java21_master'
        maven 'maven-399'
    }
    stages{
        stage("Limpiar Espacio de Trabajo"){
            steps{
                 cleanWs()
            }
        }
        stage("Descargar Proyecto"){
            steps{
                git credentialsId: 'git_secret', branch: 'dev', url: "https://github.com/andresmerida/academic-management.git"
            }
        }
        stage("Realizar build"){
            steps{
                sh "mvn -v"
                sh "pwd"
                sh "mvn clean compile package"
            }
        }
        stage("Archivar artefacto")
        {
            steps{
                sh "mv am-core-web-service/target/am-core-web-service-1.0.0.jar am-core-web-service/target/app.jar"
                stash includes: 'am-core-web-service/target/app.jar', name:'backartifact'
                archiveArtifacts artifacts: 'am-core-web-service/target/app.jar', onlyIfSuccessful: true
            }
        }
        stage("Test de vulnerabilidades de seguridad"){
          agent { label 'grype_test'}
          steps{
              unstash 'backartifact'
              sh "/grype /home/workspace/QA/job_build-back/am-core-web-service/target/app.jar > Informe-scan.txt"
              archiveArtifacts artifacts: 'Informe-scan.txt', onlyIfSuccessful: true
          }   
        }
    }
}