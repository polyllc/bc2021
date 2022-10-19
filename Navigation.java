package poly;
import battlecode.common.*;

import java.util.*;

public class Navigation {

    RobotController rc;

    boolean finishedPath = false;
    boolean startedPath = false;

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    }; //for use in foreach(Direction dir in directions)
    // navigation shit here
    public Navigation(RobotController r){
        rc = r;
    }
    //better nav, try not to move on low movement area

    boolean tryMove(Direction dir) throws GameActionException {
        if(rc.isReady()) {
            if(rc.canMove(dir) && rc.sensePassability(rc.getLocation().add(dir)) >= 0.8){
                rc.move(dir);
            }
            else if(rc.canMove(dir.rotateLeft()) && rc.sensePassability(rc.getLocation().add(dir.rotateLeft())) >= 80){
                rc.move(dir.rotateLeft());
            }
            else if(rc.canMove(dir.rotateRight()) && rc.sensePassability(rc.getLocation().add(dir.rotateRight())) >= 80){
                rc.move(dir.rotateRight());
            }
            else if(rc.canMove(dir)){
                rc.move(dir);
            }
            else if(rc.canMove(dir.rotateLeft())){
                rc.move(dir.rotateLeft());
            }
            else if(rc.canMove(dir.rotateRight())){
                rc.move(dir.rotateRight());
            }
            else if(rc.canMove(dir.rotateLeft().rotateLeft())){
                rc.move(dir.rotateLeft().rotateLeft());
            }
            else if(rc.canMove(dir.rotateRight().rotateRight())){
                rc.move(dir.rotateRight().rotateRight());
            }
            else if(rc.canMove(dir.rotateLeft().rotateLeft().rotateLeft())){
                rc.move(dir.rotateLeft().rotateLeft().rotateLeft());
            }
            else if(rc.canMove(dir.rotateRight().rotateRight().rotateRight())){
                rc.move(dir.rotateRight().rotateRight().rotateRight());
            }
            else{
                System.out.println("oh no");
                return false;
            }
            return true;
        }
        return false;
    }

    boolean move(MapLocation coords) throws GameActionException {
        if(tryMove(rc.getLocation().directionTo(coords))){
            return true;
        }
        return false;
    }


    MapLocation newCoord(MapLocation coords) throws GameActionException {
        List<MapLocation> bfs = getBfs(coords);
        return bfs.get(0);
    }

    List<MapLocation> getBfs(MapLocation coords) throws GameActionException {
        Map<MapLocation, MapLocation> bfsLookup = new HashMap<MapLocation, MapLocation>();
        Queue<MapLocation> toLookFor = new LinkedList<MapLocation>();
        List<MapLocation> correctPath = new ArrayList<>();
        List<MapLocation> searched = new ArrayList<>();
        MapLocation currentPos;
        toLookFor.add(rc.getLocation());
        while(!bfsLookup.containsKey(coords) && toLookFor.size() > 0) {
            currentPos = toLookFor.remove();
            if(!searched.contains(currentPos)) {
                for(Direction dir : directions) {
                    if(!bfsLookup.containsKey(currentPos.add(dir))) {
                        if(rc.canDetectLocation(currentPos.add((dir)))) {
                                bfsLookup.put(currentPos.add(dir), currentPos);
                                toLookFor.add(currentPos.add(dir));
                        }
                        else{
                            bfsLookup.put(currentPos.add(dir), currentPos);
                            toLookFor.add(currentPos.add(dir));
                        }
                    }
                }
                searched.add(currentPos);
            }
        }
        if(bfsLookup.containsKey(coords)) {
            currentPos = coords;
            while(currentPos != rc.getLocation()) {
                correctPath.add(currentPos);
                currentPos = bfsLookup.get(currentPos);
            }
            correctPath.add(rc.getLocation());
        }
        Collections.reverse(correctPath);
        return correctPath;

    }



}