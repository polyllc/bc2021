package poly;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;


public class EnlightenmentCenter {

    RobotController rc;
    Communications comms;

    Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };


    int startRound = 0;
    int lastAmountOfVotes = 0;
    int bidAmount = 1;
    int POLITICIANCost = 100;
    int bidAmountMax = 120;
    int numSlanderers = 0;
    int numPol = 0;
    int numMucks = 0;
    int initSpawn = 0;

    int empowerCooldown = 0;

    int maxMucks = 0;
    int nextPoli = 0;
    int currentFlag = 0;

    List<Integer> listM = new ArrayList<Integer>();
    List<Integer> listFlag = new ArrayList<Integer>();

    Direction nextDirToSpawn = directions[(int) Math.floor(Math.random()*8)];
    Direction muDir = directions[(int) Math.floor(Math.random()*8)];
    Direction poDir = directions[(int) Math.floor(Math.random()*8)];

    int slPrices [] = {63, 85, 107, 130, 154, 178, 203, 228, 255, 282, 310, 339, 368, 399, 431, 463, 497, 532, 568, 605, 643, 683, 724, 766, 810, 855, 902, 949};
    //int slPrices [] = {85, 107, 130, 154};

    public EnlightenmentCenter(RobotController r) {
        this.rc = r;
        comms = new Communications(rc);
        startRound = rc.getRoundNum();
    }


    int amountOfMuckrakers = 0;
    public void takeTurn() throws GameActionException {
        if(rc.getRoundNum() > 100) {
            bid();
        }
        updateFlags();
    if(rc.isReady()) {
        //todo MORE!!!!! slanderers, change their value depending on the amount that you have
        senseRobots();

        if(empowerCooldown > 0){
            empowerCooldown--;
        }
        if(empowerCooldown == 1){
            nextPoli = (int) (rc.getInfluence() * 0.9);
            rc.setFlag(777);
        }


        if(comms.decypherFlag(rc.getFlag(rc.getID()))[0] == 5 && rc.getRoundNum() % 250 == 0){
            rc.setFlag(0);
        }

        if(rc.getRoundNum() % 150 == 0){
            maxMucks = 0;
            rc.setFlag(0);
        }

        comms = new Communications(rc);

        if (initSpawn >= 44 || rc.getRoundNum() > 100 && initSpawn >= 0) {
            mainSpawning();
        } else { //this shit is not spawning each turn even though i can spawn cause i have enough
            InitialSetup();
        }

    }
    System.out.println(POLITICIANCost);

    }

    private void mainSpawning() throws GameActionException {
        Direction randomDir = directions[(int) Math.floor(Math.random() * 8)];
        int i = 0;

        if((rc.getRoundNum() % 3 == 0 && rc.getRoundNum() < 650) || (rc.getRoundNum() % 6 == 0 && rc.getRoundNum() >= 650)){
            int muckPrice = 2;
            while (!rc.canBuildRobot(RobotType.MUCKRAKER, randomDir, muckPrice) && i < 8) {
                randomDir = randomDir.rotateLeft();
                i++;
            }
            if (i < 8) {
                if (rc.canBuildRobot(RobotType.MUCKRAKER, randomDir, muckPrice)) {
                    rc.buildRobot(RobotType.MUCKRAKER, randomDir, muckPrice);
                    numMucks++;
                }
            }
        }

        if(numPol < numSlanderers) {
            i = 0;
            if(rc.getRoundNum() < 300){
                POLITICIANCost = 300;
            }
            else{
                POLITICIANCost = 80 + rc.getInfluence()/100;
            }

            if(nextPoli != 0){
                POLITICIANCost = nextPoli;
                nextPoli = 0;
                if(comms.decypherFlag(rc.getFlag(rc.getID()))[0] == 7){
                    rc.setFlag(0);
                }
            }
            while (!rc.canBuildRobot(RobotType.POLITICIAN, randomDir, (int) (POLITICIANCost)) && i < 8) {
                randomDir = randomDir.rotateLeft();
                i++;
            }
            if (i < 8) {
                if (rc.canBuildRobot(RobotType.POLITICIAN, randomDir, (int) (POLITICIANCost))) {
                    rc.buildRobot(RobotType.POLITICIAN, randomDir, (int) (POLITICIANCost));
                    numPol++;
                }
            }
        }
        else{
            //send out some slanderers for easy money
            i = 0;
            int slPrice = (Math.max(Math.min(rc.getInfluence(), 949), 85));
            while (!rc.canBuildRobot(RobotType.SLANDERER, nextDirToSpawn, slPrice) && i < 8) {
                    nextDirToSpawn = nextDirToSpawn.rotateLeft();
                    i++;
                }
                if (rc.canBuildRobot(RobotType.SLANDERER, nextDirToSpawn, slPrice)) {
                    rc.buildRobot(RobotType.SLANDERER, nextDirToSpawn, slPrice);
                    nextDirToSpawn = nextDirToSpawn.rotateLeft();
                    numSlanderers++;
                }
        }

        if (rc.getRoundNum() % 150 == 0) {
            numSlanderers = 0;
        }



    }

    private int getSlPrice() {
        int currentPrice = 63;
        for(int i : slPrices){
            if(rc.getInfluence() >= i){
                currentPrice = i;
            }
        }
        return currentPrice;
    }

    private void updateFlags() throws GameActionException {
        for (Integer id : listM) {
            if (rc.canGetFlag(id)) {
                int[] flag = comms.decypherFlag(rc.getFlag(id));
                if (flag[0] == 9) { //set influence on next poli for the ec
                    nextPoli = flag[1];
                }
                if (flag[0] == 7 && rc.getRoundNum() > 300) {
                    empowerCooldown = 5;
                }
                if (flag[0] == 1 || flag[0] == 8) {
                    if (!listFlag.contains(rc.getFlag(id))) {
                        listFlag.add(rc.getFlag(id));
                    }
                } else if (flag[0] == 3) {
                    int cypheredFlag = comms.cypherFlag(1, new MapLocation(flag[1], flag[2]));
                    int cypheredFlag2 = comms.cypherFlag(8, new MapLocation(flag[1], flag[2]));
                    if (listFlag.contains(cypheredFlag)) {
                        listFlag.remove(listFlag.indexOf(cypheredFlag));
                    }
                    if (listFlag.contains(cypheredFlag2)) {
                        listFlag.remove(listFlag.indexOf(cypheredFlag2));
                    }
                }
            }
        }

        if (rc.getRoundNum() % 10 == 0) {
            cycleFlags();
        }
    }

    private void cycleFlags() throws GameActionException {
        if (listFlag.size() > 0) {
            if (currentFlag + 2 > listFlag.size()) {
                currentFlag = 0;
            } else {
                currentFlag++;
            }
            rc.setFlag(listFlag.get(currentFlag));
        }
    }

    private void senseRobots() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            int poliBackup = POLITICIANCost;
            if(robot.getTeam() != rc.getTeam() && robot.getType() != RobotType.ENLIGHTENMENT_CENTER){
               // POLITICIANCost = 2;
            }

            if ((robot.getType() != RobotType.ENLIGHTENMENT_CENTER) && robot.getTeam() == rc.getTeam()) {
                if (rc.getFlag(rc.getID()) == 0) {
                    int flag[] = comms.decypherFlag(rc.getFlag(robot.getID()));
                    if (flag[0] == 1) {
                        rc.setFlag(comms.cypherFlag(1, new MapLocation(flag[1], flag[2])));
                    }
                } else {
                    int flag[] = comms.decypherFlag(rc.getFlag(robot.getID()));
                    if (flag[0] == 3) {
                        if (flag[1] == comms.decypherFlag(rc.getFlag(rc.getID()))[1] && flag[2] == comms.decypherFlag(rc.getFlag(rc.getID()))[2]) {
                            int indexOf = listFlag.indexOf(rc.getFlag(robot.getID()));
                            if(indexOf != -1) {
                                listFlag.remove(indexOf);
                            }
                        }
                    }
                }
            }

            if(robot.getTeam() != rc.getTeam() && robot.getTeam() != Team.NEUTRAL && robot.getType() == RobotType.ENLIGHTENMENT_CENTER && comms.decypherFlag(rc.getFlag(rc.getID()))[0] != 5){
                comms.cypherFlag(5, new MapLocation(robot.getLocation().x - rc.getLocation().x, robot.getLocation().y - rc.getLocation().y));
                int i = 0;
                while (!rc.canBuildRobot(RobotType.MUCKRAKER, muDir, 1) && i < 8) {
                    muDir = muDir.rotateLeft();
                    i++;
                }
                if (rc.canBuildRobot(RobotType.MUCKRAKER, muDir, 1) && rc.getRoundNum() % 10 == 0 && maxMucks < 25) {
                    rc.buildRobot(RobotType.MUCKRAKER, muDir, 1);
                    maxMucks++;
                }
            }

            if(robot.getTeam() != rc.getTeam() && robot.getType() != RobotType.ENLIGHTENMENT_CENTER){
                int i = 0;
                int poliCost = 0;
                int numRobots = rc.senseNearbyRobots().length;
                if((rc.getInfluence() - numRobots * robot.getInfluence() + robot.getInfluence()) > 10){
                    poliCost = (numRobots * robot.getInfluence() + robot.getInfluence());
                }
                if(poliCost > 79){
                    poliCost = 79;
                }
                if(poliCost != 0 && rc.getRoundNum() % 2 == 0) {
                    while (!rc.canBuildRobot(RobotType.POLITICIAN, muDir, poliCost) && i < 8) {
                        muDir = muDir.rotateLeft();
                        i++;
                    }
                    if (rc.canBuildRobot(RobotType.POLITICIAN, muDir, poliCost)) {
                        rc.buildRobot(RobotType.POLITICIAN, muDir, poliCost);
                    }
                }
            }

            if(robot.getTeam() == rc.getTeam() && robot.getType() == RobotType.MUCKRAKER && rc.getLocation().distanceSquaredTo(robot.getLocation()) < 3){
                if(!listM.contains(robot.getID())){
                    listM.add(robot.getID());
                }
            }
            POLITICIANCost = poliBackup;
        }
    }

    private void InitialSetup() throws GameActionException {

        //put a poli before the muck and after
        if (initSpawn >= 1 && initSpawn <= 8) {
            int i = 0;
            muDir = muDir.rotateLeft();
            while (!rc.canBuildRobot(RobotType.MUCKRAKER, muDir, 5) && i < 8) {
                muDir = muDir.rotateLeft();
                i++;
            }
            if (rc.canBuildRobot(RobotType.MUCKRAKER, muDir, 5)) {
                rc.buildRobot(RobotType.MUCKRAKER, muDir, 5);
                initSpawn++;
            }
        }
        if (initSpawn <= 1) {
            int i = 0;
            while (!rc.canBuildRobot(RobotType.SLANDERER, muDir, 130) && i < 8) {
                muDir = muDir.rotateLeft();
                i++;
            }
            if (rc.canBuildRobot(RobotType.SLANDERER, muDir, 130)) {
                rc.buildRobot(RobotType.SLANDERER, muDir, 130);
                muDir = muDir.rotateLeft();
                initSpawn++;
            }
        }
        if (initSpawn >= 9 && initSpawn < 12) {
            int i = 0;
            int slPrice = 41;
            while (!rc.canBuildRobot(RobotType.SLANDERER, muDir, slPrice) && i < 8) {
                muDir = muDir.rotateLeft();
                i++;
            }
            if (rc.canBuildRobot(RobotType.SLANDERER, muDir, slPrice)) {
                rc.buildRobot(RobotType.SLANDERER, muDir, slPrice);
                muDir = muDir.rotateLeft();
                initSpawn++;
            }
        }

        if (initSpawn >= 12 && initSpawn < 21) {
            int i = 0;
            muDir = muDir.rotateLeft();
            while (!rc.canBuildRobot(RobotType.MUCKRAKER, muDir, 5) && i < 8) {
                muDir = muDir.rotateLeft();
                i++;
            }
            if (rc.canBuildRobot(RobotType.MUCKRAKER, muDir, 5)) {
                rc.buildRobot(RobotType.MUCKRAKER, muDir, 5);
                initSpawn++;
            }
        }

        if (initSpawn >= 21 && initSpawn < 36 && comms.decypherFlag(rc.getFlag(rc.getID()))[0] == 1) {
            int i = 0;
            int poli = 100;
            while(!rc.canBuildRobot(RobotType.POLITICIAN, poDir, poli) && i < 8){
                poDir = poDir.rotateLeft();
                i++;
            }
            if (rc.canBuildRobot(RobotType.POLITICIAN, poDir, poli) && i < 8) {
                rc.buildRobot(RobotType.POLITICIAN, poDir, poli);
                initSpawn++;
            }
        }
        else if(initSpawn >= 21 && initSpawn < 36){
            int i = 0;
            while (!rc.canBuildRobot(RobotType.SLANDERER, muDir, 63) && i < 8) {
                muDir = muDir.rotateLeft();
                i++;
            }
            if (rc.canBuildRobot(RobotType.SLANDERER, muDir, 63)) {
                rc.buildRobot(RobotType.SLANDERER, muDir, 63);
                muDir = muDir.rotateLeft();
               // initSpawn++;
            }
        }

        if (initSpawn >= 36 && initSpawn < 52) {
            int i = 0;
            muDir = muDir.rotateLeft();
            while (!rc.canBuildRobot(RobotType.MUCKRAKER, muDir, 5) && i < 8) {
                muDir = muDir.rotateLeft();
                i++;
            }
            if (rc.canBuildRobot(RobotType.MUCKRAKER, muDir, 5)) {
                rc.buildRobot(RobotType.MUCKRAKER, muDir, 5);
                initSpawn++;
            }
        }
    }

    private void bid() throws GameActionException {
        if(rc.getRoundNum() < 750 && rc.getInfluence() > 100) {
            if (lastAmountOfVotes + 1 != rc.getTeamVotes()) {
                bidAmount += Math.floor(Math.random() * 1) + 1;
                if (bidAmount > bidAmountMax) {
                    bidAmount = bidAmountMax;
                }
            } else if (bidAmount > 1) {
                bidAmount--;
            }
            if (rc.canBid(bidAmount) && rc.getTeamVotes() <= 751) {
                lastAmountOfVotes = rc.getTeamVotes();
                rc.bid(bidAmount);
            }

            if (rc.getRoundNum() > 300) {
                bidAmountMax = 400;
            } else {
                bidAmountMax = 5;
            }
        }
        if(rc.getRoundNum() > 750 || rc.getInfluence() > 1000) {
            if (lastAmountOfVotes + 1 != rc.getTeamVotes()) {
                bidAmount += Math.floor(Math.random() * 5) + 1 + (int) rc.getInfluence()/100;
                if (bidAmount > bidAmountMax) {
                    bidAmount = bidAmountMax;
                }
            } else if (bidAmount > 1) {
                bidAmount--;
            }
            if(bidAmount > rc.getInfluence()){
                bidAmount = rc.getInfluence();
            }
            if (rc.canBid(bidAmount) && rc.getTeamVotes() <= 751) {
                lastAmountOfVotes = rc.getTeamVotes();
                rc.bid(bidAmount);
            }
                bidAmountMax = 2000;
        }
    }
}