<<<<<<< HEAD
package communication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import enums.RoverDriveType;
import enums.RoverToolType;

/** Represent each Group or Team. Use to identify and communicates with the
 * ROVERS
 * 
 * @author Shay */
public class Group {

    private String ip;
    private int port;
    private String name;
    private RoverToolType[] tools;
    private RoverDriveType driveType;

    public Group(String name, String ip, int port) {
        this.name = name;
        this.port = port;
        this.ip = ip;

    }

    public Group(String name, String ip, int port, RoverDriveType driveType,
            RoverToolType... tools) {
        this(name, ip, port);
        this.driveType = driveType;
        this.tools = tools;
    }

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public RoverToolType[] getTools() {
        return tools;
    }

    public void setTools(RoverToolType[] tools) {
        this.tools = tools;
    }

    public RoverDriveType getDriveType() {
        return driveType;
    }

    public void setDriveType(RoverDriveType driveType) {
        this.driveType = driveType;
    }

    public String toString() {
        return "name=" + name;
    }

    /** @param ip
     *            I assume that all the ROVERS will be connected to the same ip
     *            address. This method will return a list of all the ROVERS with
     *            provided ip address. This ip address should be yours. Please
     *            note that this will only return ROVERS that can gather, as
     *            they are the only ones that expects communications.
     *            Furthermore the list will includes a dummy rover at port 53799
     *            which is use mostly for testing purposes.
     * @return */
    public final static List<Group> blueCorp(String ip) {

        List<Group> blueCorp = new ArrayList<Group>();

        /* For testing purpose. This server will display any message sent to
         * it */
        blueCorp.add(new Group("ROVER_DUMMY", ip, 53799, RoverDriveType.WALKER, RoverToolType.DRILL,
                RoverToolType.HARVESTER));

        /* All the ROVERS and their equipments */
        blueCorp.add(new Group("ROVER_01", ip, 53701, RoverDriveType.WALKER, RoverToolType.DRILL,
                RoverToolType.SPECTRAL_SENSOR));
        blueCorp.add(new Group("ROVER_02", ip, 53702, RoverDriveType.WALKER,
                RoverToolType.RADIATION_SENSOR, RoverToolType.CHEMICAL_SENSOR));
        blueCorp.add(new Group("ROVER_03", ip, 53703, RoverDriveType.TREADS, RoverToolType.DRILL,
                RoverToolType.HARVESTER));
        blueCorp.add(new Group("ROVER_04", ip, 53704, RoverDriveType.WALKER, RoverToolType.DRILL,
                RoverToolType.RADAR_SENSOR));
        blueCorp.add(new Group("ROVER_05", ip, 53705, RoverDriveType.WHEELS,
                RoverToolType.RANGE_BOOSTER, RoverToolType.SPECTRAL_SENSOR));
        blueCorp.add(new Group("ROVER_06", ip, 53706, RoverDriveType.WHEELS,
                RoverToolType.RANGE_BOOSTER, RoverToolType.RADIATION_SENSOR));
        blueCorp.add(new Group("ROVER_07", ip, 53707, RoverDriveType.TREADS,
                RoverToolType.HARVESTER, RoverToolType.RADAR_SENSOR));
        blueCorp.add(new Group("ROVER_08", ip, 53708, RoverDriveType.TREADS,
                RoverToolType.HARVESTER, RoverToolType.SPECTRAL_SENSOR));
        blueCorp.add(new Group("ROVER_09", ip, 53709, RoverDriveType.WALKER, RoverToolType.DRILL,
                RoverToolType.CHEMICAL_SENSOR));

        return blueCorp;
    }

    public static List<Group> filteredGroup(List<Group> groups, RoverToolType tool) {
        return groups.stream().filter(g -> g.tools[0] == tool || g.tools[1] == tool)
                .collect(Collectors.toList());
    }
    
    public static List<Group> filteredGroup(List<Group> groups, RoverToolType... tools) {
        Set<Group> groupSet = new HashSet<Group>();
        for (RoverToolType rtt : tools) {
            groupSet.addAll(filteredGroup(groups, rtt));
        }
        return new ArrayList<Group>(groupSet);
    }

    public static List<Group> getGatherers(List<Group> groups) {
        return filteredGroup(groups, RoverToolType.DRILL, RoverToolType.HARVESTER);
    }

    public static List<Group> filteredGroup(List<Group> groups, RoverDriveType drive) {
        return groups.stream().filter(g -> g.driveType == drive).collect(Collectors.toList());
    }

