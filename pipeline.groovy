pipeline{
    agent any   
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
    }
}