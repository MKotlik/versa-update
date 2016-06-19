/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * VersaServerThread
 */

package CheckerServer;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

import java.util.HashMap;

class VersaServerThread extends Thread{
    //===Instance Variables

    //Client Communication Variables
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private boolean listening = true;
    private boolean ready = false;

    //Local Server Variables
    private VersaServer server = null;
    private HashMap<String, VersaServerThread> clients;
    private HashMap<String, VersaCheckers> games;

    //Reference To Opponent Handler
    private VersaServerThread opponent;

    //Client's Username (Must be unique for server)
    private String name = "";

    //===Nested Message Class

    //Simplifies handling of inter-thread and inter-client messaging
    private static class Message {
        private String recip;
        private String content;

        String getRecip() {
            return recip;
        }

        String getContent() {
            return content;
        }

        Message(String plainText) {
            recip = plainText.substring(plainText.indexOf("###sendto")+10, plainText.indexOf("###message="));
            content = plainText.substring(plainText.indexOf("###message")+11, plainText.length());
        }

        @Override
        public String toString() {
            return "Recipient: " + recip + " Message: " + content;
        }
    }

    //===Constructor
    VersaServerThread(Socket socket, VersaServer server,
                             HashMap<String, VersaServerThread> clients, HashMap<String, VersaCheckers> games) {
        super("MyCheckersServerThread"); //TODO - give unique IDs to VSThreads
        this.socket = socket;
        this.server = server;
        this.clients = clients;
        this.games = games;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("SERVER: Couldn't initialize client I/O for a thread - " + e.getMessage());
            //TODO - Replace "a thread" with thread ID once we get one
        }
    }

    //===Handler Detail Accessors

    String getClientName() {
        return name;
    }

    Socket getSocket() {
        return socket;
    }

    //===Thread Methods

    //Run loops and blocks continuoisly, waiting for messages from client, and acting on them
    //Handles connects, disconnects, and game operations
    @Override
    public void run() {
        try {
            String inputLine;
            //TODO - allow server to send server-wide commands
            while (listening && (inputLine = in.readLine()) != null) {
                Message message = new Message(inputLine); //Converts text into message format
                if (message.getRecip().equals("server")) { //If message meant for server-wide action
                    if (message.getContent().equals("###disconnecting###")) { //Disconnecting
                        server.sendMessage("all", name, "###potential_chat_disconnected###"); //confusing name
                        break;
                    } else if (message.getContent().contains("###name=")) { //First contact, sending name
                        name = message.getContent().substring(message.getContent().indexOf("=")+1,
                                message.getContent().length()-3);
                        int res = server.addClient(this); //Attempt to add to list, check uniqueness and client limit
                        if (res == 0) {
                            ready = true;
                            //We don't notify the client that it's been accepted? It just assumes?
                        } else if (res == 1) {
                            sendMessage("server", "###too_many_connections###");
                            break;
                        } else if (res == 2) {
                            sendMessage("server", "###name_already_taken###");
                            break;
                        }
                    }
                } else { //Message must be game-related (meant for opponent if not for server)
                    if (ready) { //Name has been accepted, ready for game
                        //TODO - change handling so that other client is asked for game confirmation first
                        //Calls appropriate game handling method for command
                        if (message.getContent().equals("###new_game_window###")) {
                            newGame(name, message.getRecip());
                        }
                        else if (message.getContent().contains("###new_move")) {
                            gameMove(name, message.getRecip(), message.getContent());
                        }
                        else if (message.getContent().equals("###game_over###you_win###")) {
                            //sent by the client that lost, which is this client
                            endGame(name, message.getRecip());
                        }
                        else if (message.getContent().equals("###new_game_restarted###")) {
                            restartGame(name, message.getRecip());
                        }
                        else {
                            server.sendMessage(message.getRecip(), name, message.getContent());
                        }
                    }
                }
            } //end of while loop
            //Exit occurs when listening is false, or when connection is closed on other side
            //Close all network objects
            out.close();
            in.close();
            socket.close();
            if (ready) {
                server.removeClient(this);
            }

        } catch (IOException e) {
            System.err.println("SERVER: IOException during client thread operation - " + e.getMessage());
        }
    }

    //Kill stops operation by setting listening to false, ending while loop of run()
    void kill() {
        listening = false;
    }

    //===Game Operation Methods

    //Client1 is always this thread, name
    //Called if this client tries to start a game with its chosen oppponent
    private void newGame(String client1, String client2) {
        String gameString = "";
        opponent = clients.get(client2); //get the VersaServerThread of the opponent & store it
        if (games.containsKey(client1+":"+client2)) { //Check if a game exists between these two players
            gameString = client1+":"+client2;
        } else if (games.containsKey(client2+":"+client1)) {
            gameString = client2+":"+client1;
        }
        if (!gameString.equals("")) { //If game already exists
            VersaCheckers game = games.get(gameString); //retrieve game object
            String board;
            //Get game board in appropriate orientation for this game
            if (gameString.substring(0, gameString.indexOf(":")).equals(client1)) {
                board = game.getBoard();
            } else {
                board = VersaCheckers.getRotated(game.getBoard());
            }
            //Tell client about existing game, and board and turn status (message addressed as if from opponent)
            server.sendMessage(client1, client2, "###game_already_exists###board="+board+"###turn="+game.getTurn()+"###");
        } else { //New game must be started
            VersaCheckers game = new VersaCheckers(client1, client2); //Create new game object
            games.put(client1+":"+client2, game); //Put the game object in the games map
            //Inform this client and opponent about the new game (addressed as if from opponent for both)
            server.sendMessage(client1, client2, "###new_game_started###board="+game.getBoard()+"###");
            opponent.sendMessage(client1, "###new_game_started###board="+game.getBoard()+"###");
        }
    }

    //Called when this client is the loser, opponent is the winner
    private void endGame(String loser, String winner){
        String save = "";
        if(games.containsKey(loser+":"+winner)){ //Find the name of the game for this pair
            save = loser+":"+winner;
        }else if(games.containsKey(winner+":"+loser)){
            save = winner+":"+loser;
        }else{ //If somehow, command sent but no game exists (weird error)
            System.err.println("SERVER: Unable to end game, none found for loser: "+loser+" and winner:"+winner);
        }
        if (winner.equals(name)) { //if this clientThread won, which shouldn't happen in this command format...
            sendMessage(loser, "###you_won###"); //weird that formats don't match when informing gui
            //Also, loser == winner == this client in this case
        } else { //send message to opponent
            opponent.sendMessage(loser, "###you_won###");
        }
        games.remove(save); //Remove the finished game from the map
    }

    //Called if new game started after one just finished between these two clients
    //(As in the game windows are still open, and the "start new game" button is pressed in client1)
    //Acts almost identical to the truly new game part of newGame(...)
    private void restartGame(String client1, String client2){
        VersaCheckers game = new VersaCheckers(client1, client2); //Create a new game object
        games.put(client1+":"+client2, game); //Put game object into game map
        //Inform both clients about the start of a new game between them
        //Use new_game_restarted code so that the game can start in the already open game windows
        sendMessage(client2,
                "###new_game_restarted###board="+game.getBoard()+"###turn="+game.getTurn()+"###");
        opponent.sendMessage(client1,
                "###new_game_restarted###board="+
                        VersaCheckers.getRotated(game.getBoard())+"###turn="+game.getTurn()+"###");
    }


    //Takes message containing updated game board, updates the VersaCheckers game object, and sends new board to
    //the other player
    private void gameMove(String from, String to, String message) {
        String gameString = "";
        if (games.containsKey(from+":"+to)) { //Checks that a game between these two clients exists
            gameString = from+":"+to;
        } else if (games.containsKey(to+":"+from)) {
            gameString = to+":"+from;
        } else { //If somehow a game move command received but no game between these clients (weird error)
            System.err.println("SERVER: Game move registered for non-existant game between "+from+" and "+to);
        }
        VersaCheckers game = games.get(gameString); //Retrieve game object for this game

        //Update the board in the game object, by taking the board sent by the client and converting to array
        int[][] realBoard = VersaCheckers.stringToBoard(
                message.substring(message.indexOf("###new_board=")+14, message.length()-4));
        game.setBoard(realBoard); //set the game to the realBoard
        game.changeTurns();

        //Get and send the new board after it was rotated for next player
        String newBoard;
        if (gameString.substring(0, gameString.indexOf(":")).equals(to)) {
            newBoard = game.getBoard();
        } else {
            newBoard = VersaCheckers.getRotated(game.getBoard());
        }
        if (from.equals(name)) { //if message from this thread to opponent
            opponent.sendMessage(from, "###checkers_move###new_board="+newBoard+"###");
        } else { //message from opponent to this client
            sendMessage(from, "###checkers_move###new_board="+newBoard+"###");
        }

    }

    //===Messaging Sending Method

    //Simply sends message to client from specified sender with message
    //Server sendMessage is called like sendMessage(recipient, sender, message)
    void sendMessage(String sender, String incMessage) {
        out.println("###sentfrom=" + sender + "###message=" + incMessage);
    }

}
