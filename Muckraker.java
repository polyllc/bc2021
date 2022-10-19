package poly;
import battlecode.common.*;


public class Muckraker {
    RobotController rc;
    Communications comms;
    int turnsInDirection = 0;
    boolean initialMove = false;
    Navigation nav;
    MapLocation myBase = null;
    boolean stopMoving = false;

    boolean startSurround = false;

    int startedRound;

    int numRoundsWithFlag = 0;

    int numRoundsWOFlag = 0;

    int clearFlag3 = -1;

    MapLocation muLocationGoing = new MapLocation(0,0);
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
    public Muckraker(RobotController r) throws GameActionException {
        this.rc = r;
        startedRound = rc.getRoundNum();
        rc.setFlag(0);
        comms = new Communications(rc);
        nav = new Navigation(rc);
    }
    Direction muDirectionGoing = Direction.CENTER;
    int zigzag = 0; //0 for going one to the right, then 1 for going back to the normal position, then 2 for going to the left and then 3 for going back to normal position
    int zigzagLen = 2;
    int zigzagDur = 0;


    public void takeTurn () throws  GameActionException {
        nav = new Navigation(rc);
        if(rc.isReady()) {

            if(clearFlag3 == 0){
                rc.setFlag(0);
                clearFlag3 = -1;
            }
            if(clearFlag3 != -1) {
                clearFlag3--;
            }

            // System.out.println("startSurround is " + startSurround);

            gridMovement();
            zigzagMovement();

            if(startSurround == false) {
                for (RobotInfo robot : rc.senseNearbyRobots(30)) {

                    if (startedRound > rc.getRoundNum() - 10) {
                        if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER && myBase == null) {
                            myBase = robot.getLocation();
                        }
                    }


                    if (robot.getType() == RobotType.SLANDERER && robot.getTeam() != rc.getTeam()) {
                        if (robot.getLocation().distanceSquaredTo(rc.getLocation()) <= 12) {
                            if (rc.canExpose(robot.getLocation())) {
                                rc.expose(robot.getLocation());
                            }
                        } else {
                            nav.move(robot.getLocation());
                        }
                    }

                    if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {

                        if (robot.getTeam() == rc.getTeam() && comms.decypherFlag(rc.getFlag(robot.getID()))[0] == 7) {
                            muLocationGoing = robot.getLocation().add(directions[(int) Math.floor(Math.random() * 8)]).add(directions[(int) Math.floor(Math.random() * 8)]);
                        }
                        else if(comms.decypherFlag(rc.getFlag(robot.getID()))[0] == 1){
                            muLocationGoing = new MapLocation(0,0);
                        }

                        if (myBase == robot.getLocation() && rc.getTeam() != robot.getTeam()) {
                            myBase = null;
                        }

                        //limit mucks or use poli's
                        int[] flag = comms.decypherFlag(rc.getFlag(rc.getID()));
                        if (rc.getFlag(rc.getID()) != 0 && flag[0] != 1) {
                            if (robot.getTeam() == rc.getTeam() && myBase != null) {
                                if (flag[1] == robot.getLocation().x && flag[2] == robot.getLocation().y) {
                                    rc.setFlag(comms.cypherFlag(3, new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y)));
                                    if (myBase != null) {
                                        muLocationGoing = myBase;
                                    }
                                }
                            }

                            //  //Found a base, so starting to surround it
                            //  System.out.println("I have a flag, but it's not code 1");
                            if (robot.getTeam() != rc.getTeam() && robot.getTeam() != Team.NEUTRAL) {
                                //       System.out.println("Found the hq that i was told about");
                                startSurround = true;
                                muLocationGoing = new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y).add(myBase.directionTo(robot.getLocation()));
                                rc.setFlag(comms.cypherFlag(1, new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y)));
                            }

                            //Found a base, but somehow i dont have a myBase loc
                            if (robot.getTeam() != rc.getTeam() && robot.getTeam() != Team.NEUTRAL && myBase == null) {
                                if(robot.getTeam() != Team.NEUTRAL) {
                                    myBase = rc.getLocation();
                                    startSurround = true;
                                    muLocationGoing = new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y).add(myBase.directionTo(robot.getLocation()));
                                    rc.setFlag(comms.cypherFlag(5, new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y)));
                                }
                            }


                        }
                        //Intel flag
                        else if (flag[0] == 1 && rc.getTeam() == robot.getTeam()) {
                            //  System.out.println("I have a flag and it's code 1");

                            if (myBase == null) {
                                myBase = robot.getLocation();
                            }

                            if (flag[1] == robot.getLocation().x && flag[2] == robot.getLocation().y) {
                                rc.setFlag(comms.cypherFlag(3, new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y)));
                                if (myBase != null) {
                                    //  muLocationGoing = myBase;
                                }
                            } else {
                                muDirectionGoing = robot.getLocation().directionTo(rc.getLocation());
                            }
                        }
                        else if(flag[0] == 1){
                            if(robot.getTeam() == Team.NEUTRAL){
                                muDirectionGoing = robot.getLocation().directionTo(rc.getLocation());
                            }
                        }
                        //Starting to find other bases
                        else {
                            // System.out.println("I have no flag"); //fix
                            if (robot.getTeam() != rc.getTeam() && myBase != null) {
                                // System.out.println("But I found a place where I can go");
                                if (robot.getLocation() != myBase && robot.getTeam() != Team.NEUTRAL) {
                                    rc.setFlag(comms.cypherFlag(1, new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y)));
                                }
                                else{
                                    if(clearFlag3 == -1) {
                                        rc.setFlag(comms.cypherFlag(8, new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y)));
                                        clearFlag3 = 5;
                                    }
                                    int code = comms.decypherFlag(rc.getFlag(rc.getID()))[0];
                                    if(code == 8 || code == 1){
                                        if(clearFlag3 == -1){
                                            rc.setFlag(Integer.parseInt("9" + String.valueOf(robot.getInfluence())));
                                        }
                                    }
                                }
                            }
                        }

                        if (robot.getTeam() == rc.getTeam() && comms.decypherFlag(rc.getFlag(robot.getID()))[0] == 3) {
                            rc.setFlag(rc.getFlag(robot.getID()));
                            clearFlag3 = 10;
                        }

                    }


                    //
                    if (robot.getTeam() == rc.getTeam()) {
                        if (rc.canGetFlag(robot.getID())) {
                            int flag[] = comms.decypherFlag(rc.getFlag(robot.getID()));
                            if (flag[0] == 3) {
                                if(flag.length > 1) {
                                    if(comms.decypherFlag(rc.getFlag(rc.getID())).length > 1) {
                                        if (flag[1] == comms.decypherFlag(rc.getFlag(rc.getID()))[1] && flag[2] == comms.decypherFlag(rc.getFlag(rc.getID()))[2]) {
                                            rc.setFlag(0);
                                        }
                                    }
                                }
                            }

                            if (flag[0] == 5 && robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                                System.out.println("Going to base as my ec told me so");
                                startSurround = true;
                                muLocationGoing = new MapLocation(flag[1] + myBase.x, flag[2] + myBase.y).add(myBase.directionTo(robot.getLocation()));
                                rc.setFlag(comms.cypherFlag(5, new MapLocation(flag[1], flag[2])));
                            }

                            if (rc.getFlag(rc.getID()) == 0) {
                                System.out.println("My flag is 0");
                                if (myBase != null) {
                                    System.out.println("I do have a base");
                                    if (flag[0] == 5) {
                                        System.out.println("Found someone that found another base");
                                //        startSurround = true;
                                        muLocationGoing = new MapLocation(flag[1] + myBase.x, flag[2] + myBase.y).add(myBase.directionTo(robot.getLocation()));
                                        rc.setFlag(comms.cypherFlag(5, new MapLocation(flag[1], flag[2])));
                                    }
                                }
                            }
                        }
                    }
                }


                if (initialMove == false && rc.getFlag(rc.getID()) == 0) {
                    for (RobotInfo robot : rc.senseNearbyRobots(25)) {
                        if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                            if (robot.getTeam() == rc.getTeam()) {
                                muDirectionGoing = robot.getLocation().directionTo(rc.getLocation());
                                initialMove = true;
                            }
                        }
                    }
                }

                if (turnsInDirection == 20) {
                    muDirectionGoing = directions[(int) Math.floor(Math.random() * 8)];
                    turnsInDirection = 0;
                }
            }
            else{ //start surround is true
                for (RobotInfo robot : rc.senseNearbyRobots(12)) {
                    if (robot.getType() == RobotType.SLANDERER && robot.getTeam() != rc.getTeam()) {
                        if (rc.canExpose(robot.getLocation())) {
                            rc.expose(robot.getLocation());
                        }
                    }
                }
                surround();
            }

            if (muLocationGoing != new MapLocation(0, 0) && muLocationGoing.x != 0 && muLocationGoing.y != 0 && stopMoving == false) {
                // System.out.println("Going to location: " + muLocationGoing);
                if (rc.getLocation().distanceSquaredTo(muLocationGoing) <= 1) {
                    muLocationGoing = new MapLocation(0, 0);
                }
                nav.move(muLocationGoing);
            } else if (muDirectionGoing != Direction.CENTER && stopMoving == false) {
                //   System.out.println("Going to direction: " + muLocationGoing);
                //  System.out.println("DIRECTION IS NOT CENTER");
                if (nav.tryMove(muDirectionGoing)) {
                    turnsInDirection++;
                } else {

                }
            }
        }

    }

    private void zigzagMovement() {
        if(zigzagDur == 0) {
            if (muDirectionGoing != Direction.CENTER) {
                switch (zigzag) {
                    case 0:
                    case 3:
                        muDirectionGoing = muDirectionGoing.rotateRight(); break;
                    case 1:
                    case 2:
                        muDirectionGoing = muDirectionGoing.rotateLeft(); break;
                }
                zigzagDur = zigzagLen;
                zigzag++;
                if(zigzag == 4){
                    zigzag = 0;
                }
            } else {
                zigzag = 0;
            }
        }
        if(zigzagDur > 0) {
            zigzagDur--;
        }
    }

    private void gridMovement() {
        if(muDirectionGoing != Direction.CENTER){
            for(RobotInfo robot : rc.senseNearbyRobots()){
                if(robot.getTeam() == rc.getTeam() && robot.getType() == RobotType.MUCKRAKER) {
                    int muRange = 10;
                    if (rc.getLocation().add(muDirectionGoing).distanceSquaredTo(robot.getLocation()) + muRange < rc.getLocation().distanceSquaredTo(robot.getLocation()) + muRange){
                        if(rc.getLocation().add(muDirectionGoing.rotateLeft()).distanceSquaredTo(robot.getLocation()) + muRange > rc.getLocation().distanceSquaredTo(robot.getLocation()) + muRange){
                            muDirectionGoing = muDirectionGoing.rotateLeft();
                        }
                        if(rc.getLocation().add(muDirectionGoing.rotateRight()).distanceSquaredTo(robot.getLocation()) + muRange > rc.getLocation().distanceSquaredTo(robot.getLocation()) + muRange){
                            muDirectionGoing = muDirectionGoing.rotateRight();
                        }
                    }
                }
            }
        }
    }

    private void surround() throws GameActionException {
        //Make it so if it finds the enemy base, it'll calculate position on its own rather than trusting the other teammate that gave them the
        System.out.println("1");
        for(RobotInfo robot : rc.senseNearbyRobots()){
            if(robot.getTeam() != rc.getTeam() && robot.getType() == RobotType.ENLIGHTENMENT_CENTER){
                int numEnemies = 0;
                for (RobotInfo r : rc.senseNearbyRobots(25)) {
                    if (r.getType() != RobotType.ENLIGHTENMENT_CENTER && r.getTeam() != rc.getTeam()) {
                        numEnemies++;
                    }
                }
                if(robot.getTeam() != Team.NEUTRAL){
                    muLocationGoing = robot.getLocation();
                    if (myBase != null) {
                        rc.setFlag(comms.cypherFlag(5, new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y)));
                    }
                }
                else{
                    startSurround = false;
                    break;
                }
            }
        }
        System.out.println("2");
        int flag[] = comms.decypherFlag(rc.getFlag(rc.getID()));
        System.out.println("2.1");
        if(rc.getLocation().distanceSquaredTo(muLocationGoing) <= 4){
            System.out.println("Stopping movement");
            stopMoving = true;
        }
        else{
            System.out.println("2.2");
            if(flag[0] == 5) {
                System.out.println("2.3");
                if (rc.canSenseLocation(muLocationGoing)) {
                    System.out.println("2.4");
                    if (rc.senseRobotAtLocation(muLocationGoing) != null && myBase != null) {
                        System.out.println("2.5");

                    }
                }
            }
        }
        System.out.println("3");
        if(flag[0] != 0){
            if(stopMoving == true){
                for(RobotInfo robot : rc.senseNearbyRobots()) {
                    if (robot.getTeam() == rc.getTeam() && robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                        if(rc.getLocation().distanceSquaredTo(robot.getLocation()) < 3) {
                            stopMoving = false;
                        }
                    }
                }
            }
        }
    }
}