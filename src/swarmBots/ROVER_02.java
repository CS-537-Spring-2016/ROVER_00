//// Using A-Star algorithm
//// get the target
//// find the nearest in the 7*7 matrix
//// then use A star from the current location to the selectedLoc

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
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
<<<<<<< HEAD
=======
//import common.ScienceCoord;
>>>>>>> anu
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

<<<<<<< HEAD
	int timeremaining = 0;
	boolean[] paths = new boolean[4]; // N,E,W,S
	Coord currentLoc = null;
	Coord previousLoc = null;

	Coord startLoc = null;
	Coord targetLoc = null;

=======
>>>>>>> anu
	Coord[] targetLocations = new Coord[3];
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
	String direction = west;
	
	/* Communication Module*/
    RoverCommunication rocom;

	RoverCommunication rocom;
	
	public ROVER_02() {
		// constructor
		System.out.println("ROVER_02 rover object constructed");
		rovername = "ROVER_02";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 200; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public ROVER_02(String serverAddress) {
		// constructor
		System.out.println("ROVER_02 rover object constructed");
		rovername = "ROVER_02";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 300; // in milliseconds - smaller is faster, but the server
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
		
		// ******************* SET UP COMMUNICATION MODULE by Shay *********************
        /* Your Group Info*/
        Group group = new Group(rovername, SERVER_ADDRESS, 53702, RoverDriveType.WALKER,
                RoverToolType.RADIATION_SENSOR, RoverToolType.CHEMICAL_SENSOR);

<<<<<<< HEAD
		/*
		 * connect to all the ROVERS on a separate thread
		 */
		
		 Group group = new Group(rovername, SERVER_ADDRESS, 53702, RoverDriveType.WALKER,
	                RoverToolType.RADIATION_SENSOR, RoverToolType.CHEMICAL_SENSOR);

	        /* Setup communication, only communicates with gatherers */
	        rocom = new RoverCommunication(group,
	                Group.getGatherers(Group.blueCorp(SERVER_ADDRESS)));

	        /* Connect to the other ROVERS */
	        rocom.run();
		
=======
        /* Setup communication, only communicates with gatherers */
        rocom = new RoverCommunication(group);
        rocom.setGroupList(Group.getGatherers());

        // ******************************************************************
        
		// Gson gson = new GsonBuilder().setPrettyPrinting().create();
>>>>>>> anu

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

		out.println("START_LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println("ROVER_02 check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			Coord Loc = extractLOC(line);
			targetLocations[2] = new Coord(Loc.xpos, Loc.ypos);
		}

		

//		out.println("TIMER");
//		line = in.readLine();
//		timeremaining = Integer.parseInt(line);

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
			System.out.println("ROVER_02 currentLoc at start: " + currentLoc);

			// after getting location set previous equal current to be able to
			// check for stuckness and blocked later
			previousLoc = currentLoc;

			// **** get equipment listing ****
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			// System.out.println("ROVER_02 equipment list results drive " +
			// equipment.get(0));
			System.out.println("ROVER_02 equipment list results " + equipment + "\n");

			// ***** do a SCAN *****
			// System.out.println("ROVER_02 sending SCAN request");
			this.doScan();
			scanMap.debugPrintMap();

			// MOVING

			MapTile[][] scanMapTiles = scanMap.getScanMap();

<<<<<<< HEAD
			make_a_move(scanMapTiles);
			// moving(scanMapTiles);

=======
			make_a_move(scanMapTiles, currentLoc);
>>>>>>> anu
			// another call for current location
			out.println("LOC");
			line = in.readLine();
			if (line.startsWith("LOC")) {
				currentLoc = extractLOC(line);
			}

			System.out.println("ROVER_02 currentLoc after recheck: " + currentLoc);
			System.out.println("ROVER_02 previousLoc: " + previousLoc);

			// test for stuckness

			System.out.println("ROVER_02 stuck test " + stuck);
			// System.out.println("ROVER_02 blocked test " + blocked);
			
            /* ********* Detect and Share Science  by Shay ***************/
			doScan();
            rocom.detectAndShare(scanMap.getScanMap(), currentLoc, 3);
            /* *************************************************/

			Thread.sleep(sleepTime);

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
		sStr = sStr.substring(4);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);
			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;

	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_02 client = new ROVER_02("localhost");
		client.run();
	}

	/////////////////////////////////// NEWLY ADDED FUNCTIONS
	/////////////////////////////////// ////////////////////////////

	// make a move

	public void move(String direction) {
		out.println("MOVE " + direction);
	}

