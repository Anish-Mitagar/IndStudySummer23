package ClientDocker.Query;

import com.proto.ShortReadQuery5.Request;
import com.proto.ShortReadQuery5.ShortReadQuery5Request;
import com.proto.ShortReadQuery5.ShortReadQuery5Response;
import com.proto.ShortReadQuery5.ShortReadQuery5ServiceGrpc;
import io.grpc.ManagedChannel;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class ShortReadQuery5 implements Runnable {
    private Connection connection;
    private int localId;
    private ArrayList<Integer> neighbors;
    private HashMap<Integer, ManagedChannel> idToChannel;
    private int sleepTime;
    private int messageID;
    private int counter;
    private int queryID;
    private int totalNodes = 4;
    private long creatorPersonID;
    private String firstName;
    private String lastName;
    private List<Future<String>> message_futures = new ArrayList<>();
    private List<Future<String>> person_futures = new ArrayList<>();
    private ExecutorService executor;
    private String temp_message_table_name = "temp_message";
    private String temp_person_table_name = "temp_person";
    private String create_temp_message_table = "CREATE TABLE IF NOT EXISTS temp_message (\n" +
            "    creationDate timestamp with time zone NOT NULL,\n" +
            "    id bigint PRIMARY KEY,\n" +
            "    language varchar(80),\n" +
            "    content varchar(2000),\n" +
            "    imageFile varchar(80),\n" +
            "    locationIP varchar(80) NOT NULL,\n" +
            "    browserUsed varchar(80) NOT NULL,\n" +
            "    length int NOT NULL,\n" +
            "    CreatorPersonId bigint NOT NULL,\n" +
            "    ContainerForumId bigint,\n" +
            "    LocationCountryId bigint NOT NULL,\n" +
            "    ParentMessageId bigint,\n" +
            "    serverId int NOT NULL, -- Add the \"serverId\" field of type INT\n" +
            "    queryId int NOT NULL,\n" +
            "    INDEX (LocationCountryId),\n" +
            "    INDEX (CreatorPersonId),\n" +
            "    INDEX (ContainerForumId),\n" +
            "    INDEX (ParentMessageId)\n" +
            ")";
    private String create_temp_person_table = "CREATE TABLE IF NOT EXISTS temp_person (\n" +
            "    creationDate timestamp with time zone NOT NULL,\n" +
            "    id bigint PRIMARY KEY,\n" +
            "    firstName varchar(80) NOT NULL,\n" +
            "    lastName varchar(80) NOT NULL,\n" +
            "    gender varchar(80) NOT NULL,\n" +
            "    birthday date NOT NULL,\n" +
            "    locationIP varchar(80) NOT NULL,\n" +
            "    browserUsed varchar(80) NOT NULL,\n" +
            "    LocationCityId bigint NOT NULL,\n" +
            "    speaks varchar(640) NOT NULL,\n" +
            "    email varchar(8192) NOT NULL,\n" +
            "    serverId int NOT NULL, -- Add the \"serverId\" field of type INT\n" +
            "    queryId int NOT NULL,\n" +
            "    INDEX (LocationCityId)\n" +
            ")";
    //Query 5
    private String find_person_id = "SELECT * FROM message WHERE id="+this.messageID+" AND serverId="+this.localId;

    public ShortReadQuery5(Connection connection, int localId, ArrayList<Integer> neighbors, HashMap<Integer, ManagedChannel> idToChannel, int sleepTime, int messageID) {
        this.connection = connection;
        this.localId = localId;
        this.neighbors = neighbors;
        this.idToChannel = idToChannel;
        this.sleepTime = sleepTime;
        this.messageID = messageID;
    }

    @Override
    public void run() {
        try {
            //Create a statement
            Statement statement = connection.createStatement();
            //Use the 'socialnetwork' database
            statement.execute("USE socialnetwork");
            //Run the query locally
            ResultSet resultSet = statement.executeQuery(find_person_id);
            if(resultSet.next()) {
                this.creatorPersonID = resultSet.getLong("CreatorPersonId");
                System.out.println("PersonID: "+creatorPersonID);
            }
            else {
                // Rows are not available
                System.out.println("No rows found in the result message set locally.");
                //Create temp_message table
                statement.execute(create_temp_message_table);
                System.out.println("Temp message table created successfully");
                this.queryID = this.totalNodes * this.counter + this.localId;
                ++this.counter;

                //If result not found, ping all other nodes
                try {
                    if(this.neighbors.size() > 0) {
                        this.executor = Executors.newFixedThreadPool(this.neighbors.size());
                        String subquery = "SELECT * FROM message WHERE id="+this.messageID+" AND serverId=";
                        for(int i = 0; i < this.neighbors.size(); ++i) {
                            String serverSubquery = subquery + neighbors.get(i) + "";
                            Future<String> future = executor.submit(new gRPC(serverSubquery, 1, this.localId, this.neighbors.get(i), this.messageID, this.queryID, this.temp_message_table_name, this.idToChannel));
                            message_futures.add(future);
                        }

                        //Wait for acknowledgement from other nodes
                        List<String> message_results = new ArrayList<>();
                        if(this.neighbors.size() > 0) {
                            for(Future<String> fut : message_futures) {
                                try {
                                    message_results.add(fut.get());
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        System.out.println("I Client Node " + this.localId + " had all message threads return");

                        //Fetch person.id from temp table
                        String fetch_person_id = "SELECT * FROM "+temp_message_table_name;
                        try {
                            ResultSet fetchSet = statement.executeQuery(fetch_person_id);
                            if(fetchSet.next()) {
                                this.creatorPersonID = fetchSet.getLong("CreatorPersonId");
                                System.out.println("PersonID: "+creatorPersonID);
                            }
                            else {
                                System.out.println("Message row NOT found in temp message table");
                            }
                        }
                        catch(SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch(Exception e) {
                    System.out.println("Query failed at node: "+this.localId);
                    e.printStackTrace();
                }
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }


        //Create temp person table
        //Fetch person.firstName and person.lastName locally
        try {
            int personID = (int)creatorPersonID;
            Statement statement = connection.createStatement();
            statement.execute("USE socialnetwork");
            String findPerson = "SELECT * FROM person WHERE id="+personID+" AND serverId="+this.localId;
            ResultSet personSet = statement.executeQuery(findPerson);
            if(personSet.next()) {
                this.firstName = personSet.getString("firstName");
                this.lastName = personSet.getString("lastName");
                System.out.println("First Name: "+this.firstName);
                System.out.println("Last Name: "+this.lastName);
            }
            else {
                // Rows are not available
                System.out.println("No rows found in the result person set locally.");
                //Create temp_message table
                statement.execute(create_temp_person_table);
                System.out.println("Temp person table created successfully");
                String subquery = "SELECT * FROM person WHERE id="+personID+" AND serverId=";

                //Ping other nodes
                for(int i = 0; i < this.neighbors.size(); ++i) {
                    String person_subquery = subquery + this.neighbors.get(i) + "";
                    Future<String> future = executor.submit(new gRPC(person_subquery, 2, this.localId, this.neighbors.get(i), this.messageID, this.queryID, this.temp_person_table_name, this.idToChannel));
                    person_futures.add(future);
                }

                //Wait for acknowledgement from other nodes\
                List<String> person_results = new ArrayList<>();
                if(this.neighbors.size() > 0) {
                    for(Future<String> fut : person_futures) {
                        try {
                            person_results.add(fut.get());
                        }
                        catch(InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("I Client Node " + this.localId + " had all person threads return");

                //Fetch person.firstName and person.lastName from the person temp table
                String fetch_person_name = "SELECT * FROM "+temp_person_table_name;
                try {
                    ResultSet nameSet = statement.executeQuery(fetch_person_name);
                    if(nameSet.next()) {
                        this.firstName = nameSet.getString("firstName");
                        this.lastName = nameSet.getString("lastName");
                        System.out.println("First Name: "+this.firstName);
                        System.out.println("Last Name: "+this.lastName);
                    }
                    else {
                        System.out.println("Person row NOT found in temp message table");
                    }
                }
                catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }

        if (this.executor != null && !this.executor.isShutdown()) {
            this.executor.shutdown();
        }

        // OUTPUT
        System.out.println("-----------OUTPUT--------------");
        System.out.println("Person ID: "+this.creatorPersonID);
        System.out.println("Person First Name: "+this.firstName);
        System.out.println("Person Last Name: "+this.lastName);
    }

    class gRPC implements Callable<String> {
        private String subquery;
        private int flagID;
        private int localID;
        private int neighborID;
        private int messageID;
        private int queryID;
        private String temp_table_name;
        private HashMap<Integer, ManagedChannel> idToChannel;

        public gRPC(String subquery, int flagID, int localID, int neighborID, int messageID, int queryID, String temp_table_name, HashMap<Integer, ManagedChannel> idToChannel) {
            this.subquery = subquery;
            this.flagID = flagID;
            this.localID = localID;
            this.neighborID = neighborID;
            this.messageID = messageID;
            this.queryID = queryID;
            this.temp_table_name = temp_table_name;
            this.idToChannel = idToChannel;
        }

        @Override
        public String call() {
            System.out.println("ClientNode " + this.localID + " pinging ClientNode " + this.neighborID);
            ShortReadQuery5ServiceGrpc.ShortReadQuery5ServiceBlockingStub client = ShortReadQuery5ServiceGrpc.newBlockingStub(idToChannel.get(neighborID));
            Request request = Request.newBuilder()
                    .setSubQuery(subquery)
                    .setFlag(flagID)
                    .setMessageId(messageID)
                    .setQueryId(queryID)
                    .setTempTableName(temp_table_name)
                    .build();
            ShortReadQuery5Request SR_request = ShortReadQuery5Request.newBuilder()
                    .setSRQ5Request(request)
                    .build();
            ShortReadQuery5Response SR_response = client.shortReadQuery5(SR_request);
            String msg = SR_response.getSRQ5Response().getNodeResponse();
            System.out.println(msg);
            return msg;
        }
    }
}
