import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import random

img = mpimg.imread('atlantic_ocean.png')

columns, rows = 39, 39
height_ratio = img.shape[0] / rows
width_ratio = img.shape[1] / columns

imgplot = plt.imshow(img)

for i in range(0, rows):
    plt.plot([0, img.shape[1]-1], [i*height_ratio, i*height_ratio], color="black", alpha=0.2, linewidth=1)

for i in range(0, columns):
    plt.plot([i*width_ratio, i*width_ratio], [0, img.shape[0]-1], color="black", alpha=0.2, linewidth=1)



plt.plot(36*width_ratio, 26*height_ratio, "og", markersize=5, color="red")
plt.plot(2*width_ratio, 23*height_ratio, "og", markersize=5, color="red")


max_height = 32
curr_height = 23
hit_max = False

for i in range(3, 36):
    if random.random() > 0.4 and not hit_max:
        curr_height += 1
    if curr_height == max_height:
        hit_max = True
    if hit_max and random.random() > 0.8 and curr_height > 26:
        curr_height -= 1
    plt.plot(i*width_ratio, curr_height*height_ratio, "og", markersize=5, color="red")

for i in range(1, rows):
    for j in range(1, columns):
        plt.plot(i*height_ratio, j*width_ratio, "og", markersize=10)

plt.show()