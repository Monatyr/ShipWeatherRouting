import pygmt
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

# Function to plot a sphere
def plot_sphere(ax, radius=1):
    u = np.linspace(0, 2 * np.pi, 100)
    v = np.linspace(0, np.pi, 100)
    x = radius * np.outer(np.cos(u), np.sin(v))
    y = radius * np.outer(np.sin(u), np.sin(v))
    z = radius * np.outer(np.ones(np.size(u)), np.cos(v))
    ax.plot_surface(x, y, z, color="white", alpha=0.3)

# Convert lat/lon to Cartesian coordinates for the sphere
def latlon_to_xyz(lat, lon, radius=1.0):
    lat = np.radians(lat)
    lon = np.radians(lon)
    x = radius * np.cos(lat) * np.cos(lon)
    y = radius * np.cos(lat) * np.sin(lon)
    z = radius * np.sin(lat)
    return x, y, z

# Plot a great circle route between two points
def plot_great_circle(ax, lat1, lon1, lat2, lon2, n_points=50, radius=1.0):
    res_points = create_great_circle((lat1, lon1), ((lat2, lon2)))

    for lat, lon in res_points:
        x, y, z = latlon_to_xyz(lat, lon, 1)
        ax.plot([x], [y], [z], marker='o', color='r', markersize=3)

    ax.plot([], [], [], color='r', label='Great Circle (shortest distance)')

# Approximate rhumb line by linearly interpolating latitude and longitude
def plot_rhumb_line(ax, lat1, lon1, lat2, lon2, n_points=100, radius=1.0):
    lats = np.linspace(lat1, lat2, n_points)
    lons = np.linspace(lon1, lon2, n_points)
    
    # Convert lat/lon to Cartesian and plot
    for lat, lon in zip(lats, lons):
        x, y, z = latlon_to_xyz(lat, lon, radius)
        ax.plot([x], [y], [z], marker='o', color='g', markersize=3)

    ax.plot([], [], [], color='g', label='Rhumb line (constant azimuth)')

def plot_lat_lon_lines(ax, radius=1.0, num_lines=12):
    # Latitude lines (parallels)
    latitudes = np.linspace(-np.pi / 2, np.pi / 2, num_lines)  # Divides into equal angles
    for lat in latitudes:
        theta = np.linspace(0, 2 * np.pi, 100)
        x = radius * np.cos(theta) * np.cos(lat)
        y = radius * np.sin(theta) * np.cos(lat)
        z = radius * np.sin(lat) * np.ones_like(theta)
        ax.plot(x, y, z, color='black', linestyle='--', linewidth=0.5, alpha=0.5)

    # Longitude lines (meridians)
    longitudes = np.linspace(0, 2 * np.pi, num_lines)
    for lon in longitudes:
        phi = np.linspace(-np.pi / 2, np.pi / 2, 100)
        x = radius * np.cos(lon) * np.cos(phi)
        y = radius * np.sin(lon) * np.cos(phi)
        z = radius * np.sin(phi)
        ax.plot(x, y, z, color='black', linestyle='--', linewidth=0.5, alpha=0.5)


def round_off_rating(number, part=5):
    """Round a number to the closest decimal part of integer."""
    return round(number * part) / part


def create_great_circle(start_coords: tuple, end_coords: tuple, density=10):
    #pygmt needs lat/long flipped
    start_coords = (start_coords[1], start_coords[0])
    end_coords = (end_coords[1], end_coords[0])
    great_circle_points_data = pygmt.project(center=start_coords, endpoint=end_coords, generate=20, unit=True)
    latitudes = great_circle_points_data["s"].tolist()
    longitudes = great_circle_points_data["r"].tolist()
    great_circle_points = list(zip(latitudes, longitudes))
    great_circle_points = list(map(lambda x: (round_off_rating(x[0], density), round_off_rating(x[1], density)), great_circle_points))
    return great_circle_points


def main():
    fig = plt.figure(figsize=(15, 15))
    ax = fig.add_subplot(111, projection='3d')
    start_lat, start_lon = 60, 4
    end_lat, end_lon = 40, 150
    plot_great_circle(ax, start_lat, start_lon, end_lat, end_lon)
    plot_sphere(ax)
    plot_lat_lon_lines(ax)
    plot_rhumb_line(ax, start_lat, start_lon, end_lat, end_lon)

    # Adjust the view angle for better visualization
    ax.view_init(elev=30, azim=-60)
    ax.grid(False)
    ax.set_axis_off()


    # legend = ax.legend(['Great Circle', 'Rhumb Line'], fontsize=18, loc='center', bbox_to_anchor=(0.5, 0.9))
    legend = plt.legend(fontsize=18, loc='center', bbox_to_anchor=(0.5, 0.9))
    plt.setp(legend.get_texts(), fontsize=18)  # Set font size for legend texts

    plt.show()


if __name__ == "__main__":
    main()

