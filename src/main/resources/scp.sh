#/usr/bin/bash
hosts=(
    "ob01"
    "ob02"
    "ob03"
    )
    for host in "${hosts[@]}"
    do
        echo "begin to scp " $@ " on " $host
        scp -r $1 $host:$2
    done