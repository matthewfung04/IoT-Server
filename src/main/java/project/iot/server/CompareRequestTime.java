package project.iot.server;

import java.util.Comparator;

import project.iot.client.Request;



public class CompareRequestTime implements Comparator<RequestWithWaitTime> {


    @Override
    public int compare(RequestWithWaitTime a, RequestWithWaitTime b){
        double atime = a.getTimeStamp();
        double btime = b.getTimeStamp();
        double aMaxWait = a.getMaxWaitTime();
        double bMaxWait = b.getMaxWaitTime();
        double currentTime = System.currentTimeMillis();

        double aCurrentWait = currentTime - atime;
        double bCurrentWait = currentTime - btime;

        if(atime == btime && bMaxWait == aMaxWait){
            return 0;
        } else if(aMaxWait - aCurrentWait < bMaxWait - bCurrentWait){
            return 1;
        }
        return -1;
    }
}