<<<<<<< HEAD
=======
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
	
>>>>>>> anu
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
<<<<<<< HEAD

	public String switchDirectionEdge(MapTile[][] scanMapTiles, String direction) {
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

	// is it a corner ?

	public void switchCornerDirection(MapTile[][] scanMapTiles) {

		boolean n = true, s = true, e = true, w = true;
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex;

		if (scanMapTiles[x - 1][y].getTerrain() == Terrain.NONE)
			w = false;
		if (scanMapTiles[x][y - 1].getTerrain() == Terrain.NONE)
			n = false;
		if (scanMapTiles[x + 1][y].getTerrain() == Terrain.NONE)
			e = false;
		if (scanMapTiles[x][y + 1].getTerrain() == Terrain.NONE)
			s = false;

		if (e && s) {
			// 1
			if (currentDir == south) {
				currentDir = east;
				nextDir = east;
			} else if (currentDir == east)
				currentDir = south;
			nextDir = south;
		}
		if (w && s) {
			// 2
			if (currentDir == west) {
				currentDir = south;
				nextDir = south;
			} else if (currentDir == south) {
				currentDir = west;
				nextDir = west;
			}
		}
		if (n && w) {
			// 3
			if (currentDir == north) {
				currentDir = west;
				nextDir = west;
			} else if (currentDir == west) {
				currentDir = north;
				nextDir = north;
			}
		}
		if (n && e) {
			// 4
			if (currentDir == east) {
				currentDir = north;
				nextDir = north;

			} else if (currentDir == north) {
				nextDir = east;
				currentDir = east;
			}
		}

	}

	public void possiblePaths(MapTile[][] scanMapTiles) {
		paths[0] = isValidMove(scanMapTiles, "N");
		paths[1] = isValidMove(scanMapTiles, "E");
		paths[2] = isValidMove(scanMapTiles, "W");
		paths[3] = isValidMove(scanMapTiles, "S");

	}

	public int isAWall(MapTile[][] scanMapTiles) {
		int c = 0;
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex;

		if (scanMapTiles[x - 1][y].getTerrain() == Terrain.NONE) {
			System.out.println("WAll left");
			c++;
		}

		if (scanMapTiles[x][y - 1].getTerrain() == Terrain.NONE) {
			System.out.println("WAll up");
			c++;
		}
		if (scanMapTiles[x + 1][y].getTerrain() == Terrain.NONE) {
			System.out.println("WAll right");
			c++;
		}
		if (scanMapTiles[x][y + 1].getTerrain() == Terrain.NONE) {
			System.out.println("WAll down");
			c++;
		}
		return c;
	}

	String nextDir = west;
	String currentDir = west;

	public void oneDeviation_West(MapTile[][] scanMapTiles) {

		if (isAWall(scanMapTiles) == 2) {
			switchCornerDirection(scanMapTiles);
			move(currentDir);
		}

		if (isValidMove(scanMapTiles, north)) {
			currentDir = north;
			nextDir = west;
		} else if (isValidMove(scanMapTiles, south)) {
			currentDir = south;
			nextDir = west;
		} else {
			currentDir = east;
			nextDir = east;
		}

	}

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

	public void moving(MapTile[][] scanMapTiles) {
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex;
		possiblePaths(scanMapTiles);

		currentDir = west;

		if (isValidMove(scanMapTiles, currentDir)) {
			// nextDir = west;
			move(currentDir);
		} else {
			oneDeviation_West(scanMapTiles);
			move(currentDir);
		}
		currentDir = nextDir;
	}

=======
	
	// To be explained by Siddhi
	
>>>>>>> anu
	// Move
	public void make_a_move(MapTile[][] scanMapTiles, Coord currentLoc) throws IOException {
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex;
<<<<<<< HEAD
		scanScience(scanMapTiles);
		System.out.println("SCIENCE DISCOVERED: " + science_discovered);
		shareScience();
=======
		scanScience(scanMapTiles, currentLoc);
>>>>>>> anu

		if (isValidMove(scanMapTiles, direction)) {
			move(direction);

		} else {

			while (!isValidMove(scanMapTiles, direction)) {

				direction = switchDirection(scanMapTiles, direction);
			}
			move(direction);
		}
	}

<<<<<<< HEAD
	// Reverse move

	public void makeAReverseMove(MapTile[][] scanMapTiles) throws IOException {
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex;
		scanScience(scanMapTiles);
		System.out.println("SCIENCE DISCOVERED: " + science_discovered);
		shareScience();
		double[][] distance = nearestTileToTarget(scanMapTiles, startLoc);
		Coord nextTargetCoord;

		nextTargetCoord = largestinArray(scanMapTiles, distance);
		
		// continue here ...
		// implement a star in a different file
		//change the nodes to coord
		//use it to find the path required

	}

	public Coord largestinArray(MapTile[][] scanMapTiles, double[][] distance) {
		Coord coord = null;
		double max = 0;

		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex;
		int xpos, ypos;

		int coordX = currentLoc.xpos - centerIndex;
		int coordY = currentLoc.ypos - centerIndex;

		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				if (max < distance[i][j] && isValidTile(scanMapTiles, i, j)  ) {
					xpos = coordX + i;
					ypos = coordY + j;
					max = distance[i][j];
					coord = new Coord(xpos, ypos);
				}
			}
		}
		return coord;
	}

	public double[][] nearestTileToTarget(MapTile[][] scanMapTiles, Coord TargetLoc) throws IOException {
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex;
		int xpos, ypos;

		int coordX = currentLoc.xpos - centerIndex;
		int coordY = currentLoc.ypos - centerIndex;

		int targetX = TargetLoc.xpos;
		int targetY = TargetLoc.ypos;

		double[][] distance = null;

		for (int i = 0; i < scanMapTiles.length; i++) {
			for (int j = 0; j < scanMapTiles.length; j++) {

				xpos = coordX + i;
				ypos = coordY + j;

				distance[i][j] = Math.sqrt((targetX - xpos) * (targetX - xpos) + (targetY - ypos) * (targetY - ypos));
			}
		}

		return distance;

	}

	public boolean isValidTile(MapTile[][] scanMapTiles, int x, int y) {

		if (scanMapTiles[x][y].getTerrain() == Terrain.SAND || scanMapTiles[x][y].getTerrain() == Terrain.NONE
				|| scanMapTiles[x][y].getHasRover() == true)
			return false;

		return true;
	}

	/*
	 * ----------------------------------- COMMUNICATION PROTOCOL
	 * ----------------------------------------
	 */

	/**
	 * Try to connect each socket on a separate thread. Will try until it works.
	 * When socket is created, save it to a LIST
	 *
	 */
	class RoverComm implements Runnable {

		String ip;
		int port;
		Socket socket;

		public RoverComm(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		@Override
		public void run() {
			do {
				try {
					socket = new Socket(ip, port);
				} catch (UnknownHostException e) {

				} catch (IOException e) {

				}
			} while (socket == null);

			outputSockets.add(socket);
			System.out.println(socket.getPort() + " " + socket.getInetAddress());
		}

	}

	/**
	 * add all the group's rover into a LIST
	 */
	public void initConnection() {
		// dummy value # 1
		blue.add(new Group("Dummy Group #1", "localhost", 53799));

		// blue rooster
		blue.add(new Group("GROUP_01", "localhost", 53701));
		blue.add(new Group("GROUP_03", "localhost", 53703));
		blue.add(new Group("GROUP_04", "localhost", 53704));
		blue.add(new Group("GROUP_05", "localhost", 53705));
		blue.add(new Group("GROUP_06", "localhost", 53706));
		blue.add(new Group("GROUP_07", "localhost", 53707));
		blue.add(new Group("GROUP_08", "localhost", 53708));
		blue.add(new Group("GROUP_09", "localhost", 53709));
	}

}

// Using A-Star algorithm
// get the target
// find the nearest in the 7*7 matrix
// then use A star from the current location to the selectedLoc
=======
}
>>>>>>> anu
