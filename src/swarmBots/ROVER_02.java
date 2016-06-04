


////Using A-Star algorithm
////get the target
////find the nearest in the 7*7 matrix
////then use A star from the current location to the selectedLoc

package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
//import common.ScienceCoord;
import communication.Group;
import communication.RoverCommunication;
import enums.RoverDriveType;
import enums.RoverToolType;
import enums.Science;
import enums.Terrain;

/**
* The seed that this program is built on is a chat program example found here:
* http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
* publishing their code examples
* 
* 
* Rover_02 --- > Walker , Radiation , Chemical
*/

public class ROVER_02 {

Coord[] targetLocations = new Coord[3];
Coord target = null;
int i = 0;
BufferedReader in;
PrintWriter out;
String rovername;
ScanMap scanMap;
int sleepTime;
String SERVER_ADDRESS = "192.168.1.106";
static final int PORT_ADDRESS = 9537;

Set<String> scienceLocations = new HashSet<String>();

String north = "N";
String south = "S";
String east = "E";
String west = "W";
String direction = east;
String previousMove;
/* Communication Module */
RoverCommunication rocom;

public ROVER_02() {
	// constructor
	System.out.println("ROVER_02 rover object constructed");
	rovername = "ROVER_02";
	SERVER_ADDRESS = "localhost";
	// this should be a safe but slow timer value
	sleepTime = 300; // in milliseconds - smaller is faster, but the server
						// will cut connection if it is too small
}

public ROVER_02(String serverAddress) {
	// constructor
	System.out.println("ROVER_02 rover object constructed");
	rovername = "ROVER_02";
	SERVER_ADDRESS = serverAddress;
	sleepTime = 200; // in milliseconds - smaller is faster, but the server
						// will cut connection if it is too small
}

/**
* Connects to the server then enters the processing loop.
*/
private void run() throws IOException, InterruptedException {

	// Make connection and initialize streams
	// TODO - need to close this socket
	Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS); // set port
																// here
	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	out = new PrintWriter(socket.getOutputStream(), true);

	// ******************* SET UP COMMUNICATION MODULE by Shay
	// *********************
	/* Your Group Info */
	Group group = new Group(rovername, SERVER_ADDRESS, 53702, RoverDriveType.WALKER, RoverToolType.RADIATION_SENSOR,
			RoverToolType.CHEMICAL_SENSOR);

	/* Setup communication, only communicates with gatherers */
	rocom = new RoverCommunication(group);
	rocom.setGroupList(Group.getGatherers());

	// ******************************************************************

	// Gson gson = new GsonBuilder().setPrettyPrinting().create();

	// Process all messages from server, wait until server requests Rover ID
	// name
	while (true) {
		String line = in.readLine();
		if (line.startsWith("SUBMITNAME")) {
			out.println(rovername); // This sets the name of this instance
									// of a swarmBot for identifying the
									// thread to the server
			break;
		}
	}

	// ******** Rover logic *********
	// int cnt=0;
	String line = "";

	int counter = 0;

	boolean stuck = false; // just means it did not change locations between
							// requests,
							// could be velocity limit or obstruction etc.
	boolean blocked = false;

	Coord currentLoc = null;
	Coord previousLoc = null;

	targetLocations[0] = new Coord(0, 0);

	// out.println("START_LOC");
	// line = in.readLine();
	// if (line == null) {
	// System.out.println("ROVER_02 check connection to server");
	// line = "";
	// }
	// if (line.startsWith("LOC")) {
	// // loc = line.substring(4);
	// Coord Loc = extractLOC(line);
	// targetLocations[2] = new Coord(Loc.xpos, Loc.ypos);
	// }

	System.out.println("getting target loc");
	
	 out.println("TARGET_LOC");
	 line = in.readLine();
	 System.out.println("line =" + line);
	 if (line == null) {
	 System.out.println("ROVER_02 check connection to server");
	 line = "";
	 }
	 if (line.startsWith("TARGET_LOC")) {
	 // loc = line.substring(4);
		 System.out.println("inside if");
	System.out.println(line);
	target = extractLOC(line);
//	 target = Loc;
	// target = new Coord(Loc.xpos,Loc.ypos);
	 }
//	 System.out.println(target.xpos + " , " + target.ypos);

