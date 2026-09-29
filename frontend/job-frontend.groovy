
def url_repo= "https://github.com/rudyrsr/academy-front.git" 
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
                git credentialsId: 'github-secret', branch: "develop", url:"${url_repo}"
        
            }    
        }
        stage("Realizar build"){
            steps{
              
                   sh "docker run --rm -v \$(pwd):/usr/src/app -w /usr/src/app node:20.11 sh -c 'npm install && npm run build'"
                   sh 'tar -cvzf nodemodule.tar.gz node_modules/*'
                   stash includes: 'nodemodule.tar.gz', name:'nodmodule'
                   sh 'tar -cvzf dist.tar.gz dist/*'
                   archiveArtifacts artifacts: 'dist.tar.gz', followSymlinks: false
                   stash includes: 'dist.tar.gz', name:'distfront'
                
            }
        }
        stage("Archivar artefacto"){
            steps{
                   sh 'tar -cvzf nodemodule.tar.gz node_modules/*'
                   stash includes: 'nodemodule.tar.gz', name:'nodmodule'
                   sh 'tar -cvzf dist.tar.gz dist/*'
                   archiveArtifacts artifacts: 'dist.tar.gz', followSymlinks: false
                   stash includes: 'dist.tar.gz', name:'distfront'
            }
        }
    }
}