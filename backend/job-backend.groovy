pipeline{
    agent{ label 'agent_deploy'}
    tools{
        jdk 'java_21'
        maven 'maven-399'
    }
    stages{
        stage("Limpiar Espacio de trabajo"){
            steps{
                cleanWs()
            }
        }
        stage("Descargar Proyecto"){
            steps{
                 git credentialsId: 'github-secret', branch: "develop", url:"https://github.com/rudyrsr/academy-back.git"
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