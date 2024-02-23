package org.example;

import ClientLocal.ClientNode;
import ClientLocal.Utils.ClientNodeMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        System.out.println("Hello and welcome!");

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

        ClientNode nodeOne = new ClientNode("jdbc:postgresql://localhost:26257/",
                                            "socialnetwork?sslmode=disable",
                                            "org.postgresql.Driver",
                                            "anish",
                                            "",
                                            clientNodeMap,
                                            50051,
                                            1);

        ClientNode nodeTwo = new ClientNode("jdbc:postgresql://localhost:26257/",
                                            "socialnetwork?sslmode=disable",
                                            "org.postgresql.Driver",
                                            "anish",
                                            "",
                                            clientNodeMap,
                                            50052,
                                            2);

        ClientNode nodeThree = new ClientNode("jdbc:postgresql://localhost:26257/",
                                            "socialnetwork?sslmode=disable",
                                            "org.postgresql.Driver",
                                            "anish",
                                            "",
                                            clientNodeMap,
                                            50053,
                                            3);

        ClientNode nodeFour = new ClientNode("jdbc:postgresql://localhost:26257/",
                                            "socialnetwork?sslmode=disable",
                                            "org.postgresql.Driver",
                                            "anish",
                                            "",
                                            clientNodeMap,
                                            50054,
                                            4);

        ClientNode nodeFive = new ClientNode("jdbc:postgresql://localhost:26257/",
                                            "socialnetwork?sslmode=disable",
                                            "org.postgresql.Driver",
                                            "anish",
                                            "",
                                            clientNodeMap,
                                            50055,
                                            5);

        ClientNode nodeSix = new ClientNode("jdbc:postgresql://localhost:26257/",
                                            "socialnetwork?sslmode=disable",
                                            "org.postgresql.Driver",
                                            "anish",
                                            "",
                                            clientNodeMap,
                                            50056,
                                            6);

        nodeOne.connectDB();
        nodeTwo.connectDB();
        nodeThree.connectDB();
        nodeFour.connectDB();
        nodeFive.connectDB();
        nodeSix.connectDB();

        nodeOne.startMessengers();
        nodeTwo.startMessengers();
        nodeThree.startMessengers();
        nodeFour.startMessengers();
        nodeFive.startMessengers();
        nodeSix.startMessengers();

        try {
            nodeOne.startReceiver();
        } catch (IOException e) {
            System.out.println(e);
        }
        try {
            nodeTwo.startReceiver();
        } catch (IOException e) {
            System.out.println(e);
        }
        try {
            nodeThree.startReceiver();
        } catch (IOException e) {
            System.out.println(e);
        }
        try {
            nodeFour.startReceiver();
        } catch (IOException e) {
            System.out.println(e);
        }
        try {
            nodeFive.startReceiver();
        } catch (IOException e) {
            System.out.println(e);
        }
        try {
            nodeSix.startReceiver();
        } catch (IOException e) {
            System.out.println(e);
        }

        ArrayList<Long> times = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            nodeOne.executeQueryOne(true);
            System.out.println("Total time: " + nodeOne.getTotalTime() + " microseconds");

            times.add(nodeOne.getTotalTime());

//            nodeOne.reset(false);
//            nodeTwo.reset(false);
//            nodeThree.reset(false);
//            nodeFour.reset(false);
//            nodeFive.reset(false);
//            nodeSix.reset(false);
            nodeOne.broadcastReset(false); //NEED TO TEST
            System.out.println("Trail " + (i+1) + " completed!");

        }

        System.out.println("Average time: " + calculateAverage(times) + " microseconds");
        calculateStatistics(times);

        // Column names
        List<String> columnNames = Arrays.asList("Dummy Query");
        // Combine columns into a list of columns
        List<List<Long>> columns = Arrays.asList(times);

        // Specify the file path where you want to save the CSV
        String filePath = "output.csv";

        // Call the method to export the data
        exportToCsv(columnNames, columns, filePath);
    }

    public static long calculateAverage(ArrayList<Long> numbers) {
        long sum = 0;
        for (Long num : numbers) {
            sum += num;
        }

        // Avoid division by zero
        if (numbers.size() == 0) {
            return 0;
        }

        return sum / numbers.size();
    }

    public static void calculateStatistics(ArrayList<Long> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            System.out.println("The list is empty or null.");
            return;
        }

        Collections.sort(numbers); // Sort the list for median calculation

        // Calculate mean
        double mean = numbers.stream().mapToLong(Long::longValue).average().orElse(Double.NaN);

        // Calculate median
        double median;
        int size = numbers.size();
        if (size % 2 == 0) {
            median = (numbers.get(size / 2 - 1) + numbers.get(size / 2)) / 2.0;
        } else {
            median = numbers.get(size / 2);
        }

        // Calculate min and max
        long min = numbers.get(0);
        long max = numbers.get(size - 1);

        // Calculate variance
        double variance = numbers.stream().mapToDouble(n -> Math.pow(n - mean, 2)).sum() / size;

        // Calculate standard deviation
        double std = Math.sqrt(variance);

        // Output the results
        System.out.println("Mean: " + mean);
        System.out.println("Median: " + median);
        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        System.out.println("Variance: " + variance);
        System.out.println("Standard Deviation: " + std);
    }

    public static void exportToCsv(List<String> columnNames, List<List<Long>> columns, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // Write column names
            bw.write(String.join(",", columnNames));
            bw.newLine();

            // Assuming all columns are of the same length
            int numRows = columns.get(0).size();

            // Write data rows
            for (int i = 0; i < numRows; i++) {
                List<String> row = new ArrayList<>();
                for (List<Long> column : columns) {
                    row.add(column.get(i).toString()); // Convert Long to String
                }
                bw.write(String.join(",", row));
                bw.newLine();
            }
            System.out.println("Data exported successfully to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
