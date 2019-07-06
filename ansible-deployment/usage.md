# Ansible deployment

In order to deploy SkipGraph nodes to servers, you should provide the following
files:

- slave.jar
- master.jar
- node.conf-master
- node.conf-slave

You will also need to put the IPs of the servers you have into the hosts file.

The files mentioned above should go into the directory containing `play.yaml`.
You would also need to have ansible installed.

Then its a matter of running the following command and watching the cows scroll
by:

```
ansible-playbook -i hosts -u ec2-user play.yaml
```

If you are using a separate key file:
```
ansible-playbook -i hosts -u ec2-user play.yaml --key-file <key.pem>
```
