import numpy as np

def generate_storm(x_center, y_center, r, delta_w, grid_shape):
    """Generate storm wind speed increase for a grid."""
    X, Y = np.meshgrid(np.arange(grid_shape[1]), np.arange(grid_shape[0]))
    distances = (X - x_center)**2 + (Y - y_center)**2
    storm_increase = delta_w * np.exp(-distances / (r**2))
    return storm_increase

# Parameters for the storm: center, radius, wind speed increase
storm_a = generate_storm(40, 30, r=2, delta_w=30, grid_shape=(100, 100))

print(storm_a)

storm_b = generate_storm(60, 10, r=4, delta_w=15, grid_shape=(100, 100))
storm_c = generate_storm(20, 40, r=5, delta_w=25, grid_shape=(100, 100))

# Base wind field
base_wind = np.full((100, 100), 10)

# Add storms to the base wind field
wind_field = base_wind + storm_a + storm_b + storm_c


import matplotlib.pyplot as plt

plt.imshow(wind_field, cmap='coolwarm', interpolation='nearest')
plt.colorbar(label='Wind Speed (knots)')
plt.title('Simulated Wind Field with Fake Storms')
plt.show()
