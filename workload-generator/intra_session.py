import logging
import os
import pickle
import random
import warnings
from multiprocessing import Pool

import pandas as pd
from scipy import stats
import matplotlib.pyplot as plt

from config import Config


def get_dist_of_intra_interval():
    burr12_params = pickle.load(open(os.path.join(Config.ROOT_DIR, 'Dataset', 'burr12_params'), 'rb'))
    num_params = len(burr12_params)
    idx = random.randint(0, num_params - 1)
    params = burr12_params[idx]

    return stats.burr12(*params)


def get_dists_of_req_and_query_num(data_of_num_of_requests_and_distinct_queries):
    data_of_num_of_requests_and_distinct_queries = os.path.join(
        Config.ROOT_DIR, 'Dataset', data_of_num_of_requests_and_distinct_queries)

    num_request_query = {}
    num_total_sessions = 0
    with open(data_of_num_of_requests_and_distinct_queries) as f:
        for line in f:
            line = line.strip()
            request_num, query_num, num_sessions = line.split(',')
            request_num = int(request_num)
            query_num = int(query_num)
            num_sessions = int(num_sessions)
            num_total_sessions += num_sessions

            if request_num not in num_request_query:
                num_request_query[request_num] = {}
            num_request_query[request_num][query_num] = num_sessions

    request_num_table = []
    request_num_probability_table = []
    distributions_of_distinct_query_num_in_sessions_with_diff_length = {}
    for request_num, queries in num_request_query.items():
        num_sessions = sum(queries.values())
        request_num_table.append(request_num)
        request_num_probability_table.append(num_sessions / num_total_sessions)

        if request_num <= 100:
            query_num_table = []
            query_num_probability_table = []
            for query_num, total in queries.items():
                query_num_table.append(query_num)
                query_num_probability_table.append(total / num_sessions)
            distributions_of_distinct_query_num_in_sessions_with_diff_length[request_num] = stats.rv_discrete(
                values=(query_num_table, query_num_probability_table))

    distribution_of_request_num = stats.rv_discrete(values=(request_num_table, request_num_probability_table))

    return distribution_of_request_num, distributions_of_distinct_query_num_in_sessions_with_diff_length


def test():
    get_dist_of_intra_interval()
    # get_dists_of_req_and_query_num('session_length_and_distinct_query_num')


if __name__ == '__main__':
    test()
