import math
import tkinter as tk

import matplotlib
import numpy as np

matplotlib.use("TkAgg")

from networktables import NetworkTables
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.image import imread
from matplotlib.figure import Figure
from matplotlib import animation

import logging

logging.basicConfig(level=logging.DEBUG)


class Dashboard(tk.Tk):
    def __init__(self, *args, **kwargs):
        tk.Tk.__init__(self, *args, **kwargs)

        tk.Tk.title(self, "FRC Team 5190 Dashboard")

        container = tk.Frame(self)
        container.pack(side="top", fill="both", expand=True)

        PosePlotter(parent=self).pack()


class PosePlotter(tk.Frame):
    def __init__(self, parent):
        tk.Frame.__init__(self, parent)
        NetworkTables.initialize(server='127.0.1.1')
        nt_instance = NetworkTables.getTable("PosePlotter")

        def draw_field(subplot):
            subplot.set_axis_off()
            subplot.set_title("Robot Position on Field")

            img = imread("images/red_alliance.png")
            img2 = imread("images/blue_alliance.png")

            subplot.imshow(img, extent=[0.0, 32 * 12, 0.0, 27 * 12])
            subplot.imshow(img2, extent=[22 * 12, 54 * 12, 0.0, 27 * 12])

        def rotate_point(p, center, angle):
            sin = math.sin(angle)
            cos = math.cos(angle)

            px = p[0] - center[0]
            py = p[1] - center[1]
            pxn = px * cos - py * sin
            pyn = px * sin + py * cos

            px = pxn + center[0]
            py = pyn + center[1]
            return px, py

        def gen_robot_square(p, heading):
            robot_width = 33.0
            robot_length = 27.0

            top_left = (p[0] - robot_width / 2.0, p[1] + robot_length / 2.0)
            top_right = (p[0] + robot_width / 2.0, p[1] + robot_length / 2.0)
            bottom_left = (p[0] - robot_width / 2.0, p[1] - robot_length / 2.0)
            bottom_right = (p[0] + robot_width / 2.0, p[1] - robot_length / 2.0)

            top_left = rotate_point(top_left, p, heading)
            top_right = rotate_point(top_right, p, heading)
            bottom_left = rotate_point(bottom_left, p, heading)
            bottom_right = rotate_point(bottom_right, p, heading)

            box = [top_left, top_right, bottom_right, bottom_left, top_left]
            return box

        robot_x_values = [18.5]
        robot_y_values = [276]
        robot_headings = [0.0]

        # Path
        path_x_values = [18.5]
        path_y_values = [276]
        path_headings = [0.0]

        lookahead_x_values = [100]
        lookahead_y_values = [276]

        fig = Figure(figsize=(10, 5.75), dpi=100)
        plot = fig.add_subplot(111, aspect='equal')

        x_location_display = plot.text(0, -18, "")
        y_location_display = plot.text(0, -38, "")
        theta_display = plot.text(0, -58, "")

        def update_point(n, robot_point, path_point, lookahead_point, robot, robot_path, path):

            if nt_instance.getBoolean('Reset', False):
                del robot_x_values[:]
                del robot_y_values[:]
                del robot_headings[:]
                del path_x_values[:]
                del path_y_values[:]
                del path_headings[:]
                nt_instance.putBoolean('Reset', False)

            robot_x = nt_instance.getNumber('Robot X', 18.5)
            robot_y = nt_instance.getNumber('Robot Y', 276)
            robot_heading = nt_instance.getNumber('Robot Heading', 0.0)

            x_location_display.set_text("Robot X: " + str(robot_x / 12.0) + " ft")
            y_location_display.set_text("Robot Y: " + str(robot_y / 12.0) + " ft")
            theta_display.set_text("Robot θ: " + str(np.degrees(robot_heading)) + "°")
        

            if robot_x > .01 or robot_y > .01:
                robot_x_values.append(robot_x)
                robot_y_values.append(robot_y)
                robot_headings.append(robot_heading)

            path_x = nt_instance.getNumber('Path X', 0.0)
            path_y = nt_instance.getNumber('Path Y', 0.0)
            path_heading = nt_instance.getNumber('Path Heading', 0.0)

            lookahead_x = nt_instance.getNumber("Lookahead X", 100.0)
            lookahead_y = nt_instance.getNumber("Lookahead Y", 100.0)

            lookahead_x_values.append(lookahead_x)
            lookahead_y_values.append(lookahead_y)

            if path_x > .01 or path_y > .01:
                path_x_values.append(path_x)
                path_y_values.append(path_y)
                path_headings.append(path_heading)

            robot_point.set_data(np.array([robot_x, robot_y]))
            path_point.set_data(np.array([path_x, path_y]))
            lookahead_point.set_data(np.array([lookahead_x, lookahead_y]))

            robotData = gen_robot_square((robot_x, robot_y), robot_heading)

            robot.set_data([p[0] for p in robotData], [p[1] for p in robotData])
            robot_path.set_data(robot_x_values, robot_y_values)
            path.set_data(path_x_values, path_y_values)

            return [robot_point, path_point, lookahead_point, robot, robot_path, path]

        draw_field(plot)

        canvas = FigureCanvasTkAgg(fig, master=parent)
        canvas.get_tk_widget().pack()

        targetPath, = plot.plot(path_x_values, path_y_values, color='red', alpha=0.5)
        actualPath, = plot.plot(robot_x_values, robot_y_values, color='black', alpha=0.25)

        path_point, = plot.plot(path_x_values[0], path_y_values[0], marker='o', markersize=2, color="red")
        robot_point, = plot.plot(robot_x_values[0], robot_y_values[0], marker='o', markersize=2, color="blue")
        lookahead_point, = plot.plot(lookahead_x_values[0], lookahead_y_values[0], marker="*", markersize=5,
                                     color="black")
        starting_robot = gen_robot_square((robot_x_values[0], robot_y_values[0]), 0.0)
        robot, = plot.plot([p[0] for p in starting_robot], [p[1] for p in starting_robot], color="green")

        ani = animation.FuncAnimation(fig, update_point, frames=None, interval=20,
                                      fargs=(robot_point, path_point, lookahead_point, robot, actualPath, targetPath))

        canvas.draw()


app = Dashboard()
app.mainloop()
