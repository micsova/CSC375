import javax.swing.*;
import java.util.Scanner;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {

    public static boolean restart = false;

    public static void main(String[] args) {
        JFrame minesweeper;
        GameBoard board = new GameBoard(20, 70);
        outer: for(;;) {
            minesweeper = new JFrame();
            minesweeper.setSize(board.getSize());
            minesweeper.setResizable(false);
            minesweeper.add(board);
            minesweeper.setDefaultCloseOperation(EXIT_ON_CLOSE);
            minesweeper.setVisible(true);
            Scanner kb = new Scanner(System.in);
            for (; ; ) {
                if (board.getState() != 0) break;
                System.out.println("What would you like to do? (quit, click {tile}, flag {tile}, automove)");
                String command = kb.next();
                String tile;
                char letter;
                int i, j;
                switch (command) {
                    case "quit":
                        break outer;
                    case "click":
                        tile = kb.nextLine().trim();
                        letter = tile.charAt(0);
                        i = Integer.parseInt(tile.substring(1));
                        j = getNumber(letter);
                        if (j == -1) {
                            break;
                        }
                        board.click(i, j);
                        break;
                    case "flag":
                        tile = kb.nextLine().trim();
                        letter = tile.charAt(0);
                        i = Integer.parseInt(tile.substring(1));
                        j = getNumber(letter);
                        if (j == -1) {
                            break;
                        }
                        board.toggleFlagged(i, j);
                        break;
                    case "automove":
                        kb.nextLine();
                        board.move();
                        //uncomment this if you want it to solve the entire game
//                    break outer;
                        break;
                    default:
                        kb.nextLine();
                        break;
                }
            }
            if (board.getState() == -1) {
                System.err.println("You lost!");
            } else if (board.getState() == 1) {
                System.out.println("You won!");
            }
            String reply;
            do {
                System.out.println("Would you like to play again? (y/n)");
                reply = kb.nextLine();
                if (reply.equals("n")) {
                    break outer;
                } else if (!reply.equals("y")) {
                    System.out.println("Invalid response.");
                }
            } while(!reply.equals("y"));
//            minesweeper.dispose();
        }
        System.out.println("Thanks for playing!");
//        minesweeper.dispose();
    }

    public static int getNumber(char letter) {
        int i = (int)letter;
        if(i >= 65 && i <= 90) {
            i -= 65;
        } else if(i >= 97 && i <= 122) {
            i -= 97;
        } else {
            i = -1;
        }
        return i;
    }
}
