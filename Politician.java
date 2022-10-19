package poly;
import battlecode.common.*;



public class Politician {
    RobotController rc;
    Communications comms;
    int turnsInDirection = 0;
    boolean initialMove = false;
    Navigation nav;
    MapLocation myBase = null;
    boolean stopMoving = false;
    int startedRound;
    boolean tryingToEmpower = false;

    int myBaseID = 0;

    int numRoundsWithFlag = 0;

    int numRoundsWOFlag = 0;

    MapLocation poLocationGoing = new MapLocation(0,0);
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
    public Politician(RobotController r) throws GameActionException {
        this.rc = r;
        startedRound = rc.getRoundNum();
        rc.setFlag(0);
        comms = new Communications(rc);
        nav = new Navigation(rc);
    }
    Direction poDirectionGoing = Direction.CENTER;
    public void takeTurn () throws  GameActionException {
        //In order of priority
        //todo coordinated rush at enemy base

        nav = new Navigation(rc);
        passiveFlags();
        if(rc.isReady()) {

            if(poLocationGoing == null){
                poLocationGoing = new MapLocation(0,0);
            }
            System.out.println(poLocationGoing);
            if (poLocationGoing == new MapLocation(0, 0) || poLocationGoing.x == 0 && poLocationGoing.y == 0 || rc.getRoundNum() % 25 == 0) {
                if (rc.getFlag(rc.getID()) != 0) {
                    int flag[] = comms.decypherFlag(rc.getFlag(rc.getID()));
                    if (flag[0] == 2 || flag[0] == 1 || flag[0] == 8) {
                        if(myBase != null) {
                            poLocationGoing = new MapLocation(flag[1] + myBase.x, flag[2] + myBase.y);
                        }
                    }
                }
            }



            for (RobotInfo robot : rc.senseNearbyRobots(25)) {
                if (startedRound > rc.getRoundNum() - 10) {
                    if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER && myBase == null) {
                        myBase = robot.getLocation();
                        myBaseID = robot.ID;
                    }
                }
            }


            if(rc.getRoundNum() > 650) {
                for (RobotInfo robot : rc.senseNearbyRobots(25)) {

                    if (robot.getType() == RobotType.MUCKRAKER && rc.getTeam() != robot.getTeam()) {
                        if (rc.getLocation().distanceSquaredTo(robot.getLocation()) <= 9) {
                            tryingToEmpower = true;
                            System.out.println("Found a muckraker to empower");
                        } else {
                            poLocationGoing = robot.getLocation();
                        }
                    }
                }
            }


            if(rc.getRoundNum() < 650) {
                if (tryingToEmpower == false) {
                    for (RobotInfo robot : rc.senseNearbyRobots(25)) {
                            if (robot.getType() == RobotType.MUCKRAKER && rc.getTeam() != robot.getTeam() && rc.getInfluence() <= 79) {
                                if (rc.getLocation().distanceSquaredTo(robot.getLocation()) <= 9) {
                                    tryingToEmpower = true;
                                    System.out.println("Found a muckraker to empower");
                                } else {
                                    poLocationGoing = robot.getLocation();
                                }
                        }


                        if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {

                            if (myBase == robot.getLocation() && rc.getTeam() != robot.getTeam()) {
                                myBase = null;
                            }


                            if (rc.getFlag(rc.getID()) != 0 && comms.decypherFlag(rc.getFlag(rc.getID()))[0] != 1) {
                                if (robot.getTeam() == rc.getTeam() && myBase != null) {
                                    if (comms.decypherFlag(rc.getFlag(rc.getID()))[1] + myBase.x == robot.getLocation().x && comms.decypherFlag(rc.getFlag(rc.getID()))[2] + myBase.y == robot.getLocation().y) {
                                        rc.setFlag(comms.cypherFlag(3, new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y)));
                                        if (myBase != null) {
                                            // poLocationGoing = myBase; //fix
                                            numRoundsWithFlag = 150;
                                        }
                                    }
                                }
                             //   System.out.println("I have a flag, but it's not code 1");
                                if (robot.getTeam() != rc.getTeam()) {
                                 //   System.out.println("Found the hq that i was told about");
                                    if (rc.canEmpower(9) && robot.getLocation().distanceSquaredTo(rc.getLocation()) <= 9) {
                                       // System.out.println("Empowering the enemy base");
                                        rc.empower(9);
                                    } else if (robot.getLocation().distanceSquaredTo(rc.getLocation()) > 9) {
                                        int numEnemies = 0;
                                        for (RobotInfo r : rc.senseNearbyRobots(25)) {
                                            if (r.getType() != RobotType.ENLIGHTENMENT_CENTER && r.getTeam() != rc.getTeam()) {
                                                numEnemies++;
                                            }
                                        }

                                        if (numEnemies > 3) {
                                            tryingToEmpower = true;
                                            System.out.println("more than one enemy!!!");
                                        }

                                    } else {
                                        tryingToEmpower = true;
                                        System.out.println("Near enough to enemy base to empower");
                                    }
                                }

                                if (robot.getTeam() != rc.getTeam() && myBase == null) {
                                    // System.out.println("Found the hq that i was told about");
                                    if (rc.canEmpower(9) && robot.getLocation().distanceSquaredTo(rc.getLocation()) <= 9) {
                                        //   System.out.println("Empowering the enemy base");
                                        rc.empower(9);
                                    } else {
                                        tryingToEmpower = true;
                                        System.out.println("dont have mybase but empowering because i found an enemy base");
                                    }
                                }
                            } else if ((comms.decypherFlag(rc.getFlag(rc.getID()))[0] == 1 || comms.decypherFlag(rc.getFlag(rc.getID()))[0] == 8) && rc.getTeam() == robot.getTeam()) {
                                //System.out.println("I have a flag and it's code 1");

                                if (myBase == null) {
                                    myBase = robot.getLocation();
                                    myBaseID = robot.ID;
                                }

                                if (comms.decypherFlag(rc.getFlag(rc.getID()))[1] == robot.getLocation().x && comms.decypherFlag(rc.getFlag(rc.getID()))[2] == robot.getLocation().y) {
                                    rc.setFlag(comms.cypherFlag(3, new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y)));
                                    numRoundsWithFlag = 150;
                                }
                            } else {
                            //     System.out.println("I have no flag");
                                int numPoliticians = 0;
                                for (RobotInfo r : rc.senseNearbyRobots()) {
                                    if (rc.getTeam() == r.getTeam() && r.getType() == RobotType.POLITICIAN) {
                                        if (robot.getTeam() != rc.getTeam()) {
                                            if (rc.canEmpower(9) && robot.getLocation().distanceSquaredTo(rc.getLocation()) <= 9) {
                                                rc.empower(9);
                                                System.out.println("Found a fellow politician whom i will commit with");
                                            } else {
                                                nav.move(robot.getLocation());
                                            }
                                        }
                                    }
                                }
                                if (robot.getTeam() != rc.getTeam() && myBase != null) {
                                  //  System.out.println("But I found a place where I can go");
                                    if (robot.getLocation() != myBase) {
                                        rc.setFlag(comms.cypherFlag(2, new MapLocation(robot.getLocation().x - myBase.x, robot.getLocation().y - myBase.y)));
                                        if (myBase != null) {
                                            numRoundsWithFlag = 150;
                                        }
                                    }
                                }

//                                if (robot.getTeam() != rc.getTeam() && robot.getType() == RobotType.ENLIGHTENMENT_CENTER && myBase == null) {
//                                    poLocationGoing = robot.getLocation();
//                                    if (rc.getLocation().distanceSquaredTo(poLocationGoing) <= 9) {
//                                        tryingToEmpower = true;
//                                    }
//                                }
                            }
                        }
                        if (robot.getTeam() == rc.getTeam()) {
                            if (rc.canGetFlag(robot.getID())) {
                                int flag[] = comms.decypherFlag(rc.getFlag(robot.getID()));
                                if (flag[0] == 3) { //fix
                                    if (flag.length > 1 && comms.decypherFlag(rc.getFlag(rc.getID())).length > 1) {
                                        if (flag[1] == comms.decypherFlag(rc.getFlag(rc.getID()))[1] && flag[2] == comms.decypherFlag(rc.getFlag(rc.getID()))[2]) {
                                            rc.setFlag(0);
                                        }
                                    }
                                }
                                if (rc.getFlag(rc.getID()) == 0) {
                                     //System.out.println("My flag is 0");
                                    if (myBase != null) {
                                         // System.out.println("I do have a base");
                                        if (flag[0] == 1) {
                                              // System.out.println("Someone with the code 1!");
                                                poLocationGoing = new MapLocation(myBase.x + flag[1], myBase.y + flag[2]);
                                                rc.setFlag(comms.cypherFlag(2, new MapLocation(flag[1], flag[2])));
                                        }
                                    }
                                }
                            }
                        }
                        if (robot.getTeam() != rc.getTeam() && rc.getRoundNum() > 600) {
                            if (rc.canEmpower(9) && robot.getLocation().distanceSquaredTo(rc.getLocation()) <= 9) {
                                if (rc.getRoundNum() > 500) {
                                    rc.empower(9);
                                } else if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                                    rc.empower(9);
                                }
                            } else {
                                nav.move(robot.getLocation());
                            }
                        }

//                        if (robot.getTeam() != rc.getTeam() && robot.getType() != RobotType.ENLIGHTENMENT_CENTER) {
//                            boolean moveOrEmpower = false;
//                            for (RobotInfo r : rc.senseNearbyRobots(25)) {
//                                if (r.getType() == RobotType.ENLIGHTENMENT_CENTER && r.getTeam() == rc.getTeam()) {
//                                    moveOrEmpower = true;
//                                }
//                            }
//                            if (moveOrEmpower) {
//                                if (robot.getLocation().distanceSquaredTo(rc.getLocation()) <= 9) {
//                                    //tryingToEmpower = true;
//                                    System.out.println("move or empower was set to true");
//                                } else {
//                                //    nav.move(robot.getLocation());
//                                }
//                            }
//                        }
                    }

