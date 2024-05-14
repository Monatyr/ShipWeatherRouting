import json
import pygmt
from rhumb_line import RhumbLineCalc

def flatten(S):
    print(S)
    if S == []:
        return S
    if isinstance(S[0], tuple):
        return flatten(S[0]) + flatten(S[1:])
    return S[:1] + flatten(S[1:])


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


def round_off_rating(number, part=2):
    """Round a number to the closest decimal part of integer."""
    return round(number * part) / part


# Decorator to save result in file
def write_to_file(filename):
    def decorator(func):
        def wrapper(*args, **kwargs):
            result = func(*args, **kwargs)
            result = list(map(lambda x: f"{x[0]}, {x[1]}", result))
            with open(filename, 'w') as file:
                for el in result:
                    file.write(f'{el}\n')
            return result
        return wrapper
    return decorator


@write_to_file('src/main/resources/initial-routes/rhumb_line_route.txt')
def create_rhumb_line(start_coords, end_coords, density=2):
    rhumb = RhumbLineCalc()
    mid_points = rhumb.loxodromic_power_interpolation(start_coords, end_coords, 31)
    mid_points = flatten_nested_tuples(mid_points)
    mid_points = create_coords(mid_points)
    rhumb_points = [start_coords]
    rhumb_points.extend(mid_points)
    rhumb_points.append(end_coords)
    rhumb_points = list(map(lambda x: (round_off_rating(x[0], density), round_off_rating(x[1], density)), rhumb_points))
    return rhumb_points


@write_to_file('src/main/resources/initial-routes/great_circle_route.txt')
def create_great_circle(start_coords: tuple, end_coords: tuple, density=2):
    #pygmt needs lat/long flipped
    start_coords = (start_coords[1], start_coords[0])
    end_coords = (end_coords[1], end_coords[0])
    great_circle_points_data = pygmt.project(center=start_coords, endpoint=end_coords, generate=100, unit=True)
    latitudes = great_circle_points_data["s"].tolist()
    longitudes = great_circle_points_data["r"].tolist()
    great_circle_points = list(zip(latitudes, longitudes))
    great_circle_points = list(map(lambda x: (round_off_rating(x[0], density), round_off_rating(x[1], density)), great_circle_points))
    return great_circle_points


def read_coords_from_file(filename):
    with open(filename, 'r') as file:
        data = json.load(file)
        start_data = data['map']['startPos']
        end_data = data['map']['endPos']
        start_coords = (start_data['latitude'], start_data['longitude'])
        end_coords = (end_data['latitude'], end_data['longitude'])

        return start_coords, end_coords



if __name__ == "__main__":
    start_coords, end_coords = read_coords_from_file('src/main/resources/config.json')
    density = 10
    rhumb_points = create_rhumb_line(start_coords, end_coords, density)
    great_circle_points = create_great_circle(start_coords, end_coords, density)

    # Print results in the [LONGITUDE/LATITUDE] format for visualisation
    rhumb_points = list(map(lambda x: f'{x.split(",")[1]}, {x.split(",")[0]}', rhumb_points))
    great_circle_points = list(map(lambda x: f'{x.split(",")[1]}, {x.split(",")[0]}', great_circle_points))

    # print('RHUMB LINE:\n')
    for el in rhumb_points:
        print(el)
    # print('\n\nGREAT CIRCLE ROUTE:\n')
    for el in great_circle_points:
        print(el)
