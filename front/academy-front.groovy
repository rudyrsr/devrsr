
def url_repo= "https://git.digitalharborbolivia.com:8081/rsalvatierra.teacher/academy.git" 
def low_back =""
def medium_back =""
def critical_back =""
def high_back =""
pipeline{
    agent{
        //label 'built-in'
        label 'node_deploy'
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
                dir('front'){
                   sh "docker run --rm -v \$(pwd):/usr/src/app -w /usr/src/app node:20.11 sh -c 'npm install && npm run build'"
                   sh 'pwd'
                }
            }
        }
        stage("Archivar artefacto"){
            steps{
                dir('front'){
                   sh 'tar -cvzf nodemodule.tar.gz node_modules/*'
                   stash includes: 'nodemodule.tar.gz', name:'nodmodule'
                   sh 'tar -cvzf dist.tar.gz dist/*'
                   archiveArtifacts artifacts: 'dist.tar.gz', followSymlinks: false
                   stash includes: 'dist.tar.gz', name:'distfront'
                }
            }
        }
        stage("Test de vulnerabilidades con grype"){
          when {equals expected: 'YES', actual: SCAN_GRYPE}  
          agent{ label 'agent_grype' }
          steps{
               script{
               unstash 'nodmodule'
               sh "tar xvfz nodemodule.tar.gz"
               sh "/grype dir:node_modules > Informe-scan-front.txt"
               stash includes: 'Informe-scan-front.txt', name: 'frontreports'
               archiveArtifacts artifacts: 'Informe-scan-front.txt', onlyIfSuccessful: true
               low_back = sh(returnStdout: true, script: "cat Informe-scan-front.txt | grep 'Low' | wc -l").trim()
               medium_back = sh(returnStdout: true, script: "cat Informe-scan-front.txt | grep 'Medium' | wc -l").trim()
               high_back = sh(returnStdout: true, script: "cat Informe-scan-front.txt | grep 'High' | wc -l").trim()
               critical_back = sh(returnStdout: true, script: "cat Informe-scan-front.txt | grep 'Critical' | wc -l").trim()
               sh "echo 'vulnerabilities: low_back->${low_back}, medium_back->${medium_back}, high_back->${high_back}, critical_back->${critical_back}'"
               sh "rm -rf nodemodule.tar.gz node_modules"
               }
          }  

        }
        stage("Test con SonarQube") {
            when {equals expected: 'YES', actual: SCAN_SONARQ}
            steps {
                dir('front') {
                    script {
                        writeFile encoding: 'UTF-8', file: 'sonar-project.properties', text: """
                            sonar.projectKey=academy-vue-frontend
                            sonar.projectName=Academy Vue Frontend
                            sonar.projectVersion=1.0.0
                            sonar.sourceEncoding=UTF-8
                            sonar.sources=src
                            sonar.inclusions=**/*.js,**/*.ts,**/*.vue
                            sonar.exclusions=**/node_modules/**,**/dist/**,**/*.spec.js,**/*.spec.ts
                            # sonar.javascript.lcov.reportPaths=coverage/lcov.info
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