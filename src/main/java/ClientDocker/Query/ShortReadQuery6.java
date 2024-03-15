package ClientDocker.Query;

import com.proto.ShortReadQuery5.Request;
import com.proto.ShortReadQuery5.ShortReadQuery5Request;
import com.proto.ShortReadQuery5.ShortReadQuery5Response;
import com.proto.ShortReadQuery5.ShortReadQuery5ServiceGrpc;
import io.grpc.ManagedChannel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class ShortReadQuery6 implements Runnable{
    private Connection connection;
    private int localId;
    private ArrayList<Integer> neighbors;
    private HashMap<Integer, ManagedChannel> idToChannel;
    private ExecutorService executor;
    private List<Future<String>> message_futures = new ArrayList<>();
    private List<Future<String>> forum_futures = new ArrayList<>();
    private List<Future<String>> moderator_futures = new ArrayList<>();
    private int sleepTime;
    private int messageID;
    private int counter = 0;
    private String temp_message_table_name = "temp_message";
    private String temp_forum_table_name = "temp_forum";
    private String temp_person_table_name = "temp_person";
    private int queryID;
    private int forumID;
    private int moderatorID;
    private String moderatorFirstName;
    private String moderatorLastName;
    private String forumTitle;
    private int totalNodes = 4;
    private String find_message_row;
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
            "    queryId int NOT NULL," +
            "    INDEX (LocationCountryId),\n" +
            "    INDEX (CreatorPersonId),\n" +
            "    INDEX (ContainerForumId),\n" +
            "    INDEX (ParentMessageId)\n" +
            ");";

    private String find_forum_row;
    private String create_temp_forum_table = "CREATE TABLE IF NOT EXISTS temp_forum (\n" +
            "    creationDate timestamp with time zone NOT NULL,\n" +
            "    id bigint PRIMARY KEY,\n" +
            "    title varchar(256) NOT NULL,\n" +
            "    ModeratorPersonId bigint, -- can be null as its cardinality is 0..1\n" +
            "    serverId int NOT NULL,\n" +
            "    queryId int NOT NULL\n" +
            ");";

    private String find_person_row;
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
            "    queryId int NOT NULL,\n"+
            "    INDEX (LocationCityId)\n" +
            ");";

    public ShortReadQuery6(Connection connection, int localId, ArrayList<Integer> neighbors, HashMap<Integer, ManagedChannel> idToChannel, int sleepTime, int messageID) {
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
            //Run query locally
            this.find_message_row = "SELECT * FROM message WHERE id="+this.messageID+" AND serverId=";
            String find_message_row_locally = this.find_message_row + this.localId + "";
            ResultSet resultSet = statement.executeQuery(find_message_row_locally);
            if(resultSet.next()) {
                //message found locally -> fetch containerForumId
                this.forumID = (int)resultSet.getLong("ContainerForumId");
                System.out.println("Message ID: "+this.messageID+" has forum id: "+this.forumID);
            }
            else {
                //message row not found locally. 1-> Create temp message table  2-> Call other nodes
                System.out.println("No rows found in the result message set locally.");
                //Create temp message table
                statement.execute(create_temp_message_table);
                System.out.println("Temp message table created successfully");
                this.queryID = this.totalNodes * this.counter + this.localId;
                ++this.counter;

                //ping all other nodes
                try {
                    if(this.neighbors.size() > 0) {
                        this.executor = Executors.newFixedThreadPool(this.neighbors.size());
                        for(int i = 0; i < this.neighbors.size(); ++i) {
                            String serverSubquery = this.find_message_row + neighbors.get(i) + "";
                            Future<String> future = executor.submit(new gRPC(serverSubquery, 1, this.localId, this.neighbors.get(i), this.messageID, this.queryID, this.temp_message_table_name, this.idToChannel));
                            this.message_futures.add(future);
                        }
                    }
                }
                catch(Exception e) {
                    System.out.println("Query failed at node: "+this.localId);
                    e.printStackTrace();
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

                //Fetch message.ContainerForumId from temp table
                String fetch_forum_id = "SELECT * FROM "+temp_message_table_name;
                try{
                    ResultSet fetchSet = statement.executeQuery(fetch_forum_id);
                    if(fetchSet.next()) {
                        this.forumID = (int)fetchSet.getLong("ContainerForumId");
                        System.out.println("ForumID: "+this.forumID);
                    }
                    else {
                        System.out.println("Message row NOT found in temp message table");
                    }
                }
                catch(SQLException e) {
                    e.printStackTrace();
                }
            }

            //Forum ID found. Now look for forum row locally if not found ping other nodes to fetch forum ID.
            this.find_forum_row = "SELECT * FROM forum where id="+this.forumID+" AND serverId=";
            String find_forum_row_locally = this.find_forum_row + this.localId + "";
            ResultSet forumSet = statement.executeQuery(find_forum_row_locally);
            if(forumSet.next()) {
                //forum row found locally -> fetch ModeratorPersonId
                this.moderatorID = (int)forumSet.getLong("ModeratorPersonId");
                this.forumTitle = forumSet.getString("title");
                System.out.println("Forum ID: "+this.forumID+" called "+this.forumTitle+" has ModeratorPersonID: "+this.moderatorID);
            }
            else {
                //forum row not found locally -> ping other nodes
                //1. Create temp forum table
                System.out.println("Forum rows not found locally");
                statement.execute(create_temp_forum_table);
                System.out.println("Temp forum table created successfully");

                //2. Ping other nodes
                for(int i = 0; i < this.neighbors.size(); ++i) {
                    String forum_subquery = this.find_forum_row + this.neighbors.get(i) + "";
                    Future<String> future = executor.submit(new gRPC(forum_subquery, 2, this.localId, this.neighbors.get(i), this.messageID, this.queryID, this.temp_forum_table_name, this.idToChannel));
                    this.forum_futures.add(future);
                }

                //Wait for acknowledgement from other nodes
                List<String> forum_results = new ArrayList<>();
                for(Future<String> fut : forum_futures) {
                    try {
                        forum_results.add(fut.get());
                    }
                    catch(InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("I Client Node " + this.localId + " had all person threads return");

                //Fetch ModeratorPersonId from the temp table
                String fetch_moderator_person_id = "SELECT * FROM "+this.temp_forum_table_name;
                try {
                    ResultSet fetchSet = statement.executeQuery(fetch_moderator_person_id);
                    if(fetchSet.next()) {
                        this.moderatorID = (int)fetchSet.getLong("ModeratorPersonId");
                        this.forumTitle = fetchSet.getString("title");
                        System.out.println("Forum ID: "+this.forumID+" called "+this.forumTitle+" has ModeratorPersonID: "+this.moderatorID);
                    }
                    else {
                        System.out.println("Forum row NOT found in temp forum table");
                    }
                }
                catch(SQLException e) {
                    e.printStackTrace();
                }
            }


            //Find moderator details
            this.find_person_row = "SELECT * FROM person where id="+this.moderatorID+" AND serverId=";
            String find_person_row_locally = this.find_person_row + this.localId + "";
            ResultSet personSet = statement.executeQuery(find_person_row_locally);
            if(personSet.next()) {
                //Moderator row found locally -> fetch moderator firstName and lastName
                this.moderatorFirstName = personSet.getString("firstName");
                this.moderatorLastName = personSet.getString("lastName");
                System.out.println("Moderator first name: "+this.moderatorFirstName+" last name: "+this.moderatorLastName);
            }
            else {
                //moderator row not found locally -> ping other nodes
                //1. Create temp person table
                System.out.println("Moderator rows not found locally");
                statement.execute(create_temp_person_table);
                System.out.println("Temp person table created successfully");

                //Ping other nodes
                for(int i = 0; i < this.neighbors.size(); ++i) {
                    String moderator_subquery = this.find_person_row + this.neighbors.get(i) + "";
                    Future<String> future = executor.submit(new gRPC(moderator_subquery, 3, this.localId, this.neighbors.get(i), this.messageID, this.queryID, this.temp_person_table_name, this.idToChannel));
                    this.moderator_futures.add(future);
                }

                //Wait for acknowledgement from other nodes
                List<String> moderator_results = new ArrayList<>();
                for(Future<String> fut : moderator_futures) {
                    try {
                        moderator_results.add(fut.get());
                    }
                    catch(InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("I Client Node " + this.localId + " had all person threads return");

                //Fetch Moderator firstname and last name from the temp table
                String fetch_moderator_details = "SELECT * FROM "+this.temp_person_table_name;
                try {
                    ResultSet fetchSet = statement.executeQuery(fetch_moderator_details);
                    if(fetchSet.next()) {
                        this.moderatorFirstName = fetchSet.getString("firstName");
                        this.moderatorLastName = fetchSet.getString("lastName");
                    }
                    else {
                        System.out.println("Moderator person row NOT found in temp person table");
                    }
                }
                catch(SQLException e) {
                    e.printStackTrace();
                }
            }

            if (this.executor != null && !this.executor.isShutdown()) {
                this.executor.shutdown();
            }

            //OUTPUT FOR QUERY 6
            System.out.println("-----------OUTPUT--------------");
            System.out.println("Forum ID: "+this.forumID);
            System.out.println("Forum Title: "+this.forumTitle);
            System.out.println("Moderator ID: "+this.moderatorID);
            System.out.println("Moderator First Name: "+this.moderatorFirstName);
            System.out.println("Moderator Last Name: "+this.moderatorLastName);

        }
        catch(SQLException e) {
            e.printStackTrace();
        }
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
