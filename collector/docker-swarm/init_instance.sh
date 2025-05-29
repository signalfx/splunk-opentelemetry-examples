NM=$1

orb create --arch amd64 ubuntu ${NM}
orb -m ${NM} sudo mkdir /home/ubuntu
orb -m ${NM} sudo chown ${USER}:${USER} /home/ubuntu
orb push -m ${NM} install_docker.sh /home/ubuntu/install_docker.sh

orb -m ${NM} /home/ubuntu/install_docker.sh
