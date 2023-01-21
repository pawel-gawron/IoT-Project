#!/usr/bin/python3

from sense_emu import SenseHat
import json

sense = SenseHat()

temperature = sense.get_temperature()
humidity = sense.get_humidity()
pressure = sense.get_pressure()

orientation = sense.get_orientation_degrees()
roll = orientation["roll"]
pitch = orientation["pitch"]
yaw = orientation["yaw"]

measurements = [temperature, humidity, pressure, roll, pitch, yaw]
print(measurements)
