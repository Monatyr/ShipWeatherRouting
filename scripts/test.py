import matplotlib.pyplot as plt
import cartopy.crs as ccrs
import cartopy.feature as cfeature
from matplotlib.patches import Rectangle

# Create a plot with Cartopy
fig, ax = plt.subplots(figsize=(10, 10), subplot_kw={'projection': ccrs.PlateCarree()})

# Set the extent (optional)
ax.set_extent([-130, -60, 20, 60])  # Example for part of the North Atlantic

# Add land and coastlines to the map
land = cfeature.NaturalEarthFeature('physical', 'land', '50m', facecolor='lightgray')
ax.add_feature(land)
ax.coastlines(resolution='50m')

# Define the rectangles (regions) to cover islands
# Example: Covering a region with a rectangle, defined by its bottom-left corner and width/height

# Example rectangle coordinates (lon_min, lat_min, width, height)
rectangles = [
    (-80, 18, 2, 2),  # Caribbean island example
    (-78, 22, 3, 1.5),  # Another island
]

# Plot the rectangles AFTER plotting the coastlines to ensure they are on top
for rect in rectangles:
    ax.add_patch(Rectangle((rect[0], rect[1]), rect[2], rect[3], 
                           linewidth=0, edgecolor='none', facecolor='lightgray', transform=ccrs.PlateCarree()))

# Add other features such as gridlines or borders if needed
ax.gridlines(draw_labels=True)

# Show the plot
plt.show()
