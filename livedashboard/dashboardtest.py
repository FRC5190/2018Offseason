import csv
import time
from networktables import NetworkTables

import logging
logging.basicConfig(level=logging.DEBUG)

NetworkTables.initialize()
sd = NetworkTables.getTable("Live Dashboard")

time.sleep(1)
with open("C:/Users/prate/Desktop/FRC/Projects/5190 Offseason 2018/robot/src/main/resources/LS-LL/1st Cube Source.csv") as csvfile:
    reader = csv.DictReader(csvfile)
    sd.putBoolean('Reset', True)
    sd.putBoolean('Is Climbing', True)
    sd.putString("Game Data", "RLR")
    for row in reader:
        x = float(row['x'])
        y = float(row['y'])
        velocity = float(row['velocity'])
        heading = row['heading']
     
        sd.putNumber('Robot X', x)
        sd.putNumber('Robot Y', y)

        # sd.putNumber('Lookahead X', x)
        # sd.putNumber('Lookahead Y', y)

        
        sd.putNumber('Robot Heading', heading)
        print(x, y)
        time.sleep(0.02)

with open("C:/Users/prate/Desktop/FRC/Projects/5190 Offseason 2018/robot/src/main/resources/LS-LL/4th Cube Source.csv") as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        x = float(row['x'])
        y = float(row['y'])
        velocity = float(row['velocity'])
        heading = row['heading']
     
        sd.putNumber('Robot X', x)
        sd.putNumber('Robot Y', y)

        # sd.putNumber('Lookahead X', x)
        # sd.putNumber('Lookahead Y', y)

        
        sd.putNumber('Robot Heading', heading)
        print(x, y)
        time.sleep(0.02)

