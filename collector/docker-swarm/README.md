## Docker Swarm example

This document explains how to setup a docker swarm environment, that we will deploy the OTel collector in.

Based on [this article](https://dev.to/mattdark/docker-swarm-with-virtual-machines-using-multipass-39b0) for setting up swarm with multipass.

### Setup

Run the following from the host to deploy the 3 systems with docker

```bash
sh -x init_instance.sh manager
sh -x init_instance.sh worker1
sh -x init_instance.sh worker2
```

Then initialize the swarm:

```bash
multipass exec manager -- docker swarm init
```
Take the token and IP address provided as output from the previous command and use it to connect the workers:

```bash
multipass exec worker1 -- docker swarm join --token [token] [ip]:2377
multipass exec worker2 -- docker swarm join --token [token] [ip]:2377
```

Confirm the swarm:
```bash
multipass exec manager -- docker node ls
```

The output should look like the following: 
````
ID                            HOSTNAME   STATUS    AVAILABILITY   MANAGER STATUS   ENGINE VERSION
jf6b2v57nx0wlchnk0zk7dviz *   manager    Ready     Active         Leader           28.1.1
o6mfzffyu1y9jahb3cf3g7zrn     worker1    Ready     Active                          28.1.1
ryjfhsj6unp9uktecd0uc7jzc     worker2    Ready     Active                          28.1.1
````

### OpenTelemetry Collector deployment

Now we want to deploy the OpenTelemetry collector. Update the
[docker-compose_config.yml](./docker-compose_config.yml) file 
with the target realm and access token.

Next, use the following commands to copy the `docker-compose_config.yml` to the 
manager node, and the collector configuration file (`config1.yml`) to the worker 
nodes.  Then deploy the collector service. 

```bash
# Push the compose file and the config file
multipass transfer docker-compose_config.yml manager:/home/ubuntu/docker-compose.yml
multipass transfer config1.yml worker1:/home/ubuntu/collector.yml
multipass transfer config1.yml worker2:/home/ubuntu/collector.yml
# Deploy
multipass exec manager -- docker stack deploy --compose-file docker-compose.yml otelcol
# Verify
multipass exec manager -- docker stack services otelcol
```

Now when you run:
```bash
multipass exec manager -- docker stack services otelcol
```

you will know it is deployed when it reaches 2/2.

You can shell into each of the instances and do the regular investigations what's happening:

```bash
# Go onto the worker
multipass shell worker1
# View running containers
docker ps -a
# View logs of otel collector
docker logs [Container ID]
# Exit back to your host
exit
```

Here's an example of the `cpu.utilization` metric being sent:
![CPU Graph](img/cpu.png)