#!/usr/bin/python3

from sense_emu import SenseHat
import json
import sys


class Pixel:

    def __init__(self, R: int, G: int, B: int):
        self.R = R
        self.G = G
        self.B = B

        self.state = (R != 0 or G != 0 or G != 0)


sense = SenseHat()
pixels_list = sense.get_pixels()

led_matrix = {}
for x in range(8):
    row = {}
    for y in range(8):
        pixel = Pixel(pixels_list[8 * x + y][0], pixels_list[8 * x + y][1], pixels_list[8 * x + y][2])
        row[str(y)] = pixel.__dict__

    led_matrix[str(x)] = row

ret_json = ""
try:
    ret_json = json.dumps(led_matrix)

except:
    sys.exit(1)

print(ret_json)
sys.exit(0)
