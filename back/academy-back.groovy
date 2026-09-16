
def url_repo= "https://git.digitalharborbolivia.com:8081/rsalvatierra.teacher/academy.git" 
pipeline{
    agent{
        //label 'built-in'
        label 'agent1'
    }
    tools{
        jdk 'java_21m'
        maven 'maven-399'
    }
    stages{
        stage("limpiar espacio de trabajo"){
            steps{
                cleanWs()
            }
        }
        stage("Descargar Proyecto"){
            steps{
                git credentialsId: 'gitlab_secret', branch: "devrsr", url:"${url_repo}"
        
            }    
        }
        stage("Realizar build"){
            steps{
                dir('back'){
                sh "mvn -v"
                sh "pwd"
                sh "mvn clean compile package"
                }
            }
        }
        stage("Archivar artefacto"){
            steps{
                dir('back'){
                sh "mv am-core-web-service/target/am-core-web-service-1.0.0.jar am-core-web-service/target/app.jar"
                stash includes: 'am-core-web-service/target/app.jar', name: 'backartifact'
                archiveArtifacts artifacts: 'am-core-web-service/target/app.jar', onlyIfSuccessful: true
                }
            }
        }
    }
}