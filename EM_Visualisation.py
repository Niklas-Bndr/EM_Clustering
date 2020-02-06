# import
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.patches import Ellipse

# Parameters:
dataPoints = "H:\\Programming\\TH_Bingen\\SYSA\\EM_Clustering\\inputMouse.dat"
clusters =   "H:\\Programming\\TH_Bingen\\SYSA\\EM_Clustering\\resultMouse.dat"
plt_axis = [0, 1, 0.1, 0.9]

# dataPoints = "H:\\Programming\\TH_Bingen\\SYSA\\EM_Clustering\\inputFaithful.dat"
# clusters =   "H:\\Programming\\TH_Bingen\\SYSA\\EM_Clustering\\resultFaithful.dat"
# plt_axis = [1, 6, 40, 100]

# setting for plot
fig = plt.gcf()
fig.canvas.set_window_title('EM-Clustering - Niklas Bender')
plt.suptitle('Ergebnisse durch EM-Clustering')
plt.grid(True)
plt.xlabel("x")
plt.ylabel("y")
# set from - to value per axis [y-from, y-to, x-from, x-to]
plt.axis(plt_axis)
# colorset for ellipse surface 
colorset = ["goldenrod","mediumseagreen","cornflowerblue","orchid","peru"]

# load dataPoints
x_axis = []
y_axis = []
with open(dataPoints) as f:
    for line in f:
        dataPoint = line.split()
        x_axis.append(float(dataPoint[0]))
        y_axis.append(float(dataPoint[1]))

# plot loaded datapoints as green "x"es 
plt.plot(x_axis, y_axis, "gx")

# load determinted clusters and plot it (ax.add_artist)
ax = plt.subplot(1,1,1)
with open(clusters) as f:
    for line in f:
        stuff = line.split()
        x = float(stuff[0])
        y = float(stuff[1])
        width = float(stuff[2])
        height = float(stuff[3])
        angle = float(stuff[4])
        # width must be smaller than height -> if not also angle was must be change
        if  width>height:
            width, height = height, width
            angle = angle * -1
        ellipseColor = np.random.choice(colorset) # choose an random facecolor for the ellipse
        # draw for each cluster 3 ellipse with differnt chi-square probabilities and alpha values 
        # further information on https://www.visiondummy.com/2014/04/draw-error-ellipse-representing-covariance-matrix/
        e = Ellipse((x,y), 2 * np.sqrt(1.388 * width), 2 * np.sqrt(1.388 * height), -angle)
        e.set_alpha(0.8) 
        e.set_facecolor(ellipseColor)
        ax.add_artist(e)
        e = Ellipse((x, y), 2 * np.sqrt(4.605 * width), 2 * np.sqrt(4.605 * height), -angle)
        e.set_alpha(0.4) 
        e.set_facecolor(ellipseColor) 
        ax.add_artist(e)
        e = Ellipse((x, y), 2 * np.sqrt(9.210 * width), 2 * np.sqrt(9.210 * height), -angle)
        e.set_alpha(0.1) 
        e.set_facecolor(ellipseColor)
        ax.add_artist(e)

# show the results
plt.show()