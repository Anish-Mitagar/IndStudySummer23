package ClientLocal.Services;

import ClientLocal.ClientNode;
import com.proto.reset.NodeInfo;
import com.proto.reset.ResetRequest;
import com.proto.reset.ResetResponse;
import com.proto.reset.ResetServiceGrpc;
import io.grpc.stub.StreamObserver;

public class ResetQueryImpl extends ResetServiceGrpc.ResetServiceImplBase {

    private ClientNode clientNode;

    public ResetQueryImpl(ClientNode clientNode) { this.clientNode = clientNode; }

    @Override
    public void resetQuery(ResetRequest request, StreamObserver<ResetResponse> responseObserver) {

        NodeInfo nodeInfo = request.getNodeInfo();
        int prevClientNodeID = nodeInfo.getId();
        boolean new_state = nodeInfo.getNewState();

        if (this.clientNode.isVisited() == new_state) {
            System.out.println("ClientNode " + this.clientNode.getId() + " already reset");
        } else {
            try {
                this.clientNode.broadcastReset(new_state);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        ResetResponse response = ResetResponse.newBuilder().setResult("Ping from ClientNode " + prevClientNodeID + " to ClientNode " + this.clientNode.getId() + " success!").build();

        //Send response
        responseObserver.onNext(response);

        //Complete the RPC call
        responseObserver.onCompleted();
    }
}
