import csv
import time
from networktables import NetworkTables

import logging
logging.basicConfig(level=logging.DEBUG)

NetworkTables.initialize()
sd = NetworkTables.getTable("Live Dashboard")

time.sleep(1)
with open("C:/Users/prate/Desktop/FRC/Projects/5190 Falcon Heavy/src/main/resources/LS-LL/Drop First Cube Right Detailed.csv") as csvfile:
    reader = csv.DictReader(csvfile)
    sd.putBoolean('Reset', True)
    sd.putBoolean('Is Climbing', True)
    sd.putString("Game Data", "RLR")
    for row in reader:
        x = float(row['x'])
        y = float(row['y'])
        velocity = float(row['velocity'])
        heading = row['heading']
     
        sd.putNumber('Robot (C) X', x)
        sd.putNumber('Robot (C) Y', y)

        # sd.putNumber('Lookahead X', x)
        # sd.putNumber('Lookahead Y', y)

        
        sd.putNumber('Robot (C) Heading', heading)
        print(x, y)
        time.sleep(0.02)