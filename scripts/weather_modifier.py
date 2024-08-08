import json
import random

#  0: 0.0 - 0.72 km/h
#  1: 1.08 - 5.4 km/h
#  2: 5.76 - 11.88 km/h
#  3: 12.24 - 19.44 km/h
#  4: 19.8 - 28.44 km/h
#  5: 28.8 - 38.52 km/h
#  6: 38.88 - 49.68 km/h
#  7: 50.04 - 61.56 km/h
#  8: 61.92 - 74.52 km/h
#  9: 74.88 - 87.84 km/h
# 10: 88.2 - 102.24 km/h
# 11: 102.6 - 117.36 km/h
# 12: ≥ 117.72 km/h


def wind_speed_to_beaufort(wind_speed_ms):
    if wind_speed_ms < 0:
        raise ValueError("Wind speed cannot be negative")
    if wind_speed_ms < 0.3:
        return 0
    elif wind_speed_ms < 1.6:
        return 1
    elif wind_speed_ms < 3.4:
        return 2
    elif wind_speed_ms < 5.5:
        return 3
    elif wind_speed_ms < 8.0:
        return 4
    elif wind_speed_ms < 10.8:
        return 5
    elif wind_speed_ms < 13.9:
        return 6
    elif wind_speed_ms < 17.2:
        return 7
    elif wind_speed_ms < 20.8:
        return 8
    elif wind_speed_ms < 24.5:
        return 9
    elif wind_speed_ms < 28.5:
        return 10
    elif wind_speed_ms < 32.7:
        return 11
    else:
        return 12
    

def get_random_wave_height(beaufort_scale):
    wave_height_ranges = {
        0: (0, 0),
        1: (0.1, 0.2),
        2: (0.2, 0.5),
        3: (0.5, 1.25),
        4: (1.25, 2.5),
        5: (2.5, 4),
        6: (4, 6),
        7: (6, 9),
        8: (9, 12.5),
        9: (12.5, 16),
        10: (16, 20),
        11: (20, 25),
        12: (25, 30)  # For simplicity, we cap the upper range of hurricanes at 30 m.
    }
    
    if beaufort_scale not in wave_height_ranges:
        raise ValueError("Invalid Beaufort scale value. It must be between 0 and 12.")
    
    min_height, max_height = wave_height_ranges[beaufort_scale]
    return random.uniform(min_height, max_height)


def increase_weather_conditions(
        start_point: tuple[float, float],
        end_point: tuple[float, float],
        wind_speed_increase_ms: float,
        input_file: str,
        output_file: str

):
    max_speed = 0
    max_wave_height = 0
    wind_speed_increase_km = 3.6 * wind_speed_increase_ms
    with open(input_file) as file:
        data = json.loads(file.read())
    min_lat, max_lat = min(start_point[0], end_point[0]), max(start_point[0], end_point[0])
    min_lon, max_lon = min(start_point[1], end_point[1]), max(start_point[1], end_point[1])
    for point_k, point_v in data.items():
        curr_lat, curr_lon = list(map(lambda x: float(x), point_k.split(", ")))
        if min_lat <= curr_lat <= max_lat and min_lon <= curr_lon <= max_lon:
            for timestamp_k, timestamp_v in point_v.items():
                if timestamp_k == 'is_water':
                    continue
                timestamp_v['wind_speed_10m'] = timestamp_v['wind_speed_10m'] + wind_speed_increase_km
                if timestamp_v['wave_height'] != None:
                    wind_speed_ms = timestamp_v['wind_speed_10m'] / 3.6
                    max_speed = max(max_speed, wind_speed_ms)
                    wave_height = get_random_wave_height(wind_speed_to_beaufort(wind_speed_ms))
                    max_wave_height = max(max_wave_height, wave_height)
                    timestamp_v['wave_height'] = wave_height
    print("Max wind speed (m/s): ", max_speed)
    print("Max wave height (m):  ", max_wave_height)
    with open(output_file, 'w') as file:
        json.dump(data, file)
    


if __name__ == "__main__":
    increase_weather_conditions(
        (42, -60),
        (45, -20),
        6,
        "src/main/resources/weather-data-final.json",
        'src/main/resources/weather-data-rough-2.json'
    )