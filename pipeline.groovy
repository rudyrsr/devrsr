pipeline{
    agent{
        label 'nod2_slave'
    }
    stages{
        stage("Test"){
            steps{
                sh " pwd"
                sh "sleep 20"
                sh "echo 'hola mundo'"
            }
        }
    }
}