import logging
import re

import numpy as np
from scipy import stats
from collections import OrderedDict
import argparse

from intra_session import get_dist_of_intra_interval
from intra_session import get_dists_of_req_and_query_num
from query_generator import QueryGenerator

load_ratio_to_12=OrderedDict(
    [(0,0.749925885),
     (1,0.390990445),
    (2,0.216821723),
    (3,0.139521005),
    (4,0.108642635),
    (5,0.110131302),
    (6,0.205616285),
    (7,0.373913643),
    (8,0.574695686),
    (9,0.785277366),
    (10,0.96596103),
    (11,0.980837242),
    (12,1),
    (13,1.12280701),
    (14,1.139861674),
    (15,1.168604279),
    (16,1.122065757),
    (17,0.991905537),
    (18,0.928222123),
    (19,1.091556134),
    (20,1.362405431),
    (21,1.642100582),
    (22,1.695194395),
    (23,1.283931648)
    ]
)

def parser_add_argument():
    usage="%(prog)s [-h] [-t {time in 24-hour}] [-u {user_scale}]"
    parser = argparse.ArgumentParser(usage=usage)

    parser.add_argument(
        "-t","--time",
        dest="time",
        default=12,
        help="set the time when the worload generated belong to"
    )

    parser.add_argument(
        "-u","--user_scale",
        dest="user_scale",
        default=10000,
        help="set user scale"
    )
    
    return parser

class WorkloadGenerator:
    def __init__(self, session_length_and_distinct_query_num):
        self.req_num_dist, self.query_num_dist = get_dists_of_req_and_query_num(session_length_and_distinct_query_num)
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
        avg_query_times = req_num // query_num
        num_more_queries = req_num % query_num

        search_words = []
        for _ in range(num_more_queries):
            query = self.query_generator.generate_a_query()
            # query = None
            search_words = search_words + [query] * (avg_query_times + 1)
        for _ in range(query_num - num_more_queries):
            query = self.query_generator.generate_a_query()
            # query = None
            search_words = search_words + [query] * avg_query_times

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


if __name__ == '__main__':
    parser = parser_add_argument()
    args = parser.parse_args()
    time = args.time
    user_scale=args.user_scale
    session_num_gen = load_ratio_to_12[time] * 60000000 
    workload_generator = WorkloadGenerator('session_length_and_distinct_query_num')
    with open('query_workload.csv', 'w') as file:
        for i in range(session_num_gen):
            uid = np.random.randint(user_scale)
            request_time, queries = workload_generator.generate_a_session()
            request_num = len(request_time)
            queries = list(set(queries))
            if len(request_time) < 10 and len(queries) < 3:
                queries = [re.sub('[",]', ' ', query) for query in queries]
                request_time = request_time.tolist()
                req_session = request_time + [-1] * 8
                req_session = [str(x) for x in req_session[1:9]]
                req_session = [str(request_num)] + req_session
                if len(queries) == 1:
                    # req_session += ['1']
                    req_session += queries * 2
                else:
                    # req_session += ['2']
                    req_session += queries
                req_session = [str(uid)] + req_session
                file.write(','.join(req_session))
                file.write('\n')
