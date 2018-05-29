import math
import tkinter as tk

import matplotlib
import numpy as np

matplotlib.use("TkAgg")

from networktables import NetworkTables
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure
from matplotlib import animation


class Dashboard(tk.Tk):
    def __init__(self, *args, **kwargs):
        tk.Tk.__init__(self, *args, **kwargs)

        tk.Tk.title(self, "FRC Team 5190 Dashboard")

        container = tk.Frame(self)
        container.pack(side="top", fill="both", expand=True)
        container.grid_rowconfigure(0, weight=1)
        container.grid_columnconfigure(0, weight=1)

        PosePlotter(parent=self).pack()


# noinspection SpellCheckingInspection
class PosePlotter(tk.Frame):
    def __init__(self, parent):
        tk.Frame.__init__(self, parent)
        NetworkTables.initialize(server='10.51.90.2')

        # ALL UNITS IN INCHES
        def drawField(plax):

            plax.set_axis_off()
            plax.set_title("Robot Position on Field")

            # Calculate field length and width in inches
            fieldDim = [12 * 54, 12 * 27]

            # Draw individual elements
            fieldBoundary = [(29.69, 0), (fieldDim[0] - 29.69, 0), (fieldDim[0], 35.0),
                             (fieldDim[0], fieldDim[1] - 35.0),
                             (fieldDim[0] - 29.69, fieldDim[1]), (29.69, fieldDim[1]), (0, fieldDim[1] - 35.0),
                             (0, 35.0),
                             (29.69, 0)]
            plax.plot([p[0] for p in fieldBoundary], [p[1] for p in fieldBoundary], color="black")
            plax.fill([p[0] for p in fieldBoundary], [p[1] for p in fieldBoundary], color="#e1e2e2")

            switchClose = [(140, 85.25), (140, fieldDim[1] - 85.25), (196, fieldDim[1] - 85.25), (196, 85.25),
                           (140, 85.25)]
            plax.plot([p[0] for p in switchClose], [p[1] for p in switchClose], color="red")

            switchFar = [(fieldDim[0] - 140, 85.25), (fieldDim[0] - 140, fieldDim[1] - 85.25),
                         (fieldDim[0] - 196, fieldDim[1] - 85.25), (fieldDim[0] - 196, 85.25),
                         (fieldDim[0] - 140, 85.25)]
            plax.plot([p[0] for p in switchFar], [p[1] for p in switchFar], color="blue")

            scale = [(299.65, 71.57), (299.65, fieldDim[1] - 71.57), (348.35, fieldDim[1] - 71.57,), (348.35, 71.57),
                     (299.65, 71.57)]
            plax.plot([p[0] for p in scale], [p[1] for p in scale], color="purple")

            platform = [(261.47, 95.25), (261.47, fieldDim[1] - 95.25), (386.53, fieldDim[1] - 95.25), (386.53, 95.25),
                        (261.47, 95.25)]
            plax.plot([p[0] for p in platform], [p[1] for p in platform], color="gray")

            nullZoneLeft = [(288.0, 0.0), (288.0, 95.25), (288.0 + 72.0, 95.25), (288.0 + 72.0, 0)]
            plax.plot([p[0] for p in nullZoneLeft], [p[1] for p in nullZoneLeft], linestyle=':', color="black")

            nullZoneRight = [(288.0, fieldDim[1]), (288.0, fieldDim[1] - 95.25), (288.0 + 72.0, fieldDim[1] - 95.25),
                             (288.0 + 72.0, fieldDim[1])]
            plax.plot([p[0] for p in nullZoneRight], [p[1] for p in nullZoneRight], linestyle=':', color="black")

        def rotatePoint(p, center, angle):
            s = math.sin(math.radians(angle))
            c = math.cos(math.radians(angle))

            px = p[0] - center[0]
            py = p[1] - center[1]
            pxn = px * c - py * s
            pyn = px * s + py * c

            px = pxn + center[0]
            py = pyn + center[1]
            return px, py

        def genRobotSquare(p, heading):
            robotWidth = 33.0
            robotLength = 27.0

            topLeft = (p[0] - robotWidth / 2.0, p[1] + robotLength / 2.0)
            topRight = (p[0] + robotWidth / 2.0, p[1] + robotLength / 2.0)
            bottomLeft = (p[0] - robotWidth / 2.0, p[1] - robotLength / 2.0)
            bottomRight = (p[0] + robotWidth / 2.0, p[1] - robotLength / 2.0)

            trianglepoint = ((topRight[0] + bottomRight[0]) / 2.0, (topRight[1] + bottomRight[1]) / 2.0)

            topLeft = rotatePoint(topLeft, p, heading)
            bottomLeft = rotatePoint(bottomLeft, p, heading)
            trianglepoint = rotatePoint(trianglepoint, p, heading)

            boxArr = [topLeft, trianglepoint, bottomLeft, topLeft]
            return boxArr

        robotX = [18.5]
        robotY = [276]
        robotHeadings = [0.0]

        # Path
        pathX = [18.5]
        pathY = [276]
        pathHeadings = [0.0]

        lookaheadX = [100]
        lookaheadY = [276]

        fig = Figure(figsize=(10, 5), dpi=100)
        ax = fig.add_subplot(111, aspect='equal')

        xprint = ax.text(54*6 - 90, -20, "Robot X: 18.5", ha="left")
        yprint = ax.text(54*6 + 10, -20, "Robot Y: 276")

        # noinspection PyShadowingNames
        def updatePoint(_, point, pathpoint, lookaheadpoint, robot, actualPath, targetPath):
            ntinstance = NetworkTables.getTable('PosePlotter')

            if ntinstance.getBoolean('ResetPlot', False):
                del robotX[:]
                del robotY[:]
                del robotHeadings[:]
                del pathX[:]
                del pathY[:]
                del pathHeadings[:]
                ntinstance.putBoolean('ResetPlot', False)

            xval = ntinstance.getNumber('Robot X', 0.0)
            yval = ntinstance.getNumber('Robot Y', 0.0)
            hval = ntinstance.getNumber('Robot Heading', 0.0)

            xprint.set_text(str(xval))
            yprint.set_text(str(yval))

            if xval > .01 or yval > .01:
                robotX.append(xval)
                robotY.append(yval)
                robotHeadings.append(hval)

            pxval = ntinstance.getNumber('Path X', 0.0)
            pyval = ntinstance.getNumber('Path Y', 0.0)
            phval = ntinstance.getNumber('Path Heading', 0.0)

            lax = ntinstance.getNumber("Lookahead X", 100.0)
            lay = ntinstance.getNumber("Lookahead Y", 100.0)

            lookaheadX.append(lax)
            lookaheadY.append(lay)

   
            # Try to avoid "teleporting" when observer is reset.
            if pxval > .01 or pyval > .01:
                pathX.append(pxval)
                pathY.append(pyval)
                pathHeadings.append(phval)

            point.set_data(np.array([xval, yval]))
            pathpoint.set_data(np.array([pxval, pyval]))
            lookaheadpoint.set_data(np.array([lax, lay]))
            robotData = genRobotSquare((xval, yval), hval)
            robot.set_data([p[0] for p in robotData], [p[1] for p in robotData])
            actualPath.set_data(robotX, robotY)
            targetPath.set_data(pathX, pathY)
            return [point, pathpoint, lookaheadpoint, robot, actualPath, targetPath]

        # Generate the figure and draw static elements


        drawField(ax)
        targetPath, = ax.plot(pathX, pathY, color='red', alpha=0.5)
        actualPath, = ax.plot(robotX, robotY, color='black', alpha=0.25)

        pathpoint = ax.plot(pathX[0], pathY[0], marker='o', markersize=2, color="red")
        point = ax.plot(robotX[0], robotY[0], marker='o', markersize=2, color="blue")
        lookaheadpoint = ax.plot(lookaheadX[0], lookaheadY[0], marker="*", markersize=5, color="green")
        startingRobot = genRobotSquare((robotX[0], robotY[0]), 0.0)
        robot = ax.plot([p[0] for p in startingRobot], [p[1] for p in startingRobot], color="maroon")

        canvas = FigureCanvasTkAgg(fig, self)
        canvas.draw()
        canvas.get_tk_widget().pack(side=tk.BOTTOM, fill=tk.BOTH, expand=True)
        canvas.tkcanvas.pack(side=tk.TOP, fill=tk.BOTH, expand=True)

        animation.FuncAnimation(fig, updatePoint, len(robotX),
                                fargs=(point, pathpoint, lookaheadpoint, robot, actualPath, targetPath))


app = Dashboard()
app.mainloop()
