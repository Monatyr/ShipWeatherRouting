import json
import matplotlib.pyplot as plt


def read_data(filename: str):
    with open(filename) as file:
        data = json.loads(file.read())
    res = []
    for route in data:
        if route["functionValues"] is not None:
            res.append(route["functionValues"])
    return res


def plot_2d_multiple(ax, xs, ys, label, i):
    ax[i].scatter(xs, ys, marker='o', label=label)
    ax[i].legend()


def plot_2d(ax, xs, ys, label):
    ax.scatter(xs, ys, marker='o', label=label)


if __name__ == "__main__":
    # index_list = list(range(250, 1001, 250))
    # fig, ax = plt.subplots(len(index_list), 1)
    index_list = [250, 5000]
    fig, ax = plt.subplots()

    for i, index in enumerate(index_list):
        values = read_data(f'results/resultSolutions{index}.json')
        fuel = list(map(lambda x: x['FuelUsed'], values))
        travel_time = list(map(lambda x: x['TravelTime'], values))
        safety = list(map(lambda x: x['Danger'], values))
        plot_2d(ax, fuel, travel_time, len(values))
        # plot_2d(ax, fuel, safety, len(values))
        # plot_2d(ax, safety, travel_time, len(values))
    
    # ax.set_title("")
    plt.legend()
    plt.show()