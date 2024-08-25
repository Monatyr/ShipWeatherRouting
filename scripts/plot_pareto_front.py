import matplotlib.pyplot as plt
import argparse
import json

parser = argparse.ArgumentParser()
parser.add_argument('--routes', type=str)
parser.add_argument('--resultFile', type=str)
args = parser.parse_args()

with open(args.routes) as file:
    data = json.loads(file.read())

time_array, fuel_array, safety_array = [], [], []

for sol in data:
    values = sol["functionValues"]
    time_array.append(values["TravelTime"])
    fuel_array.append(values["FuelUsed"])
    safety_array.append(values["Danger"])

# Create a 3D scatter plot to visualize the Pareto front
fig = plt.figure()
fig.set_size_inches(15, 15)
ax = fig.add_subplot(111, projection='3d')

# Plot the Pareto front
sc = ax.scatter(time_array, fuel_array, safety_array, c='r', marker='o')

# Set axis labels
ax.set_xlabel('Travel time')
ax.set_ylabel('Fuel used')
ax.set_zlabel('Danger')

# Set the title
ax.set_title('Pareto Front for 3-Objective Optimization')

plt.savefig(f'results/{args.resultFile}', bbox_inches='tight')
# plt.show()
