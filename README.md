## Benchmark

Baseline
```
gunzip -c message_1gb.txt.gz| netcat localhost 9999 | pv > /dev/null                            a.m.shcherbakov@Macbookpro-amshcherbakov
 262MiB 0:00:10 [26.8MiB/s]
```

Simple service
```
gunzip -c message_1gb.txt.gz| netcat localhost 9080 | pv > /dev/null
 184MiB 0:00:14 [14.1MiB/s]
```

Service + pipeline client
```
cat message_1gb.txt.gz | netcat localhost 9081 | pv > /dev/null
 21.5MiB 0:00:13 [1.66MiB/s]
```

For baseline used echo server from
https://github.com/beary/echo-server