	//target = new Coord(49, 49);
	// start Rover controller process
	while (true) {

		// currently the requirements allow sensor calls to be made with no
		// simulated resource cost

		// **** location call ****
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println("ROVER_02 check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = extractLOC(line);
		}
//		System.out.println("ROVER_02 currentLoc at start: " + currentLoc);

		// after getting location set previous equal current to be able to
		// check for stuckness and blocked later
		previousLoc = currentLoc;

		// **** get equipment listing ****
		ArrayList<String> equipment = new ArrayList<String>();
		equipment = getEquipment();
		// System.out.println("ROVER_02 equipment list results drive " +
		// equipment.get(0));
//		System.out.println("ROVER_02 equipment list results " + equipment + "\n");

		// ***** do a SCAN *****
//		System.out.println("ROVER_02 sending SCAN request");
		this.doScan();
//		System.out.println("debug");
		scanMap.debugPrintMap();

		// MOVING
		System.out.println("moving");
		MapTile[][] scanMapTiles = scanMap.getScanMap();
	//	System.out.println("calling make a star");
		
	//	makeAStarmove(scanMapTiles, currentLoc, target);

	//	System.out.println("Astar done");

		out.println("LOC");
		line = in.readLine();
		if (line == null) {
							line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = extractLOC(line);
		}
		
		if(currentLoc.equals(target))
		{
			out.println("START_LOC");
			 line = in.readLine();
			 if (line == null) {
			 System.out.println("ROVER_02 check connection to server");
			 line = "";
			 }
			 if (line.startsWith("START_LOC")) {
			 // loc = line.substring(4);
			 Coord Loc = extractLOC(line);
			 target = Loc;
			 }
			 continue;
		}
		
		for (int i = 0; i < 10; i++) {
			out.println("LOC");
			line = in.readLine();
			if (line == null) {
								line = "";
			}
			if (line.startsWith("LOC")) {
				// loc = line.substring(4);
				currentLoc = extractLOC(line);
			}
			//System.out.println("ROVER_02 currentLoc at start: " + currentLoc);
			make_a_move(scanMapTiles, currentLoc);
		}

		// test for stuckness

		System.out.println("ROVER_02 stuck test " + stuck);
		// System.out.println("ROVER_02 blocked test " + blocked);

		/* ********* Detect and Share Science by Shay ***************/
		doScan();
		rocom.detectAndShare(scanMap.getScanMap(), currentLoc, 3);
		/* *************************************************/

		// Thread.sleep(sleepTime);

		// System.out.println("ROVER_02 ------------ bottom process control
		// --------------");

	}

}

// ################ Support Methods ###########################

private void clearReadLineBuffer() throws IOException {
	while (in.ready()) {
		// System.out.println("ROVER_02 clearing readLine()");
		String garbage = in.readLine();
	}
}

// method to retrieve a list of the rover's equipment from the server
private ArrayList<String> getEquipment() throws IOException {
	// System.out.println("ROVER_02 method getEquipment()");
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	out.println("EQUIPMENT");

	String jsonEqListIn = in.readLine(); // grabs the string that was
											// returned first
	if (jsonEqListIn == null) {
		jsonEqListIn = "";
	}
	StringBuilder jsonEqList = new StringBuilder();
	// System.out.println("ROVER_02 incomming EQUIPMENT result - first
	// readline: " + jsonEqListIn);

	if (jsonEqListIn.startsWith("EQUIPMENT")) {
		while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
			if (jsonEqListIn == null) {
				break;
			}
			// System.out.println("ROVER_02 incomming EQUIPMENT result: " +
			// jsonEqListIn);
			jsonEqList.append(jsonEqListIn);
			jsonEqList.append("\n");
			// System.out.println("ROVER_02 doScan() bottom of while");
		}
	} else {
		// in case the server call gives unexpected results
		clearReadLineBuffer();
		return null; // server response did not start with "EQUIPMENT"
	}

	String jsonEqListString = jsonEqList.toString();
	ArrayList<String> returnList;
	returnList = gson.fromJson(jsonEqListString, new TypeToken<ArrayList<String>>() {
	}.getType());
	// System.out.println("ROVER_02 returnList " + returnList);

	return returnList;
}

