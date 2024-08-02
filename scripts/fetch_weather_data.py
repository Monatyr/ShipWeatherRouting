import time
import requests
import json

"""
More than 10 weather variables or more than 7 days are considered more than a single API call.
E.g. 15 variables would be worth 1.5 API calls. 4 weeks of data would be 3.0 API calls.

The limit for API calls is:
- 10000 daily
- 5000 per hour
- 600 per minute
"""

GRID_FILE = 'src/main/resources/map-grid.txt'

BASE_URL = 'https://api.open-meteo.com/v1/forecast'
MARINE_URL = 'https://marine-api.open-meteo.com/v1/marine'
BASE_VARIABLES = ["wind_speed_10m", "wind_direction_10m"]
MARINE_VARIABLES = ["wave_height","ocean_current_velocity","ocean_current_direction"]

MINUTE_LIMIT = 500
WAIT_TIME = 65


def read_grid(filename: str):
	'''Read the grid corrdinates based on the generated file (at the start of each simulation)'''
	with open(filename, 'r') as file:
		lines = file.readlines()
	lines = list(map(lambda x: (float(x.split(', ')[0]), float(x.split(', ')[1])), lines))
	return lines


def fetch_point_data(
		url: str,
		latitude: float,
		longitude: float,
		start_date: str,
		end_date: str,
		variables: list[str]
):
	variables = ','.join(variables)
	url = url + f'?latitude={latitude}&longitude={longitude}&hourly={variables}&start_date={start_date}&end_date={end_date}'
	response = requests.get(url)
	if response.status_code != 200:
		print(f'Failed to fetch data for: ({latitude}, {longitude})')
		return
	resp_dict = json.loads(response.content)
	return resp_dict


def fetch_grid(
		url: str,
		start_date: str,
		end_date: str,
		variables: list[str],
		points: list[tuple[float, float]],
		result_filename: str
):
	'''Fetch the data for the entire grid from the given url endpoints (forecast or marine)'''
	result = dict()
	file = open(result_filename, 'w')
	for i, point in enumerate(points):
		if i != 0 and i % MINUTE_LIMIT == 0:
			print(f'\nIteration: {i}\n')
			time.sleep(WAIT_TIME)
		(latitude, longitude) = point

		point_result = fetch_point_data(url, latitude, longitude, start_date, end_date, variables)
		data = point_result['hourly']
		timestamps = data['time']
		
		result[f'{latitude}, {longitude}'] = dict()

		for j, timestamp in enumerate(timestamps):
			result[f'{latitude}, {longitude}'][timestamp] = dict()
			for variable in variables:
				result[f'{latitude}, {longitude}'][timestamp][variable] = data[variable][j]
	json.dump(result, file)
	file.close()


def merge_dicts(dict1: dict, dict2: dict):
	'''Merge data from the /foreast and /marine endpoints'''
	for point, point_data in dict2.items():
		if point in dict1.keys():
			for timestamp, timestamp_data in point_data.items():
				if timestamp in dict1[point].keys():
					for variable, value in timestamp_data.items():
						dict1[point][timestamp][variable] = value
				else:
					dict1[point][timestamp] = timestamp_data
		else:
			dict1[point] = point_data
	return dict1




if __name__ == "__main__":
	start_date = '2024-07-29'
	end_date = '2024-08-01'

	points = read_grid(GRID_FILE)

	'''
	1. Run below fetch_grid functions to gather data (uses API calls!)
	'''
	# fetch_grid(BASE_URL, start_date, end_date, BASE_VARIABLES, points, 'src/main/resources/weather-data-forecast-2907-0108.json')
	# fetch_grid(MARINE_URL, start_date, end_date, MARINE_VARIABLES, points, 'src/main/resources/weather-data-marine-2907-0108.json')


	'''
	2. Merge two dicts into one
	'''
	# file1 = open('src/main/resources/weather-data-forecast-2907-0108.json')
	# file2 = open('src/main/resources/weather-data-marine-2907-0108.json')
	file1 = open('src/main/resources/weather-data.json')
	file2 = open('src/main/resources/weather-data-2907-0108.json')
	str1 = file1.read()
	str2 = file2.read()
	file1.close()
	file2.close()

	d1 = json.loads(str1)
	d2 = json.loads(str2)

	resulting_dict = merge_dicts(d1, d2)

	res_file = open('src/main/resources/weather-data-full.json', 'w')
	json.dump(resulting_dict, res_file)
	res_file.close()
