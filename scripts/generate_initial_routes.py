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



if __name__ == "__main__":

    # data
    # start_coords = (-74, 40)
    # end_coords = (-9.1, 38.7)
    start_coords = (-74, 40)
    end_coords = (-9, 38.5)
    
    # rhumb line
    rhumb = RhumbLineCalc()
    mid_points = rhumb.loxodromic_power_interpolation(start_coords, end_coords, 31)
    mid_points = flatten_nested_tuples(mid_points)
    mid_points = create_coords(mid_points)
    rhumb_points = [start_coords]
    rhumb_points.extend(mid_points)
    rhumb_points.append(end_coords)

    # great circle route
    great_circle_points_data = pygmt.project(center=start_coords, endpoint=end_coords, generate=100, unit=True)
    latitudes = great_circle_points_data["r"].tolist()
    longitudes = great_circle_points_data["s"].tolist()
    great_circle_points = list(zip(latitudes, longitudes))


    # rounding to nearest half
    rhumb_points = list(map(lambda x: (round_off_rating(x[0]), round_off_rating(x[1])), rhumb_points))
    great_circle_points = list(map(lambda x: (round_off_rating(x[0]), round_off_rating(x[1])), great_circle_points))

    # results
    for el in rhumb_points:
        print(f"{el[0]}, {el[1]}")

    for el in great_circle_points:
        print(f"{el[0]}, {el[1]}")
