This repository consists of an example micro service which is developed using Scala , Akka and Spray framework and also details about specifics about packaging the service using docker container.

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
Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.synced_folder ".", "/vagrant"
  config.vm.provider "virtualbox" do |v|
    v.memory = 2048
  end
  config.vm.provision "shell", path: "bootstrap.sh"
  config.vm.define :dev do |dev|
    dev.vm.provision :shell, inline: 'ansible-playbook /vagrant/ansible/dev.yml -c local'
    dev.vm.hostname = "transactions-dev"
  end
  config.vm.define :prod do |prod|
    prod.vm.network :forwarded_port, host: 8080, guest: 8080
    prod.vm.provision :shell, inline: 'ansible-playbook /vagrant/ansible/prod.yml -c local'
    prod.vm.hostname = "transactions-prod"
  end
  if Vagrant.has_plugin?("vagrant-cachier")
    config.cache.scope = :box
  end
end

```

```
config.vm.box = "ubuntu/trusty64"
```

The above code snippet in Vagrantfile  says the box (OS) to be Ubuntu.

```
config.vm.synced_folder ".", "/vagrant"
```

We can also specify the sync folder as /vagrant as mentioned below which means that everything in the current directory on the host will be avaiable as the /vagrant directory inside the VM.

```
config.vm.synced_folder ".", "/vagrant"
```

We are using the shell script to do the rest of the provisioning in this example we use bootstrap.sh which has the instructions to install the required software for developer environment using Ansible.

The rest of things we’ll need will be installed using Ansible so we’re provisioning our VM with it through the bootstrap.sh script. Finally, this Vagrantfile has two VMs defined: dev and prod. Each of them will run Ansible that will make sure that everything is installed properly.

Preferable way to work with Ansible is to divide configurations into roles. In our case, there are four roles located in ansible/roles directory. One will make sure that Scala and SBT are installed, the other that Docker is up and running, and another one will run the MongoDB container. The last role (books) will be used later to deploy the service we’re building to the production VM.



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

You can delete using the following.

```
curl -H 'Content-Type: application/json' -X DELETE http://localhost:8080/api/v1/transactions/_id/2
```

You can play around with some other REST operations..

