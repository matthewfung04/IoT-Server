package project.iot.handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import project.iot.server.Server;
import project.iot.client.Client;
import java.net.InetAddress;
import java.util.*;

public class MessageHandler {
    private ServerSocket serverSocket;
    private int port;

    // you may need to add additional private fields and methods to this class

    private Map<Client, Server> servers = new HashMap<>();

    public MessageHandler(int port) {
        this.port = port;
    }

    /**
     * Starts accepting sockets from entities and clients to send requests and events to the server
     * Creates a thread to process the information for each socket received
     */
    public void start() {
        // the following is just to get you started
        // you may need to change it to fit your implementation
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket incomingSocket = serverSocket.accept();

                //Connect to entity or client request
                System.out.println("Client/Entity connected: " + incomingSocket.getInetAddress().getHostAddress());
                BufferedReader in = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()));

                //read incoming message
                String metaData = in.readLine();

                //Split off the client ID
                String[] splitString = metaData.split("/");


                //form a client and server
                Client client = new Client(Integer.valueOf(splitString[0]), "Joe@gmail.com", serverSocket.getInetAddress().getHostAddress(), port);
                Server serverToPass;

                //check if server exists already
                if(servers.containsKey(client)){
                    serverToPass = servers.get(client);
                } else {
                    serverToPass = new Server(client);
                    servers.put(client, serverToPass);
                }

                // create a new thread to handle the client request or entity event
                Thread handlerThread = new Thread(new MessageHandlerThread(incomingSocket, splitString[1], serverToPass));
                handlerThread.start();

            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int port = 12345; // Replace with your desired port number
        MessageHandler messageHandler = new MessageHandler(port);
        messageHandler.start();
    }
}
