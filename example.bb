Graphics 800,600,16,2
Include "a-star pathfinding.bb"

;Array to store pathfinding nodes
Dim Tile%(16,12)

Local Position[2]
;Create nodes
For x = 0 To 16 Step 1
	For y = 0 To 12 Step 1
		Position[0] = x
		Position[1] = y
		Tile(x,y) = AStarPathfinding_AddNode(Position,x+"-"+y)
	Next
Next

;Add ajacent nodes
For x = 0 To 16-1 Step 1
	For y = 0 To 12-1 Step 1
		If x > 0    Then AStarPathfinding_AttachNode(Tile(x,y),Tile(x-1,y),100)
		If y > 0    Then AStarPathfinding_AttachNode(Tile(x,y),Tile(x,y-1),100) 
		If x < 16-1 Then AStarPathfinding_AttachNode(Tile(x,y),Tile(x+1,y),100)
		If y < 12-1 Then AStarPathfinding_AttachNode(Tile(x,y),Tile(x,y+1),100)
	Next
Next

Local StartX, StartY
Local EndX, EndY

SetBuffer BackBuffer()
While Not KeyHit(1)
	If MouseHit(1)
		StartX% = MouseX()/50
		StartY% = MouseY()/50
	EndIf
	
	If MouseHit(2)
		EndX% = MouseX()/50
		EndY% = MouseY()/50
	EndIf
	
	If KeyHit(57) Then
		AStarPathfinding_DeleteNode(Tile(StartX,StartY))
	EndIf
	
	If MouseHit(3)
		DebugLog AStarPathfinding_DropRequest%(Request)
		Request% = AStarPathfinding_RequestPath%(Tile(StartX,StartY),Tile(EndX,EndY))
		DebugLog Request%
	EndIf

	AStarPathfinding_UpdateRequests(10)

	Cls
	For x = 0 To 16-1
		For y = 0 To 12-1
			Color 255,255,255
			Rect x*50,y*50,50,50,0
			
			If RectsOverlap(x*50,y*50,50,50,MouseX(),MouseY(),1,1)
				Node.AStarPathfinding_Node = Object.AStarPathfinding_Node(Tile(x,y))
				Color 0,255,0
				Text MouseX()+20,MouseY(),X+"-"+Y
			EndIf
		Next
	Next
	
	If AStarPathfinding_RequestStatus(Request) = AStarPathfinding_Done%
		For Cont = AStarPathfinding_RequestWaypointStart(Request) To AStarPathfinding_RequestWaypointEnd(Request) Step -1
			AStarPathfinding_WaypointPosition%(Cont, Position)
			
			Color 0,0,200
			Rect Position[0]*50, Position[1]*50,50,50,1
			
		Next
	EndIf
	
	If 0
	For Node.AStarPathfinding_Node = Each AStarPathfinding_Node
		NodeX% = 50*Int(Left(Node\Name,Instr(Node\Name,"-")-1))
		NodeY% = 50*Int(Right(Node\Name,Len(Node\Name)-Instr(Node\Name,"-")))
		If Node\List = AStarPathfinding_OpenList%
			Color 100,0,100
			Rect NodeX, NodeY,50,50,1
			Color 255,255,255
			Text NodeX+25,NodeY,Node\G,1
			Text NodeX+25,NodeY+15,Node\H,1
			Text NodeX+25,NodeY+30,(Node\G+Node\H),1
		ElseIf Node\List = AStarPathfinding_ClosedList%
			Color 100,100,0
			Rect NodeX, NodeY,50,50,1
			Color 255,255,255
			Text NodeX+25,NodeY,Node\G,1
			Text NodeX+25,NodeY+15,Node\H,1
			Text NodeX+25,NodeY+30,(Node\G+Node\H),1
		EndIf
	Next
	EndIf
	
	Color 100,100,100
	Rect StartX*50,StartY*50,50,50,1
	
	Color 200,0,0
	Rect EndX*50,EndY*50,50,50,1
	
	Flip
	;WaitKey	
Wend
