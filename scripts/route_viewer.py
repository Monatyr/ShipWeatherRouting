import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import random
import copy


def read_route(filename: str, height_ratio, width_ratio):
    xs, ys = [], []
    with open(filename, 'r') as file:
        lines = file.readlines()
    for line in lines:
        x, y = list(map(lambda x: int(x), line.split()))
        xs.append(x * width_ratio)
        ys.append(y * height_ratio)
    return xs, ys

img = mpimg.imread('atlantic_ocean.png')

columns, rows = 100, 200
height_ratio = img.shape[0] / rows
width_ratio = img.shape[1] / columns

xs_1, ys_1 = read_route('route1.txt', height_ratio, width_ratio)
xs_2, ys_2 = read_route('route2.txt', height_ratio, width_ratio)
xs_3, ys_3 = [], []

flag = True
for i in range(len(xs_1)):
    if flag:
        xs_3.append(xs_1[i])
        ys_3.append(ys_1[i])
    else:
        xs_3.append(xs_2[i])
        ys_3.append(ys_2[i])
    if ys_1[i] == ys_2[i]:
        flag = not flag

imgplot = plt.imshow(img)

# -----------------------------------------------------------------------

'''Plot crossover operation'''
#plt.plot(xs_1, ys_1, marker='o', linestyle='-', c='#069c18', label='Parent 1')
#plt.plot(xs_2, ys_2, marker='o', linestyle='-', c='#000075', label='Parent 2')
#plt.plot(xs_3, ys_3, marker='o', linestyle='-', c='#bf2a2a', label='Result')

'''Plot mutation operation'''
mutated_xs, mutated_ys = copy.deepcopy(xs_3), copy.deepcopy(ys_3)
mutated_ys[41] -= 1 * height_ratio
mutated_ys[42] += 1 * height_ratio
mutated_ys[46] -= 2 * height_ratio
mutated_ys[50] -= 2 * height_ratio
mutated_ys[54] -= 1 * height_ratio
mutated_ys[57] += 1 * height_ratio
plt.plot(xs_3, ys_3, marker='o', linestyle='-', c='#f98c0c', alpha=1, label='Original solution')
plt.plot(mutated_xs, mutated_ys, marker='o', linestyle='-', c='#bf2a2a', label='Mutation result')

# ----------------------------------------------------------------------

plt.ylim(275, 350)
plt.xlim(700, 1200)
plt.legend(loc=1, prop={'size': 30})

#for i, el in enumerate(zip(xs_3, ys_3)):
#    plt.annotate(i, el)

for i in range(0, rows):
    plt.plot([0, img.shape[1]-1], [i*height_ratio, i*height_ratio], color="black", alpha=0.2, linewidth=1)

for i in range(0, columns):
    plt.plot([i*width_ratio, i*width_ratio], [0, img.shape[0]-1], color="black", alpha=0.2, linewidth=1)

plt.plot()

plt.show()
