package project.iot.client;

import project.iot.entity.Entity;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {

    private final int clientId;
    private String email;
    private String serverIP;
    private int serverPort;

    private BufferedReader in;

    private PrintWriter out;
    private Socket socket;

    public Client(int clientId, String email, String serverIP, int serverPort) {
        this.clientId = clientId;
        this.email = email;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public int getClientId() {
        return clientId;
    }

    /**
     * Registers an entity for the client
     *
     * @return true if the entity is new and gets successfully registered, false if the Entity is already registered
     */
    public boolean addEntity(Entity entity) {

        if(entity.getClientId() == -1){
            entity.registerForClient(clientId);
            return true;
        }
        return false;
    }

    /**
     * Sends a request to the server using a socket
     * @param request, the request being made
     * @throws IOException
     */
    public void sendRequest(Request request) throws IOException{
        socket = new Socket(serverIP, serverPort);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String serializedRequest = clientId+"/"+request.toString() + "\n";

        out.print(serializedRequest);
        out.flush();
    }

    /**
     * Gets the return value for requests made to the server
     *
     * @return String, the return data for the requests made
     * @throws IOException
     */
    public String getReply() throws IOException {

            String reply = in.readLine();
            if (reply == null) {
                throw new IOException("connection terminated unexpectedly");
            }
            try {
                return reply;
            } catch (NumberFormatException nfe) {
                throw new IOException("misformatted reply: " + reply);
            }
    }

    public static void main(String[] args) {
        try {
            Client client = new Client(0, "EMAIL", "localhost", 12345);
            Request request = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, "Hi");

            try {
                Thread.sleep(10000);
            } catch(Exception e){

            }
            client.sendRequest(request);
            System.out.println(client.getReply());
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }



    }

}