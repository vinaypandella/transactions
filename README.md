This repository consists of an example micro service which is developed using Scala as a programming language, Akka as a messagin platform and Spray framework for REST API and also uses docker for packaging the service in to a container.

You might need to have a basic understading about Scala, Akka, Spray and Docker and Ansible to play on this repository. It has few instructions to build the service, packaing the service and deploy the service as the container.

Example uses vagrant for development and also in production environment to deploy the containers. It uses the Ansible as the configuration management tool.

You need to have Vagrant installed to run this service.

Example is about REST service for transactions.

Cloning the repo

```
git clone 

cd transactions
```

If you look at the vagrant file you will notice that we are using the ubuntu server for development.

```
config.vm.box = "ubuntu/trusty64"
```

The above code snippet in Vagrantfile  says the box (OS) to be Ubuntu.


We can also specify the sync folder as /vagrant as mentioned below which means that everything in the current directory on the host will be avaiable as the /vagrant directory inside the VM.

```
config.vm.synced_folder ".", "/vagrant"
```

As mentioned below we are using the shell script to do the rest of the provisioning. This example uses bootstrap.sh which has the instructions to install the Ansible.

```
  config.vm.provision "shell", path: "bootstrap.sh"
```

If look at the Vagrantfile you will see that it has two VMs defined dev and prod. Each of them will run Ansible that will install the required software based on the file which is selected either dev or prod.

Best way to use Ansible for configurations is to divide the configurations in to roles.
In our case we have four different roles located in ansible/roles directory.
Role 1 : To install Scala and SBT 
Role 2 : TO install Docker and it is up and running.
Role3 : To Run the MongoDb container.
Role4 : To install/deploy the transactions docker container is used only in production.

Lets spin up the dev VM and build something.

Execute the following to spin up the VM of transactions service in dev mode.

```
vagrant up dev
```

SSH in to the vagrant box using the following command.

```
vagrant ssh dev
```

Vagrant file consists of the configuration to mount the current directory of worksapce in to /vagrant folder of Virtual Machine. Do the following to go to workspace folder.

```
cd /vagrant 
```

All the required software for the service development is already installed in Virtual Machine. You can use scala build tool to run the tests.

```
sbt "~test-quick *Spec"
```

Building the service using the scala build tool.

You can just run the tests using sbt tool as follows again to verify if your changes are good.

```
sbt "testOnly *Spec"
```

Assembling or packging the servcie using Scala build tool.

```
sbt assembly
```

Packaing / Building the service target in to docker container.

```
sudo docker build -t vinaypandella/transactions .
```

Pushing the container changes to docker respository.

```
sudo docker push vinaypandella/transactions
```

After uploading the transactions service container to docker repository you can deploy this container anywhere you want.

In this example to just show how this can be used in the production environment i just created a Virtual Machine which would use the docker instance from the docker hub and confiugre the properties with respect to production using Ansible.

Stop the running dev Virtual Machine if it is already running.

```
vagrant halt dev
```

Spinning up the Prod Virtual Machine.

```
vagrant up prod
```

Once the Virtual Machine is up and running you can try invoke the service.

POST some transactions using the following.

```
curl -H 'Content-Type: application/json' -X PUT -d '{"_id": 1, "transactionType": "SALE", "transactionDetails": "TransactionDetails1", "total":99.99}' http://localhost:8080/api/v1/transactions

curl -H 'Content-Type: application/json' -X PUT -d '{"_id": 2, "transactionType": "RETURN", "transactionDetails": "TransactionDetails1", "total":99.99}' http://localhost:8080/api/v1/transactions

curl -H 'Content-Type: application/json' -X PUT -d '{"_id": 3, "transactionType": "SALE", "transactionDetails": "TransactionDetails1", "total":99.99}' http://localhost:8080/api/v1/transactions
```

GET the transactions and verify if the above are inserted

```
curl -H 'Content-Type: application/json' http://localhost:8080/api/v1/transactions
```

You can delete using the following.

```
curl -H 'Content-Type: application/json' -X DELETE http://localhost:8080/api/v1/transactions/_id/2
```

You can play around with some other REST operations..

