pipeline{
    agent{
        label 'nod2_slave'
    }
    tools{
        maven 'maven-39'
        jdk 'openjdk-17slave'
    }
    stages{
        stage("Limpiar"){
           steps{
                cleanWs()
           }
        }
        stage("Descargar Proyecto"){
            steps{
                git credentialsId: 'git_cred',branch: "dev", url:"https://github.com/andresmerida/academic-management.git"
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
    }
}