# /usr/bin/bash

# cluster name as the first args
sed -i "s/cluster_name: 'Test Cluster'/cluster_name: '$1'/g" /etc/cassandra/conf/cassandra.yaml

# local ip as the second args
sed -i "s/listen_address: localhost/listen_address: $2/g" /etc/cassandra/conf/cassandra.yaml
sed -i "s/rpc_address: localhost/rpc_address: $2/g" /etc/cassandra/conf/cassandra.yaml

# seeds as the third args split with ',' no blank
sed -i "s/- seeds: \"127.0.0.1\"/- seeds: \"$3\"/g" /etc/cassandra/conf/cassandra.yaml

systemctl start cassandra

