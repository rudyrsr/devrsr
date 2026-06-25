pipeline{
    agent {
        label 'slave1'
    }
    stages{
       stage("test inicial"){
         steps{
            sh "echo \"hola mundo\""   
         }
       }
       stage("Esperando 10 seg")
       {
           steps{
               script{
                   echo 'esperando 15 segundos'
                   sleep(15)
               }
           }
       }
    }
}