import matplotlib.pyplot as plt
import matplotlib.image as mpimg


def read_route(filename: str):
    xs, ys = [], []
    with open(filename, 'r') as file:
        lines = file.readlines()
    for el in lines:
        x, y = el.split(', ')
        xs.append(int(x))
        ys.append(int(y))
    return xs, ys

img = mpimg.imread('heightmap.png')

columns, rows = 60, 200
height_ratio = img.shape[0] / rows
width_ratio = img.shape[1] / columns


if __name__ == "__main__":
    xs, ys = read_route("grid_points1.txt")
    xs = list(map(lambda x: width_ratio * (x), xs))
    xs.reverse()
    ys = list(map(lambda x: height_ratio * (rows - x), ys))
    imgplot = plt.imshow(img)
    plt.plot(xs, ys)
    plt.show()
