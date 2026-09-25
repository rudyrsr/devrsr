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
        stage("Archivar artefacto"){
            steps{ 
                sh "mv am-core-web-service/target/am-core-web-service-1.0.0.jar am-core-web-service/target/app.jar"
                stash includes: 'am-core-web-service/target/app.jar', name: 'backartifact'
                archiveArtifacts artifacts: 'am-core-web-service/target/app.jar', onlyIfSuccessful: true
            }
        }
    }
}