    public static List<Group> filteredGroup(List<Group> groups, RoverDriveType... driveTypes) {
        List<Group> groupAccumualtor = groups;
        for (RoverDriveType rdt : driveTypes) {
            groupAccumualtor.addAll(filteredGroup(groups, rdt));
        }
        return groupAccumualtor;
    }
=======
package communication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import enums.RoverDriveType;
import enums.RoverToolType;

/** Represent each Group or Team. Use to identify and communicates with the
 * ROVERS
 * 
 * @author Shay */
public class Group {

    private String ip;
    private int port;
    private String name;
    private RoverToolType[] tools;
    private RoverDriveType driveType;

    /** Command Center, Not really a ROVER. Use to test ROVER communication */
    public static final Group G99 = new Group("COMMAND_CENTER", "localhost", 53799,
            RoverDriveType.WALKER, RoverToolType.DRILL, RoverToolType.HARVESTER);

    /* ROVER 1 - 9 */
    public static final Group G1 = new Group("ROVER_01", "localhost", 53701, RoverDriveType.WALKER,
            RoverToolType.DRILL, RoverToolType.SPECTRAL_SENSOR);
    public static final Group G2 = new Group("ROVER_02", "localhost", 53702, RoverDriveType.WALKER,
            RoverToolType.RADIATION_SENSOR, RoverToolType.CHEMICAL_SENSOR);
    public static final Group G3 = new Group("ROVER_03", "localhost", 53703, RoverDriveType.TREADS,
            RoverToolType.DRILL, RoverToolType.HARVESTER);
    public static final Group G4 = new Group("ROVER_04", "localhost", 53704, RoverDriveType.WALKER,
            RoverToolType.DRILL, RoverToolType.RADAR_SENSOR);
    public static final Group G5 = new Group("ROVER_05", "localhost", 53705, RoverDriveType.WHEELS,
            RoverToolType.RANGE_BOOSTER, RoverToolType.SPECTRAL_SENSOR);
    public static final Group G6 = new Group("ROVER_06", "localhost", 53706, RoverDriveType.WHEELS,
            RoverToolType.RANGE_BOOSTER, RoverToolType.RADIATION_SENSOR);
    public static final Group G7 = new Group("ROVER_07", "localhost", 53707, RoverDriveType.TREADS,
            RoverToolType.HARVESTER, RoverToolType.RADAR_SENSOR);
    public static final Group G8 = new Group("ROVER_08", "localhost", 53708, RoverDriveType.TREADS,
            RoverToolType.HARVESTER, RoverToolType.SPECTRAL_SENSOR);
    public static final Group G9 = new Group("ROVER_09", "localhost", 53709, RoverDriveType.WALKER,
            RoverToolType.DRILL, RoverToolType.CHEMICAL_SENSOR);

    /** A list of all the ROVERS plus the Command Center */
    public final static List<Group> BLUE_CORP = Arrays.asList(Group.G99, Group.G1, Group.G2,
            Group.G3, Group.G4, Group.G5, Group.G6, Group.G7, Group.G8, Group.G9);

    public Group(String name, String ip, int port) {
        this.name = name;
        this.port = port;
        this.ip = ip;
    }

    public Group(String name, String ip, int port, RoverDriveType driveType,
            RoverToolType... tools) {
        this(name, ip, port);
        this.driveType = driveType;
        this.tools = tools;
    }

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public RoverToolType[] getTools() {
        return tools;
    }

    public void setTools(RoverToolType[] tools) {
        this.tools = tools;
    }

    public RoverDriveType getDriveType() {
        return driveType;
    }

    public void setDriveType(RoverDriveType driveType) {
        this.driveType = driveType;
    }

    public String toString() {
        return "name=" + name;
    }

    /** Filtered BLUE CROP by Tool Type. For example, can return only ROVERS
     * with a DRILL or SENSOR_BOOSTER */
    private static List<Group> filteredGroup(RoverToolType tool) {
        return Group.BLUE_CORP.stream().filter(g -> g.tools[0] == tool || g.tools[1] == tool)
                .collect(Collectors.toList());
    }

    /** Filtered BLUE CROP by Tool Type. For example, can return only ROVERS
     * with a DRILL or SENSOR_BOOSTER */
    private static List<Group> filteredGroup(RoverToolType... tools) {
        Set<Group> groupSet = new HashSet<Group>();
        for (RoverToolType rtt : tools) {
            groupSet.addAll(filteredGroup(rtt));
        }
        return new ArrayList<Group>(groupSet);
    }

    /** A list of ROVER that contains a DRILL or a Harvester; All ROVERS that
     * can GATHER */
    public static List<Group> getGatherers() {
        return filteredGroup(RoverToolType.DRILL, RoverToolType.HARVESTER);
    }
>>>>>>> anu
}