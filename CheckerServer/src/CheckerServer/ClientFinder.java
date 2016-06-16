package CheckerServer;

import java.io.IOException;

import java.net.ServerSocket;

import java.util.HashMap;

/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * ClientFinder
 */

public class ClientFinder extends Thread{
    private VersaServer server;
    private ServerSocket serverSocket;
    private boolean keepLooking = true;
    private HashMap<String, VersaServerThread> clients;
    private HashMap<String, VersaCheckers> games;

    public ClientFinder(VersaServer server, ServerSocket serverSocket, HashMap<String, VersaServerThread> clients, HashMap<String, VersaCheckers> games){
        this.server = server;
        this.serverSocket = serverSocket;
        this.clients = clients;
        this.games = games;
    }

    public void kill(){
        keepLooking = false;
    }

    @Override
    public void run(){
        while(keepLooking){
            try{
                VersaServerThread thread = new VersaServerThread(server, serverSocket.accept(), clients, games);
                thread.start();
            }catch(IOException e){
                //Something here
            }
        }
    }
}
