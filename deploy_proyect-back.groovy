def url_repo = "https://github.com/andresmerida/academic-management.git"
pipeline{
    agent{
        label 'nod2_slave'
    }
    tools{
        maven 'maven-39'
        jdk 'openjdk-17slave'
    }
    parameters{
        string defaultValue: 'dev', description: 'Colocar un branch a deployar', name:'BRANCH', trim: false
        choice (name: 'SCAN_GRYPE',  choices: ['YES','NO'], description: 'Activar si desea escanear con grype')

    }
    stages{
        stage("Colocar nombre de build")
        {
            steps{
                script
                {
                    currentBuild.displayName= "service_back-"+ currentBuild.number
                }
            }
        }
        stage("Limpiar"){
           steps{
                cleanWs()
           }
        }
        stage("Descargar Proyecto"){
            steps{
                git credentialsId: 'git_cred',branch: "${params.BRANCH}", url:"${url_repo}"
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
        stage("Test seguridad grype"){
            when { equals expected: 'YES', actual: SCAN_GRYPE}
            steps{
                script{
                  sh "/grype /tmp/app.jar > Informe-scan.txt"
                  archiveArtifacts artifacts: 'Informe-scan.txt', onlyIfSuccessful:true
                
                }
            }
        }
        stage("Test con SonarQube"){
            steps{
                script{
                    sh "pwd"
                    writeFile encoding: 'UTF-8', file: 'sonar-project.properties', text: """sonar.projectKey=academy
						sonar.projectName=academy
						sonar.projectVersion=academy
						sonar.sourceEncoding=UTF-8
						sonar.sources=am-core-web-service/src/main/
						sonar.java.binaries=am-core-web-service/target/
						sonar.java.libraries=am-core-web-service/target/classes
						sonar.language=java
						sonar.scm.provider=git
						"""
                        // Sonar Disabled due to we don't have a sonar in tools account yet
						withSonarQubeEnv('Sonar_CI') {
						     def scannerHome = tool 'Sonar_CI'
						     sh "${tool("Sonar_CI")}/bin/sonar-scanner -X"
						}   
                }
            }

        }
    }
}