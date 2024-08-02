import requests
import json

GRID_FILE = 'src/main/resources/map-grid.txt'
URL = "https://is-on-water.balbona.me/api/v1/get"


def read_grid(filename: str):
    '''Read the grid corrdinates based on the generated file (at the start of each simulation)'''
    with open(filename, 'r') as file:
        lines = file.readlines()
    lines = list(map(lambda x: (float(x.split(', ')[0]), float(x.split(', ')[1])), lines))
    return lines


def check_if_water(lat: float, lon: float):
    resp = requests.get(f"{URL}/{lat}/{lon}")
    if resp.status_code != 200:
        print("ERROR: ", resp.status_code, lat, lon)
    data = json.loads(resp.content)
    return data["isWater"]


def check_grid(points: list[tuple[float, float]], result_filename: str):
    result = dict()
    file = open(result_filename, 'w')
    for i, point in enumerate(points):
        if i % 10 == 0:
            print(i)
        (latitude, longitude) = point
        result[f"{latitude}, {longitude}"] = check_if_water(latitude, longitude)
    json.dump(result, file)
    file.close()


def merge_dicts(weather_data_dict, water_dict: dict):
    for k, v in water_dict.items():
        weather_data_dict[k]["is_water"] = v
    return weather_data_dict


if __name__ == "__main__":
    points = read_grid(GRID_FILE)

    # 1. Check if grid points are water (API calls!)
    # check_grid(points, 'src/main/resources/is_water.json')

    # 2. Merge dicts and save them to a file
    file1 = open('src/main/resources/weather-data-full.json')
    file2 = open('src/main/resources/is_water.json')
    str1 = file1.read()
    str2 = file2.read()
    file1.close()
    file2.close()

    d1 = json.loads(str1)
    d2 = json.loads(str2)

    resulting_dict = merge_dicts(d1, d2)

    res_file = open('src/main/resources/weather-data-final.json', 'w')
    json.dump(resulting_dict, res_file)
    res_file.close()

