# 5190 Offseason 2018

## This repository is broken up into various submodules. Each submodule contains a different aspect of the FRC Team 5190 Offseason projects or FRC Team 5190 unreleased projects during the 2018 POWER UP competition season

### ```robot```

#### Robot code with increased efficiency. Some features include

* 3 cube autonomous
* Closed loop PID control on elevator, arm, and climber
* Voltage compensation for predictable open loop feedback
* Commands with intelligent state-based execution
* Coroutine enabled subsystems and actions
* Custom Path Follower with (x, y, theta) error correction
* Efficient code through Kotlin JVM language

##### Instructions for Robot

* Open IntelliJ IDEA, click 'Open' and click on the '5190-Offseason-2018' Gradle Icon.
* Use the terminal to deploy code: ```.\gradlew deploy```

### ```poseplotter```

#### Plots the robot position relative to the field over time. Some features include

* Data sent over Network Tables from robot to Python dashboard
* ```matplotlib``` and ```tkinter```
* Shows relative robot position and path (when following path)
* Smooth animations

##### Instructions for Pose Plotter

* Download ```python``` and depedencies mentioned above.
* Download and install ```pynetworktables``` using: ```pip install pynetworktables```
* Use the terminal to run the program: ```python poseplotter.py```
* You can test the program by changing the NT IP in ```poseplotter.py``` to ```172.0.1.1``` and running ```test.py``` in a new terminal window

### ```strategysim```

#### Developed by 5190 Programming Mentors to simulate robot strategy. Some features include

* Various strategy modes for simulation
* Options to play against the computer
* Easy-to-use web app interface through Electron and TypeScript

#### Instructions for Strategy Simulator

* Download and install ```node.js```. This installation is bundled with ```npm```
* Install ```npm``` dependencies: ```npm install```
* Compile TypeScript: ```tsc```
* Use the terminal to run the program: ```npm start```
