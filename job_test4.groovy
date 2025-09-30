pipeline{
    agent
    {
      label 'slave1'        
    }
    tools{
        jdk 'Java21_master'
        maven 'Maven-3911'
    }
    stages{
        stage("Limpiar Workspace")
        {
            steps{
                cleanWs()
            }
        }
        stage("Descargar Proyecto")
        {
            steps{
                git credentialsId: 'git-hub_cred',branch: "dev", url: "https://github.com/andresmerida/academic-management.git"
            }
        }
        stage("Realizar Build")
        {
            steps{
                sh "mvn -v"
                sh "pwd"
                sh "mvn clean compile package"
            }
        }
    }
}