# Start JMeter

We use [JMeter](https://jmeter.apache.org/) for stress testing. Take the following step to start JMeter:

1. locate to `apache-jmeter-5.1.1` director.
2. replace `HTTPSampler.domain` field in `search_stress.jmx` with `search planner`'s real IP.
3. start testing use the following command
    ```shell
    bin/jmeter -n -t search_stress.jmx -l result -e -o report
    ```
4. when pressure testing end, the report is placed in `report` director.
