import json

with open('src/main/resources/weather-data-final.json') as file:
    data = json.loads(file.read())

max_wind = 0
wind_sum = 0
total_len = 0

for point_k, point_v in data.items():
    for time_k, time_v in point_v.items():
        if time_k == "is_water":
            continue
        wind_sum += time_v['wind_speed_10m']
        total_len += 1
        if time_v['wind_speed_10m'] > max_wind:
            max_wind = time_v['wind_speed_10m']

print(wind_sum / 3.6 / total_len)
print(max_wind / 3.6)