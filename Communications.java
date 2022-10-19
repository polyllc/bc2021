package poly;
import battlecode.common.*;

public class Communications {
    RobotController rc;
    //communication shit here
    public Communications(RobotController robotc){
        rc = robotc;
    }
    //codes
    //1 is I found the enemy base, go attack but i stay and tell everyone
    //2 is On my way to enemy base, come and join me if you dont have anywhere else to go
    public int [] decypherFlag(int flag) throws NumberFormatException{
        if(flag > 999999) {
            int code = 0;
            int x = 0;
            int y = 0;

            code = Integer.parseInt(String.valueOf(flag).substring(0, 1));
            int xCoord = Integer.parseInt(String.valueOf(flag).substring(2, 4));
            x = Integer.parseInt(String.valueOf(flag).substring(1, 2)) == 1 ? xCoord : -xCoord;
            int yCoord = Integer.parseInt(String.valueOf(flag).substring(5, 7));
            y = Integer.parseInt(String.valueOf(flag).substring(4, 5)) == 1 ? yCoord : -yCoord;

            return new int[]{code, x, y};
        }
        else if(flag > 10){
            int code = 0;
            code = Integer.parseInt(String.valueOf(flag).substring(0, 1));
            String substring = String.valueOf(flag).substring(1, String.valueOf(flag).length() - 1);
            System.out.println(substring);
            if(substring != "") {
                int value = Integer.parseInt(substring);
                return new int[]{code, value};
            }
            else if(code != 0){
                return new int[]{code};
            }
            else{
                return new int[]{code};
            }
        }
        else{
            return new int[]{0};
        }
    }

    public int cypherFlag(int code, MapLocation coords){

        String stringFlag = "";

        stringFlag = stringFlag.concat(String.valueOf(code));
        stringFlag = stringFlag.concat(String.valueOf(coords.x < 0 ? 0 : 1));
        stringFlag = stringFlag.concat((coords.x >= 0 && coords.x < 10) || (coords.x < 0 && coords.x > -10) ? String.valueOf(0) : "");
        stringFlag = stringFlag.concat(String.valueOf(Math.abs(coords.x)));
        stringFlag = stringFlag.concat(String.valueOf(coords.y < 0 ? 0 : 1));
        stringFlag = stringFlag.concat((coords.y >= 0 && coords.y < 10) || (coords.y < 0 && coords.y > -10) ? String.valueOf(0) : "");
        stringFlag = stringFlag.concat(String.valueOf(Math.abs(coords.y)));
        System.out.println(stringFlag);
        return Integer.parseInt(stringFlag);
    }
}