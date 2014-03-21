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

;; BINARY HEAPS INCLUDE ;;
Include "BinaryHeaps\binaryheaps.bb"

;; LIB CONFIGURATION ;;
Const AStarPathfinding_MaxRequests%  = 20
Const AStarPathfinding_MaxAdjacents% = 10

;; HEURISTIC FUNCTION ;;
; This function calculates the heuristic. If you want to use another algorithm feel free to change it.
Function AStarPathfinding_CalculateH%(NodeHandle%, EndNodeHandle%)
	Local Node.AStarPathfinding_Node  = Object.AStarPathfinding_Node(NodeHandle%)
	Local Node2.AStarPathfinding_Node = Object.AStarPathfinding_Node(EndNodeHandle%)
	
	;Returns Euclidian distance
	Return Int(Sqr( (Node\Position[0]-Node2\Position[0])^2 + (Node\Position[1]-Node2\Position[1])^2 + (Node\Position[2]-Node2\Position[2])^2 ))
End Function

;; ANOTHER COST ;;
; This function is here for you to calculate any other cost that you might need. Feel free to change it.
Function AStarPathfinding_CalculateT%(NodeHandle%, StartNodeHandle%, EndNodeHandle%)
	Return 0
End Function


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; LIB ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;; Probably there's nothing you need to change down there! ;;;;;;;;;;;;

;; LIB CONSTANTS ;;
Const AStarPathfinding_Version$ = "1.0"

;; OPEN LIST CONSTANTS ;;
Const AStarPathfinding_Unvisited%   = 1
Const AStarPathfinding_OpenList%    = 2
Const AStarPathfinding_ClosedList%  = 3

;; REQUEST CONSTANTS ;;
Const AStarPathfinding_Waiting%     = 1
Const AStarPathfinding_Calculating% = 2
Const AStarPathfinding_Done%        = 3
Const AStarPathfinding_NoPath%      = 4

;; STACK ARRAY ;;
Dim AStarPathfinding_RequestStack.AStarPathfinding_Request(AStarPathfinding_MaxRequests%)

;; BINARY HEAPS THREAD ID ;;
Global AStarPathfinding_BHThread%

;; REQUEST TYPE ;;
Type AStarPathfinding_Request
	Field StartNode%, EndNode%
	Field Status%, WaypointStart%, WaypointEnd%
End Type

;; NODE TYPE ;;
Type AStarPathfinding_Node
	Field Name$, Position%[2]
	Field Adjacent%[AStarPathfinding_MaxAdjacents%]
	Field AdjacentG%[AStarPathfinding_MaxAdjacents%]
	Field List%, Parent%, G%, H%, T%
End Type

;; WAYPOINT TYPE ;;
Type AStarPathfinding_Waypoint
	Field Node%
End Type

;;; <summary>Delete all nodes</summary>
;;; <remarks></remarks>
;;; <returns>Void</returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathFinding_FreeNodes()
	Delete Each AStarPathfinding_Node
End Function

;;; <summary>Add a node to the pathfinding list</summary>
;;; <param name="Position[2]">BlitzArray Node position</param>
;;; <param name="Name">Node name</param>
;;; <remarks></remarks>
;;; <returns>Node handle</returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathfinding_AddNode%(Position[2], Name$="")
	Local Node.AStarPathfinding_Node
	
	;Create new node
	Node = New AStarPathfinding_Node
	Node\Name$ = Name$
	Node\List% = AStarPathfinding_Unvisited%
	Node\Position[0] = Position[0]
	Node\Position[1] = Position[1]
	Node\Position[2] = Position[2]
	
	;Return its handle
	Return Handle(Node)
End Function

;;; <summary>Set a node as adjacent to another</summary>
;;; <param name="NodeObject">First Node</param>
;;; <param name="AdjacentObject">Second node</param>
;;; <param name="AdjacentG">Distance or G value</param>
;;; <remarks></remarks>
;;; <returns>1 if successfull or 0 if failed</returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathfinding_AttachNode(NodeObject%, AdjacentObject%, AdjacentG%)
	If NodeObject% = 0 Or AdjacentObject% = 0 Then Return 0 ;Invalid handle
	
	Local Node.AStarPathfinding_Node, Node2.AStarPathfinding_Node
	
	;Get node object and adjacent node object
	Node = Object.AStarPathfinding_Node(NodeObject%)
	Node2 = Object.AStarPathfinding_Node(AdjacentObject%)
	If Node = Null Or Node2 = Null Then Return 0 ;Node doesn't exists
	
	;Add adjacent node to node's array
	For Cont = 1 To AStarPathfinding_MaxAdjacents%
		If Node\Adjacent[Cont] = 0 Then 
			Node\Adjacent[Cont] = AdjacentObject%
			Node\AdjacentG[Cont] = AdjacentG%
			Exit
		ElseIf Node\Adjacent[Cont] = AdjacentObject% Then ;Node already attached
			Return 0
		EndIf
		If Cont = AStarPathfinding_MaxAdjacents% Then Return 0 ;No more room for adjacent nodes
	Next
	
	;Add node to adjacent node's array
	For Cont = 1 To AStarPathfinding_MaxAdjacents%
		If Node2\Adjacent[Cont] = 0 Then 
			Node2\Adjacent[Cont] = NodeObject%
			Node2\AdjacentG[Cont] = AdjacentG%
			Exit
		EndIf
		If Cont = AStarPathfinding_MaxAdjacents% Then Return 0 ;No more room for adjacent nodes
	Next
	
	;Success
	Return 1
