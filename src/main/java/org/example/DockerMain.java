package org.example;

import Client.ClientNode;
import Client.Utils.ClientNodeMap;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class DockerMain {
    public static void main(String[] args) throws InterruptedException {
        //Setting up the graph
        System.out.println("Hello and Welcome!");

        String clientArgs = System.getenv("clientArgs");

        if(args.length < 1) {
            System.out.println("Usage: DockerMain <server-id>");
            return;
        }

        ArrayList<int[]> idPortPairs = new ArrayList<>();

        int[] pair1 = { 1, 50051 };
        int[] pair2 = { 2, 50052 };
        int[] pair3 = { 3, 50053 };
        int[] pair4 = { 4, 50054 };
        int[] pair5 = { 5, 50055 };
        int[] pair6 = { 6, 50056 };

        idPortPairs.add(pair1);
        idPortPairs.add(pair2);
        idPortPairs.add(pair3);
        idPortPairs.add(pair4);
        idPortPairs.add(pair5);
        idPortPairs.add(pair6);

        ClientNodeMap clientNodeMap = new ClientNodeMap(idPortPairs);

        clientNodeMap.addClientNodeConnection(1, 2);
        clientNodeMap.addClientNodeConnection(1, 3);
        clientNodeMap.addClientNodeConnection(1, 4);
        clientNodeMap.addClientNodeConnection(2, 1);
        clientNodeMap.addClientNodeConnection(2, 3);
        clientNodeMap.addClientNodeConnection(2, 5);
        clientNodeMap.addClientNodeConnection(3, 1);
        clientNodeMap.addClientNodeConnection(3, 2);
        clientNodeMap.addClientNodeConnection(3, 6);
        clientNodeMap.addClientNodeConnection(4, 1);
        clientNodeMap.addClientNodeConnection(5, 2);
        clientNodeMap.addClientNodeConnection(6, 3);

        System.out.println("Server ID printed below");
        int serverID = Integer.parseInt(clientArgs);
        System.out.println("server ID: "+serverID);
        int portNumber = clientNodeMap.getIdsToPorts().get(serverID);
        int countDown = 120; // 120 seconds countdown

        // Print the gRPC server address and port
        System.out.println("gRPC Server is starting on IP: " + getServerIpAddress() + " Port: " + portNumber);

        //Create a ClientNode
        ClientNode node = new ClientNode("jdbc:postgresql://host.docker.internal:26257/",
                "socialnetwork?sslmode=disable",
                "org.postgresql.Driver",
                "anish",
                "",
                clientNodeMap,
                portNumber,
                serverID);
        //Initiate a connection of the node to the database
        node.connectDB();
        //Start a server stub for the node
        try {
            node.startReceiver();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Start a client stub for the node
        node.startMessengers();


        //Fire execution only from node 1
        if(serverID == 1) {
            while(countDown > 0) {
                System.out.println("Countdown: "+countDown+" seconds");
                Thread.sleep(1000);
                --countDown;
            }
            System.out.println("Beginning execution ....");
            node.executeQueryOne(true);
            System.out.println("Total time: " + node.getTotalTime() + " microseconds");
        }
        else {
            //Continue running to listen to gRPC requests
            while (true) {
                Thread.sleep(500); // Sleep for half a second or adjust as needed
            }
        }
    }

    // Add a method to get the server's IP address
    private static String getServerIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}