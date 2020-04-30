import os
class Config:
    ROOT_DIR = os.path.abspath(os.path.join(os.path.abspath(__file__), os.pardir))
    @staticmethod
    def get_distributions(version):
        assert version is 'FULL' or version is 'MINI'

        distributions = []
        if version is 'FULL':
            dist_path = os.path.join(Config.ROOT_DIR, 'WorkloadModel', 'scipy_continuous_distributions')
        else:
            dist_path = os.path.join(Config.ROOT_DIR, 'WorkloadModel', 'minimal_scipy_continuous_distributions')

        with open(dist_path) as f:
            for dist in f:
                dist = dist.strip()
                distributions.append(dist)
        return distributions

    @staticmethod
    def get_full_distributions():
        return Config.get_distributions('FULL')

    @staticmethod
    def get_minimal_distribution():
        return Config.get_distributions('MINI')


