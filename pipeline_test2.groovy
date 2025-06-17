def url_repo = "https://github.com/andresmerida/academic-management.git"
def low_vp = ""
def high_vp = ""
def medium_vp = ""
def critical_vp = ""
pipeline{
    agent {
       label 'slave1'
    }
    tools{
        jdk 'jdk_17'
        maven 'Maven-399'
    }
    parameters{
        string defaultValue: 'dev', description: 'Colocar el branch a ejecutar', name: 'BRANCH', trim: false
        choice(name: 'SCAN_GRYPE', choices: ['NO', 'YES'], description: 'Activar si desea escanear con grype')
        choice(name: 'SCAN_SONARQ', choices: ['NO', 'YES'], description: 'Activar si desea escanear con Sonar Qube')
    }
    environment{
        EMAIL_RECIPIENTS= 'rudy3112cursos@gmail.com'
        EMAIL_RECIPIENTS_SUPPORT= 'rudy3112cursos@gmail.com'
    }
    stages{
        stage("Limpiar Workspace"){
            steps{
                cleanWs()
            }
        }
        stage("Colocar nombre de build")
        {
            steps{
                 script{
                    currentBuild.displayName= "service_back-"+ currentBuild.number
                 }
            }
        }
        stage("Descargar Proyecto")
        {
            steps{
                git credentialsId: 'git_credentials',branch: "${params.BRANCH}", url:"${url_repo}"
            }
        }
        stage("Realizar Build")
        {
            steps{
                sh "mvn -v"
                sh "pwd"
                sh "mvn clean compile package"
                sh "pwd"
               
            }
        }
        stage("archivar artefacto"){
            steps{
                sh "mv am-core-web-service/target/am-core-web-service-1.0.0.jar am-core-web-service/target/app.jar"
                stash includes: 'am-core-web-service/target/app.jar', name: 'backartifact'
                archiveArtifacts artifacts: 'am-core-web-service/target/app.jar', onlyIfSuccessful: true
            }
        }
        stage("Test de vulnerabilidades de seguridad"){
            when {equals expected: 'YES', actual: SCAN_GRYPE}
            agent { label 'grype_test'}
            steps{
               script{
                 unstash 'backartifact'
                 sh "/grype /home/workspace/APP-DEV/job_test2/am-core-web-service/target/app.jar > Informe-scan.txt"
                 archiveArtifacts artifacts: 'Informe-scan.txt', onlyIfSuccessful: true
                 stash includes: 'Informe-scan.txt', name: 'back_reports'
                 low_vp = sh(returnStdout: true, script: "cat Informe-scan.txt | grep 'Low' | wc -l").trim()
                 medium_vp = sh(returnStdout: true, script: "cat Informe-scan.txt | grep 'Medium' | wc -l").trim()
                 high_vp = sh(returnStdout: true, script: "cat Informe-scan.txt | grep 'High' | wc -l").trim()
                 critical_vp = sh(returnStdout: true, script: "cat Informe-scan.txt | grep 'Critical' | wc -l").trim()
                 sh "echo 'vulnerabilidades: low_vp->${low_vp}, medium_vp->${medium_vp}, high_vp->${high_vp}, critical_vp->${critical_vp}'"
               }
            }

        }
        stage("Test con SonarQube"){
            when {equals expected: 'YES', actual:SCAN_SONARQ}
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
        stage('Image push images')
        {
            agent {label 'node_deploy'}
            steps{
                script{
                    unstash 'backartifact'
                    sh "rm /data/publish/app.jar | true"
                    sh "cp am-core-web-service/target/app.jar /data/publish/"
                    sh "docker rmi  192.168.137.10:8082/docker-test/back-prueba:latest | true; cd /data/publish/ ; docker build -t 192.168.137.10:8082/docker-test/back-prueba:latest ."
                    sh "docker push  192.168.137.10:8082/docker-test/back-prueba:latest "
                }
            }
        }


    }
    post {
        success {
			unstash  'back_reports'
		    sendEmail("Successful","${env.EMAIL_RECIPIENTS}","${low_vp}","${medium_vp}","${high_vp}","${critical_vp}")
	    }
		failure {
		    sendEmail("failed", "${env.EMAIL_RECIPIENTS_SUPPORT}","${low_vp}","${medium_vp}","${high_vp}","${critical_vp}")
		}
	}
}
def sendEmail(status, EMAIL_RECIPIENTS,low_vp,medium_vp,high_vp,critical_vp) {
    def body1= '''<html>
  <style>
     BODY{
       background-color:white;
     }
     TABLE{
       border-width: 1px;border-style: solid;border-color: blue;border-collapse: collapse;font-family: Verdana;font-size: 10.5pt;
     }
     TH{
      border-width: 1px;padding: 5px;border-style: solid;border-color: blue;background-color:lightblue;
     }
     TD{
      border-width: 1px;padding: 5px;border-style: solid;border-color: blue;text-align:center
     }
  </style>
<body>
 <a> Hola, </a><br><br>
 <a> Se muestran los detalles del despliegue del backend</a><br><br>
  <FONT COLOR="green"> Vulnerabilities found on this version</FONT><br><br>
  <table>
   <th>ARTIFACT</th>
   <th>CRITICO</th>
   <th>ALTO</th>
   <th>MEDIO</th>
   <th>BAJO</th>
   <tr>
     <td>BACK-END</td>
     <td style="color:red;">'''+critical_vp+'''</td>
     <td style="color:OrangeRed;">'''+high_vp+'''</td>
     <td style="color:GoldenRod;">'''+medium_vp+'''</td>
     <td style="color:DarkSeaGreen;">'''+low_vp+'''</td>
   </tr>
 </table>
 <br><br>
  <FONT COLOR="navy"> Si desea revisar los informes en documentos que se encuentran adjuntos </FONT><br><br>
 </body>
</html>'''
    def body2= '''<html>
  <style>
     BODY{
       background-color:white;
     }    
  </style>
<body>
 <a> Hi, </a><br><br>
 <h1 style="color:red;">Actualizacion del BACK-END FALLO</h1><br><br>
 <br><br>
 </body>
</html>'''
 if(status=="Successful"){
     
        email body:  body1 + "<br><a>REGARDS</a><br><a>JENKINS</a><br><br>", mimeType: 'text/html',attachmentsPattern:'*.txt', subject: 'Actualizando el BACK-END', to: EMAIL_RECIPIENTS
     
        
    }
    else{
        email body: body2 + "<br><a>REGARDS</a><br><a>JENKINS</a><br><br>", mimeType: 'text/html', attachLog: true, subject: 'Actualizando el BACK-END', to: EMAIL_RECIPIENTS_SUPPORT
    }
}