pipeline{
    agent{ label 'agent_deploy'}
    stages{
        stage("Test inicial"){
            steps{
                sh "echo \"hola Mundo\""
            }
        }
        stage("Espera 15 minutos"){
            steps{
                 script{
                     echo  'Esperando 15 segundos'
                    sleep(15)
                 }
            }
        }
    }
}