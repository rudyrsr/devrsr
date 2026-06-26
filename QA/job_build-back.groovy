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
    }
}