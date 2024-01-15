package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import client.ClientHandler;
import utils.PersistenceCalculator;
import worker.WorkerInfo;

public class Server {
    private ServerSocket serverSocket;
    private Hashtable<Integer, WorkerInfo> workerTable;
    private Hashtable<Integer, ClientHandler> clientTable;
    private int workerCounter;
    private ExecutorService workerExecutor;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        workerTable = new Hashtable<>();
        clientTable = new Hashtable<>();
        workerCounter = 0;
        workerExecutor = Executors.newCachedThreadPool();
    }

    public void start() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String messageType = in.readLine();
                int clientId = clientTable.size();
                if (messageType.equals("CLIENT")) {
                    ClientHandler clientHandler = new ClientHandler(socket, this, clientId);
                    clientTable.put(clientId, clientHandler);
                    clientHandler.start();
                    System.out.println("Client " + clientId + " connecté.");
                } else  {
                    System.out.println("Worker " + workerCounter + " connecté.");
                    int cores = Integer.parseInt(in.readLine());
                    int workerId = workerCounter++;
                    PrintWriter workerOut = new PrintWriter(socket.getOutputStream(), true);
                    WorkerInfo workerInfo = new WorkerInfo(workerId, cores, workerOut);
                    workerTable.put(workerId, workerInfo);
                    //attribtuion tache au worker
                    // assignTasks(workerId);
                // } else if (messageType.equals("RESULTAT")) {
                //     int resultClientId = Integer.parseInt(in.readLine());
                //     long resultNumber = Long.parseLong(in.readLine());
                //     int resultPersistence = Integer.parseInt(in.readLine());
                //     handleWorkerResult(resultClientId, resultNumber, resultPersistence);
                // } else if (messageType.equals("INTERVALLES")) {
                //     long start = Long.parseLong(in.readLine());
                //     long end = Long.parseLong(in.readLine());
                //     int clientIdd = clientTable.size();
                //     ClientHandler clientHandler = new ClientHandler(socket, this, clientId);
                //     clientTable.put(clientIdd, clientHandler);
                //     clientHandler.start();
                //     assignTasksToWorkers(start, end, clientIdd);
                // }
                //affichage des clients et workers actifs
                System.out.println("nb clients: " + getActiveClientCount());
                System.out.println("nb workers: " + getActiveWorkerCount());
            }} catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void assignTasks(int workerId, long start, long end, int clientId) {
        WorkerInfo worker = workerTable.get(workerId);
        worker.getOut().println(clientId + " " + start + " " + end);
    } 

    public void assignTasksToWorkers(long start, long end, int clientId) {
        // Trouver le worker avec le plus petit nombre de tâches en cours
        int workerIdWithLeastTasks = -1;
        int minTasksInProgress = Integer.MAX_VALUE;
        for (int workerId : workerTable.keySet()) {
            WorkerInfo worker = workerTable.get(workerId);
            if (worker.getTasksInProgress() < minTasksInProgress) {
                workerIdWithLeastTasks = workerId;
                minTasksInProgress = worker.getTasksInProgress();
            }
        }
    
        if (workerIdWithLeastTasks != -1) {
            assignTasks(workerIdWithLeastTasks, start, end, clientId);
            WorkerInfo worker = workerTable.get(workerIdWithLeastTasks);
            worker.incrementTasksInProgress();
        } else {
            System.out.println("Aucun worker disponible pour traiter la demande.");
        }
    }
    

    public void handleWorkerResult(int clientId, long number, int persistence) {
        ClientHandler clientHandler = clientTable.get(clientId);
        if (clientHandler != null) {
            clientHandler.sendResult(number, persistence);
            for (WorkerInfo worker : workerTable.values()) {
                if (worker.getOut() == clientHandler.getOut()) {
                    worker.decrementTasksInProgress();
                    break;
                }
            }
        }
    }
    
    
    
    public void storeResult(long number, int persistence, int clientId) {
        ClientHandler clientHandler = clientTable.get(clientId);
        if (clientHandler != null) {
            clientHandler.sendResult(number, persistence);
        }

    }

    public String getStatistics() {
        return "Statistiques";
    }

    public int getPersistence(long number) {
        return PersistenceCalculator.calculatePersistence(number);
    }

    public String getRangeResults(long start, long end) {
        return "pas implémenté";
    }

    public String getMaxPersistenceNumbers() {
        return "pas implémenté";
    }

    public int getActiveClientCount() {
        return clientTable.size();
    }
    
    public int getActiveWorkerCount() {
        return workerTable.size();
    } 

    public static void main(String[] args) {
        int port = 12345; 
        try {
            Server server = new Server(port);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}    