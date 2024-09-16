import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import cartopy.crs as ccrs
import cartopy.feature as cfeature
from matplotlib.patches import Rectangle
import argparse
import ast
import json
import random


parser = argparse.ArgumentParser()
parser.add_argument('--resultFile', type=str)
parser.add_argument('--weatherFile',type=str)
parser.add_argument('--routes', type=str)
parser.add_argument('--top', action='store_true')
args = parser.parse_args()

routes = []
data = []

if (args.routes):
    if ".txt" in args.routes:
        with open(args.routes) as file:
            for line in file.readlines():
                routes.append(ast.literal_eval(line))
    else: # must be passed as a directory    
        for i in range(3, 4):
            with open(args.routes + f"/emas{i}.json") as file:
                new_data = json.loads(file.read())
                data.extend(new_data)


time_sorted_data = sorted(data, key=lambda x: x["functionValues"]["TravelTime"], reverse=False)
fuel_sorted_data = sorted(data, key=lambda x: x["functionValues"]["FuelUsed"], reverse=False)
danger_sorted_data = sorted(data, key=lambda x: x["functionValues"]["Danger"], reverse=False)

show_number = 20

if not '.txt' in args.routes:
    if args.top:
        data = [time_sorted_data[0], fuel_sorted_data[0], danger_sorted_data[0]]
    else:
        random.shuffle(data)
        data = data[:show_number]
    for el in data:
        new_route = []
        for point in el['routePoints']:
            coords = point['coordinates']
            new_route.append(tuple(coords.values()))
        routes.append(new_route)

# Create a figure with a specific size
fig = plt.figure(figsize=(20, 10))

# Define the projection, in this case, a Plate Carrée projection
ax = plt.axes(projection=ccrs.PlateCarree())

# Add natural features for better visualization
ax.add_feature(cfeature.LAND)
ax.add_feature(cfeature.OCEAN)
ax.add_feature(cfeature.COASTLINE)

distance = float('inf')

def on_click(event):
    print(event.xdata, event.ydata)


cid = fig.canvas.mpl_connect('button_press_event', on_click)

#98b4e4
# ax.add_patch(Rectangle((-32, 30), 10, 20, linewidth=0, edgecolor='none', facecolor='gray', transform=ccrs.PlateCarree(), alpha=1))


colors = ['blue', 'red', 'green']
labels = ["Time", "Fuel", "Safety"]

# 38.864, -28.656
# 38.864, -27.629
# 38.043, -25.566
# 37.645, -26.601

# Plot each route with a different color
for i, route in enumerate(routes):
    # if i == 1:
    #     route[41] = (38.864, -28.656)
    #     route[42] = (38.864, -27.629)
    # if i == 2:
    #     route[43] = (37.645, -26.601)
    #     route[44] = (38.043, -25.566)
    lats, lons = zip(*route)
    color = colors[i % len(colors)]  # Cycle through the list of colors
    ax.plot(lons, lats, linewidth=1, marker='o', transform=ccrs.PlateCarree(), label=f'{labels[i % len(labels)]} route')

# Extend the image with invisible points
ax.plot([-70, -10], [50, 30], alpha=0)

min_norm = 10

dangerous_lats, dangerous_longs, danger = [], [], []
with open(args.weatherFile) as file:
    weather_data = json.loads(file.read())
    for coords, coords_value in weather_data.items():
        if coords_value["is_water"] == False:
            continue
        latitude, longitude = [float(x) for x in coords.split(", ")]
        for timestamp, timestamp_value in coords_value.items():
            if timestamp == "is_water":
                continue
            if timestamp_value.get('wind_speed_10m') / 3.6 > min_norm:
                dangerous_lats.append(latitude)
                dangerous_longs.append(longitude)
                danger.append(float(timestamp_value.get('wind_speed_10m')) / 3.6)
                break


max_norm = max(danger)
cmap = plt.get_cmap("Oranges")
norm = mcolors.Normalize(vmin=min_norm, vmax=max_norm)

scatter = ax.scatter(dangerous_longs, dangerous_lats, c=danger, cmap='Oranges', marker='.', s=10, alpha=1, norm=norm)

cbar = plt.colorbar(scatter, orientation='horizontal', pad=0.01, shrink=1, aspect=50)
cbar.set_label('Wind Speed (m/s)', fontsize=15)
cbar.ax.tick_params(labelsize=15)  # Set the font size for the colorbar ticks


# Add titles and labels
plt.title("Top route per objective function" if args.top else f"{show_number} resulting non-dominated routes", fontsize=20)
plt.xlabel('Longitude')
plt.ylabel('Latitude')

# Add a legend
if len(routes) <= 3:
    plt.legend(fontsize=15)

# Display the plot
plt.savefig(f'results/{args.resultFile}', bbox_inches='tight')
plt.show()
