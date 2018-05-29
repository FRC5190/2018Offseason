import math
import tkinter as tk

import matplotlib
import numpy as np
from matplotlib.image import imread

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


class PosePlotter(tk.Frame):
    def __init__(self, parent):
        tk.Frame.__init__(self, parent)
        NetworkTables.initialize(server='10.51.90.2')

        # ALL UNITS IN INCHES
        # noinspection SpellCheckingInspection
        def drawField(plax):

            plax.set_axis_off()
            plax.set_title("Robot Position on Field")

            img = imread("C:/Users/prate/Downloads/bgtest.png")
            img2 = imread("C:/Users/prate/Downloads/bgtest2.png")

            plax.imshow(img, extent=[0.0, 32 * 12, 0.0, 27 * 12])
            plax.imshow(img2, extent=[22 * 12, 54 * 12, 0.0, 27 * 12])




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

            topLeft = rotatePoint(topLeft, p, heading)
            bottomLeft = rotatePoint(bottomLeft, p, heading)

            boxArr = [topLeft, topRight, bottomRight, bottomLeft, topLeft]
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

        xprint = ax.text(54 * 6 - 90, -20, "Robot X: 1.541", ha="left")
        yprint = ax.text(54 * 6 + 10, -20, "Robot Y: 23")

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

            xprint.set_text("Robot X: " + str(xval))
            yprint.set_text("Robot Y: " + str(yval))

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
