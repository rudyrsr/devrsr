pipeline{
    agent{
        label 'nod2_slave'
    }
    tools{
        maven 'maven-38'
        jdk 'jdk21'
    }
    environment{
       workspace= "/data/"
    }
    stages{
        stage("Limpiar"){
            steps{
                 cleanWs()
            }
        }
        stage("Ejecutar prueba"){
            steps{
                echo "empezando con la creacion de jobs"
            }
        }
        stage('descargando proyecto')
        {
            steps{
                git credentialsId: 'git_jenkins', branch: "dev", url: "https://github.com/andresmerida/academic-management.git" 
                sh "mvn -v"
                sh "pwd"
                sh "mvn clean compile package"
               }
            
        }
    }
}