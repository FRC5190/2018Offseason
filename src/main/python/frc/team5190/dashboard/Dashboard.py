import math
import matplotlib
import numpy as np
import tkinter as tk

matplotlib.use("TkAgg")

from networktables import NetworkTables
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure
from matplotlib import animation


class Dashboard(tk.Tk):
    def __init__(self, *args, **kwargs):
        tk.Tk.__init__(self, *args, **kwargs)

        tk.Tk.wm_title(self, "FRC Team 5190 Dashboard")

        container = tk.Frame(self)
        container.pack(side="top", fill="both", expand=True)
        container.grid_rowconfigure(0, weight=1)
        container.grid_columnconfigure(0, weight=1)

        self.frames = {}

        for F in (PosePlotter,):
            frame = F(container, self)
            self.frames[F] = frame
            frame.grid(row=0, column=0, sticky="nsew")

        self.show_frame(PosePlotter)

    def show_frame(self, cont):
        frame = self.frames[cont]
        frame.tkraise()


# noinspection SpellCheckingInspection
class PosePlotter(tk.Frame):

    def __init__(self, parent, _):
        tk.Frame.__init__(self, parent)

        label = tk.Label(self, text="Pose Plotter")
        label.pack(pady=10, padx=10)

        NetworkTables.initialize(server='10.51.90.2')

        # ALL UNITS IN INCHES
        def drawField(plax):
            # Calculate field length and width in inches
            fieldDim = [12 * 27, 12 * 54]

            # Draw individual elements
            fieldBoundary = [(29.69, 0), (fieldDim[0] - 29.69, 0), (fieldDim[0], 35.0),
                             (fieldDim[0], fieldDim[1] - 35.0),
                             (fieldDim[0] - 29.69, fieldDim[1]), (29.69, fieldDim[1]), (0, fieldDim[1] - 35.0),
                             (0, 35.0),
                             (29.69, 0)]
            plax.plot([p[0] for p in fieldBoundary], [p[1] for p in fieldBoundary], color="black")

            switchClose = [(85.25, 140), (fieldDim[0] - 85.25, 140), (fieldDim[0] - 85.25, 196), (85.25, 196),
                           (85.25, 140)]
            plax.plot([p[0] for p in switchClose], [p[1] for p in switchClose], color="red")

            switchFar = [(85.25, fieldDim[1] - 140), (fieldDim[0] - 85.25, fieldDim[1] - 140),
                         (fieldDim[0] - 85.25, fieldDim[1] - 196), (85.25, fieldDim[1] - 196),
                         (85.25, fieldDim[1] - 140)]
            plax.plot([p[0] for p in switchFar], [p[1] for p in switchFar], color="blue")

            scale = [(71.57, 299.65), (fieldDim[0] - 71.57, 299.65), (fieldDim[0] - 71.57, 348.35), (71.57, 348.35),
                     (71.57, 299.65)]
            plax.plot([p[0] for p in scale], [p[1] for p in scale], color="black")

            platform = [(95.25, 261.47), (fieldDim[0] - 95.25, 261.47), (fieldDim[0] - 95.25, 386.53), (95.25, 386.53),
                        (95.25, 261.47)]
            plax.plot([p[0] for p in platform], [p[1] for p in platform], color="black")

            nullZoneLeft = [(0.0, 288.0), (95.25, 288.0), (95.25, 288.0 + 72.0), (0, 288.0 + 72.0)]
            plax.plot([p[0] for p in nullZoneLeft], [p[1] for p in nullZoneLeft], linestyle=':', color="black")

            nullZoneRight = [(fieldDim[0], 288.0), (fieldDim[0] - 95.25, 288.0), (fieldDim[0] - 95.25, 288.0 + 72.0),
                             (fieldDim[0], 288.0 + 72.0)]
            plax.plot([p[0] for p in nullZoneRight], [p[1] for p in nullZoneRight], linestyle=':', color="black")

            autoLineClose = [(0, 120.0), (fieldDim[0], 120.0)]
            plax.plot([p[0] for p in autoLineClose], [p[1] for p in autoLineClose], linestyle=':', color="black")

            autoLineFar = [(0, fieldDim[1] - 120.0), (fieldDim[0], fieldDim[1] - 120.0)]
            plax.plot([p[0] for p in autoLineFar], [p[1] for p in autoLineFar], linestyle=':', color="black")

            exchangeZoneClose = [(149.69, 0), (149.69, 36.0), (149.69 - 48.0, 36.0), (149.69 - 48.0, 0)]
            plax.plot([p[0] for p in exchangeZoneClose], [p[1] for p in exchangeZoneClose], linestyle=':', color="red")

            exchangeZoneFar = [(fieldDim[0] - 149.69, fieldDim[1]), (fieldDim[0] - 149.69, fieldDim[1] - 36.0),
                               (fieldDim[0] - 149.69 + 48.0, fieldDim[1] - 36.0),
                               (fieldDim[0] - 149.69 + 48.0, fieldDim[1])]
            plax.plot([p[0] for p in exchangeZoneFar], [p[1] for p in exchangeZoneFar], linestyle=':', color="black")

            powerCubeZoneClose = [(161.69 - 22.5, 140), (161.69 - 22.5, 140 - 42.0), (161.69 + 22.5, 140 - 42.0),
                                  (161.69 + 22.5, 140)]
            plax.plot([p[0] for p in powerCubeZoneClose], [p[1] for p in powerCubeZoneClose], linestyle=':',
                      color="green")

            powerCubeZoneFar = [(161.69 - 22.5, fieldDim[1] - 140), (161.69 - 22.5, fieldDim[1] - 140 + 42.0),
                                (161.69 + 22.5, fieldDim[1] - 140 + 42.0), (161.69 + 22.5, fieldDim[1] - 140)]
            plax.plot([p[0] for p in powerCubeZoneFar], [p[1] for p in powerCubeZoneFar], linestyle=':', color="green")

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
            robotWidth = 27.0
            robotLength = 33.0

            topLeft = (p[0] - robotWidth / 2.0, p[1] + robotLength / 2.0)
            topRight = (p[0] + robotWidth / 2.0, p[1] + robotLength / 2.0)
            bottomLeft = (p[0] - robotWidth / 2.0, p[1] - robotLength / 2.0)
            bottomRight = (p[0] + robotWidth / 2.0, p[1] - robotLength / 2.0)

            topLeft = rotatePoint(topLeft, p, heading)
            topRight = rotatePoint(topRight, p, heading)
            bottomLeft = rotatePoint(bottomLeft, p, heading)
            bottomRight = rotatePoint(bottomRight, p, heading)

            boxArr = [topLeft, topRight, bottomRight, bottomLeft, topLeft]
            return boxArr

        robotX = [0.0]
        robotY = [0.0]
        robotHeadings = [0.0]

        # Path
        pathX = [0.0]
        pathY = [0.0]
        pathHeadings = [0.0]

        # noinspection PyShadowingNames
        def updatePoint(_, point, pathpoint, robot, actualPath, targetPath):
            ntinstance = NetworkTables.getTable('PosePlotter')

            if not ntinstance.getBoolean('Is Auto', False):
                return

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

            if xval > .01 or yval > .01:
                robotX.append(xval)
                robotY.append(yval)
                robotHeadings.append(hval)

            pxval = ntinstance.getNumber('Path X', 0.0)
            pyval = ntinstance.getNumber('Path Y', 0.0)
            phval = ntinstance.getNumber('Path Heading', 0.0)

            # Try to avoid "teleporting" when observer is reset.
            if pxval > .01 or pyval > .01:
                pathX.append(pxval)
                pathY.append(pyval)
                pathHeadings.append(phval)

            point.set_data(np.array([xval, yval]))
            pathpoint.set_data(np.array([pxval, pyval]))
            robotData = genRobotSquare((xval, yval), hval)
            robot.set_data([p[0] for p in robotData], [p[1] for p in robotData])
            actualPath.set_data(robotX, robotY)
            targetPath.set_data(pathX, pathY)
            return [point, pathpoint, robot, actualPath, targetPath]

        # Generate the figure and draw static elements
        fig = Figure(figsize=(5, 5), dpi=100)
        ax = fig.add_subplot(111, aspect='equal')
        drawField(ax)
        targetPath, = ax.plot(pathX, pathY, color='red', alpha=0.5)
        actualPath, = ax.plot(robotX, robotY, color='black', alpha=0.25)

        pathpoint = ax.plot(pathX[0], pathY[0], marker='o', markersize=2, color="red")
        point = ax.plot(robotX[0], robotY[0], marker='o', markersize=2, color="blue")
        startingRobot = genRobotSquare((robotX[0], robotY[0]), 0.0)
        robot = ax.plot([p[0] for p in startingRobot], [p[1] for p in startingRobot], color="maroon")

        canvas = FigureCanvasTkAgg(fig, self)
        canvas.draw()
        canvas.get_tk_widget().pack(side=tk.BOTTOM, fill=tk.BOTH, expand=True)
        canvas.tkcanvas.pack(side=tk.TOP, fill=tk.BOTH, expand=True)

        animation.FuncAnimation(fig, updatePoint, len(robotX), fargs=(point, pathpoint, robot, actualPath, targetPath))


app = Dashboard()
app.mainloop()
