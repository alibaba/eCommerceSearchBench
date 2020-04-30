import logging
import re
import os

import pandas as pd
import numpy as np
from scipy import stats
from collections import OrderedDict
import argparse
import random

from intra_session import get_dist_of_intra_interval
from intra_session import get_dists_of_req_and_query_num
from query_generator import QueryGenerator

from config import Config

def parser_add_argument():
    usage="%(prog)s [-h] [-t {time in 24-hour}] [-u {user_scale}]"
    parser = argparse.ArgumentParser(usage=usage)

    parser.add_argument(
        "-t","--start_time",
        dest="start_time",
        default=12,
        help="set the start time when the worload generated belong to"
    )

    parser.add_argument(
        "-f","--factor",
        dest="workload_factor",
        default=0.1,
        help="set the workload factor the worload generated for"
    )

    parser.add_argument(
        "-d","--duration",
        dest="duration",
        default=1800,
        help="set the duration that the worload generated for"
    )

    parser.add_argument(
        "-u","--user",
        dest="user_scale",
        default=10000,
        help="set user scale"
    )
    

    return parser

class WorkloadGenerator:
    def __init__(self, session_length_and_distinct_query_num,session_num_per_second_model):
        self.req_num_dist, self.query_num_dist, self.turningpage_probability_dict = get_dists_of_req_and_query_num(session_length_and_distinct_query_num)
        self.session_num_per_second_model_file = os.path.join(
        Config.ROOT_DIR, 'WorkloadModel', session_num_per_second_model)

        self.interval_dist: stats.rv_continuous = get_dist_of_intra_interval()
        self.query_generator = QueryGenerator()

    def generate_a_session(self):
        req_num = self.req_num_dist.rvs()
        while req_num > 100:
            req_num = self.req_num_dist.rvs()
        query_num = self.query_num_dist[req_num].rvs()

        req_time = np.array([0])
        if req_num > 1:
            intervals = self.generate_intra_interval(req_num - 1)
            req_time = np.append(req_time, intervals)

        req_time = np.cumsum(req_time)
        # avg_query_times = req_num // query_num
        # num_more_queries = req_num % query_num

        search_words = []
        # for _ in range(num_more_queries):
        #     query = self.query_generator.generate_a_query()
        #     # query = None
        #     search_words = search_words + [query] * (avg_query_times + 1)
        # for _ in range(query_num - num_more_queries):
        #     query = self.query_generator.generate_a_query()
        #     # query = None
        #     search_words = search_words + [query] * avg_query_times

        first_key=0
        for k in range(req_num):
            if first_key == 0:
                query = self.query_generator.generate_a_query()
                first_key = 1
            else :
                rand_value = random.random()
                if (random.random() > self.turningpage_probability_dict[req_num]):
                    query = self.query_generator.generate_a_query()
                else :
                    query = last_query
            last_query = query
            search_words = search_words + [query]

        return req_time, search_words

    def generate_intra_interval(self, num_interval):
        intervals = self.interval_dist.rvs(size=num_interval)
        num_gen_intra_interval = 1
        while np.isinf(intervals).any():
            intervals = self.interval_dist.rvs(size=num_interval)
            num_gen_intra_interval += 1
        intervals = np.ceil(intervals)
        intervals = intervals.astype(np.int)
        if num_gen_intra_interval > 1:
            logging.warning('generate number: %d', num_gen_intra_interval)

        return intervals

    def workload_generator (self, start_time, workload_factor, duration, user_scale, workload_file_to_save):
        session_num_per_second=pd.read_csv(self.session_num_per_second_model_file)
        session_num_per_second.set_index("time_dhms", inplace=True)
        with open(workload_file_to_save, 'w') as file:
            for i in range(int(duration)):
                hour = int(i/3600)
                res = int(i%3600)
                minute = int(res/60)
                second = int(res%60)
                sessions_index = 303000000 + hour*10000 + minute*100 + second
                print(sessions_index)
                sessions_of_the_sencond = session_num_per_second.loc[sessions_index,'session_num']
                for j in range(sessions_of_the_sencond):
                    uid = np.random.randint(user_scale)
                    request_time, queries = self.generate_a_session()
                    request_time = request_time.tolist()
                    request_num = len(request_time)
                    queries = [re.sub('[",]', ' ', query) for query in queries]
                    req_session = request_time + [-1] * (100 - request_num)
                    req_session = [str(x) for x in req_session] + queries
                    req_session = [str(request_num)] + req_session
                    req_session = [str(i)] + [str(uid)] + req_session
                    file.write(','.join(req_session))
                    file.write('\n')

if __name__ == '__main__':
    parser = parser_add_argument()
    args = parser.parse_args()
    start_time = args.start_time
    workload_factor = args.workload_factor
    duration = args.duration
    user_scale = args.user_scale
    output_file = "workload_u" + str(user_scale) + '_h' + str(start_time) + '_d' + str(duration) + '_f' + str(workload_factor) + '.csv' 
    print(output_file)
    workload_gen = WorkloadGenerator('session_length_and_distinct_query_num','session_num_per_second.model')
    workload_gen.workload_generator(start_time, workload_factor, duration, user_scale, output_file)
