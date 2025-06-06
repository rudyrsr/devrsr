pipeline{
    agent {
       label 'slave1'
    }
    tools{
        jdk 'jdk_17'
        maven 'Maven-399'
    }
    stages{
        stage("Limpiar Workspace"){
            steps{
                cleanWs()
            }
        }
        stage("Descargar Proyecto")
        {
            steps{
                git credentialsId: 'git_credentials',branch: "dev", url:"https://github.com/andresmerida/academic-management.git"
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