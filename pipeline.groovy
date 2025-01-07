pipeline{
    agent{
        label 'nod2_slave'
    }
    tools{
        maven 'maven-39'
        jdk 'openjdk-17'
    }
    parameters{
           choice (name:'SCAN_SECURITY', choices:['YES','NO'], description: 'Control de escaneo de seguridad')
           choice (name:'SCAN_CODE', choices:['NO','YES'], description: 'Control de escaneo de codigo estatico')
    }
    stages{
        stage("Creando nombre del build"){
            steps{
                script{
                    currentBuild.displayName= "Deploy_servicio_back-"+ currentBuild.number
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
                git credentialsId: 'git_jenkins', branch: "dev", url: "https://github.com/andresmerida/academic-management.git"
            }
        }
        stage("Realizar Build"){
             steps{
                sh 'mvn -v'
                sh 'pwd'
                sh "mvn clean compile package"
                sh "mv am-core-web-service/target/*.jar am-core-web-service/target/app.jar"
                stash includes: 'am-core-web-service/target/app.jar', name: 'backartifact'
                archiveArtifacts artifacts: 'am-core-web-service/target/app.jar', onlyIfSuccessful: true
                sh "cp am-core-web-service/target/app.jar /tmp/"
             }
        }
        stage("Test de vulnerabilidades de seguridad"){
         when { equals expected: 'YES', actual: params.SCAN_SECURITY}
           steps{
               sh "/grype /tmp/app.jar > informe-scan.txt"
               archiveArtifacts artifacts: 'informe-scan.txt', onlyIfSuccessful: true
           }
        }
        stage("Test de analisis de codigo estatico")
        {
            steps{
                script{
                     sh "pwd"
                     writeFile encoding: 'UTF-8', file:'sonar-project.properties', text: """ sonar.projectKey=academy
                     sonar.projectName=academy
                     sonar.projectVersion=academy
                     sonar.sourceEnconding=UTF-8
                     sonar.sources=am-core-web-service/src/main/
                     sonar.java.binaries=am-core-web-service/target/
                     sonar.java.libraries=am-core-web-service/target/classes/
                     sonar.language=java
                     sonar.scm.provider=git
                     """
                     withSonarQubeEnv('Sonar_CI')
                     {
                        def scannerHome= tool 'Sonar_CI'
                        sh "${tool("Sonar_CI")}/bin/sonar-scanner -X"
                     }
                }
            }
        }
    }
}