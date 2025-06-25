package project.iot.entity;

import project.iot.event.ActuatorEvent;
import project.iot.event.Event;
import project.iot.event.SensorEvent;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class    Sensor implements Entity {
    private final int id;
    private int clientId;
    private final String type;
    private String serverIP = null;
    private int serverPort = 0;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)

    private Socket socket;

    private PrintWriter out;

    public Sensor(int id, String type) {
        this.id = id;
        this.clientId = -1;
        this.type = type;
    }

    public Sensor(int id, int clientId, String type) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
    }

    public Sensor(int id, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;   // remains unregistered
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public Sensor(int id, int clientId, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;

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
        return false;
    }

    /**
     * Registers the sensor for the given client
     *
     * @return true if the sensor is new (clientID is -1 already) and gets successfully registered or if it is already registered for clientId, else false
     */
    public boolean registerForClient(int clientId) {
        // implement this method
        if(clientId == -1 || clientId == this.clientId){
            this.clientId = clientId;
            return true;
        }

        return false;
    }

    /**
     * Sets or updates the http endpoint that 
     * the sensor should send events to
     *
     * @param serverIP the IP address of the endpoint
     * @param serverPort the port number of the endpoint
     */
    public void setEndpoint(String serverIP, int serverPort){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Sets the frequency of event generation
     *
     * @param frequency the frequency of event generation in Hz (1/s)
     */
    public void setEventGenerationFrequency(double frequency){
        // implement this method
        this.eventGenerationFrequency = frequency;
    }

    public void sendEvent(Event event) throws IOException{
        if(serverIP == null || serverPort == 0){
            throw new UnknownHostException();
        }
        try{
            socket = new Socket(serverIP, serverPort);
        } catch(IOException e){
            System.out.println("ERROR");
        }

        String eventInfo = event.toString();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        System.out.println(eventInfo);

        out.println(clientId+"/"+eventInfo);
        out.flush();
    }

    public void sendFrequentEvents() throws IOException, InterruptedException {
        if(serverIP == null || serverPort == 0){
            throw new UnknownHostException();
        }

        while (true) {
            Event currentEvent = new SensorEvent(System.currentTimeMillis(), clientId, id, type, generateRandomDouble());
            sendEvent(currentEvent);

            try {
                Thread.sleep((long) (1000 / eventGenerationFrequency));
            } catch(InterruptedException e){
                System.out.println("Interrupted");
            }
        }
    }

    private double generateRandomDouble(){
        Random random = new Random();

        switch (type) {
            case "TempSensor" :
                return 20 + random.nextDouble()*(24-20);
            case "PressureSensor":
                return 1020 + random.nextDouble()*(1024-1020);
            case "CO2" :
                return Math.random();
            default:
        }
        return -1;
    }

    public static void main(String[] args){
        try {
        Sensor sensor = new Sensor(1,-1,"TempSensor", "localhost", 12345);
        Event event = new SensorEvent(1,1,1,"f",3.0);

            sensor.sendFrequentEvents();
        }catch(InterruptedException|IOException e){
            e.printStackTrace();
        }
    }
}