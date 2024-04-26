def url_repo = "https://github.com/andresmerida/academic-management.git"
pipeline{
   agent
   {
      label 'jenkins_slave'
   }
    tools{
        maven 'maven-396'
        jdk 'jdk21'
    }
    parameters{
         string defaultValue: 'dev', description: 'Colocar un brach a deployar', name: 'BRANCH', trim: false
         choice (name: 'SCAN_GRYPE', choices: ['YES','NO'], description: 'Activar escáner con grype')
    }
    environment{
       VAR='NUEVO'
    }
    stages{
        stage("create build name"){
            steps{           
                script{
                   currentBuild.displayName= "service_back-"+ currentBuild.number
                }
            }
        }
        stage("Limpiar"){
            steps{
                cleanWs()
            }
        }
        stage("download proyect"){
            steps{
                git credentialsId: 'git_credentials', branch: "${BRANCH}", url: "${url_repo}"
                echo "proyecto descargado"
            }
        }
        stage('build proyect')
        {
            steps{
                sh "mvn clean compile package -Dmaven.test.skip=true -U"
                sh "mv am-core-web-service/target/*.jar am-core-web-service/target/app.jar"
                stash includes: 'am-core-web-service/target/app.jar', name: 'backartifact'
                archiveArtifacts artifacts: 'am-core-web-service/target/app.jar', onlyIfSuccessful: true
                sh "cp am-core-web-service/target/app.jar /tmp/"
            }
        }
        stage("Test vulnerability")
        {
           when {equals expected: 'YES', actual: SCAN_GRYPE} 
            steps{
               sh "/grype /tmp/app.jar > informe-scan.txt"
               archiveArtifacts artifacts: 'informe-scan.txt', onlyIfSuccessful: true
            }
        }
        stage('sonarqube analysis'){
              when {equals expected: 'YES', actual: SCAN_GRYPE}
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
        stage('Image push artifactory')
        {
            agent {label 'node_deploy'}
            steps{
                script{
                    unstash 'backartifact'
                    echo "hola"

                }
            }
        }
        
    }
}