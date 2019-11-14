import os
from enum import Enum

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

from config import Config

plt.style.use('ggplot')


class TimeSeriesComponent(Enum):
    DATA = 'data'
    TREND = 'trend'
    DAY = 'day'
    WEEK = 'week'
    REMINDER = 'reminder'


def seasonal_changes(time_series, seconds_per_season, num_seasons, seasonal_str):
    seasonal_change = []
    for second in range(0, seconds_per_season):
        num_in_same_second_of_season = [
            time_series.iloc[second + seconds_per_season * season_order][seasonal_str]
            for season_order in range(0, num_seasons)
        ]
        average = sum(num_in_same_second_of_season) / len(num_in_same_second_of_season)
        seasonal_change.append(average)
    return seasonal_change


def day_seasonal_change(time_series):
    return seasonal_changes(time_series, 86400, 30, TimeSeriesComponent.DAY)


def week_seasonal_change(time_series):
    return seasonal_changes(time_series, 604800, 4, TimeSeriesComponent.WEEK)


def avg_trend(time_series):
    return time_series[TimeSeriesComponent.TREND].mean()


def decompose(time_series_data):
    time_series = transform_data(time_series_data)
    return avg_trend(time_series), day_seasonal_change(time_series), week_seasonal_change(time_series)


def transform_data(time_series_data):
    time_series = pd.read_csv(time_series_data, header=None)
    date_range = pd.date_range(start='20180302000000', end='20180331235959', periods=2592000)
    time_series.index = date_range
    time_series.columns = [TimeSeriesComponent.DATA, TimeSeriesComponent.TREND, TimeSeriesComponent.DAY,
                           TimeSeriesComponent.WEEK, TimeSeriesComponent.REMINDER]
    return time_series


def show_seasonal_change(time_series_data, seconds_per_season, num_seasons, seasonal_str):
    time_series = transform_data(time_series_data)
    for season_order in range(0, num_seasons):
        begin = seconds_per_season * season_order
        end = seconds_per_season * (season_order + 1)
        one_season = time_series.iloc[begin:end][seasonal_str].values
        plt.subplot(5, num_seasons // 5, season_order + 1)
        plt.plot(one_season)
        plt.title(np.mean(one_season))
        print(one_season)
    plt.show()


def load_inter_time_series_model(inter_time_series_decomposition_data):
    inter_time_series_decomposition_data = os.path.join(
        Config.ROOT_DIR, 'Dataset', inter_time_series_decomposition_data)

    trend, day_changes, week_changes = None, None, None
    with open(inter_time_series_decomposition_data) as f:
        for line in f:
            line = line.strip('[]\n')
            line = line.replace(' ', '')
            if trend is None:
                trend = float(line)
            elif day_changes is None:
                day_changes = line.split(',')
                day_changes = list(map(float, day_changes))
            elif week_changes is None:
                week_changes = line.split(',')
                week_changes = list(map(float, week_changes))
    return trend, day_changes, week_changes


def main():
    print(Config.ROOT_DIR)
    trend, day_component, week_component = decompose(
        os.path.join(
            Config.ROOT_DIR, 'Dataset', 'inter_session_time_series.model.tmp'))
    week_component = week_component[86400 * 3:] + week_component[:86400 * 3]
    print(trend)
    print(day_component)
    print(week_component)


if __name__ == '__main__':
    main()
