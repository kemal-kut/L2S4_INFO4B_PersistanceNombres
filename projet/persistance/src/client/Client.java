package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    //déclaration des variables pour la communication et l'entrée utilisateur
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner sc;

    //constructeur de la classe Client
    public Client(String host, int port) throws IOException {
        clientSocket = new Socket(host, port); //création d'un socket pour se connecter au serveur
        out = new PrintWriter(clientSocket.getOutputStream(), true); //initialisation des flux d'entrée et de sortie pour la communication
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        sc = new Scanner(System.in); //initialisation du scanner pour lire les entrées de l'utilisateur
    }

    //méthode pour lancer le client
    public void start() {
        try {
            out.println("CLIENT"); //envoie le type de connexion au serveur
            System.out.println("Connecté au serveur."); //affiche un message confirmant la connexion au serveur
            String input;
            while (true) { //invite l'utilisateur à entrer une commande
                System.out.print("Entrez commande: ");
                input = sc.nextLine(); //Lit l'entrée de l'utilisateur
                if (input.equalsIgnoreCase("QUITTER")) { //Vérifie si l'entrée de l'utilisateur est "QUITTER"
                    break; //on stop la boucle
                }
                if (input.startsWith("INTERVALLES")) { //Vérifie si la commande est "INTERVALLES"
                    String[] inputParts = input.split(" "); //Sépare la commande en parties
                    if (inputParts.length == 3) { //Vérifie si la commande est bien formée
                        long start = Long.parseLong(inputParts[1]); //onvertit les arguments en long
                        long end = Long.parseLong(inputParts[2]);
                        out.println("INTERVALLES " + start + " " + end); //eenvoie la commande au serveur
                    } else {
                        System.out.println("INTERVALLES <A> <B>"); //afiche une erreur si la commande est mal formée
                    }
                } else {
                    out.println(input); //Envoie la commande au serveur
                }
                //affiche la requête envoyée
                System.out.println("Requête envoyée: " + input);
                //lit la réponse du serveur
                String response = in.readLine();
                //affiche la réponse du serveur
                System.out.println("Réponse serveur: " + response);
                //verifie si le traitement est terminé pour un client
                if (response.startsWith("TERMINÉ")) {
                    //separe la réponse en parties
                    String[] responseParts = response.split(" ");
                    // recupère l'ID du client
                    int clientId = Integer.parseInt(responseParts[1]);
                    //affiche un message indiquant que le traitement est terminé
                    System.out.println("Traitement terminé pour le client " + clientId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {//fermeture
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;
        try {
            Client client = new Client(host, port);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
