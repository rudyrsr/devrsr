pipeline{
    agent{
        label 'nod2_slave'
    }
    tools{
        maven 'maven-362'
        jdk 'jdk21'
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
                git credentialsId: 'Jenkins', branch: "dev", url: "https://github.com/andresmerida/academic-management.git" 
                sh "mvn -v"
                sh "pwd"
                sh "mvn clean compile package"
               }
            
        }
    }
}