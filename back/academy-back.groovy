
def url_repo ="https://git.digitalharborbolivia.com:8081/rsalvatierra.teacher/academy.git"
def low_back =""
def medium_back =""
def critical_back =""
def high_back ="" 
pipeline{
    agent{
        //label 'built-in'
        label 'agent1'
    }
    tools{
        jdk 'java_21m'
        maven 'maven-399'
    }
    parameters{
           string defaultValue: 'devrsr',description: 'Colocar el branch a ejecutar',name: 'BRANCH', trim: 'false'
           choice (name: 'SCAN_GRYPE', choices: ['YES','NO'],description: 'Seleccione YES si desea escanear vulnerabilidades de seguridad') 
           choice (name: 'SCAN_SONARQ', choices: ['NO','YES'],description: 'Seleccione YES si desea escanear codigo con Sonarqube')
    }
    stages{
        stage("limpiar espacio de trabajo"){
            steps{
                cleanWs()
            }
        }
        stage("Descargar Proyecto"){
            steps{
                git credentialsId: 'gitlab_secret', branch: "${params.BRANCH}", url:"${url_repo}"
        
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
        stage("Test de vulnerabilidades con grype"){
          when {equals expected: 'YES', actual: SCAN_GRYPE}
          agent{ label 'agent_grype' }
          steps{
               script{
               unstash 'backartifact'
               sh "/grype /home/workspace/DEV/BACKEND/Job_academy-back/am-core-web-service/target/app.jar > Informe-scan-back.txt"
               stash includes: 'Informe-scan-back.txt', name: 'backreports'
               archiveArtifacts artifacts: 'Informe-scan-back.txt', onlyIfSuccessful: true
               low_back = sh(returnStdout: true, script: "cat Informe-scan-back.txt | grep 'Low' | wc -l").trim()
               medium_back = sh(returnStdout: true, script: "cat Informe-scan-back.txt | grep 'Medium' | wc -l").trim()
               high_back = sh(returnStdout: true, script: "cat Informe-scan-back.txt | grep 'High' | wc -l").trim()
               critical_back = sh(returnStdout: true, script: "cat Informe-scan-back.txt | grep 'Critical' | wc -l").trim()
               sh "echo 'vulnerabilities: low_back->${low_back}, medium_back->${medium_back}, high_back->${high_back}, critical_back->${critical_back}'"
               }
          }  

        }
        stage("Test con SonarQube"){
            when {equals expected: 'YES', actual: SCAN_SONARQ}
            steps{
                dir ('back'){
                    script{
                        sh "pwd"
                        writeFile encoding: 'UTF-8', file: 'sonar-project.properties', text: """sonar.projectKey=academy-back
								sonar.projectName=academy-back
								sonar.projectVersion=1.0.0
								sonar.sourceEncoding=UTF-8
								sonar.sources=am-core-web-service/src/main/java
								sonar.java.binaries=am-core-web-service/target/classes
								sonar.language=java
								sonar.scm.provider=git
                                """
                        withSonarQubeEnv('Sonar_CI') {                                                    
                             sh "${tool('Sonar_CI')}/bin/sonar-scanner -X"
                        }
                    }
                }
            }
        }
   }  
}