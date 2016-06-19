/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * VersaServer
 */

package CheckerServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

class VersaServer extends Thread{
    //===Instance variables

    //Other threads
    private ClientFinder clientListener = null; //listens for client connections
    private VersaServerGUI gui = null; //gui interface and starting thread for server

    //Networking
    private ServerSocket serverSocket = null;
    private int num_connected = 0; //counter of active connections
    private int max_connections; //connection limit (to prevent server overloading)
    private boolean listening = false;

    //Client and game maps
    private HashMap<String, VersaServerThread> clients = null; //map of all the clientHandlers
    private HashMap<String, VersaCheckers> games = null; //map of all the game objects

    //===Constructor
    VersaServer(VersaServerGUI GUI){
        //We can add parameters to alter max_connection in the future
        this.gui = GUI;
        max_connections = 10;
    }

    //===Client Add/Remove Methods

    //Adds client name and handler to clients map if all good
    //Returns 0 - if all good, 1 - if server is full, 2 - if name is taken
    int addClient(VersaServerThread client){
        if(clients.containsKey(client.getClientName())){
            System.err.println("SERVER: Please choose another name"); //TODO - have the errors print to server GUI
            return 2;
        }else if(num_connected >= max_connections){
            System.err.println("SERVER: Connections full");
            return 1;
        }else{
            clients.put(client.getClientName(), client);
            num_connected += 1;
            sendClientList(); //Updates gui and clientHandlers with new client list
            return 0;
        }
    }

    //Returns client handler from clients map, updates gui and clientHandlers with new list
    void removeClient(VersaServerThread client){
        if(clients.containsKey(client.getClientName())){
            clients.remove(client.getClientName());
            num_connected -= 1;
            sendClientList(); //informs about removed client
        }else{ //This should never happen...
            System.err.println("SERVER: Could not remove client '" + client.getClientName() + "', not in client list.");
        }
    }

    //===Client List Update Method

    //Sends lists of IPs and usernames of all connected clients to server GUI and to clients
    //Called when a client connects or disconnects
    //TODO - optimize by having vars with name strings and lists that are just updated and resent upon add/remove
    private void sendClientList(){
        String names = "###name_list="; //command key
        String[] clientIPList = new String[clients.size()];
        VersaServerThread[] clientsList = (VersaServerThread[]) clients.values().toArray(new VersaServerThread[0]);
        for(int n = 0; n < clientsList.length; n++) {
            clientIPList[n] = String.valueOf(clientsList[n].getSocket().getRemoteSocketAddress()) + " "
                    + clientsList[n].getClientName();
            names += clientsList[n].getClientName() + ",";
        }
        names = names.substring(0, names.length()-1) + "###";
        gui.writeClientList(clientIPList); //writes the client IP/name list to the GUI
        //TODO - ensure that clients aren't getting client lists when not in awaiting game, to not disturb message flow
        for (VersaServerThread clientThread : clientsList) { //replaced with foreach for readability
            if (clientThread != null) {
                clientThread.sendMessage("server", names); //sends client list to each client
            }
        }
    }

    //===Listener Start/Stop Methods

    //Initializes socket, client list, game list, and starts socket listener thread
    //Called from VersaServerGUI
    //Returns 1 - success, 0 - error
    int startListener(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clients = new HashMap<String, VersaServerThread>();
            games = new HashMap<String, VersaCheckers>();
            clientListener = new ClientFinder(this, serverSocket, clients, games);
            clientListener.start();
            listening = true;
            return 1;
        } catch (IOException e) {
            System.err.println("SERVER: Could not listen on port: " + port);
            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println("SERVER: Failed to start listening, bad port number.");
            return 0;
        }
    }

    //Stops listening, but does not end program. Informs and disconnects all clients,
    //closes socket and ends listener thread, clears client and game lists
    //Returns 1 - success, 0 - error
    int stopListening() {
        try {
            if (listening) {
                for (VersaServerThread client : clients.values().toArray(new VersaServerThread[0])) {
                    client.sendMessage("server", "###disconnected###"); //sends server disconnect msg to each client
                    client.kill(); //kills each client thread
                }
                clientListener.kill();
                clientListener = null;
                serverSocket.close();
                gui.writeClientList(new String[0]); //clears GUI client list
                clients.clear();
                games.clear();
                num_connected = 0;
                listening = false;
            }
            return 1;
        } catch (IOException e) {
            System.err.println("SERVER: IOException - Could not close server.");
            return 0;
        }
    }

    //SendMessage Helper Method

    //Pushes commands/messages to <receiver> clients with specified <sender>
    void sendMessage(String receiver, String sender, String message) {
        if (clients.containsKey(receiver)) {
            VersaServerThread threadReceiver = clients.get(receiver); //gets the thread with receiver name
            threadReceiver.sendMessage(sender, message); //sends message to client through thread's sendMessage method
        } else if (receiver.equals("all")) {
            VersaServerThread[] clientsList = clients.values().toArray(new VersaServerThread[0]);
            for (VersaServerThread c : clientsList) {
                c.sendMessage(sender, message); //sends message to all of the servers
            }
        } else {
            System.err.println("SERVER: sendMessage Error - could not find client: " + receiver);
        }
    }
}
