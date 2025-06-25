package project.iot.handler;

import project.iot.client.Request;
import project.iot.client.RequestCommand;
import project.iot.client.RequestType;
import project.iot.event.ActuatorEvent;
import project.iot.event.Event;
import project.iot.event.SensorEvent;
import project.iot.server.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.Buffer;
import java.io.*;

class MessageHandlerThread implements Runnable {
    private Socket incomingSocket;
    private BufferedReader in;

    private String requestOrEvent;

    private Server server;

    private PrintWriter out;

    public MessageHandlerThread(Socket incomingSocket, String requestOrEvent, Server server) {
        this.incomingSocket = incomingSocket;
        this.requestOrEvent = requestOrEvent;
        this.server = server;
    }

    /**
     * Sends the request/event data to the server
     * Prints any output request data back to the client
     */
    @Override
    public void run() {
        System.out.println(requestOrEvent);
        String[] requestOrEventSplit = requestOrEvent.split(", ");
        String output = "Error no server data sent (could just be an event)";

        if(requestOrEventSplit[0].equals("Request")) {
            Request request = new Request(Double.valueOf(requestOrEventSplit[1]), RequestType.valueOf(requestOrEventSplit[2]),
                    RequestCommand.valueOf(requestOrEventSplit[3]), requestOrEventSplit[4]);
            output = server.getRequestOutput(request);
        } else {
            Event event = null;
            if(requestOrEventSplit[1].equals("Actuator")) {
                event = new ActuatorEvent(Double.valueOf(requestOrEventSplit[2]),Integer.valueOf(requestOrEventSplit[3]), Integer.valueOf(requestOrEventSplit[4]), requestOrEventSplit[5], Boolean.valueOf(requestOrEventSplit[6]));
            } else {
                event = new SensorEvent(Double.valueOf(requestOrEventSplit[2]),Integer.valueOf(requestOrEventSplit[3]), Integer.valueOf(requestOrEventSplit[4]), requestOrEventSplit[5], Double.valueOf(requestOrEventSplit[6]));
            }

            server.processIncomingEvent(event);
            output = "Event recorded in Server";
        }

        try {
            out = new PrintWriter(new OutputStreamWriter(incomingSocket.getOutputStream()));
            out.print(output);
            out.close();
        } catch(IOException e){
            System.out.println("Could not print to client");
        }

    }
}