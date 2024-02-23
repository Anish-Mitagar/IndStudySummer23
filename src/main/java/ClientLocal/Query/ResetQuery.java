package ClientLocal.Query;

import ClientLocal.ClientNode;
import com.proto.reset.NodeInfo;
import com.proto.reset.ResetRequest;
import com.proto.reset.ResetResponse;
import com.proto.reset.ResetServiceGrpc;
import io.grpc.ManagedChannel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class ResetQuery implements Runnable {

    private ClientNode clientNode;
    private int localId;
    private int sleepTime;
    private boolean beganBranching  = true;
    private ArrayList<Integer> neighbors;
    private HashMap<Integer, ManagedChannel> idToChannel;
    private ArrayList<Thread> threads = new ArrayList<>();
    private ExecutorService executor;
    private boolean new_state;

    public ResetQuery(ClientNode clientNode, int localId, ArrayList<Integer> neighbors, HashMap<Integer, ManagedChannel> idToChannel, boolean new_state, int sleepTime) {
        this.localId = localId;
        this.sleepTime = sleepTime;
        this.clientNode = clientNode;
        this.neighbors = neighbors;
        this.idToChannel = idToChannel;
        this.new_state = new_state;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(this.sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("I Client Node " + this.localId + " am resetting");

        List<Future<String>> list = new ArrayList<>();

        try {

            this.clientNode.reset(new_state);

            if (this.beganBranching && this.neighbors.size() > 0) {
                this.beganBranching = false;
                this.executor = Executors.newFixedThreadPool(this.neighbors.size());
                for (int i = 0; i < this.neighbors.size(); i++) {
                    Future<String> future = executor.submit(new ResetQuery.gRPC(this.neighbors.get(i), this.localId, this.idToChannel));
                    list.add(future);
                }
            }


        } catch (Exception e) {
            System.out.println("Reset failed at ClientNode " + this.localId);
            return;
        }

        System.out.println("I Client Node " + this.localId + " have successfully finished resetting!");

        List<String> results = new ArrayList<>();

        if (this.neighbors.size() > 0) {
            for (Future<String> fut : list) {
                try {
                    results.add(fut.get());

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            this.executor.shutdown();
        }



        System.out.println("I Client Node " + this.localId + " had all threads return");

    }

    class gRPC implements Callable<String> {

        private int neighborId;
        private int localId;

        private HashMap<Integer, ManagedChannel> idToChannel;

        public gRPC(int neighborId, int localId, HashMap<Integer, ManagedChannel> idToChannel) {
            this.neighborId = neighborId;
            this.localId = localId;
            this.idToChannel = idToChannel;
        }
        @Override
        public String call() throws Exception {
            System.out.println("ClientNode " + this.localId + " pinging ClientNode " + this.neighborId);
            ResetServiceGrpc.ResetServiceBlockingStub client = ResetServiceGrpc.newBlockingStub(this.idToChannel.get(neighborId));
            NodeInfo nodeInfo = NodeInfo.newBuilder().setId(this.localId).build();
            ResetRequest request = ResetRequest.newBuilder().setNodeInfo(nodeInfo).build();
            ResetResponse response = client.resetQuery(request);
            String msg = response.getResult();
            System.out.println(msg);
            return msg;
        }
    }
}
