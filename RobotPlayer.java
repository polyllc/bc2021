package poly;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static int turnCount;
    static int ECInt = 0;
    static int POInt = 0;
    static int SLInt = 0;
    static int MUInt = 0;
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        EnlightenmentCenter ec = null;
        Politician po = null;
        Slanderer sl = null;
        Muckraker mu = null;

        ec = new EnlightenmentCenter(rc);
        po = new Politician(rc);
        sl = new Slanderer(rc);
        mu = new Muckraker(rc);


        turnCount = 0;

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                switch(rc.getType()) {
                    case ENLIGHTENMENT_CENTER: ec.takeTurn(); break;
                    case POLITICIAN: po.takeTurn(); break;
                    case SLANDERER: sl.takeTurn(); break;
                    case MUCKRAKER: mu.takeTurn(); break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }


}