// sends a SCAN request to the server and puts the result in the scanMap
// array
public void doScan() throws IOException {
	// System.out.println("ROVER_02 method doScan()");
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	out.println("SCAN");

	String jsonScanMapIn = in.readLine(); // grabs the string that was
											// returned first
	if (jsonScanMapIn == null) {
		// System.out.println("ROVER_02 check connection to server");
		jsonScanMapIn = "";
	}
	StringBuilder jsonScanMap = new StringBuilder();
	// System.out.println("ROVER_02 incomming SCAN result - first readline:
	// " + jsonScanMapIn);

	if (jsonScanMapIn.startsWith("SCAN")) {
		while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
			// System.out.println("ROVER_02 incomming SCAN result: " +
			// jsonScanMapIn);
			jsonScanMap.append(jsonScanMapIn);
			jsonScanMap.append("\n");
			// System.out.println("ROVER_02 doScan() bottom of while");
		}
	} else {
		// in case the server call gives unexpected results
		clearReadLineBuffer();
		return; // server response did not start with "SCAN"
	}
	// System.out.println("ROVER_02 finished scan while");

	String jsonScanMapString = jsonScanMap.toString();
	// debug print json object to a file
	// new MyWriter( jsonScanMapString, 0); //gives a strange result -
	// prints the \n instead of newline character in the file

	// System.out.println("ROVER_02 convert from json back to ScanMap
	// class");
	// convert from the json string back to a ScanMap object
	scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);
}

// this takes the LOC response string, parses out the x and y values and
// returns a Coord object
public static Coord extractLOC(String sStr) {
	int indexOf;
	indexOf = sStr.indexOf(" ");
	
	sStr = sStr.substring(indexOf +1);
	
	if (sStr.lastIndexOf(" ") != -1) {
		String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
		
		String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
		
		return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
	}
	return null;

}

public static Coord extractTargetLOC(String sStr) {
	
	String[] splitString = sStr.split(" ");
	
	for(String s : splitString)
		System.out.println(s);
	
	String xStr = splitString[1];
	String yStr = splitString[2];
	System.out.println("X value = " + splitString[1]);
	System.out.println("Y value = " + splitString[2]);
	
	return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));

}

/**
* Runs the client
*/
public static void main(String[] args) throws Exception {
	ROVER_02 client = new ROVER_02("192.168.1.106");
	client.run();
}

/////////////////////////////////// NEWLY ADDED FUNCTIONS
/////////////////////////////////// ////////////////////////////

// make a move

public void move(String direction) {
	previousMove = direction;
	out.println("MOVE " + direction);
}

// To be explained by Darsh

// check for sand / rover / wall in the next move
public boolean isValidMove(MapTile[][] scanMapTiles, String direction) {
	int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
	int x = centerIndex, y = centerIndex;

	switch (direction) {
	case "N":
		y = y - 1;
		break;
	case "S":
		y = y + 1;
		break;
	case "E":
		x = x + 1;
		break;
	case "W":
		x = x - 1;
		break;
	}

	if (scanMapTiles[x][y].getTerrain() == Terrain.SAND || scanMapTiles[x][y].getTerrain() == Terrain.NONE
			|| scanMapTiles[x][y].getHasRover() == true)
		return false;

	return true;
}

// To be explained by Anuradha

// list of science locations nearby
public void scanScience(MapTile[][] scanMapTiles, Coord currentLoc) {
	int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
	int x = centerIndex, y = centerIndex;

	int xpos, ypos;
	int coordX = currentLoc.xpos - centerIndex;
	int coordY = currentLoc.ypos - centerIndex;

	for (int i = 0; i < scanMapTiles.length; i++) {
		for (int j = 0; j < scanMapTiles.length; j++) {
			if (scanMapTiles[i][j].getScience() == Science.RADIOACTIVE
					|| scanMapTiles[i][j].getScience() == Science.ORGANIC) {
				xpos = coordX + i;
				ypos = coordY + j;
				scienceLocations.add(scanMapTiles[i][j].getTerrain() + " " + scanMapTiles[i][j].getScience() + " "
						+ xpos + " " + ypos);
			}
		}
	}

}

