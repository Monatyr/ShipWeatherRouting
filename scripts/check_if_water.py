import json


API_KEY = 'x'


def read_map_size(filepath):
    with open(filepath, 'r') as file:
        data = json.loads(file.read())
    return data["map"]["rows"], data["map"]["columns"]


def create_is_water_file(filepath):
    height, width = read_map_size("src/main/resources/config.json")
    with open(filepath, 'w') as file:
        for i in range (height):
            for j in range(width):
                file.write(f"{i} {j} 1\n")



if __name__ == "__main__":
    create_is_water_file("src/main/resources/is_water.txt")

