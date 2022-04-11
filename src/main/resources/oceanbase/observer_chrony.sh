#/usr/bin/bash

# chrony[CentoOS8]
# trust obcontrol
sed -i "s/pool 2.centos.pool.ntp.org iburst/server $1 iburst/g" /etc/chrony.conf
cat /etc/chrony.conf
systemctl restart chronyd.service && systemctl enable chronyd.service --now
timedatectl set-ntp true