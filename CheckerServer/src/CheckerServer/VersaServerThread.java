package CheckerServer;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

import java.util.HashMap;


/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * VersaServerThread
 */

public class VersaServerThread extends Thread{
    private VersaServer server = null;
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private boolean listening = true;
    private boolean ready = false;

    private HashMap<String, VersaServerThread> clients;
    private HashMap<String, VersaCheckers> games;

    private VersaServerThread opponent;

    public String name = "";

    private static class Message {
        public String recip;
        public String content;

        public Message(String plainText) {
            recip = plainText.substring(plainText.indexOf("###sendto")+10, plainText.indexOf("###message="));
            content = plainText.substring(plainText.indexOf("###message")+11, plainText.length());
        }

        @Override
        public String toString() {
            return "Recipient: " + recip + " Message: " + content;
        }
    }

    public VersaServerThread(VersaServer server, Socket socket, HashMap<String, VersaServerThread> clients, HashMap<String, VersaCheckers> games) {
        super("MyCheckersServerThread"); //TODO - give unique IDs to VSThreads
        this.socket = socket;
        this.server = server;
        this.clients = clients;
        this.games = games;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Cannot initial I/O for this thread.");
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void sendMessage(String sender, String incMessage) {
        out.println("###sentfrom=" + sender + "###message=" + incMessage);
    }

    public void kill() {
        listening = false;
    }

    //Client1 is always this thread, name
    public void newGame(String client1, String client2) {
        String gameString = "";
        opponent = clients.get(client2); //get the VersaServerThread of the opponent
        if (games.containsKey(client1+":"+client2)) {
            gameString = client1+":"+client2;
        } else if (games.containsKey(client2+":"+client1)) {
            gameString = client2+":"+client1;
        }

        if (!gameString.equals("")) {
            VersaCheckers game = (VersaCheckers) games.get(gameString);
            String board;
            if (gameString.substring(0, gameString.indexOf(":")).equals(client1)) {
                board = game.getBoard();
            } else {
                board = game.getRotated(game.getBoard());
            }
            server.sendMessage(client1, client2, "###game_already_exists###board="+board+"###turn="+game.getTurn()+"###");
        } else {
            VersaCheckers game = new VersaCheckers(client1, client2);
            games.put(client1+":"+client2, game);
            server.sendMessage(client1, client2, "###new_game_started###board="+game.getBoard()+"###");
            opponent.sendMessage(client1, "###new_game_started###board="+game.getBoard()+"###");
        }
    }

    public void endGame(String loser, String winner){
        String save = "";
        if(games.containsKey(loser+":"+winner)){
            save = loser+":"+winner;
        }else if(games.containsKey(winner+":"+loser)){
            save = winner+":"+loser;
        }else{
            System.err.println("unable to disconnect, does not exist");
        }
        if (winner.equals(name)) { //if this clientThread won
            sendMessage(loser, "###you_won###");
        } else { //send message to opponent
            opponent.sendMessage(loser, "###you_won###");
        }
        games.remove(save);
    }

    public void restartGame(String client1, String client2){
        VersaCheckers game = new VersaCheckers(client1, client2);
        games.put(client1+":"+client2, game);
        sendMessage(client2,
                "###new_game_restarted###board="+game.getBoard()+"###turn="+game.getTurn()+"###");
        opponent.sendMessage(client1,
                "###new_game_restarted###board="+game.getRotated(game.getBoard())+"###turn="+game.getTurn()+"###");
    }

    public void gameMove(String from, String to, String message) {
        String gameString = "";
        if (games.containsKey(from+":"+to)) {
            gameString = from+":"+to;
        } else if (games.containsKey(to+":"+from)) {
            gameString = to+":"+from;
        } else {
            System.err.println("Error: gameMove registered for non-existant game");
        }

        VersaCheckers game = (VersaCheckers) games.get(gameString);

        int[][] realBoard = new int[8][8];
        String res = message.substring(message.indexOf("###new_board=")+14, message.length()-4);
        String[] rows = res.split("\\],\\[");
        rows[0] = rows[0].substring(1, rows[0].length());
        rows[7] = rows[7].substring(0, rows[7].length()-1);

        for (int y = 0; y < 8; y++) {
            String chars[] = rows[y].split(",");
            for (int x = 0; x < 8; x++) {
                realBoard[y][x] = Integer.parseInt(chars[x]);
            }
        }

        game.setBoard(realBoard);
        game.changeTurns();
        String newBoard;
        if (gameString.substring(0, gameString.indexOf(":")).equals(to)) {
            newBoard = game.getBoard();
        } else {
            newBoard = game.getRotated(game.getBoard());
        }
        if (from.equals(name)) { //if message from this thread to opponent
            opponent.sendMessage(from, "###checkers_move###new_board="+newBoard+"###");
        } else { //message from opponent to this client
            sendMessage(from, "###checkers_move###new_board="+newBoard+"###");
        }

    }

    @Override
    public void run() {
        try {
            String inputLine;
            //TODO - allow server to send server-wide commands
            while ((inputLine = in.readLine()) != null && listening) {
                Message message = new Message(inputLine);
                if (message.recip.equals("server")) {
                    if (message.content.equals("###disconnecting###")) {
                        server.sendMessage("all", name, "###potential_chat_disconnected###"); //confusing name
                        break;
                    } else if (message.content.contains("###name=")) {
                        name = message.content.substring(message.content.indexOf("=")+1, message.content.length()-3);

                        int res = server.addClient(this);
                        if (res == 0) {
                            ready = true;
                        } else if (res == 1) {
                            sendMessage("server", "###too_many_connections###");
                            break;
                        } else if (res == 2) {
                            sendMessage("server", "###name_already_taken###");
                            break;
                        }
                    }
                } else { //Assumes that only 3 possible commands, could be problematic when scaling up
                    if (ready) {
                        //TODO - change handling so that other client is asked for game confirmation first
                        if (message.content.equals("###new_game_window###")) {
                            newGame(name, message.recip);
                        }
                        else if (message.content.contains("###new_move")) {
                            gameMove(name, message.recip, message.content);
                        }
                        else if (message.content.equals("###game_over###you_win###")) {
                            endGame(name, message.recip);
                        }
                        else if (message.content.equals("###new_game_restarted###")) {
                            restartGame(name, message.recip);
                        }
                        else {
                            server.sendMessage(message.recip, name, message.content);
                        }
                    }
                }
            }

            out.close();
            in.close();
            socket.close();
            if (ready) {
                server.removeClient(this);
            }

        } catch (IOException e) {}
    }
}
