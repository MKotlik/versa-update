package CheckerClient;

import java.net.Socket;
import java.net.UnknownHostException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * VersaServer
 */


public class VersaClient extends Thread{
    /**
     * This is the thread that handles the messages sent from the server
     * The thread will use the messages to modify the board
     */
    private VersaClientGUI gui = null;
    private Socket clientSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private boolean listening = true;

    public static class Message{
        public String sender;
        public String content;

        public Message(String message){
            sender = message.substring(message.indexOf("###sentfrom=")+12, message.indexOf("###message="));
            content = message.substring(message.indexOf("###message=")+11, message.length());
        }

        @Override
        public String toString(){
            return "Sender: " + sender + " Message: " + content;
        }
    }

    public VersaClient(VersaClientGUI gui){
        this.gui = gui;
    }

    public int connect(String IPAddress, int port, String name) {
        try {
            this.clientSocket = new Socket(IPAddress, port);
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            sendMessage("server", "###name=" + name + "###");
            return 1;
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host");
            return 0;
        } catch (IOException e) {
            System.err.println("Couldn't connect to host");
            return 0;
        }
    }

    public int disconnect() {
        sendMessage("server", "###disconnecting###");
        listening = false;
        return 1;
    }

    public int sendMessage(String destination, String message) {
        out.println("###sendto=" + destination + "###message=" + message);
        return 1;
    }

    @Override
    public void run() {
        String fromServer;

        try {
            while((fromServer = in.readLine()) != null && listening) {
                Message message = new Message(fromServer);
                if (message.sender.equals("server")) {
                    if (message.content.equals("###disconnected###")) {
                        gui.disconnect("Server disconnected");
                    }
                    else if (message.content.equals("###too_many_connections###")) {
                        gui.disconnect("Too many connections to server");
                    }
                    else if (message.content.equals("###name_already_taken###")) {
                        gui.disconnect("That name is already taken");
                    }
                    else if (message.content.contains("###name_list=")) {
                        String[] names = message.content.substring(13, message.content.length()-3).split(",");
                        gui.setUserList(names);
                    }
                } else {
                    if (message.content.equals("###potential_chat_disconnected###")) {
                        gui.connectionDied(message.sender);
                    }
                    else if (message.content.contains("###game_already_exists")) {
                        String b = message.content.substring(message.content.indexOf("###board=")+9, message.content.indexOf("###turn="));
                        String t = message.content.substring(message.content.indexOf("###turn=")+8, message.content.length()-3);
                        gui.updateGame(message.sender, b, t, "Opened an existing game.");
                    }
                    else if (message.content.contains("###new_game_started")) {
                        String b = message.content.substring(message.content.indexOf("###board=")+9, message.content.length()-3);
                        gui.updateGame(message.sender, b, "You have started a new game.");
                    }
                    else if (message.content.contains("###new_game_restarted")) {
                        String b = message.content.substring(message.content.indexOf("###board=")+9, message.content.indexOf("###turn="));
                        String t = message.content.substring(message.content.indexOf("###turn=")+8, message.content.length()-3);
                        gui.updateGame(message.sender, b, t, "A new game has been started.");
                    }
                    else if (message.content.contains("###checkers_move")) {
                        String b = message.content.substring(message.content.indexOf("###new_board=")+13, message.content.length()-3);
                        gui.updateGame(message.sender, b, "New move from "+message.sender+".");
                    }
                    else if (message.content.equals("###you_won###")) {
                        gui.notifyWin(message.sender);
                    }

                    else {
                        gui.recievedMessage(message.sender, message.content);
                    }
                }
            }

            in.close();
            out.close();
            clientSocket.close();
        } catch(IOException e) {
            System.err.println("Error listening to server input");
            System.exit(1);
        }
    }
}
