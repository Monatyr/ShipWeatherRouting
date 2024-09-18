from mpl_toolkits.mplot3d.art3d import Poly3DCollection
import numpy as np
import matplotlib.pyplot as plt
import json
import pygmo as pg


def read_data(filename: str):
    keys = {"TravelTime": 0, "FuelUsed": 1, "Danger": 2}
    with open(filename) as file:
        data = json.loads(file.read())
    mins = [float('inf') for _ in range(3)]
    maxs = [float('-inf') for _ in range(3)]
    res = []

    for el in data:
        temp_res = [0 for _ in range(3)]
        values = el["functionValues"]
        for k in keys:
            temp_res[keys.get(k)] = values.get(k)
            if values.get(k) > maxs[keys.get(k)]:
                maxs[keys.get(k)] = values.get(k)
            if values.get(k) < mins[keys.get(k)]:
                mins[keys.get(k)] = values.get(k)
        res.append(temp_res)
    res = np.array(res)
    for i in range(3):
        res[:,i] = (res[:,i]-np.min(res[:,i]))/(np.max(res[:,i])-np.min(res[:,i]))
    return res


# Function to create a cuboid between a Pareto front point and the reference point
def create_cuboid(point, ref_point):
    """ Create a cuboid between a Pareto front point and the reference point. """
    x = [point[0], ref_point[0], ref_point[0], point[0], point[0], ref_point[0], ref_point[0], point[0]]
    y = [point[1], point[1], ref_point[1], ref_point[1], point[1], point[1], ref_point[1], ref_point[1]]
    z = [point[2], point[2], point[2], point[2], ref_point[2], ref_point[2], ref_point[2], ref_point[2]]
    
    # Create faces of the cuboid
    vertices = [[x[0], y[0], z[0]], [x[1], y[1], z[1]], [x[2], y[2], z[2]], [x[3], y[3], z[3]],
                [x[4], y[4], z[4]], [x[5], y[5], z[5]], [x[6], y[6], z[6]], [x[7], y[7], z[7]]]
    
    faces = [[vertices[j] for j in [0, 1, 5, 4]],  # front face
             [vertices[j] for j in [3, 2, 6, 7]],  # back face
             [vertices[j] for j in [0, 3, 7, 4]],  # left face
             [vertices[j] for j in [1, 2, 6, 5]],  # right face
             [vertices[j] for j in [0, 1, 2, 3]],  # top face
             [vertices[j] for j in [4, 5, 6, 7]]]  # bottom face
    
    return faces


def plot_data(pareto_front, reference_point):
    # Create 3D plot
    fig = plt.figure(figsize=(10, 8))
    ax = fig.add_subplot(111, projection='3d')

    # Plot the Pareto front points
    ax.scatter(pareto_front[:, 0], pareto_front[:, 1], pareto_front[:, 2], color='blue', label='Pareto Front')

    # Plot the reference point
    ax.scatter(reference_point[0], reference_point[1], reference_point[2], color='red', label='Reference Point', s=100)

    # Plot cuboids (dominated regions)
    for point in pareto_front:
        cuboid_faces = create_cuboid(point, reference_point)
        cuboid = Poly3DCollection(cuboid_faces, facecolors='lightblue', edgecolors='gray', linewidths=0.5, alpha=0.5)
        ax.add_collection3d(cuboid)

    # Customize plot
    ax.set_xlabel('Objective 1')
    ax.set_ylabel('Objective 2')
    ax.set_zlabel('Objective 3')
    ax.set_title('3D Pareto Front with Hypervolume Visualization')
    ax.legend()


if __name__ == "__main__":
    for i in range(1, 4):
        for filename in ['emas', 'jmetal']:
            pareto_front = read_data(f'results/experiments/calmWater/{filename}{i}.json')
            reference_point = [1.1, 1.1, 1.1]
            
            hv = pg.hypervolume(pareto_front)
            hypervolume_value = hv.compute(reference_point)

            print(f'Hypervolume for {filename}{i}: {hypervolume_value}')
        print()

    # plot_data(pareto_front, reference_point)
    # plt.show()