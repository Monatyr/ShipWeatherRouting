import matplotlib.pyplot as plt
import cartopy.crs as ccrs
import cartopy.feature as cfeature
import sys
import argparse
import ast

parser = argparse.ArgumentParser()
parser.add_argument('routes', type=str)
args = parser.parse_args()

routes = ast.literal_eval(args.routes)

# Create a figure with a specific size
fig = plt.figure(figsize=(20, 15))

# Define the projection, in this case, a Plate Carrée projection
ax = plt.axes(projection=ccrs.PlateCarree())

# Add natural features for better visualization
ax.add_feature(cfeature.LAND)
ax.add_feature(cfeature.OCEAN)
ax.add_feature(cfeature.COASTLINE)

colors = ['blue', 'red', 'green']
labels = ["Time", "Fuel", "Safety"]

# Plot each route with a different color
for i, route in enumerate(routes):
    lats, lons = zip(*route)
    color = colors[i % len(colors)]  # Cycle through the list of colors
    ax.plot(lons, lats, color=color, linewidth=2, marker='o', transform=ccrs.PlateCarree(), label=f'{labels[i]} route')

ax.plot([-70, -10], [50, 30], alpha=0)

# Add titles and labels
plt.title('Multiple Routes through the Atlantic Ocean')
plt.xlabel('Longitude')
plt.ylabel('Latitude')

# Add a legend
plt.legend()

# Display the plot
plt.savefig('results/top3_routes.png')
# plt.show()
