def url_repo = "https://github.com/andresmerida/academic-management.git"
pipeline{
    agent{
        label 'nod2_slave'
    }
    tools{
        maven 'maven-39'
        jdk 'openjdk-17slave'
    }
    parameters{
        string defaultValue: 'dev', description: 'Colocar un branch a deployar', name:'BRANCH', trim: false
        choice (name: 'SCAN_GRYPE',  choices: ['YES','NO'], description: 'Activar si desea escanear con grype')

    }
    stages{
        stage("Limpiar"){
           steps{
                cleanWs()
           }
        }
        stage("Descargar Proyecto"){
            steps{
                git credentialsId: 'git_cred',branch: "dev", url:"${url_repo}"
            }
        }
        stage("Realizar Build"){
           steps{
               sh "mvn -v"
               sh "pwd"
               sh "mvn clean compile package"
               sh "mv am-core-web-service/target/am-core-web-service-1.0.0.jar am-core-web-service/target/app.jar"
               stash includes: 'am-core-web-service/target/app.jar', name: 'backartifact'
               archiveArtifacts artifacts: 'am-core-web-service/target/app.jar', onlyIfSuccessful: true
               sh "cp am-core-web-service/target/app.jar /tmp"
           }
        }
        stage("Test seguridad grype"){
            steps{
                sh "/grype /tmp/app.jar > Informe-scan.txt"
                archiveArtifacts artifacts: 'Informe-scan.txt', onlyIfSuccessful:true
            }
        }
    }
}