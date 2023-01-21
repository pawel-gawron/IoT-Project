#!/usr/bin/python3

from sense_emu import SenseHat
import json
import sys

sense = SenseHat()

if len(sys.argv) < 2:
    sys.exit(1)

input_json = sys.argv[1]

settings = {}
try:
    settings = json.loads(input_json)

except json.JSONDecodeError:
    sys.exit(1)

try:
    led_matrix = 64 * [[0, 0, 0]]
    for x in range(8):
        for y in range(8):
            if "state" in settings[str(x)][str(y)].keys() and not settings[str(x)][str(y)]["state"]:
                continue
            r = settings[str(x)][str(y)]["R"]
            g = settings[str(x)][str(y)]["G"]
            b = settings[str(x)][str(y)]["B"]
            led_matrix[8 * x + y] = [r, g, b]

    sense.set_pixels(led_matrix)

except Exception as e:
    print(e)
    sys.exit(1)

sys.exit(0)
