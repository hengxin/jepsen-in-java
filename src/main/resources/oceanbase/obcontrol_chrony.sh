#/usr/bin/bash

# chrony[CentOS8]
# delete "#" in this row
# TODO maybe modify in line with args
# Attention: 用sed不要忘记最后的/g!!!!!
sed -i "s/\#allow 192.168.0.0\/16/allow 192.168.0.0\/16/g" /etc/chrony.conf
systemctl restart chronyd.service && systemctl enable chronyd.service --now
echo "StrictHostKeyChecking no" >> /etc/ssh/ssh_config
echo "UserKnownHostsFile /dev/null" >> /etc/ssh/ssh_config