import numpy as np
import matplotlib.pyplot as plt
import matplotlib.patches as patches
import string



normal_points = [(0.33870967741935487, 0.6661255411255411), (0.4092741935483871, 0.658008658008658), (0.3467741935483871, 0.5768398268398269), (0.41330645161290325, 0.5551948051948052), (0.530241935483871, 0.5524891774891775), (0.46169354838709675, 0.4199134199134199), (0.6219067052549427, 0.4547097247990106), (0.6447592839490114, 0.39731176561533715), (0.66761186264308, 0.43557707173778615)]
dominated_point = [(0.6028225806451613, 0.373917748917749)]
dominating_points = [(0.49321060418834545, 0.3415081941867657), (0.5449295980749219, 0.30962043908472486), (0.5064410444849116, 0.2681663574520718)]
edge_points = [(0.810483870967742, 0.13041125541125545), (0.8810483870967741, 0.17640692640692643)]



def on_click(e):
    print(e.xdata, e.ydata)
    

fig, ax = plt.subplots()
cid = fig.canvas.mpl_connect('button_press_event', on_click)

letters = list(string.ascii_uppercase)
start_letter_index = 0

for i, point in enumerate(dominating_points):
    plt.annotate(f'{letters[i]}', (point[0], point[1]), textcoords="offset points", xytext=(5,5), ha='center', fontsize=20)
start_letter_index += len(dominating_points)

for i, point in enumerate(dominated_point):
    plt.annotate(f'{letters[i + start_letter_index]}', (point[0], point[1]), textcoords="offset points", xytext=(5,5), ha='center', fontsize=20)
start_letter_index += len(dominated_point)

for i, point in enumerate(edge_points):
    plt.annotate(f'{letters[i + start_letter_index]}', (point[0], point[1]), textcoords="offset points", xytext=(-5,5), ha='center', fontsize=20)

ax.scatter(list(map(lambda x: x[0], normal_points)), list(map(lambda x: x[1], normal_points)))
ax.scatter(list(map(lambda x: x[0], dominated_point)), list(map(lambda x: x[1], dominated_point)), color='red')
ax.scatter(list(map(lambda x: x[0], dominating_points)), list(map(lambda x: x[1], dominating_points)), color='red')
ax.scatter(list(map(lambda x: x[0], edge_points)), list(map(lambda x: x[1], edge_points)), color='red')

circle = patches.Circle(dominated_point[0], 0.1, color='black', fill=False, linestyle='-', linewidth=2, label='Highlighted Area')
plt.gca().add_patch(circle)

circle = patches.Circle(edge_points[0], 0.1, color='black', fill=False, linestyle='-', linewidth=2, label='Highlighted Area')
plt.gca().add_patch(circle)

ax.set_xlabel("F1", fontsize=20)
ax.set_ylabel("F2", fontsize=20)

fig.set_size_inches(12, 12)

plt.show()
