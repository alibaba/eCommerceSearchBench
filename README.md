# e-commerce search benchmark

## Introduction

E-commerce search benchmark is the first end-to-end application benchmark for e-commerce search system with personalized recommendations. It helps people deeply understand  the characteristics of workloads for e-commerce search and drives better design options for industry search system.It specifies an e-commerce search dataset and workloads that driven by production data and real-world user queries, and targets at hardware and software systems that provide e-commerce online search service. Therefore, any of those system can be used to establish the feasibility of this benchmark.

Features of the benchmark:
* Provides a *Data Generator* using real-world datasets and producing synthetic data of various scales. 
* Provides a *Workload Generator* that driven by the real-world user logs from Taobao.
* Provides a e-commerce search model *eSearchEngineModel* that simulated the search system of Taobao.
* Evaluates the overall performance and performance of individual components.
![arch](figures/arch.png)

The e-commerce search benchmark is built on docker images. As shown in the figure above,the benchmark consists of 7 docker images:
1. aliesearch-search-planner
2. aliesearch-query-planner
3. aliesearch-tf-serving
4. aliesearch-ha3
5. aliesearch-ranking-service
6. aliesearch-jmeter-image
7. aliesearch-benchmark-cli

where, images 1~5 constitute the e-commerce search model *eSearchEngineModel*, *`aliesearch-benchmark-cli`* is the *Data Generator*, *`aliesearch-jmeter-image`* drives the workload generated by *Workload Generator* to the  e-commerce search model *eSearchEngineModel*.

## Preparation

### Dependency

The following build tools are required：

- gradle 4.x
- maven 3.x
- docker 17.09+
- jdk8

> Note:
> Make sure you can use docker without `sudo` by running
>
> `sudo usermod -aG docker $USER`

### Build 

Run `build.sh` in *`eSearchEngineModel`* directory to build and publish image, etc. 
- Compile and build docker images

    ```shell
    ./build.sh build
    ```
- Publish the docker images to specified remote docker reposition. If you are only running locally, you can skip this step.

    ```shell
    ./build.sh push ${repo_name}
    ```

## Running benchmark

> Note:
> 
> Correctly value for java maximum memory size must be required, otherwise the docker images won't work. You can set it for temporary by running
>
> `sudo sysctl -w vm.max_map_count=262144`
>
> or you can set it for permanent by directly edit the `/etc/sysctl.conf` file on the host, adding a line as flollow:
>
> `vm.max_map_count = 262144`
>
> and then run 
>
> `sysctl -p`

### Running an Example in standalone

1. Copy `appctl.sh` in this directory to your working directory;
2. Pull all the images by running: (If you are only running locally, do skip this step.)
   ```
    sudo ./appctl.sh pull
   ```
3. Start all images and automatically import the default data by running:
   ```
    sudo ./appctl.sh start
   ```
4. Check if all the images are ready to work by running the following command in any docker:
   ```
    curl -H 'Content-Type:application/json;charset=UTF-8' -d'
                {"uid":"798", "page":0, "query":"68"}' ${search_planner_ip}:8080/search
   ```
5. Run an example experiment by jmeter:
   - login to 'jmeter-image' by running:
    ```
    sudo docker exec -it aliesearch-jmeter-image bash
    ```
   - run to start the pressure test process by running:
    ```
    cd apache-jmeter-5.1.1
    ./bin/jmeter -n -t search_stress.jmx -l result -e -o report
    ```

### Running an Example on k8s

Distributed deployment is also provided depending on k8s, which is more in line with the online environment. The benchmark can be run on a k8s cluster as follows:

1. Copy `k8s.yml` to working directory on k8s master node.
2. Replace the docker images repo configuration in`k8s.yml` with your own repo
3. Pull all images and automatically import the default data by running:
   ```
   sudo kubectl create -f k8s.yml
   ```
4. Check the pods status to ensure that all pods start up properly by running:
   ```
   sudo kubectl get pod
   ```
5. Run an example experiment by jmeter:
    - log in to jmeter pod 
      ```
      sudo kubectl exec -it aliesearch-jmeter-image bash
      ```
    - run to start the pressure test process
      ```
      cd apache-jmeter-5.1.1
      ./bin/jmeter -n -t search_stress.jmx -l result -e -o report
      ```

### Running with Customized Pattern

The benchmark comes with an e-commerce data generator and workloads generator that driven by production data and real-world user queries. There are 4 steps for running an experiment with Customized data scale and workload pattern.

#### 1. Install the benchmark

Start all the images of the benchmark according to the 1~3 steps in *`Running Example in standalone`*

#### 2. Data Generation 

Login to the *`benchmark-cli`* image by running:

```shell
sudo docker exec -ti aliesearch-benchmark-cli bash
``` 

Generate goods and user data , load them into corresponding search components by running:

```shell
vim entrypoint.sh
sh entrypoint.sh ${scale_factor}
```

where, *${scale_factor}* sets the scale factor determining the dataset size (1 scale factor equals 10K goods and 6K user, 10 scale factor equals 100K goods and 60K user,and so on)

#### 3. Workload Generation

Change to the directory *`workload_generator`*, and generate workload for the specified start hour of a day (-t) , the duration of the workload (-d), the workload factor to genearte (-f), and the user scale (-u) , by running:

```shell
python3 workload_generator.py -t 21 -d 1800 -f 0.1 -u 100000
```

 A *`workload_u100000_h21_d1800_f0.1.csv`* file will be generated in the directory *`workload_generator`*, which will be driven to system model under test (e.g. *eSearchEngineModel* ).

#### 4. Run experiment by jmeter

Firstly, Copy the generated workload file *`query_workload.csv`*  to jmeter path (*`apache-jmeter-5.1.1/bin/jmeter`*) in jmeter docker.
Then, login to 'jmeter-image' by running:

```shell
sudo docker exec -ti aliesearch-jmeter-image bash
```

And change to the jmeter bin directory, start the pressure test process by running:

```shell
cd apache-jmeter-5.1.1
./bin/jmeter -n -t search_stress.jmx -l result -e -o report
```
### Running Benchmark in batches
1. Change directory to ./run-scripts;
2. Pull or build all the images
3. Start all images with default dataset
   - for running Benchmark in standalone, running:
	```
	./deploy_standalone.sh start
	```
   - for running Benchmark in a cluster, running:
        ```
	./deploy_cluster.sh start
	```
     with specified ip address for each docker in the `multi_iplist_env.sh` file
4. Check if all the images are ready to work by running:
   ```
    curl -H 'Content-Type:application/json;charset=UTF-8' -d'
                {"uid":"798", "page":0, "query":"68"}' ${search_planner_ip}:8080/search
   ```
5. Run experiments in batches by running the following script with specified cases:
    ```
    ./run_batch_cluster.sh
    ```
6. Analyze the results which are gathered in `./run-scripts/jmeter_result`:
   - Change directory to `./run-scripts/jmeter_result` and Preprocess the result logs by running:
     ```
     ./result_stat.sh
     ```
   - The results consist of QPS, response time, latency breakdown of response time , system metrics, and so on.

### Running Benchmark in arm64v8 based platform
The `build.sh` in *`eSearchEngineModel`* directory supports to build images for arm64v8 based platform when running on arm64v8 based platform. And all the experimennts can be run on arm64v8 based platform following the steps descripted above.
