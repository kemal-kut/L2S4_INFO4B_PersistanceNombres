package worker;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.*;

import utils.PersistenceCalculator;

public class Worker {
    private Socket workerSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Map<Integer, Boolean> clientsBeingProcessed;
    private int cores;

    public Worker(String host, int port, int cores) throws IOException {
        workerSocket = new Socket(host, port);
        out = new PrintWriter(workerSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
        this.cores = cores;
        clientsBeingProcessed = new ConcurrentHashMap<>(); //utilisation d'une ConcurrentHashMap pour une utilisation concurrente sans problème
    }

    public void start() {
        try {
            out.println("WORKER"); //envoi d'un message pour s'identifier en tant que worker
            out.println(cores); //envoi du nombre de coeurs de processeur
            ExecutorService taskExecutor = Executors.newFixedThreadPool(cores); // création d'un ExecutorService pour traiter les tâches

            String task;
            while ((task = in.readLine()) != null) { //boucle pour recevoir les tâches à traiter
                String[] taskParts = task.split(" ");
                int clientId = Integer.parseInt(taskParts[0]);

                //Vérifier si le client est déjà en cours de traitement
                if (clientsBeingProcessed.putIfAbsent(clientId, true) != null) { //vérifier si le client est déjà en train d'être traité
                    System.out.println("Le client " + clientId + " est déjà en cours de traitement.");
                    continue;
                }

                long start = Long.parseLong(taskParts[1]);
                long end = Long.parseLong(taskParts[2]);

                taskExecutor.submit(() -> { //soumission de la tâche à l'ExecutorService
                    System.out.println("Intervalles à calculer pour le client " + clientId + ": " + start + " -> " + end);
                    for (long number = start; number <= end; number++) {
                        int persistence = PersistenceCalculator.calculatePersistence(number); //calcul de la persistance du nombre
                        System.out.println("Calcul du nombre " + number + " pour le client " + clientId + " avec persistence " + persistence);
                        out.println("RESULTAT"); //nvoi du résultat au serveur
                        out.println(clientId);
                        out.println(number);
                        out.println(persistence);
                    }
                    System.out.println("Calcul des intervalles pour le client " + clientId + " terminé.");

                    //Marquer le client comme ayant terminé le traitement
                    clientsBeingProcessed.remove(clientId);
                });
            }

            taskExecutor.shutdown(); //arrêt de l'ExecutorService
            workerSocket.close(); //fermeture de la socket
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PrintWriter getOut() {
        return out;
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;
        int cores = Runtime.getRuntime().availableProcessors(); //récupération du nombre de coeurs de processeur disponibles
        try {
            Worker worker = new Worker(host, port, cores);
            worker.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
