def url_repo = "https://github.com/andresmerida/academic-management.git"
pipeline{
    agent {  
       label 'slave1'
    }
    tools{
        jdk 'java21_slave1'
        maven 'maven-399'
    }
    parameters{
        string defaultValue:'dev',description: 'Colocar el branch a ejecutar', name: 'BRANCH', trim: false
        choice(name: 'SCAN_GRYPE', choices: ['YES','NO'], description:'Seleccione YES si desea escanear las vulnerabilidades de seguridad')
        choice(name: 'SCAN_SONARQ', choices: ['YES','NO'], description:'Seleccione YES si desea escanear codigo con Sonarqube')
    }
    stages{
        stage("Limpiar Workspace"){
            steps{
                cleanWs()
            }
        }
        {
            steps{
                script{
                    currentBuild.displayName="Services-deploy_back-"+ currentBuild.number
                }
            }
        }
        stage("Descargar Proyecto"){
            steps{
                git credentialsId: 'git_cred', branch: "${params.BRANCH}", url: "${url_repo}"
            }
        }
        stage("Realizar Build"){
            steps{
                sh "mvn -v"
                sh "pwd"
                sh "mvn clean compile package"
            }
        }
        stage("Archivar artefacto")
        {
            steps{
                sh "mv am-core-web-service/target/*.jar am-core-web-service/target/app.jar"
                stash includes:'am-core-web-service/target/app.jar', name:'backartifact'
                archiveArtifacts artifacts: 'am-core-web-service/target/app.jar', onlyIfSuccessful: true
            }
        }
        stage("Test de Vulnerabilidades de seguridad"){
          when {equals expected: 'YES', actual: SCAN_GRYPE}
          agent { label 'grype_test'}
          steps{
              unstash 'backartifact'
              sh "/grype /home/workspace/DEV/BACKEND/job_test1/am-core-web-service/target/app.jar > Informe-scan.txt"
              archiveArtifacts artifacts: 'Informe-scan.txt', onlyIfSuccessful: true
          }

        }
        stage("Test con SonarQube"){
            when {equals expected: 'YES', actual: SCAN_SONARQ}
            steps{
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