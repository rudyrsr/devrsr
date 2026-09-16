
def url_repo= "https://github.com/andresmerida/academic-management.git" 
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
                git credentialsId: 'git-secret', branch: "dev", url:"${url_repo}"
        
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