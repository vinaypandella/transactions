This repository consists of an example service which is developed using Scala , Akka and Spray framework and packaged using docker container.

You need to have Vagrant installed to run this service.

Developer would do the following to get the service and make his changes.

After cloning the repository in the source folder

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

In this example to just show how this can be used in the production environment i just created a Virtual Machine which would use the docker instance from the docker hub and confiugre the propertied with respect to production using Ansible.

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

You can play around with some other REST operations..

