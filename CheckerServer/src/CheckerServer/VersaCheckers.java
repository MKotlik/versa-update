/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * VersaCheckers
 */

package CheckerServer;

class VersaCheckers {
    //===Instance Variables
    private int[][] board;
    private String player1 = "";
    private String player2 = "";
    private String turn = "";

    //===Constructor
    VersaCheckers(String client1, String client2){
        int [][] temp = {
                {3, 0, 3, 0, 3, 0, 3, 0},
                {0, 3, 0, 3, 0, 3, 0, 3},
                {3, 0, 3, 0, 3, 0, 3, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 1, 0, 1, 0, 1},
                {1, 0, 1, 0, 1, 0, 1, 0},
                {0, 1, 0, 1, 0, 1, 0, 1}
        }; //1 & 3 represent player pieces
        board = temp;
        player1 = client1;
        player2 = client2;
        turn = player1;
    }

    //===Accessor Methods for Board and Turn

    String getBoard(){
        return boardToString(board);
    }

    String getTurn(){
        return turn;
    }

    //===Mutator Methods for Board and Turn

    //Stores board in the orientation appropriate for the current turn
    //newBoard is assumed to always be in player1 orientation, so if turn is for player2 it gets rotated
    void setBoard(int[][] newBoard){
        if (turn.equals(player1)) {
            board = newBoard;
        } else {
            String newBoardStr = boardToString(newBoard);
            newBoardStr = getRotated(newBoardStr);
            board = stringToBoard(newBoardStr.substring(1, newBoardStr.length()-1));
        }
    }

    //Changes turns from player1 to player2 and vice versa
    void changeTurns(){
        if (turn.equals(player1)) {
            turn = player2;
        } else if (turn.equals(player2)) {
            turn = player1;
        } else {
            System.err.println("SERVER: Turn for game between "+player1+" and "+player2+" is set to a third player.");
        }
    }

    //===Board Operation Static Methods

    //Converts a board array into a string version
    static String boardToString(int[][] b){
        String save = "[";
        for(int [] y : b){
            save += "[";
            for (int x: y){
                save += x+",";
            }
            save = save.substring(0,save.length()-1);
            save += "],";
        }
        save = save.substring(0, save.length()-1);
        save += "]";
        return save;
    }

    //Converts a board string into an array version
    static int[][] stringToBoard(String boardStr) {
        int[][] boardArray = new int[8][8];
        String[] rows = boardStr.split("\\],\\[");
        rows[0] = rows[0].substring(1, rows[0].length());
        rows[7] = rows[7].substring(0, rows[7].length()-1);
        for (int y = 0; y < 8; y++) {
            String chars[] = rows[y].split(",");
            for (int x = 0; x < 8; x++) {
                boardArray[y][x] = Integer.parseInt(chars[x]);
            }
        }
        return boardArray;
    }

    //Rotates a string ver. of a board into the opponent's orientation
    static String getRotated(String current){
        String save = current;
        save = save.replace("1", "-1");
        save = save.replace("3", "1");
        save = save.replace("-1", "3");
        save = save.replace("2", "-2");
        save = save.replace("4", "2");
        save = save.replace("-2", "4");
        save = new StringBuffer(save).reverse().toString();
        save = save.replace("[", "*");
        save = save.replace("]", "[");
        save = save.replace("*", "]");
        return save;
    }

}
