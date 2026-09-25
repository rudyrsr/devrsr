def url_repo= "https://github.com/rudyrsr/academy-back.git"
def low_back =""
def medium_back =""
def critical_back =""
def high_back ="" 
pipeline{
    agent{ label 'agent_deploy'}
    tools{
        jdk 'java_21'
        maven 'maven-399'
    }
    parameters{
           string defaultValue: 'develop',description: 'Colocar el branch a ejecutar', name: 'BRANCH', trim: 'false'
           choice(name: 'SCAN_GRYPE', choices: ['YES','NO'], description: 'Seleccione YES si desea escanear con Grype')
           choice(name: 'SCAN_SONARQ', choices: ['NO','YES'], description: 'Seleccione YES si desea escanear con SONAR')
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
        stage("Test con Grype"){
           when {equals expected: 'YES', actual: SCAN_GRYPE}
           agent{ label 'agent_grype'}
           steps{
                script{
                    unstash 'backartifact' 
                    sh "/grype am-core-web-service/target/app.jar > Informe-scan-back.txt"
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
    }
}