pipeline{
    agent{ label 'agent_deploy'}
    stages{
        stage("Limpiar Espacio de trabajo"){
            steps{
                cleanWs()
            }
        }
        stage("Descargar Proyecto"){
            steps{
                 git credentialsId: 'github-secret', branch: "dev", url:"https://github.com/andresmerida/academic-management.git"
            }
        }
    }
}