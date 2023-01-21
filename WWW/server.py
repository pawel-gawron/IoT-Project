from sense_emu import SenseHat
from zmq import Context, REP
from json import dumps, loads
from re import search


class Pixel:

    def __init__(self, R: int, G: int, B: int):
        self.R = R
        self.G = G
        self.B = B

        self.state = (R != 0 or G != 0 or G != 0)


def get_color(sense):
    pixels_list = sense.get_pixels()

    led_matrix = {}
    for x in range(8):
        row = {}
        for y in range(8):
            pixel = Pixel(pixels_list[8 * x + y][0], pixels_list[8 * x + y][1],
                          pixels_list[8 * x + y][2])
            row[str(y)] = pixel.__dict__

        led_matrix[str(x)] = row

    return dumps(led_matrix)


def set_color(sense, color_settings):
    settings = {}
    settings = loads(color_settings)

    led_matrix = 64 * [[0, 0, 0]]
    for x in range(8):
        for y in range(8):
            if "state" in settings[str(x)][str(
                    y)].keys() and not settings[str(x)][str(y)]["state"]:
                continue
            r = settings[str(x)][str(y)]["R"]
            g = settings[str(x)][str(y)]["G"]
            b = settings[str(x)][str(y)]["B"]
            led_matrix[8 * x + y] = [r, g, b]

    sense.set_pixels(led_matrix)


def get_measurements(sense):
    temperature = sense.get_temperature()
    humidity = sense.get_humidity()
    pressure = sense.get_pressure()

    orientation = sense.get_orientation_degrees()
    roll = orientation["roll"]
    pitch = orientation["pitch"]
    yaw = orientation["yaw"]

    return dumps([temperature, humidity, pressure, roll, pitch, yaw])


SET_COLOR_PATTERN = r"set_color:(.+);"

sense = SenseHat()

context = Context()
socket = context.socket(REP)
socket.bind("tcp://*:5555")

while True:
    message = socket.recv().decode("utf-8")

    if message == "get_color":
        current_color = get_color(sense)
        socket.send(current_color.encode("utf-8"))
    elif message == "get_meas":
        measurements = get_measurements(sense)
        socket.send(measurements.encode("utf-8"))
    else:
        match = search(SET_COLOR_PATTERN, message)
        if match:
            color_settings = match.group(1).encode("utf-8")
            set_color(sense, color_settings)
            socket.send(b"Color set")
        else:
            socket.send(b"unknown")
