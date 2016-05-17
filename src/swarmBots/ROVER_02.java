package swarmBots;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.Group;
import common.MapTile;
import common.ScanMap;
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

	boolean[] paths = new boolean[4]; // N,E,W,S
	Coord currentLoc = null;
	Coord previousLoc = null;

	Coord startLoc = null;
	Coord targetLoc = null;

	Coord[] targetLocations = new Coord[3];
	int i = 0;
	BufferedReader in;
	PrintWriter out;
	String rovername;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost";
	static final int PORT_ADDRESS = 9537;

	Set<String> scienceLocations = new HashSet<String>();

	// all the sockets of blue team - output
	List<Socket> outputSockets = new ArrayList<Socket>();

	// objects contains each rover IP, port, and name
	List<Group> blue = new ArrayList<Group>();

	// every science detected will be added in to this set
	Set<Coord> science_discovered = new HashSet<Coord>();

	// this set contains all the science the ROVERED has shared
	// thus whatever thats in science_collection that is not in display_science
	// are "new" and "unshared"
	Set<Coord> displayed_science = new HashSet<Coord>();

	// Your ROVER is going to listen for connection with this
	ServerSocket listenSocket;

	String north = "N";
	String south = "S";
	String east = "E";
	String west = "W";
	String direction = west;

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

		/*
		 * connect to all the ROVERS on a separate thread
		 */
		initConnection();
		for (Group group : blue) {
			new Thread(new RoverComm(group.ip, group.port)).start();
		}

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

		String line = "";

		int counter = 0;

		boolean stuck = false; // just means it did not change locations between
								// requests,
								// could be velocity limit or obstruction etc.
		boolean blocked = false;

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
			startLoc = Loc;
			targetLocations[2] = new Coord(Loc.xpos, Loc.ypos);
		}

		out.println("TARGET_LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println("ROVER_02 check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			Coord Loc = extractLOC(line);
			targetLoc = Loc;

		}

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

			// **** get equipment listing ****
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			System.out.println("ROVER_02 equipment list results " + equipment + "\n");

			// ***** do a SCAN *****
			this.doScan();
			scanMap.debugPrintMap();

			// MOVING

			MapTile[][] scanMapTiles = scanMap.getScanMap();

			make_a_move(scanMapTiles);
			// moving(scanMapTiles);

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
		ROVER_02 client = new ROVER_02();
		client.run();
	}

	/*
	 * -------------------------------------------- functions
	 * ----------------------------------
	 */

	// make a move

	public void move(String direction) {
		out.println("MOVE " + direction);
	}

	// list of science locations nearby
	public void scanScience(MapTile[][] scanMapTiles) {
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;

		int xpos, ypos;
		int coordX = currentLoc.xpos - centerIndex;
		int coordY = currentLoc.ypos - centerIndex;

		for (int i = 0; i < scanMapTiles.length; i++) {
			for (int j = 0; j < scanMapTiles.length; j++) {
				if (scanMapTiles[i][j].getScience() == Science.RADIOACTIVE
						|| scanMapTiles[i][j].getScience() == Science.ORGANIC) {
					xpos = coordX + i;
					ypos = coordY + j;
					Coord coord = new Coord(scanMapTiles[i][j].getTerrain(), scanMapTiles[i][j].getScience(), xpos,
							ypos);
					science_discovered.add(coord);
				}
			}
		}

	}

	/**
	 * write to each rover the coords of a tile that contains radiation. will
	 * only write to them if the coords are new.
	 */
	private void shareScience() {
		for (Coord c : science_discovered) {
			if (!displayed_science.contains(c)) {
				for (Socket s : outputSockets)
					try {
						new DataOutputStream(s.getOutputStream()).writeBytes(c.toString() + "\r\n");
					} catch (Exception e) {

					}
				displayed_science.add(c);
			}
		}
	}

	// have we reached a wall ??

	public boolean isWall(MapTile[][] scanMapTiles, String direction) {
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

		if (scanMapTiles[x][y].getTerrain() == Terrain.NONE)
			return true;
		return false;
	}

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

	// Move
	public void make_a_move(MapTile[][] scanMapTiles) throws IOException {
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex;
		scanScience(scanMapTiles);
		System.out.println("SCIENCE DISCOVERED: " + science_discovered);
		shareScience();

		if (isValidMove(scanMapTiles, direction)) {
			move(direction);

		} else {

			while (!isValidMove(scanMapTiles, direction)) {

				direction = switchDirection(scanMapTiles, direction);
			}
			move(direction);

		}
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