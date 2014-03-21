;;;;                  a-star pathfinding.bb V1.0
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;                       Matheus Cansian
;;;;                     mscansian@gmail.com
;;;;
;;;; This LIB is an implementation of the A* Algorithm. It uses binary
;;;; heaps as sorting method, can calculate several path requests and
;;;; also support Terrain/Influence costs.
;;;;
;;;; SETTING-UP: This LIB come with a pre-built Euclidian Distance 
;;;; function as heuristic. If you want to use another heuristic, feel 
;;;; free to modify the CalculateH% function. Also you can setup a
;;;; terrain/influence cost modifying the CalculateT% function
;;;;
;;;; HOW-TO: Add all nodes using AddNode and then create their 
;;;; relationship using AttachNode. After that you can calculate any 
;;;; path with RequestPath. You can check if the calculation is finished 
;;;; using RequestStatus. When the algorithm finds the path it will put
;;;; in the waypoint list. You can loop through this list using 
;;;; RequestWaypointStart and RequestWaypointEnd to get the limitants
;;;; (remeber to use a step -1). And then get each position with 
;;;; WaypointPosition.
;;;;
;;;; ADVICE: If you want to store something else with the node, use the 
;;;; Name$ parameter.
;;;;
;;;; PRE-REQUISITES: You need to include the BinaryHeaps LIB (by Matheus 
;;;; Cansian). The include line is below (binaryheaps.bb)
;;;; Bynaryheaps: https://github.com/mscansian/b3d-binaryheaps.git


DISCLAIMER: This project is very old (from my teenage years)! The code is very ugly and I don't provide any support.
