package project.iot.entity;

import project.iot.client.Request;
import project.iot.client.RequestCommand;
import project.iot.client.RequestType;
import project.iot.event.ActuatorEvent;
import project.iot.event.Event;
import project.iot.event.SensorEvent;
import project.iot.handler.MessageHandler;
import project.iot.server.Server;
import project.iot.server.SeverCommandToActuator;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.net.ServerSocket;

public class Actuator implements Entity {
    private final int id;
    private int clientId;
    private final String type;
    private boolean state;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)
    // the following specifies the http endpoint that the actuator should send events to
    private String serverIP = null;
    private int serverPort = 0;
    // the following specifies the http endpoint that the actuator should be able to receive commands on from server
    private String host = null;
    private int port = 6666;
    private Socket socket;
    private ServerSocket serverSocket;

    public Actuator(int id, String type, boolean init_state) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        this.state = init_state;
        try {
            host = String.valueOf(InetAddress.getLocalHost());
        } catch(UnknownHostException e){
            System.out.println("ERROR in getting host");
        }
    }

    public Actuator(int id, int clientId, String type, boolean init_state) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        try {
            host = String.valueOf(InetAddress.getLocalHost());
        } catch(UnknownHostException e){
            System.out.println("ERROR, no hosts available for actuator");
        }
    }

    public Actuator(int id, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try {
            host = String.valueOf(InetAddress.getLocalHost());
        } catch(UnknownHostException e){
            System.out.println("ERROR, no hosts available for actuator");
        }
    }

    public Actuator(int id, int clientId, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try {
            host = String.valueOf(InetAddress.getLocalHost());
        } catch(UnknownHostException e){
            System.out.println("ERROR in getting host");
        }
    }

    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public String getType() {
        return type;
    }

    public boolean isActuator() {
        return true;
    }

    public boolean getState() {
        return state;
    }

    public String getIP() {
        return host;
    }

    public int getPort() {
        return port;
    }

    synchronized public void updateState(boolean new_state) {
        this.state = new_state;
    }

    /**
     * Registers the actuator for the given client
     * 
     * @return true if the actuator is new (clientID is -1 already) and gets successfully registered or if it is already registered for clientId, else false
     */
    public boolean registerForClient(int clientId) {
        if(clientId != -1 || clientId == this.clientId){
            this.clientId = clientId;
            return true;
        }

        return false;
    }

    /**
     * Sets or updates the http endpoint that 
     * the actuator should send events to
     * 
     * @param serverIP the IP address of the endpoint
     * @param serverPort the port number of the endpoint
     */
    public void setEndpoint(String serverIP, int serverPort){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try{
            socket = new Socket(serverIP, serverPort);
        } catch(IOException e){
            System.out.println("ERROR");
        }
    }

    public void setOwnEndpoint(String ownIP, int ownPort){
        this.host = ownIP;
        this.port = ownPort;
    }

    /**
     * Sets the frequency of event generation
     *
     * @param frequency the frequency of event generation in Hz (1/s)
     */
    public void setEventGenerationFrequency(double frequency){
        // implement this method
        eventGenerationFrequency = frequency;
    }

    public void sendEvent(Event event) throws IOException{
        // implement this method
        // note that Event is a complex object that you need to serialize before sending

        if(serverIP == null || serverPort == 0){
            throw new UnknownHostException();
        }
        try {
            socket = new Socket(serverIP, serverPort);
        } catch(IOException e){

        }
        String eventInfo = event.toString() + "\n";
        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        out.print(clientId+"/"+eventInfo);
        out.flush();
        out.close();
        socket.close();
    }


    public void acceptServerMessages() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
                Socket socketToAccept = serverSocket.accept();
                System.out.println("Server Connected");
                BufferedReader in = new BufferedReader(new InputStreamReader(socketToAccept.getInputStream()));

                String line = in.readLine();
                System.out.println(line);

                String[] substrings = line.split(", ");

                Request request;

                if (substrings[1].equals("CONTROL_SET_ACTUATOR_STATE")) {
                    request = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, substrings[2]);
                } else if (substrings[1].equals("CONTROL_TOGGLE_ACTUATOR_STATE")) {
                    request = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, substrings[2]);
                } else {
                    request = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, substrings[2]);
                }
                processServerMessage(request);
        } catch(IOException e){
            System.out.println("IOException");
        }
    }

    synchronized public void processServerMessage(Request command) {
        // implement this method
        System.out.println("Processing Server Message");
        if(command.getRequestCommand().equals(RequestCommand.CONTROL_SET_ACTUATOR_STATE)){
            state = true;
            System.out.println("Setting actuator state to true");
        } else if(command.getRequestCommand().equals(RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE)){
            System.out.println("Toggling actuator state");
            if(state){
                state = false;
            } else{
                state = true;
            }
        }

    }

    @Override
    public String toString() {
        return "Actuator{" +
                getId() +
                "," + getClientId() +
                "," + getType() +
                "," + getIP() +
                "," + getPort() +
                "," + getState() +
                '}';
    }

    // you will most likely need additional helper methods for this class

    public void sendFrequentEvents() throws IOException, InterruptedException {
        int errors = 0;
        while(true){
            try {
                Event currentEvent = new ActuatorEvent(System.currentTimeMillis(), clientId, id, type, switchGenerator());
                sendEvent(currentEvent);
                try {
                    Thread.sleep((long) (1000 / eventGenerationFrequency));
                } catch (InterruptedException e) {
                    System.out.println("Interrupted");
                }
            } catch(IOException e){
                errors++;
                if(errors == 5){
                    errors = 0;
                    try {
                        Thread.sleep(10000);
                    }catch(InterruptedException ex){
                        System.out.println("Interrupted");
                    }
                }
            }
        }
    }
    private boolean switchGenerator(){
        Random random = new Random();
        int zeroOrOne = random.nextInt(2);

        return zeroOrOne == 1;
    }

    public static void main(String[] args){

        //Used for testing setActuatorStateIf
        int port = 12345; // Replace with your desired port number
        Actuator actuator = new Actuator(21, "an actuator", false);
        actuator.setOwnEndpoint("localhost", port);

        System.out.println(actuator.getState());
        System.out.println(actuator.getIP());
        actuator.acceptServerMessages();
        System.out.println(actuator.getState());



//        try {
//            Actuator actuator = new Actuator(1, 1, "String type", true, "localhost", 12345);
//            Event event = new ActuatorEvent(1,1,1,"f",true);
//
//            actuator.sendFrequentEvents();
//        }catch(InterruptedException|IOException e){
//            e.printStackTrace();
//        }

    }
}
