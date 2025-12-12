pipeline{
    agent{
        label 'slave1'
    }
    tools{
        jdk 'javas-17'
        maven 'Maven-399'
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
                git credentialsId: 'Cred_git',branch: "dev",url:"https://github.com/andresmerida/academic-management.git"
            }
        }
        stage("Realizar build")
        {
            steps{
                sh "mvn -v"
                sh "java -version"
                sh "pwd"
                sh "mvn clean compile package"
            }
        }
    }
}