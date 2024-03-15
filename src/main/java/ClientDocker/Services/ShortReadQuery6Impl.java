package ClientDocker.Services;
import ClientDocker.ClientNode;
import com.proto.ShortReadQuery5.*;
import io.grpc.stub.StreamObserver;
import java.sql.*;

public class ShortReadQuery6Impl extends ShortReadQuery5ServiceGrpc.ShortReadQuery5ServiceImplBase {
    private ClientNode clientNode;

    public ShortReadQuery6Impl(ClientNode clientNode) {
        this.clientNode = clientNode;
    }

    @Override
    public void shortReadQuery5(ShortReadQuery5Request request, StreamObserver<ShortReadQuery5Response> responseObserver) {
        //Unwrap the request received
        Request req = request.getSRQ5Request();
        String subquery = req.getSubQuery();
        int flagID = req.getFlag();
        String temp_table_name = req.getTempTableName();
        int queryID = req.getQueryId();
        String return_message = "";

        try {
            Connection connection = clientNode.getDBConnection();
            Statement statement = connection.createStatement();
            //Use the 'socialnetwork' database
            statement.execute("USE socialnetwork");
            System.out.println("Subquery Received: "+subquery);
            ResultSet resultSet = statement.executeQuery(subquery);
            if(resultSet.next()) {
                return_message = "ack";

                if(flagID == 1) {
                    //Add result row to temp_message table
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + temp_table_name + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    int columnCount = resultSet.getMetaData().getColumnCount();

                    for(int i = 1; i <= columnCount; ++i) {
                        preparedStatement.setString(i, resultSet.getString(i));
                    }

                    // Set queryId as the last parameter
                    preparedStatement.setInt(columnCount + 1, queryID);

                    System.out.println("Insert message statement getting executed");
                    System.out.println(preparedStatement.toString());
                    //Execute the insert query
                    int rowsInserted = preparedStatement.executeUpdate();

                    if (rowsInserted > 0) {
                        System.out.println("Insert successful. Rows inserted: " + rowsInserted);
                    } else {
                        System.out.println("Insert did not affect any rows.");
                    }
                }
                if(flagID == 2) {
                    //Add forum row to temp_forum table
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "+temp_table_name+" VALUES(?, ?, ?, ?, ?, ?)");
                    int columnCount = resultSet.getMetaData().getColumnCount();

                    for(int i = 1; i <= columnCount; ++i) {
                        preparedStatement.setString(i, resultSet.getString(i));
                    }

                    //Set queryId as the last parameter
                    preparedStatement.setInt(columnCount+1, queryID);
                    System.out.println("Insert message statement getting executed");
                    System.out.println(preparedStatement.toString());

                    //Execute the insert query
                    int rowsInserted = preparedStatement.executeUpdate();
                    if(rowsInserted > 0) {
                        System.out.println("Insert successful. Rows inserted: "+rowsInserted);
                    }
                    else {
                        System.out.println("Insert did not affect any rows");
                    }
                }
                if(flagID == 3) {
                    //Add moderator row to temp_person_table
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "+temp_table_name+" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    int columnCount = resultSet.getMetaData().getColumnCount();

                    for(int i = 1; i <= columnCount; ++i) {
                        preparedStatement.setString(i, resultSet.getString(i));
                    }

                    //Set queryId as the last parameter
                    preparedStatement.setInt(columnCount+1, queryID);
                    System.out.println("Insert message statement getting executed");
                    System.out.println(preparedStatement.toString());

                    //Execute the insert query
                    int rowsInserted = preparedStatement.executeUpdate();
                    if(rowsInserted > 0) {
                        System.out.println("Insert successful. Rows inserted: "+rowsInserted);
                    }
                    else {
                        System.out.println("Insert did not affect any rows");
                    }
                }
            }
            else {
                return_message = "nack";
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }

        Response response = Response.newBuilder().setNodeResponse(return_message).build();
        ShortReadQuery5Response SR_reponse = ShortReadQuery5Response.newBuilder().setSRQ5Response(response).build();

        //Send Response
        responseObserver.onNext(SR_reponse);

        //Complete the RPC Call
        responseObserver.onCompleted();
    }
}
