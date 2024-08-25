import json
import matplotlib.pyplot as plt
import numpy as np
import argparse
import pandas as pd


def normalize(values):
    min_val = np.min(values)
    max_val = np.max(values)
    return (values - min_val) / (max_val - min_val)

parser = argparse.ArgumentParser()
parser.add_argument('--resultFile', type=str)
args = parser.parse_args()

with open('results/averageValues.json') as file:
    data = json.loads(file.read())

time_values, fuel_values, safety_values = [], [], []
step_size = 2

for i in range(0, len(data), step_size):
    el = data[i]
    time_values.append(float(el['TravelTime']))
    fuel_values.append(float(el['FuelUsed']))
    safety_values.append(float(el['Danger']))

time_normalized = normalize(time_values)
fuel_normalized = normalize(fuel_values)
safety_normalized = normalize(safety_values)

window_size = 1
smoothed_time_normalized = pd.Series(time_normalized).rolling(window=window_size).mean()
smoothed_fuel_normalized = pd.Series(fuel_normalized).rolling(window=window_size).mean()
smoothed_safety_normalized = pd.Series(safety_normalized).rolling(window=window_size).mean()

x = np.arange(len(time_values))

x_selected = x[::step_size]
y_f1_selected = smoothed_time_normalized[::step_size]
y_f2_selected = smoothed_fuel_normalized[::step_size]
y_f3_selected = smoothed_safety_normalized[::step_size]


fig = plt.figure(figsize=(25, 10))

plt.plot(x_selected, y_f1_selected, label='Avg Time (Normalized)', color='b')
plt.plot(x_selected, y_f2_selected, label='Avg Fuel (Normalized)', color='g')
plt.plot(x_selected, y_f3_selected, label='Avg Danger (Normalized)', color='r')

plt.xlabel('Iterations')
plt.ylabel('Normalized Values')
plt.title('Normalized Objective Functions')
ticks = np.arange(0, np.max(x_selected) + 5, 500)
plt.xticks(ticks=ticks, labels=list(map(lambda x: x * step_size, np.round(ticks, 2))))
plt.legend()

plt.savefig(f'results/{args.resultFile}', bbox_inches='tight')
# plt.show()