// To be explained by Suhani
// if blocked / stuck change the direction
public String switchDirection(MapTile[][] scanMapTiles, String direction) {
	switch (direction) {
	case "E":
		return south;
	case "S":
		return west;
	case "N":
		return east;
	case "W":
		return north;
	default:
		return null;

	}

}

// To be explained by Siddhi

// Move
public void make_a_move(MapTile[][] scanMapTiles, Coord currentLoc) throws IOException {
	int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
	int x = centerIndex, y = centerIndex;
	scanScience(scanMapTiles, currentLoc);
	try {

		if (isValidMove(scanMapTiles, direction)) {
			System.out.println("Random move "+ direction);
			move(direction);

			Thread.sleep(sleepTime);

		} else {

			while (!isValidMove(scanMapTiles, direction)) {

				direction = switchDirection(scanMapTiles, direction);
			}
			System.out.println("Random move "+ direction);
			move(direction);
			Thread.sleep(sleepTime);
		}
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

// Annother move function(MapTile[][] scanMapTiles)
///////////////////////////////////////////////////////////////////////////////////////////////////
/* Extra */
//////////////////////////////////////////////////////////////////////////////////////////////////

static PriorityQueue<Tile> open;
static boolean closed[][];
public static final int V_H_COST = 10;
static boolean blocked[][];
static Tile[][] grid = new Tile[7][7];
static int startI = 3, startJ = 3;
static int endI, endJ;

public static class Tile {
	int x, y;
	int h = 0;
	int f, g;
	Tile parent;

	public Tile(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "[" + this.x + ", " + this.y + "]";
	}
}

public static void setEndCell(int i, int j) {
	endI = i;
	endJ = j;
}

public static void setBlocked(int i, int j) {
//	System.out.println("bloacking " + i + "," + j);
	grid[i][j] = null;
//	System.out.println("end");
}

static void checkAndUpdateCost(Tile current, Tile t, int cost) {
	if (t == null || closed[t.x][t.y])
		return;
	int t_final_cost = t.h + cost;

	boolean inOpen = open.contains(t);
	if (!inOpen || t_final_cost < t.f) {
		t.f = t_final_cost;
		t.parent = current;
		if (!inOpen)
			open.add(t);
	}
}

public void Astar(MapTile[][] scanMapTiles) {
	//System.out.println("astar");
	Tile temp = new Tile(3, 3);
	open.add(temp);
	Tile current;
	//System.out.println("End : " + endI + " , " + endJ);
	while (true) {
		current = open.poll();
		if (current == null)
			break;
		closed[current.x][current.y] = true;

		if (current.x == endI && current.y == endJ) {
			return;
		}

		Tile t;

		if (current.x - 1 >= 0) {
			t = grid[current.x - 1][current.y];
			checkAndUpdateCost(current, t, current.f + V_H_COST);
		}
		if (current.y - 1 >= 0) {
			t = grid[current.x][current.y - 1];
			checkAndUpdateCost(current, t, current.f + V_H_COST);
		}

		if (current.y + 1 < grid[0].length) {
			t = grid[current.x][current.y + 1];
			checkAndUpdateCost(current, t, current.f + V_H_COST);
		}

		if (current.x + 1 < grid.length) {
			t = grid[current.x + 1][current.y];
			checkAndUpdateCost(current, t, current.f + V_H_COST);
		}
	}
}

public List<Tile> neighboursCoord(Tile currentLoc) {
	List<Tile> adj = new ArrayList<Tile>();

	adj.add(new Tile(currentLoc.x - 1, currentLoc.y));
	adj.add(new Tile(currentLoc.x + 1, currentLoc.y));
	adj.add(new Tile(currentLoc.x, currentLoc.y + 1));
	adj.add(new Tile(currentLoc.x, currentLoc.y - 1));

	return adj;
}

public boolean isValidTile(MapTile[][] scanMapTiles, Tile t)

{
	int x = t.x;
	int y = t.y;
	if (scanMapTiles[x][y].getTerrain() == Terrain.SAND || scanMapTiles[x][y].getTerrain() == Terrain.NONE
			|| scanMapTiles[x][y].getHasRover() == true)
		return false;

	return true;
}

public boolean isValidTile_XY(MapTile[][] scanMapTiles, int x, int y)

{
	// int x = t.x;
	// int y = t.y;
//	System.out.println("is valid " + x + "," + y);
	if (scanMapTiles[x][y].getTerrain() == Terrain.SAND || scanMapTiles[x][y].getTerrain() == Terrain.NONE
			|| scanMapTiles[x][y].getHasRover() == true)
		return false;

	return true;
}

public void scanSand(MapTile[][] scanMapTiles) {
	int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
	int x = centerIndex, y = centerIndex;

//	System.out.println("scan sand function");
	for (int i = 0; i < scanMapTiles.length; i++) {
		for (int j = 0; j < scanMapTiles.length; j++) {
			if (scanMapTiles[i][j].getTerrain() == Terrain.SAND || scanMapTiles[i][j].getTerrain() == Terrain.NONE
					|| scanMapTiles[i][j].getHasRover() == true) {
				setBlocked(i, j);
	//			System.out.println("out of set blocked");
				blocked[i][j] = true;
		//		System.out.println("settign bloacked");
			}
		}
	}

	System.out.println("out of for loop");

}

public void callAStar(MapTile[][] scanMapTiles, int ei, int ej) {
//	System.out.println("call a star");
	grid = new Tile[7][7];
	closed = new boolean[7][7];
	open = new PriorityQueue<>((Object o1, Object o2) -> {
		Tile c1 = (Tile) o1;
		Tile c2 = (Tile) o2;
		return c1.f < c2.f ? -1 : c1.f > c2.f ? 1 : 0;
	});

//	System.out.println("set end cell");
	setEndCell(ei, ej);
	for (int i = 0; i < 7; ++i) {
		for (int j = 0; j < 7; ++j) {
			grid[i][j] = new Tile(i, j);
			grid[i][j].h = Math.abs(i - endI) + Math.abs(j - endJ);
		}
	}
	grid[3][3].f = 0;
	blocked = new boolean[7][7];
	initialiseBlocked(scanMapTiles);
//	System.out.println("scan sand");
	scanSand(scanMapTiles);
//
//	System.out.println("Grid: ");
//	for (int i = 0; i < 7; ++i) {
//		for (int j = 0; j < 7; ++j) {
//			if (i == 3 && j == 3)
//				System.out.print("SO  "); // Source
//			else if (i == ei && j == ej)
//				System.out.print("DE  "); // Destination
//			else if (grid[i][j] != null)
//				System.out.printf("%-3d ", 0);
//			else
//				System.out.print("BL  ");
//		}
//		System.out.println();
//	}
//	System.out.println();

	
	Astar(scanMapTiles);
//	
	
	System.out.println("\nScores for cells: ");
//	for (int i = 0; i < 7; ++i) {
//		for (int j = 0; j < 7; ++j) {
//			if (grid[i][j] != null)
//				System.out.printf("%-3d ", grid[i][j].f);
//			else
//				System.out.print("BL  ");
//		}
//		System.out.println();
//	}
//	System.out.println();
	
	if (closed[endI][endJ]) {
		// Trace back the path
//		System.out.println("Path: ");
		Tile current = grid[endI][endJ];
//		System.out.print(current);
		String path = reversePath(current);
//		System.out.println(path);
		movePath(path);
	} else {
		System.out.println("No possible path");
	}
}

public void movePath(String path) {
	for (int i = 0; i < path.length(); i++) {
//		System.out.println(path.charAt(i));
		System.out.println("Astar move "+String.valueOf(path.charAt(i)));
		move(String.valueOf(path.charAt(i)));
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

public String reversePath(Tile current) {
	String path = "";
	while (current.parent != null) {
		if (current.x == current.parent.x) {
			if (current.y < current.parent.y) {

				path = "N" + path;
			} else {
				path = "S" + path;
			}
		} else if (current.y == current.parent.y) {
			if (current.x < current.parent.x) {
				path = "W" + path;
			} else {
				path = "E" + path;
			}
		}

//		System.out.print(" -> " + current.parent);
		current = current.parent;
	}
	return path;
}

public void initialiseBlocked(MapTile scanMapTiles[][]) {

	int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
	int x = centerIndex, y = centerIndex;

//	System.out.println("scan sand function");
	for (int i = 0; i < scanMapTiles.length; i++) {
		for (int j = 0; j < scanMapTiles.length; j++) {
			blocked[i][j] = false;
			closed[i][j] = false;

		}
	}
}

public void makeAStarmove(MapTile[][] scanMapTiles, Coord currentLoc, Coord destination) {

	int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
	int x = centerIndex, y = centerIndex;

	int xpos, ypos;
	int coordX = currentLoc.xpos - centerIndex;
	int coordY = currentLoc.ypos - centerIndex;

	int a = 6, b = 6;
	double d = Integer.MAX_VALUE;

	for (int i = 0; i < 7; i++) {
//
//		System.out.println(coordX + 0 + ", " + coordY + i);
//		System.out.println(coordX + i + ", " + coordY + 0);
//		System.out.println(coordX + 6 + ", " + coordY + i);
//		System.out.println(coordX + i + ", " + coordY + 6);

		if (coordX + 0 < 7 && coordY + i < 7 && coordX + 0 > -1 && coordY + i > -1) {
			if (isValidTile_XY(scanMapTiles, coordX + 0, coordY + i)) {
				scanMapTiles[coordX + 0][coordY + i].distance = calDist(coordX + 0, coordY + i, destination.xpos,
						destination.ypos);
				if (d < scanMapTiles[coordX + 0][coordY + i].distance) {
					d = scanMapTiles[coordX + 0][coordY + i].distance;
					a = coordX + 0;
					b = coordY + i;
				}
			} else
				scanMapTiles[coordX + 0][coordY + i].distance = Integer.MAX_VALUE;
		}
		if (coordX + i < 7 && coordY + 0 < 7 && coordX + i > -1 && coordY + 0 > -1) {
			if (isValidTile_XY(scanMapTiles, coordX + i, coordY + 0)) {
				scanMapTiles[coordX + i][coordY + 0].distance = calDist(coordX + i, coordY + 0, destination.xpos,
						destination.ypos);
				if (d < scanMapTiles[coordX + i][coordY + 0].distance) {
					d = scanMapTiles[coordX + i][coordY + 0].distance;
					a = coordX + i;
					b = coordY + 0;
				}
			} else
				scanMapTiles[coordX + i][coordY + 0].distance = Integer.MAX_VALUE;
		}

		if (coordX + 6 < 7 && coordY + i < 7 && coordX + 6 > -1 && coordY + i > -1) {

			if (isValidTile_XY(scanMapTiles, coordX + 6, coordY + i)) {
				scanMapTiles[coordX + 6][coordY + i].distance = calDist(coordX + 6, coordY + i, destination.xpos,
						destination.ypos);
				if (d < scanMapTiles[coordX + 6][coordY + i].distance) {
					d = scanMapTiles[coordX + 6][coordY + i].distance;
					a = coordX + 6;
					b = coordY + i;
				}
			} else
				scanMapTiles[coordX + 6][coordY + i].distance = Integer.MAX_VALUE;
		}

		if (coordX + i < 7 && coordY + 6 < 7 && coordX + i > -1 && coordY + 6 > -1) {
			if (isValidTile_XY(scanMapTiles, coordX + i, coordY + 6)) {
				scanMapTiles[coordX + i][coordY + 6].distance = calDist(coordX + i, coordY + 6, destination.xpos,
						destination.ypos);
				if (d < scanMapTiles[coordX + i][coordY + 6].distance) {
					d = scanMapTiles[coordX + i][coordY + 6].distance;
					a = coordX + i;
					b = coordY + 6;
				}
			} else
				scanMapTiles[coordX + i][coordY + 6].distance = Integer.MAX_VALUE;
		}
	}

	System.out.println("call a star move a and b : " + a + " " + b);
	callAStar(scanMapTiles, a, b);

}

public double calDist(int x1, int y1, int x2, int y2) {
	double dist;

	dist = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
	return dist;
}

}





