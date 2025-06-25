package project.iot.server;

import project.iot.client.Request;

public class RequestWithWaitTime extends Request {
    double maxWaitTime;
    Request request;

    /**
     * Keeps track of each request's maximum wait time
     *
     * @param maxWaitTime, is the max wait time determined by the client
     * @param request, is the request being made
     */
    public RequestWithWaitTime(double maxWaitTime, Request request) {
        super(request.getTimeStamp(), request.getRequestType(), request.getRequestCommand(), request.getRequestData());
        this.maxWaitTime = maxWaitTime;
        this.request = request;
    }

    public Request getRequest() {
       return request;
    }
    public double getMaxWaitTime(){
        return maxWaitTime;
    }
}
