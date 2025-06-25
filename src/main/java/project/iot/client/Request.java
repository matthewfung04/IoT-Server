package project.iot.client;

public class Request {
    private final double timeStamp;
    private final RequestType requestType;
    private final RequestCommand requestCommand;
    private final String requestData;

    public Request(RequestType requestType, RequestCommand requestCommand, String requestData) {
        this.timeStamp = System.currentTimeMillis();
        this.requestType = requestType;
        this.requestCommand = requestCommand;
        this.requestData = requestData;
    }

    public Request(double timeStamp, RequestType requestType, RequestCommand requestCommand, String requestData) {
        this.timeStamp = timeStamp;
        this.requestType = requestType;
        this.requestCommand = requestCommand;
        this.requestData = requestData;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public RequestCommand getRequestCommand() {
        return requestCommand;
    }

    public String getRequestData() {
        return requestData;
    }

    @Override
    public String toString(){
        return "Request" +
                ", " + getTimeStamp() +
                ", " + getRequestType() +
                ", " + getRequestCommand() +
                ", " + getRequestData();
    }
}