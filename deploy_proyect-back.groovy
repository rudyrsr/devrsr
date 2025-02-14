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
           }
        }
    }
}