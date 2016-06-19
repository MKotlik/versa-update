/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * ClientFinder
 */

package CheckerServer;

import java.io.IOException;
import java.net.ServerSocket;

import java.util.HashMap;

class ClientFinder extends Thread{
    //===Instance Variables

    //Connection variables
    private VersaServer server; //reference given to handlers
    private ServerSocket serverSocket;
    private boolean keepLooking = true;

    //Client/Game Variables
    private HashMap<String, VersaServerThread> clients; //reference given to handlers
    private HashMap<String, VersaCheckers> games; //reference given to handlers

    //===Constructor
    ClientFinder(VersaServer server, ServerSocket serverSocket,
                        HashMap<String, VersaServerThread> clients, HashMap<String, VersaCheckers> games){
        this.server = server;
        this.serverSocket = serverSocket;
        this.clients = clients;
        this.games = games;
    }

    //===Thread Methods

    //Keeplooking is just sent to false as thread dies (technically useless?)
    void kill(){
        keepLooking = false;
    }

    //Run loops and blocks while waiting for connections, then spawns handlers and hands references upon connection
    @Override
    public void run(){
        while(keepLooking){
            try{
                VersaServerThread thread = new VersaServerThread(serverSocket.accept(), server, clients, games);
                thread.start();
            }catch(IOException e){
                System.err.println("SERVER: IOException while listening for connections - " + e.getMessage());
            }
        }
    }
}
