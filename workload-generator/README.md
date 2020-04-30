# Workload Generator

The *Workload Generator* is responsible for generating search workload according to the workload models in *WorkloadModel* directory. The workload models are obtained by studying the real-world user logs from Taobao.The study finds the following characteristics:
- There are daily periodic patterns exist for user inter-session intervals.
- The most average number of requests within a session is less than 20.
- The interval of intra-sessionn has an burr12 distribution. 
- For the semantic behaviors of queries, 15% query requests for the first page with new keywords, and 85% query requests is a turning page request with repeated keywords. 
- The query words obey the Ziph law. 

## Dependency

The following build tool is required:

- python 3 with scipy

## Workload Generation

The following command is to generate workload from 21:00 to 21:30 with load factor 0.1, and the user scale is 100000:

```shell
python3 workload_generator.py -t 21 -d 1800 -f 0.1 -u 100000
```

Where,
    `-t` is to specify the start hour the workload generate for,
    `-d` is to specify the duration the workload generate for,
    `-f` is to specify the workload factor the workload generate for,
    `-u` is to specify the user scale used in the system model under test.

By the *Workload Generator*, we can generate workload for specified hour of one day and specified user scale. The workload(user queries) generated are written in the `workload_xxxxxxxx.csv`, which can be driven to the system model under test (e.g. eSearchEngineModel) by Jmeter later.
