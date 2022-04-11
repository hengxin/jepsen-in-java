#/usr/bin/bash

# 只在控制节点运行这个文件即可
ssh-keygen -t rsa -N '' -f id_rsa -q
# -N:是指密码为空；
# -f:id_rsa是指保存文件为~/.ssh/id_rsa和~/.ssh/id_rsa.pub
# -q:指静默模式, 不输出显示
chmod u+x /root/scp.sh
chmod u+x /root/ssh.sh
/root/scp.sh ~/.ssh/id_rsa.pub ~/.ssh/
/root/ssh.sh "cat /root/.ssh/id_rsa.pub >> /root/.ssh/authorized_keys"