End Function

;;; <summary>Delete a node and clear its relationship</summary>
;;; <param name="NodeObject">Node handle</param>
;;; <remarks></remarks>
;;; <returns>1 if successful</returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathfinding_DeleteNode(NodeObject%)
	If NodeObject% = 0 Then Return 0 ;Invalid handle
	
	;Variables
	Local Node.AStarPathfinding_Node, Node2.AStarPathfinding_Node
	
	;Get node object
	Node = Object.AStarPathfinding_Node(NodeObject%)
	If Node = Null Then Return 0 ;Node doesent exists
	
	;Clear node relationship
	For Cont = 1 To AStarPathfinding_MaxAdjacents%
		If Node\Adjacent[Cont] <> 0 Then
			;Get node object
			Node2 = Object.AStarPathfinding_Node(Node\Adjacent[Cont])
			
			;Find adjacent reference
			For Cont2 = 1 To AStarPathfinding_MaxAdjacents%
				If Node2\Adjacent[Cont2] = NodeObject% Then
					;Shift array
					For Cont3 = Cont2 To AStarPathfinding_MaxAdjacents%
						If Cont3 < AStarPathfinding_MaxAdjacents% Then 
							Node2\Adjacent[Cont3] = Node2\Adjacent[Cont3+1]
						Else
							Node2\Adjacent[Cont3] = 0
						EndIf
					Next
				EndIf
			Next
		EndIf
	Next
	
	;Delete node
	Delete Node
	
	Return 1
End Function

;;; <summary>Add a path request to the stack</summary>
;;; <param name="StartNode">Starting node handle</param>
;;; <param name="EndNode">Ending node handle</param>
;;; <remarks></remarks>
;;; <returns>Request Handle</returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathfinding_RequestPath%(StartNode%, EndNode%)
	;Check for errors
	If StartNode% = 0 Or EndNode% = 0 Or StartNode% = EndNode% Then Return 0 ;Invalid handle
	If Object.AStarPathfinding_Node(StartNode%) = Null Or Object.AStarPathfinding_Node(EndNode%) = Null Then Return 0 ;Node doesent exists
	
	Local Request.AStarPathfinding_Request
	
	;Loop through stack array
	For Cont = 1 To AStarPathfinding_MaxRequests%
		If AStarPathfinding_RequestStack(Cont) = Null Then
			;Add new path
			Request = New AStarPathfinding_Request
			Request\StartNode = StartNode%
			Request\EndNode = EndNode%
			Request\Status = AStarPathfinding_Waiting%
			
			;Append to stack array
			AStarPathfinding_RequestStack(Cont) = Request
			Exit
		EndIf
		If Cont = AStarPathfinding_MaxRequests% Then Return 0 ;Max requests reached
	Next
	
	;Return request handle
	Return Handle(Request)
End Function

;;; <summary>Drops a request and delete all related data</summary>
;;; <param name="RequestHandle">Request handle</param>
;;; <param name="DeleteRequest">1 to delete all related data (waypoints and request type)</param>
;;; <remarks></remarks>
;;; <returns>1 if successfull</returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathfinding_DropRequest%(RequestHandle%, DeleteRequest%=1)
	If RequestHandle% = 0 Then Return 0 ;Invalid handle
	
	;Get request object
	Local Request.AStarPathfinding_Request = Object.AStarPathfinding_Request(RequestHandle%)
	If Request = Null Then Return 0 ;Request not found
	
	;Delete request
	If DeleteRequest% Then
		;Delete request path
		If Request\Status = AStarPathfinding_Done% Then
			For Cont = Request\WaypointStart To Request\WaypointEnd
				Delete Object.AStarPathfinding_Waypoint(Cont)
			Next
		EndIf
		
		Delete Request
	EndIf
	
	;Loop through stack array
	For Cont = 1 To AStarPathfinding_MaxRequests%
		If Handle(AStarPathfinding_RequestStack(Cont)) = RequestHandle% Then			
			;Shift array
			If Cont2 < AStarPathfinding_MaxRequests% Then
				For Cont2 = Cont+1 To AStarPathfinding_MaxRequests%
					AStarPathfinding_RequestStack(Cont2-1) = AStarPathfinding_RequestStack(Cont2)
				Next
			EndIf
			
			;Success
			Exit
		EndIf
	Next
	
	Return 1
