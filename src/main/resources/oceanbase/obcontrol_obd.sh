#/usr/bin/bash

# obdeploy
yum install -y yum-utils
yum-config-manager --add-repo https://mirrors.aliyun.com/oceanbase/OceanBase.repo
yum install -y ob-deploy
# ATTENTION: send config file first!
obd cluster deploy test -c /root/mini-distributed-example.yaml
obd cluster start test
obd cluster list