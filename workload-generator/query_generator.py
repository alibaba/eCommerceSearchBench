import os
from collections import Counter

import numpy as np
import pandas as pd

from config import Config


class QueryGenerator:
    def __init__(self):
        self.query_dictionary: pd.DataFrame = pd.read_pickle(
            os.path.join(Config.ROOT_DIR, 'Dataset', '1m_sampling_queries.pkl'))
        assert isinstance(self.query_dictionary, pd.DataFrame)

    def generate_a_query(self):
        idx = np.random.randint(self.query_dictionary.size)
        return self.query_dictionary.iat[idx, 0]


def test():
    queries = []
    query_generator = QueryGenerator()
    for _ in range(1000000):
        query = query_generator.generate_a_query()
        queries.append(query)
    counter = Counter(queries)
    print(counter)


if __name__ == '__main__':
    test()
