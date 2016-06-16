package CheckerClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * VersaClientChat
 */

public class CheckersPanel extends JPanel {
    /**
     * The panel displays and shows the board
     */
    private int[][] board = null;
    private int[] selected = null;

    private Image red_piece = null;
    private Image blue_piece = null;
    private Image red_piece_king = null;
    private Image blue_piece_king = null;

    public CheckersPanel() {
        try {
            red_piece = ImageIO.read(this.getClass().getResource("images/red_piece.png"));
            blue_piece = ImageIO.read(this.getClass().getResource("images/blue_piece.png"));
            red_piece_king = ImageIO.read(this.getClass().getResource("images/red_piece_king.png"));
            blue_piece_king = ImageIO.read(this.getClass().getResource("images/blue_piece_king.png"));
        } catch (IOException e) {
            System.out.println("could not open file");
        }
    }

    public int[] getSelected() {
        return selected;
    }

    public void setSelected(int x, int y) {
        if (x == -1 && y == -1) {
            selected = null;
        } else {
            selected = new int[2];
            selected[0] = x;
            selected[1] = y;
        }
        repaint();
    }

    public void addPiece(int x, int y, int type) {
        if (board[y][x] == 0 && (type == 1 || type == 2 || type == 3 || type ==3)) {
            board[y][x] = type;
            repaint();
        } else {
            System.err.println("Error: A piece is already there");
        }
    }

    public int removePiece(int x, int y) {
        if (board[y][x] != 0) {
            int res = board[y][x];
            board[y][x] = 0;
            repaint();
            return res;
        } else {
            System.err.println("Error: no piece exists there");
            return 0;
        }
    }

    public int[][] getBoard() {
        int[][] b = new int[8][8];
        for (int y = 0; y < 8; y++) {
            System.arraycopy(board[y], 0, b[y], 0, 8);
        }
        return b;
    }

    public String getFormattedBoard() {
        String res = "[";
        for (int[] y : board) {
            res += "[";
            for (int x: y) {
                res += x+",";
            }
            res = res.substring(0, res.length()-1);
            res += "],";
        }
        res = res.substring(0, res.length()-1);
        res += "]";
        return res;
    }

    public void setBoard(int[][] newBoard) {
        this.board = newBoard;
        repaint();
    }

    //This method is what paints the board
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Draw the background
        g.setColor(Color.BLACK);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if ((x+y) % 2 == 0) {
                    g.fillRect(x*50, y*50, 50, 50);
                }
            }
        }

        //Draw the selection
        g.setColor(Color.GREEN);
        if (selected != null) {
            g.fillRect(selected[0]*50, selected[1]*50, 50, 50);
        }

        //Draw the pieces
        if (board != null) {
            for (int y = 0; y < board.length; y++) {
                for (int x = 0; x < board.length; x++) {
                    if (board[y][x] == 1) {
                        g.drawImage(red_piece, x*50+5, y*50+5, x*50+45, y*50+45,
                                0, 0, red_piece.getWidth(null), red_piece.getHeight(null), null);
                    } else if (board[y][x] == 2) {
                        g.drawImage(red_piece_king, x*50+5, y*50+5, x*50+45, y*50+45,
                                0, 0, red_piece_king.getWidth(null), red_piece_king.getHeight(null), null);
                    } else if (board[y][x] == 3) {
                        g.drawImage(blue_piece, x*50+5, y*50+5, x*50+45, y*50+45,
                                0, 0, blue_piece.getWidth(null), blue_piece.getHeight(null), null);
                    } else if (board[y][x] == 4) {
                        g.drawImage(blue_piece_king, x*50+5, y*50+5, x*50+45, y*50+45,
                                0, 0, blue_piece_king.getWidth(null), blue_piece_king.getHeight(null), null);
                    }
                }
            }
        }
    }
}