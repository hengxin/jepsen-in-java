# /usr/bin/bash

# ATTENTION: send mysql-community.repo first!
dnf -y remove @mysql
dnf -y module reset mysql
dnf -y module disable mysql

dnf -y --enablerepo=mysql57-community install mysql-community-server

systemctl start mysqld.service

echo 'skip_grant_tables' >> /etc/my.cnf
service mysqld restart
mysql -u root -e "update mysql.user set authentication_string = password('root') where user = 'root'"
mysql -u root -e "update mysql.user set host = '%' where user = 'root'"
mysql -u root -e "update mysql.user set password_expired='N'"
mysql -u root -e "flush privileges"

# 删除最后一行
sed -i '$d' /etc/my.cnf

service mysqld restart