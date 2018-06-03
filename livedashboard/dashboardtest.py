import csv
import time
from networktables import NetworkTables

import logging
logging.basicConfig(level=logging.DEBUG)

NetworkTables.initialize()
sd = NetworkTables.getTable("Live Dashboard")

time.sleep(1)
with open("C:/Users/prate/Desktop/FRC/Projects/5190 Offseason 2018/robot/src/main/resources/LS-RR/1st Cube Source.csv") as csvfile:
    reader = csv.DictReader(csvfile)
    sd.putBoolean('Reset', True)
    sd.putString('Cross Auto', '1 Cube')
    sd.putBoolean('Is Climbing', True)
    for row in reader:
        x = float(row['x'])
        y = float(row['y'])
        heading = row['heading']
        sd.putNumber('Path X', x)
        sd.putNumber('Path Y', y)
        sd.putNumber('Path Heading', heading)
        sd.putNumber('Robot X', x)
        sd.putNumber('Robot Y', y)
        sd.putNumber('Robot Heading', heading)
        print(x, y)
        time.sleep(0.02)
