# Workload Generator

The *Workload Generator* is responsible for generating search workload according to the workload models in *WorkloadModel* directory. The workload models are obtained by studying the real-world user logs from Taobao.The study finds the following characteristics:
- There are periodic patterns exist for user inter-session intervals each day.
- The sessions number of one second in one hour has an uniform distribution.
- The most average number of requests within a session is less than 15.
- The interval of intra-sessionn has an burr12 distribution. 
- For the semantic behaviors of queries, 80% queries search for the first page, and 20% queries search for other pages. 
- The query words obey the Ziph law. 

## Dependency

The following build tool is required:

- python 3 with scipy

## Workload Generation

The following command is to generate workload for the 9 PM., and the user scale is 100000:

```shell
python3 workload_generator -t 21 -u 100000
```

Where,`-t` is to specify the hour of the workload, `-u` is to specify the user scale used in the system model under test.

By the *Workload Generator*, we can generate workload for specified hour of one day and specified user scale. The workload(user queries) generated are written in the `query_workload.csv`, which can be driven to the system model under test (e.g. eSearchEngineModel) by Jmeter later.
