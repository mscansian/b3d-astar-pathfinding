b3d-astar-pathfinding
===============

A* Pathfinding implementation for Blitz3D

Features
-----------
* Very fast! (uses Binary Heaps to sort nodes)
* Allow asyncronous calculation (path can be calculated in multiple iterations)
* Work with all kinds of map representation (grids, waypoints, navmesh)
* Allow customization of heuristic function (default: euclidian distance)
* Allow customization of terrain function (default: always zero)

Requisites
-----------
[b3d-binary-heaps](https://github.com/mscansian/b3d-binaryheaps) (already included)

License
-----------
[GNU LGPLv3](https://www.gnu.org/licenses/lgpl.html)
