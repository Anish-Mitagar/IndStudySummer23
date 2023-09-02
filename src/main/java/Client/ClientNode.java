package Client;

import Client.Query.QueryDemoNew;
import Client.Services.ClientNodeQueryDemoImpl;
import Client.Utils.ClientNodeMap;
import Client.Utils.DBConnection;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.io.IOException;
import java.sql.Connection;

public class ClientNode {

    private String url;
    private String db;
    private String driver;
    private String username;
    private String password;
    private int receiverPort;
    private Connection connection;
    private ArrayList<ManagedChannel> messengers = new ArrayList<>();
    private HashMap<Integer, ManagedChannel> idToMessenger = new HashMap<>();
    private Server receiver;
    private int id;
    private boolean receiverRunning = false;
    private ClientNodeMap map;
    private boolean visited = false;
    private int justCameFromClientNodeId = -1;

    private long start = -1;

    private long end = -1;

    private long total = -1;

    public ClientNode(String url, String db, String driver, String username, String password, ClientNodeMap clientNodeMap, int receiverPort, int id) {
        this.url = url;
        this.db = db;
        this.driver = driver;
        this.username = username;
        this.password = password;
        this.map = clientNodeMap;
        this.receiverPort = receiverPort;
        this.id = id;
    }

    public void connectDB() throws RuntimeException {
        try {
            this.connection = DBConnection.getInstance().setConnectionParams(this.url, this.db, this.driver, this.username, this.password).getConnection();
            System.out.println("Client Node " + this.id + " is connect to DB!");
        } catch (RuntimeException e) {
            System.out.println(e);
        }
    }

    public void startMessengers() {


        for (int i = 0; i < this.map.getMap().get(this.id).size(); i++) {
            int neighborId = this.map.getMap().get(this.id).get(i);
            int neighborPort = this.map.getIdsToPorts().get(neighborId);
            String neighborContainerName = this.map.getContainerNames().get(neighborId);
            //Docker change
            //String neighborIPAddress = getContainerIpAddress(neighborContainerName);

            System.out.println("Connecting to Container: "+neighborContainerName+ ", and port: "+neighborPort);

            //** change neighborIPAddress to "localhost" if conducting an experiment
            //on a single machine only
            //Docker change
            ManagedChannel messenger = ManagedChannelBuilder
                    .forAddress(neighborContainerName, neighborPort)
                    .usePlaintext()
                    .build();

            //localhost version
            /*ManagedChannel messenger = ManagedChannelBuilder
                    .forAddress("localhost", neighborPort)
                    .usePlaintext()
                    .build();*/

            this.messengers.add(messenger);
            this.idToMessenger.put(neighborId, messenger);

            //Docker change
            System.out.println("Client Node " + this.id + " started messenger for Container: " + neighborContainerName + " port: " + neighborPort + ", neighborId: " + neighborId);

            //localhost version
            //System.out.println("Client Node "+this.id+" started messenger for port: "+neighborPort+", neighborID: "+neighborId);

        }
    }


    public void stopMessengers() {
        for (int i = 0; i < this.messengers.size(); i++) {
            this.messengers.get(i).shutdown();
        }
        System.out.println("Client Node " + this.id + " has shut down all messengers");
    }

    public void startReceiver() throws IOException {
        this.receiver = ServerBuilder.forPort(this.receiverPort)
                .addService(new ClientNodeQueryDemoImpl(this))
                .build();
        this.receiver.start();
        this.receiverRunning = true;
        System.out.println("Client Node " + this.id + " started receiver");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (this.receiverRunning) {
                System.out.println("Received Shutdown Request");
                this.receiver.shutdown();
                System.out.println("Successfully stopped the server");
            }
        }));
    }

    public void stopReceiver() {
        this.receiver.shutdown();
        this.receiverRunning = false;
        System.out.println("Client Node " + this.id + " stopped receiver");
    }

    public void executeQueryOne(boolean isSourceNode) throws InterruptedException {

        this.start = System.nanoTime();

        if (isSourceNode) {
            this.visited = true;
        }

        ArrayList<Integer> neighbors = this.map.getMap().get(this.id);

        if (this.justCameFromClientNodeId != -1) {
            neighbors.remove(Integer.valueOf(this.justCameFromClientNodeId));
        }

        Thread newThread = new Thread(new QueryDemoNew(getDBConnection(), this.id, neighbors, getIdToMessenger(), 0));

        newThread.start();

        System.out.println("ClientNode " + this.id + " began execution!");

        newThread.join();

        this.end = System.nanoTime();

        this.total = (this.end - this.start)/1000;

    }

    private String getContainerIpAddress(String containerName) {
        try {
            // Use the container name as the hostname to resolve to its IP address
            InetAddress inetAddress = InetAddress.getByName(containerName);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    public HashMap<Integer, ManagedChannel> getIdToMessenger() {
        return this.idToMessenger;
    }

    public synchronized boolean isVisited() {
        if (this.visited == true) {
            this.end = System.nanoTime();
            this.total = (this.end - this.start)/1000;
            return true;
        } else {
            this.visited = true;
            return false;
        }
    }

    public int getId() { return this.id; }

    public Connection getDBConnection() {
        return this.connection;
    }

    public void setJustCameFromClientNodeId(int id) { this.justCameFromClientNodeId = id; }

    public long getTotalTime() { return this.total; }
    public void reset() {
        this.start = -1;
        this.end = -1;
        this.total = -1;
        this.visited = false;
    }
}
