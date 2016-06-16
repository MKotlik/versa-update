package CheckerServer;

/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * VersaCheckers
 */

public class VersaCheckers {
    private int[][] board;
    private String player1 = "";
    private String player2 = "";
    private String turn = "";

    public VersaCheckers(String client1, String client2){
        int [][] temp = {
                {3, 0, 3, 0, 3, 0, 3, 0},
                {0, 3, 0, 3, 0, 3, 0, 3},
                {3, 0, 3, 0, 3, 0, 3, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 1, 0, 1, 0, 1},
                {1, 0, 1, 0, 1, 0, 1, 0},
                {0, 1, 0, 1, 0, 1, 0, 1}
        };
        board = temp;
        player1 = client1;
        player2 = client2;
        turn = player1;
    }

    //Accessor methods
    public String getBoard(){
        return boardToString(board);
    }

    private String boardToString(int[][] b){
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

    public String getRotated(String current){
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

    public String getTurn(){
        return turn;
    }

    //Mutator methods
    public void setBoard(int[][] newBoard){
        if (turn.equals(player1)) {
            board = newBoard;
        } else {
            String newBoardStr = boardToString(newBoard);
            newBoardStr = getRotated(newBoardStr);

            int[][] realBoard = new int[8][8];
            String save = newBoardStr.substring(1, newBoardStr.length()-1);
            String[] rows = save.split("\\],\\[");
            rows[0] = rows[0].substring(1, rows[0].length());
            rows[7] = rows[7].substring(0, rows[7].length()-1);

            for (int y = 0; y < 8; y++) {
                String chars[] = rows[y].split(",");
                for (int x = 0; x < 8; x++) {
                    realBoard[y][x] = Integer.parseInt(chars[x]);
                }
            }
            board = realBoard;
        }
    }

    public void changeTurns(){
        if (turn.equals(player1)) {
            turn = player2;
        } else if (turn.equals(player2)) {
            turn = player1;
        } else {
            System.err.println("Error: turn is messed up");
        }
    }
}
