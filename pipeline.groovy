pipeline{
    agent{
        label 'nod2_slave'
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
                sleep time: 1, unit: 'MINUTES'
            }
        }
    }
}