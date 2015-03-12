# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

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