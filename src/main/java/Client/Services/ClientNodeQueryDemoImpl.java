package Client.Services;

import Client.ClientNode;
import com.proto.ping.NodeInfo;
import com.proto.ping.PingRequest;
import com.proto.ping.PingResponse;
import com.proto.ping.PingServiceGrpc;
import io.grpc.stub.StreamObserver;

public class ClientNodeQueryDemoImpl extends PingServiceGrpc.PingServiceImplBase {

    private ClientNode clientNode;

    public ClientNodeQueryDemoImpl(ClientNode clientNode) {
        this.clientNode = clientNode;
    }

    @Override
    public void  pingQuery (PingRequest request, StreamObserver<PingResponse> responseObserver) {

        NodeInfo nodeInfo = request.getNodeInfo();
        int prevClientNodeID = nodeInfo.getId();

        // Log the message the server has received
        System.out.println("Received ping request from ClientNode " + prevClientNodeID + " to ClientNode " + this.clientNode.getId());

        if (this.clientNode.isVisited()) {
            System.out.println("ClientNode " + this.clientNode.getId() + " already visited");
        } else {
            this.clientNode.setJustCameFromClientNodeId(prevClientNodeID);
            try {
                this.clientNode.executeQueryOne(false);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        PingResponse response = PingResponse.newBuilder().setResult("Ping from ClientNode " + prevClientNodeID + " to ClientNode " + this.clientNode.getId() + " success!").build();

        //Send response
        responseObserver.onNext(response);

        //Complete the RPC call
        responseObserver.onCompleted();

    }
}
