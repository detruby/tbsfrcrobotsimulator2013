
From 
WPILib_programming.pdf
and
http://wpilib.screenstepslive.com/s/3120/m/7912/l/80205-basic-networktables-operation

NetworkTables is an implementation of a distributed "dictionary". 
That is named values are created either on the robot, driver station, or potentially 
an attached coprocessor, and the values are automatically distributed to all the other participants. 

For example, a driver station laptop might receive camera images over the network, 
perform some vision processing algorithm, and come up with some values to sent back to the robot. 

The values might be an X, Y, and Distance. 
By writing these results to NetworkTable values called "X", "Y", and "Distance" they 
can be read by the robot shortly after being written. 

Then the robot can act upon them.


