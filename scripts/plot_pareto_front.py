import matplotlib.pyplot as plt
import argparse
import json

parser = argparse.ArgumentParser()
parser.add_argument('--routes', type=str)
parser.add_argument('--resultFile', type=str)
parser.add_argument('--experiments', action='store_true')
parser.add_argument('--compare', action='store_true')
args = parser.parse_args()

data, comparison_data = [], []

if args.experiments:
    for i in range(1, 3):
        with open(f"results/experiments/singlePath3/emas{i}.json") as file:
            new_data = json.loads(file.read())
            data.extend(new_data)
        with open(f"results/experiments/singlePath3/jmetal{i}.json") as file:
            new_comparison_data = json.loads(file.read())
            comparison_data.extend(new_comparison_data)
    print(len(data))
else:
    with open(args.routes) as file:
        data = json.loads(file.read())
    with open("results/comparisonSolutions.json") as file:
        comparison_data = json.loads(file.read())

time_array, fuel_array, safety_array = [], [], []
comp_time_array, comp_fuel_array, comp_safety_array = [], [], []
dominated_time_array, dominated_fuel_array, dominated_safety_array = [], [], []
dominated_comp_time_array, dominated_comp_fuel_array, dominated_comp_safety_array = [], [], []

for sol in data:
    values = sol["functionValues"]
    time_array.append(values["TravelTime"])
    fuel_array.append(values["FuelUsed"])
    safety_array.append(values["Danger"])

for comp_sol in comparison_data:
    comp_values = comp_sol["functionValues"]
    comp_time_array.append(comp_values["TravelTime"])
    comp_fuel_array.append(comp_values["FuelUsed"])
    comp_safety_array.append(comp_values["Danger"])


# Prune solutions from NSGA-II that are dominated by EMAS
dominated = []
for i in range(len(comp_time_array)):
    for j in range(len(time_array)):
        if time_array[j] < comp_time_array[i] and fuel_array[j] < comp_fuel_array[i] and safety_array[j] < comp_safety_array[i]:
            dominated_comp_time_array.append(comp_time_array[i])
            dominated_comp_fuel_array.append(comp_fuel_array[i])
            dominated_comp_safety_array.append(comp_safety_array[i])
            dominated.insert(0, i)
            break

for index in dominated:
    comp_time_array.pop(index)
    comp_fuel_array.pop(index)
    comp_safety_array.pop(index)


# Change color of solutions from EMAS that are dominated by NSGA-II
comp_dominated = []
for i in range(len(time_array)):
    for j in range(len(comp_time_array)):
        if comp_time_array[j] < time_array[i] and comp_fuel_array[j] < fuel_array[i] and comp_safety_array[j] < safety_array[i]:
            comp_dominated.insert(0, i)
            dominated_time_array.append(time_array[i])
            dominated_fuel_array.append(fuel_array[i])
            dominated_safety_array.append(safety_array[i])
            break

if args.compare:
    for index in comp_dominated:
        time_array.pop(index)
        fuel_array.pop(index)
        safety_array.pop(index)


# Create a 3D scatter plot to visualize the Pareto front
fig = plt.figure()
fig.set_size_inches(20, 15)
ax = fig.add_subplot(111, projection='3d')

# Plot the Pareto front
ax.scatter(time_array, fuel_array, safety_array, c='r', marker='o', label=f'EMAS: {len(time_array)}')
if args.compare:
    ax.scatter(comp_time_array, comp_fuel_array, comp_safety_array, c='#7e1682', marker='o', label=f'NSGA-II: {len(comp_time_array)}')
    ax.scatter(dominated_time_array, dominated_fuel_array, dominated_safety_array, c='#f79a05', marker='o', label=f'Dominated EMAS: {len(dominated_time_array)}')
    ax.scatter(dominated_comp_time_array, dominated_comp_fuel_array, dominated_comp_safety_array, c='#57acde', label=f'Dominated NSGA-II: {len(dominated_comp_time_array)}')

# Set axis labels
ax.set_xlabel('Travel time', fontsize=13, labelpad=20)
ax.set_ylabel('Fuel used', fontsize=13, labelpad=20)
ax.set_zlabel('Danger', fontsize=13, labelpad=20)

ax.tick_params(axis='both', which='major', labelsize=12)  # Set size for major ticks
ax.tick_params(axis='both', which='minor', labelsize=10)  # Set size for minor ticks (if you have them)


# Set the title
ax.set_title('Pareto Front for 3-Objective Optimization', fontsize=17, pad=0)
legend = ax.legend(loc='upper center', bbox_to_anchor=(1.0, 0.7), ncol=1, fontsize=14)

plt.savefig(f'results/{args.resultFile}', bbox_inches='tight')
plt.show()