                    poliMovement();
                } else {
                    if (rc.canEmpower(9)) {
                       // System.out.println("Empowered!");
                        rc.empower(9);
                    }
                }
            }
            else{
                if(!tryingToEmpower) {
                    for (RobotInfo robot : rc.senseNearbyRobots()) {
                        if(robot.getType() == RobotType.MUCKRAKER && rc.getTeam() != robot.getTeam() || robot.getTeam() == Team.NEUTRAL){
                            if(rc.getLocation().distanceSquaredTo(robot.getLocation()) <= 9){
                                tryingToEmpower = true;
                            }
                            else{
                                poLocationGoing = robot.getLocation();
                            }
                        }
                    }
                    poliMovement();
                }
                else{
                    if(rc.canEmpower(9)){
                        rc.empower(9);
                    }
                }
            }
        }
    }

    private void passiveFlags() throws GameActionException {
        if (rc.getFlag(rc.getID()) == 0) {
            numRoundsWOFlag++;
        } else {
            numRoundsWOFlag = 0;
        }


        if (rc.getFlag(rc.getID()) != 0) {
            numRoundsWithFlag++;
        } else {
            numRoundsWithFlag = 0;
        }

        if (numRoundsWithFlag == 200) {
            poDirectionGoing = poLocationGoing.directionTo(rc.getLocation());
            poLocationGoing = new MapLocation(0, 0);
            rc.setFlag(0);
        }

        if (rc.getFlag(rc.getID()) == 0) {
            if (myBaseID != 0) {
                if (rc.canGetFlag(myBaseID)) {
                    if (rc.getFlag(myBaseID) != 0) {
                        int code = comms.decypherFlag(rc.getFlag(myBaseID))[0];
                        if(code == 1 || code == 8) {
                            rc.setFlag(rc.getFlag(myBaseID));
                        }
                        if(code == 7 && startedRound < rc.getRoundNum() + 12){
                            tryingToEmpower = true;
                        }
                    }
                }
            }
        }


        int[] currentFlag = comms.decypherFlag(rc.getFlag(rc.getID()));
        if(currentFlag[0] != 0) {
            if(myBase != null) {
                if(currentFlag.length > 2) {
                    if (new MapLocation(currentFlag[1] + myBase.x, currentFlag[2] + myBase.y).distanceSquaredTo(rc.getLocation()) <= 25) {
                        if (rc.canSenseLocation(new MapLocation(currentFlag[1] + myBase.x, currentFlag[2] + myBase.y))) {
                            if (rc.senseRobotAtLocation(new MapLocation(currentFlag[1] + myBase.x, currentFlag[2] + myBase.y)) == null) {
                                rc.setFlag(0);
                            }
                        }
                    }
                }
            }
        }
    }

    private void poliMovement() throws GameActionException {
        if (initialMove == false && rc.getFlag(rc.getID()) == 0) {
            for (RobotInfo robot : rc.senseNearbyRobots(25)) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    if (robot.getTeam() == rc.getTeam()) {
                        poDirectionGoing = robot.getLocation().directionTo(rc.getLocation());
                        initialMove = true;
                    }
                }
            }
            if (initialMove == false) {
                poDirectionGoing = directions[(int) Math.floor(Math.random() * 8)];
            }
        }

        if (turnsInDirection == 64) {
            poDirectionGoing = directions[(int) Math.floor(Math.random() * 8)];
            turnsInDirection = 0;
        }

        if (poLocationGoing == null) {
            poLocationGoing = new MapLocation(0, 0);
        }

        if (poLocationGoing != new MapLocation(0, 0) && poLocationGoing.x != 0 && poLocationGoing.y != 0) {
            //System.out.println("Going to location: " + poLocationGoing);
            if (rc.getLocation().distanceSquaredTo(poLocationGoing) < 5) {
                poLocationGoing = new MapLocation(0, 0);
            }
            nav.move(poLocationGoing);
        } else if (poDirectionGoing != Direction.CENTER && stopMoving == false) {
            // System.out.println("Going to direction: " + poLocationGoing);
            //System.out.println("DIRECTION IS NOT CENTER");
            if (nav.tryMove(poDirectionGoing)) {
                turnsInDirection++;
            } else {

            }
        }
    }
}