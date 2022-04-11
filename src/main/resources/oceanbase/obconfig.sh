#/usr/bin/bash

# actually obcontrol and ob01 can be same node

#echo '192.168.62.6 obcontrol' >> /etc/hosts
#echo '192.168.62.7 ob01' >> /etc/hosts
#echo '192.168.62.8 ob02' >> /etc/hosts
#echo '192.168.62.9 ob03' >> /etc/hosts

# TODO 使用循环
echo '$1 obcontrol' >> /etc/hosts
echo '$2 ob01' >> /etc/hosts
echo '$3 ob02' >> /etc/hosts
echo '$4 ob03' >> /etc/hosts

sed -i 's/SELINUX\=enforcing/SELINUX\=disabled/g' /etc/selinux/config

systemctl disable firewalld
systemctl stop firewalld
systemctl status firewalld

#systemctl disable NetworkManager
#systemctl stop NetworkManager
#systemctl status NetworkManager

echo 'fs.aio-max-nr=1048576' >> /etc/sysctl.conf
sysctl -p

echo '* soft nofile 655350' >> /etc/security/limits.conf
echo '* hard nofile 655350' >> /etc/security/limits.conf

mkdir /root/.ssh