End Function

;;; <summary>Update pathfinding algorithm</summary>
;;; <param name="MaxCalculationTime">Max time spent calculating</param>
;;; <remarks></remarks>
;;; <returns>1 if any path was calculated, 0 if there are no paths</returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathfinding_UpdateRequests(MaxCalculationTime%=5)
	;Get topmost request
	Local Request.AStarPathfinding_Request = AStarPathfinding_RequestStack(1)
	
	If Request = Null Then Return 0 ;No requests to update
	
	;Start timer
	Local Timer% = MilliSecs()

	;Create variables
	Local Node.AStarPathfinding_Node, Node2.AStarPathfinding_Node
	Local NodeHandle%, Waypoint.AStarPathfinding_Waypoint
	Local Nothing% ;Variable used to fix a Blitz3D Handle Bug
	
	;Loop
	Repeat
		;Check if first execution
		If Request\Status% = AStarPathfinding_Calculating% Then ;Update
			;Get lowest score node
			NodeHandle% = BinaryHeap_Remove%(AStarPathfinding_BHThread%)
			
			;No path found
			If NodeHandle% = 0 Then
				;Change Request status
				Request\Status = AStarPathfinding_NoPath%
				
				;Drop Request (without deleting)
				AStarPathfinding_DropRequest%(Handle(Request),0)
				
				;Clear nodes status
				For Node.AStarPathfinding_Node = Each AStarPathfinding_Node
					Node\List%   = AStarPathfinding_Unvisited%
					Node\G%      = 0
					Node\H%      = 0
					Node\T%      = 0
					Node\Parent% = 0
				Next
				
				;Clear binary heaps
				BinaryHeap_Delete%(AStarPathfinding_BHThread%)
				
				Return 1
			EndIf
			
			;Close node
			Node = Object.AStarPathfinding_Node(NodeHandle%)
			Node\List = AStarPathfinding_ClosedList%
			
			;Check if EndNode is reached
			If Handle(Node) = Request\EndNode Then
				;Create last waypoint
				Waypoint = New AStarPathfinding_Waypoint
				Waypoint\Node% = Handle(Node)
				
				Request\WaypointEnd% = Handle(Waypoint)
				
				;Create waypoints
				While Node\Parent <> 0
					Node = Object.AStarPathfinding_Node(Node\Parent)
					Waypoint = New AStarPathfinding_Waypoint
					Waypoint\Node% = Handle(Node)
					Nothing% = Handle(Waypoint) ;Fixing a Blitz3D bug
				Wend
				
				Request\WaypointStart% = Handle(Waypoint)
				
				;Change Request Status
				Request\Status = AStarPathfinding_Done%
				
				;Drop Request (without deleting)
				AStarPathfinding_DropRequest%(Handle(Request),0)
				
				;Clear nodes status
				For Node.AStarPathfinding_Node = Each AStarPathfinding_Node
					Node\List%   = AStarPathfinding_Unvisited%
					Node\G%      = 0
					Node\H%      = 0
					Node\T%      = 0
					Node\Parent% = 0
				Next
				
				;Clear binary heaps
				BinaryHeap_Delete%(AStarPathfinding_BHThread%)
				
				Return 1
			EndIf
			
			;Open Adjacent nodes
			For Cont = 1 To AStarPathfinding_MaxAdjacents%
				If Node\Adjacent[Cont] = 0 Then Exit
				
				Node2 = Object.AStarPathfinding_Node(Node\Adjacent[Cont])
				If Node2\List% = AStarPathfinding_Unvisited% Then
					Node2\List%  = AStarPathfinding_OpenList%
					Node2\G      = Node\G + Node\AdjacentG[Cont]
					Node2\H      = AStarPathfinding_CalculateH(Handle(Node2), Request\EndNode)
					Node2\T      = AStarPathfinding_CalculateT(Handle(Node2), Request\StartNode, Request\EndNode)
					Node2\Parent = Handle(Node)
					If Not BinaryHeap_Add%(AStarPathfinding_BHThread%, (Node2\G+Node2\H+Node\T), Handle(Node2)) Then RuntimeError("AStarPathfinding: Impossible to add a node to the Binary Heaps")
				ElseIf Node2\List% = AStarPathfinding_OpenList% Then
					If Node2\G > Node\G + Node\AdjacentG[Cont] ;Modify node
						If Not BinaryHeap_Modify%(AStarPathfinding_BHThread%, (Node2\G+Node2\H+Node\T), Handle(Node2),( Node\G + Node\AdjacentG[Cont])) Then RuntimeError("AStarPathfinding: Impossible to modify a node to the Binary Heaps")
						Node2\G      = Node\G + Node\AdjacentG[Cont]
						Node2\Parent = Handle(Node)
					EndIf
				EndIf				
			Next
			
		ElseIf Request\Status = AStarPathfinding_Waiting% ;First execution
			Request\Status% = AStarPathfinding_Calculating%
			
			;Initilize BinaryHeaps
			AStarPathfinding_BHThread% = BinaryHeap_New%(BinaryHeap_SORT_SMALLEST)
			
			;Get starting node
			Node = Object.AStarPathfinding_Node(Request\StartNode)
			
			;Close starting node
			Node\List = AStarPathfinding_ClosedList%
			
			;Open nodes adjacent to Starting Node
			For Cont = 1 To AStarPathfinding_MaxAdjacents%
				If Node\Adjacent[Cont] = 0 Then Exit
				
				Node2 = Object.AStarPathfinding_Node(Node\Adjacent[Cont])
				If Node2\List% = AStarPathfinding_Unvisited% Then
					Node2\List%  = AStarPathfinding_OpenList%
					Node2\G      = Node\AdjacentG[Cont]
					Node2\H      = AStarPathfinding_CalculateH(Handle(Node2), Request\EndNode)
					Node2\T      = AStarPathfinding_CalculateT(Handle(Node2), Request\StartNode, Request\EndNode)
					Node2\Parent = Handle(Node)
					If Not BinaryHeap_Add%(AStarPathfinding_BHThread%, (Node2\G+Node2\H+Node2\T), Handle(Node2)) Then RuntimeError("AStarPathfinding: Impossible to add a node to the Binary Heaps")
				EndIf
			Next
		EndIf
	Until Timer%+MaxCalculationTime% <= MilliSecs()
	
	Return 1
