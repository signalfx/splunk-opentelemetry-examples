NM=$1

multipass launch --name ${NM} --memory 2G --disk 8G --cpus 2

multipass transfer install_docker.sh ${NM}:/home/ubuntu/install_docker.sh
multipass exec ${NM} -- sh -x /home/ubuntu/install_docker.sh
