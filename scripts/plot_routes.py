import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import cartopy.crs as ccrs
import cartopy.feature as cfeature
from matplotlib.patches import Rectangle
import argparse
import ast
import json


parser = argparse.ArgumentParser()
parser.add_argument('--resultFile', type=str)
parser.add_argument('--weatherFile',type=str)
parser.add_argument('--routes', type=str)
args = parser.parse_args()

routes = []

if (args.routes):
    with open(args.routes) as file:
        for line in file.readlines():
            routes.append(ast.literal_eval(line))

# Create a figure with a specific size
fig = plt.figure(figsize=(20, 15))

# Define the projection, in this case, a Plate Carrée projection
ax = plt.axes(projection=ccrs.PlateCarree())

# Add natural features for better visualization
ax.add_feature(cfeature.LAND)
ax.add_feature(cfeature.OCEAN)
ax.add_feature(cfeature.COASTLINE)

cid = fig.canvas.mpl_connect('button_press_event', lambda e: print(e.xdata, e.ydata))

#98b4e4
# ax.add_patch(Rectangle((-32, 30), 10, 20, linewidth=0, edgecolor='none', facecolor='gray', transform=ccrs.PlateCarree(), alpha=1))


colors = ['blue', 'red', 'green']
labels = ["Time", "Fuel", "Safety"]

# Plot each route with a different color
for i, route in enumerate(routes):
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
            # if windspeed in m/s above 17 (8 Beaufort Scale)
            if timestamp_value.get('wind_speed_10m') / 3.6 > min_norm:
                dangerous_lats.append(latitude)
                dangerous_longs.append(longitude)
                danger.append(float(timestamp_value.get('wind_speed_10m')) / 3.6)
                break


max_norm = max(danger)
cmap = plt.get_cmap("Oranges")
norm = mcolors.Normalize(vmin=min_norm, vmax=max_norm)

scatter = ax.scatter(dangerous_longs, dangerous_lats, c=danger, cmap='Oranges', marker='.', s=10, alpha=1, norm=norm)

cbar = plt.colorbar(scatter, orientation='horizontal', pad=0.01)
cbar.set_label('Wind Speed (m/s)')

# Add titles and labels
plt.title('Multiple Routes through the Atlantic Ocean')
plt.xlabel('Longitude')
plt.ylabel('Latitude')

# Add a legend
if len(routes) <= 3:
    plt.legend()

# Display the plot
plt.savefig(f'results/{args.resultFile}', bbox_inches='tight')
plt.show()
