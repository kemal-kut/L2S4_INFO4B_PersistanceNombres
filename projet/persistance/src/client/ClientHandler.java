package client;

import java.io.*;
import java.net.*;

import server.Server;

public class ClientHandler extends Thread {
    //éclaration des variables pour la communication et le stockage des informations
    private Socket clientSocket;
    private Server server;
    private BufferedReader in;
    private PrintWriter out;
    private int clientId;

    public ClientHandler(Socket clientSocket, Server server, int clientId) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.clientId = clientId;
    }

    ////méthode principale pour exécuter le thread ClientHandler
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Requête de la part du client " + clientId + ": " + request);
                String[] requestParts = request.split(" ");

                //Traitement des différentes commandes
                switch (requestParts[0]) {
                    case "INTERVALLES":
                    //Récupération des arguments et assignation des tâches aux workers
                        long start = Long.parseLong(requestParts[1]);
                        long end = Long.parseLong(requestParts[2]);
                        server.assignTasksToWorkers(start, end, clientId);
                        break;
                    
                    case "PERSISTANCE":
                    //Récupération de la persistance d'un nombre
                        long number = Long.parseLong(requestParts[1]);
                        out.println(server.getPersistence(number));
                        break;

                    case "STATS":
                        //récupération des statistiques
                        out.println(server.getStatistics());
                        break;

                    case "INTERVALLE_RESULTATS":
                    //récupération des résultats dans un intervalle donné
                        long a = Long.parseLong(requestParts[1]);
                        long b = Long.parseLong(requestParts[2]);
                        String results = server.getRangeResults(a, b);
                        out.println(results);
                        break;

                    case "MAX_PERSISTANCE":
                    //Récupération des nombres avec la persistance maximale
                        String maxPersistenceNumbers = server.getMaxPersistenceNumbers();
                        out.println(maxPersistenceNumbers);
                        break;

                    default:
                        out.println("COMMANDE INCORRECTE");
                        break;
                }
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //méthode pour récupérer le flux de sortie (pour envoyer des messages au client)
    public PrintWriter getOut() {
        return out;
    }

    public void sendResult(long n, int p) {
        out.println("Nombre: " + n + " persistance = " + p);
    }
}
