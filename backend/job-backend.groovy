def url_repo= "https://github.com/rudyrsr/academy-back.git"
pipeline{
    agent{ label 'agent_deploy'}
    tools{
        jdk 'java_21'
        maven 'maven-399'
    }
    parameters{
           string defaultValue: 'develop',description: 'Colocar el branch a ejecutar', name: 'BRANCH', trim: 'false'
    }
    stages{
        stage("Limpiar Espacio de trabajo"){
            steps{
                cleanWs()
            }
        }
        stage("Descargar Proyecto"){
            steps{
                 git credentialsId: 'github-secret', branch: "${params.BRANCH}", url:"${url_repo}"
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