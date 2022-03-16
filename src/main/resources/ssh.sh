#/usr/bin/bash
hosts=(
    "ob01"
    "ob02"
    "ob03"
    )
    for host in "${hosts[@]}"
    do
        echo "begin to run " $@ " on " $host
        ssh $host $@
    done