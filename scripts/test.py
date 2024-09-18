import matplotlib.pyplot as plt
import cartopy.crs as ccrs
import pygmt
from rhumb_line import RhumbLineCalc

def flatten_nested_tuples(nested_tuples):
    flattened_list = []
    for item in nested_tuples:
        if isinstance(item, tuple):
            flattened_list.extend(flatten_nested_tuples(item))
        else:
            flattened_list.append(item)
    return flattened_list


def create_coords(points):
    res = []
    for i in range(0, len(points)-1, 2):
        res.append((points[i], points[i+1]))
    return res


def round_off_rating(number, part=4):
    """Round a number to the closest decimal part of integer."""
    return round(number * part) / part


def create_rhumb_line(start_coords, end_coords):
    rhumb = RhumbLineCalc()
    mid_points = rhumb.loxodromic_power_interpolation(start_coords, end_coords, 7)
    mid_points = flatten_nested_tuples(mid_points)
    mid_points = create_coords(mid_points)
    rhumb_points = [start_coords]
    rhumb_points.extend(mid_points)
    rhumb_points.append(end_coords)
    return rhumb_points

def create_great_circle(start_coords: tuple, end_coords: tuple, density=2):
    #pygmt needs lat/long flipped
    start_coords = (start_coords[1], start_coords[0])
    end_coords = (end_coords[1], end_coords[0])
    great_circle_points_data = pygmt.project(center=start_coords, endpoint=end_coords, generate=10, unit=False)
    latitudes = great_circle_points_data["s"].tolist()
    longitudes = great_circle_points_data["r"].tolist()
    great_circle_points = list(zip(latitudes, longitudes))
    return great_circle_points


s_point = (-150, 50)
e_point = (90, 20)

gcr_points = create_great_circle(s_point, e_point)
gcr_ys, gcr_xs = zip(*gcr_points)

print(gcr_points)

# Create a figure and axis with Cartopy's PlateCarree projection
fig = plt.figure(figsize=(15, 10))
ax = plt.axes(projection=ccrs.Mollweide())

# Extend the image with invisible points
# ax.scatter([-20, 17], [-3, 20], alpha=1)
ax.scatter(*s_point, c='red')
ax.scatter(*e_point, c='red')
ax.plot([s_point[0], e_point[0]], [s_point[1], e_point[1]], linestyle='--', linewidth=2)
# ax.plot(gcr_xs, gcr_ys)

# Add gridlines and labels
gl = ax.gridlines(draw_labels=True, linestyle='--', color='gray', alpha=0.5)
gl.xlabels_top = False
gl.ylabels_right = False
gl.xlocator = plt.MaxNLocator(10)  # Number of latitude labels
gl.ylocator = plt.MaxNLocator(10)  # Number of longitude labels

# Set labels for the x and y axes
ax.set_xlabel('Longitude')
ax.set_ylabel('Latitude')

plt.show()
