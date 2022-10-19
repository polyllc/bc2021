package poly;
import battlecode.common.*;
import scala.collection.Map$class;

import java.util.ArrayList;
import java.util.List;

public class Slanderer {
    RobotController rc;
    Communications comms;
    Navigation nav;
    int turnCount = 0;

    int turnsInDirection = 0;
    boolean initialMove = false;

    MapLocation myBase = null;
    boolean stopMoving = false;
    int startedRound;
    boolean tryingToEmpower = false;
    int myBaseID = 0;
    int numRoundsWithFlag = 0;

    int numRoundsWOFlag = 0;
    Direction slDirectionGoing = Direction.CENTER;
    MapLocation slLocationGoing = new MapLocation(0,0);
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    public Slanderer(RobotController r) {
        this.rc = r;
        comms = new Communications(rc);
        Navigation nav = new Navigation(rc);
    }


    int startTurn = turnCount;

    List<MapLocation> enemyBases = new ArrayList<MapLocation>();

    //todo GET AWAY FROM POLITICIANS

    public void takeTurn() throws GameActionException {
        nav = new Navigation(rc);
        if(rc.isReady()) {

            for(MapLocation loc : enemyBases){
                if(rc.getLocation().add(slDirectionGoing).distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(loc) + 80){
                    slDirectionGoing = loc.directionTo(rc.getLocation());
                }
            }

            if(myBase != null) {
                if (rc.getRoundNum() - startTurn >= 280) {
                    //slLocationGoing = myBase;
                }

                if(rc.getLocation().add(slDirectionGoing).distanceSquaredTo(myBase) > rc.getLocation().distanceSquaredTo(myBase) + 45){
                 //   slDirectionGoing = rc.getLocation().directionTo(myBase);
                }
                if(rc.getLocation().distanceSquaredTo(myBase) < 15){
                    slDirectionGoing = myBase.directionTo(rc.getLocation());
                }

            }



            if(initialMove == false && rc.getFlag(rc.getID()) == 0){
                for (RobotInfo robot : rc.senseNearbyRobots(25)) {
                    if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                        if (robot.getTeam() == rc.getTeam()) {
                            myBase = robot.getLocation();
                            slDirectionGoing = robot.getLocation().directionTo(rc.getLocation());
                            initialMove = true;
                            myBaseID = robot.getID();
                        }
                    }
                }
            }

            if(myBaseID != 0) {
                if (rc.canGetFlag(myBaseID)) {
                    int[] flag = comms.decypherFlag(rc.getFlag(myBaseID));
                    if (flag[0] == 1 || flag[0] == 8) {
                        if (!enemyBases.contains(new MapLocation(flag[1] + myBase.x, flag[2] + myBase.y))) {
                            enemyBases.add(new MapLocation(flag[1] + myBase.x, flag[2] + myBase.y));
                        }
                    } else if (flag[0] == 3) {
                        if (enemyBases.contains(new MapLocation(flag[1] + myBase.x, flag[2] + myBase.y))) {
                            enemyBases.remove(new MapLocation(flag[1] + myBase.x, flag[2] + myBase.y));
                        }
                    }
                }
            }

            if(turnsInDirection == 64) {
                slDirectionGoing = directions[(int) Math.floor(Math.random() * 8)];
                turnsInDirection = 0;
            }

            for (RobotInfo robot : rc.senseNearbyRobots(25)) {
                if(robot.getTeam() != rc.getTeam()){
                    slDirectionGoing = robot.getLocation().directionTo(rc.getLocation());
                }
                if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER && robot.getTeam() == rc.getTeam()){
                   // slDirectionGoing = robot.getLocation().directionTo(rc.getLocation());
                }
                if(robot.getTeam() == Team.NEUTRAL){
                    slDirectionGoing = robot.getLocation().directionTo(rc.getLocation());
                }
                if (robot.getTeam() != rc.getTeam() && myBase != null) {

                }
            }

            if (slLocationGoing != new MapLocation(0, 0) && slLocationGoing.x != 0 && slLocationGoing.y != 0) {
              //  System.out.println("Going to location: " + slLocationGoing);
                if (rc.getLocation().distanceSquaredTo(slLocationGoing) < 5) {
                    slLocationGoing = new MapLocation(0, 0);
                }
                nav.move(slLocationGoing);
            } else if (slDirectionGoing != Direction.CENTER && stopMoving == false) {
             //   System.out.println("Going to direction: " + slLocationGoing);
             //   System.out.println("DIRECTION IS NOT CENTER");
                if (nav.tryMove(slDirectionGoing)) {
                    turnsInDirection++;
                } else {

                }
            }
        }
    }
}