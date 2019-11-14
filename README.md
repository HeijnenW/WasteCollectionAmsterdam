# WasteCollectionAmsterdam

Purpose of simulation model:
The simulation model simulates a network of containers that are filled daily with a Gamma distributed amount of waste. The purpose
of this simulation model is to evaluate emptying schedules (timing decision: when to empty a container, routing decision: how to route
the chosen containers)

Required input:
All input is supposed to be given using .txt-files with a predetermined format (tab-indented). The 6 required text files are 
described as follows:

  - "inputContainers"         list of all containers        [waste fraction, lat, lon, container capacity, shape parameter, scale parameter]
  - "inputExperiments"        list of experiment settings   [travel costs approx method, penalty too late factor, penalty too early factor, adding new cluster costs, using clusters, acceptable overflow probability, using sensors, length planning horizon, online rescheduling technique (not implemented), vehicle buffer]
  - "inputTransshipmentHub"   list of transshipment hubs    [lat, lon, storage capacity, storage used, storage free]
  - "inputVehicle"            list of vehicles (NOT USED YET, simulation model can be extended to include hetrogeneous vehicles)
  - "inputWasteProcessor"     list of waste processors      [lat, lon, waste fraction]
  - "inputWharf"              list of wharfs                [lat, lon]


Given output:
The output of all experiments is summarized in outputSim.txt which contains the following KPIs:
Total distance, total duration, number of emptied containers, number of overflowed containers, number of waste processors visited, 
number of sattelite facilities (transshipment hubs) visited, average fill level of containers upon emptying, average fill level of 
vehicles when dumping, total number of vehicles used, average expected fill level of containers, number of containers scheduled later 
than DED, number of containers scheduled earlier than DED, number of containers scheduled on their DED, computation time of experiment


Inner workings of simulation:
The simulation is controlled by the SimulationController, which loops over all experiments as defined in "inputExperiments.txt", within
each experiment, the ExperimentController takes over which loops over the number of replications required for each experiment, within
each replication, an x number of days is simulated using Day.java, which consists of all functions of the simulation that occur every day,
such as: scheduling the routes for that day, executing the scheduled routes, and filling the containers with a randomly generated amounts 
of waste.