End Function

;;; <summary>Get the status of a request</summary>
;;; <param name="RequestHandle">Request handle</param>
;;; <remarks></remarks>
;;; <returns>A constant showing the request status</returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathfinding_RequestStatus%(RequestHandle%)
	Local Request.AStarPathfinding_Request = Object.AStarPathfinding_Request(RequestHandle%)
	
	;Check for errors
	If Request = Null Then Return 0
	
	Return Request\Status%
End Function

;;; <summary>Get the handle of the starting waypoint</summary>
;;; <param name="RequestHandle"></param>
;;; <remarks></remarks>
;;; <returns></returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathfinding_RequestWaypointStart%(RequestHandle%)
	Local Request.AStarPathfinding_Request = Object.AStarPathfinding_Request(RequestHandle%)
	
	;Check for errors
	If Request = Null Then Return 0
	
	Return Request\WaypointStart
End Function

;;; <summary>Get the handle of the ending waypoint</summary>
;;; <param name="RequestHandle"></param>
;;; <remarks></remarks>
;;; <returns></returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathfinding_RequestWaypointEnd%(RequestHandle%)
	Local Request.AStarPathfinding_Request = Object.AStarPathfinding_Request(RequestHandle%)
	
	;Check for errors
	If Request = Null Then Return 0
	
	Return Request\WaypointEnd
End Function

;;; <summary>Get the position of a waypoint</summary>
;;; <param name="WaypointHandle">Waypoint handle</param>
;;; <param name="ReturnArray%[2]">A blitz array to return the values</param>
;;; <remarks></remarks>
;;; <returns>Void, but changes the ReturnArray</returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathfinding_WaypointPosition%(WaypointHandle%, ReturnArray%[2])
	Local Waypoint.AStarPathfinding_Waypoint = Object.AStarPathfinding_Waypoint(WaypointHandle%)
	
	;Check if waypoint exists
	If Waypoint = Null Then Return 0
	
	Local Node.AStarPathfinding_Node = Object.AStarPathfinding_Node(Waypoint\Node)
	
	;Check if node exists
	If Node = Null Then Return 0
	
	;Set return array
	ReturnArray[0] = Node\Position[0]
	ReturnArray[1] = Node\Position[1]
	ReturnArray[2] = Node\Position[2]
	
	Return 1
End Function

;;; <summary>Get the waypoint name</summary>
;;; <param name="WaypointHandle"></param>
;;; <remarks></remarks>
;;; <returns></returns>
;;; <subsystem></subsystem>
;;; <example></example>
Function AStarPathfinding_WaypointName$(WaypointHandle%)
	Local Waypoint.AStarPathfinding_Waypoint = Object.AStarPathfinding_Waypoint(WaypointHandle%)
	
	;Check if waypoint exists
	If Waypoint = Null Then Return 0
	
	Local Node.AStarPathfinding_Node = Object.AStarPathfinding_Node(Waypoint\Node)
	
	;Check if node exists
	If Node = Null Then Return 0
	
	Return Node\Name$
End Function