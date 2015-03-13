This repository consists of an example service which is developed using Scala , Akka and Spray framework and packaged using docker container.

You need to have Vagrant installed to run this service.

Developer would do the following to get the service and make his changes.

After cloning the repository in the source folder

Execute the following to spin up the VM of transactions service in dev mode.

vagrant up dev

SSH in to the vagrant box using the following command.

vagrant ssh dev

Vagrant file consists of the configuration to mount the current directory of worksapce in to /vagrant folder of Virtual Machine. Do the following to go to workspace folder.

cd /vagrant 

All the required software for the service development is already installed in Virtual Machine. You can use scala build tool to run the tests.

sbt "~test-quick *Spec"

Building the service using the scala build tool.

You can just run the tests using sbt tool as follows again to verify if your changes are good.

sbt "testOnly *Spec"

Assembling or packging the servcie using Scala build tool.

sbt assembly

Packaing / Building the service target in to docker container.

sudo docker build -t vinaypandella/transactions .

Pushing the container changes to docker respository.

sudo docker push vinaypandella/transactions
