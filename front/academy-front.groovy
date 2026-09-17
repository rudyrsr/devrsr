
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
                dir('front'){
                   sh "docker run --rm -v \$(pwd):/usr/src/app -w /usr/src/app node:20.11 sh -c 'npm install && npm run build'"
                   sh 'tar -cvzf nodemodule.tar.gz node_modules/*'
                   stash includes: 'nodemodule.tar.gz', name:'nodmodule'
                   sh 'tar -cvzf dist.tar.gz dist/*'
                   archiveArtifacts artifacts: 'dist.tar.gz', followSymlinks: false
                   stash includes: 'dist.tar.gz', name:'distfront'
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
          agent{ label 'agent_grype' }
          steps{
               script{
                unstash 'nodmodule'
                sh "tar xvfz nodemodule.tar.gz"
                sh "grype dir:node_modules > Informe-scan-front.txt"
               stash includes: 'Informe-scan-front.txt', name: 'frontreports'
               archiveArtifacts artifacts: 'Informe-scan-front.txt', onlyIfSuccessful: true
               low_back = sh(returnStdout: true, script: "cat Informe-scan-front.txt | grep 'Low' | wc -l").trim()
               medium_back = sh(returnStdout: true, script: "cat Informe-scan-front.txt | grep 'Medium' | wc -l").trim()
               high_back = sh(returnStdout: true, script: "cat Informe-scan-front.txt | grep 'High' | wc -l").trim()
               critical_back = sh(returnStdout: true, script: "cat Informe-scan-front.txt | grep 'Critical' | wc -l").trim()
               sh "echo 'vulnerabilities: low_back->${low_back}, medium_back->${medium_back}, high_back->${high_back}, critical_back->${critical_back}'"
               sh "rm -f nodemodule.tar.gz node_modules"
               }
          }  

        }

    }
}