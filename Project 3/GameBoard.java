import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ThreadLocalRandom;

public class GameBoard extends JPanel {
    private Tile[][] board;
    private int numBombs;
    private boolean placedBombs = false;
    private int state = 0;
    private JLabel message;
    private JPanel tiles;
    private JPanel options;
    private JButton automove;
//    private JButton autosolve;
    private JButton restart;

    public GameBoard(int size, int bombs) {
        board = new Tile[size][size];
        numBombs = bombs;
        setupBoard();
        print();
    }

    private void setupBoard() {
        tiles = new JPanel();
        options = new JPanel();
        automove = new JButton("Automove");
//        autosolve = new JButton("Autosolve");
        restart = new JButton("New game");
        GridLayout gl = new GridLayout();
        gl.setRows(board.length + 1);
        gl.setColumns(board[0].length);
        tiles.setLayout(gl);
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[i].length; j++) {
                board[i][j] = new Tile(i, j);
                board[i][j].setBackground(new Color(235, 235, 235));
                board[i][j].setOpaque(false);
                board[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if(((Tile)e.getSource()).isEnabled()) {
                            if (e.getButton() == MouseEvent.BUTTON3) {
                                int x = ((Tile) e.getSource()).x;
                                int y = ((Tile) e.getSource()).y;
                                toggleFlagged(x, y);
                            } else if (e.getButton() == MouseEvent.BUTTON1) {
                                int x = ((Tile) e.getSource()).x;
                                int y = ((Tile) e.getSource()).y;
                                click(x, y);
                            }
                        }
                    }
                });
                tiles.add(board[i][j]);
            }
        }
        message = new JLabel(" ");
        options.add(message);
        automove.addActionListener(e -> move());
        options.add(automove);
        restart.addActionListener(e -> newGame());
        options.add(restart);
        tiles.setSize(board[0].length * 30, board.length * 30);
        options.setSize(tiles.getWidth(), 100);
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(Box.createGlue());
        this.add(options);
        this.add(tiles);
        this.setSize(tiles.getWidth(), tiles.getHeight() + options.getHeight());
    }

    public void print() {
        int ret = 0;
        int bombCount = board.length * board[0].length;
        for(int i = 0; i < board.length; i++) {
            if(i == 0) {
                System.out.print("   ");
                for(int j = 0; j < board[i].length; j++) {
                    System.out.print((char)(j + 65) + "  ");
                }
                System.out.print("\n");
            }
            System.out.printf("%2d", i);
            for(int j = 0; j < board[i].length; j++) {
                Tile current = board[i][j];
                if(current.isClicked()) {
                    if(current.isBomb()) {
                        current.setForeground(new Color(80, 0, 0));
                        current.setText("X");
                        System.out.print("[\u001B[41mX\u001B[0m]");
                        ret = -1; // lost
                    } else {
                        bombCount--;
                        if(current.getAdjacentBombs() == 0) {
                            current.setForeground(new Color(200, 200, 200));
                            current.setText("0");
                            System.out.print("[\u001B[37m0\u001B[0m]");
                        } else if(current.getAdjacentBombs() == 1) {
                            current.setForeground(new Color(0, 0, 255));
                            current.setText("1");
                            System.out.print("[\u001B[34m1\u001B[0m]");
                        } else if(current.getAdjacentBombs() == 2) {
                            current.setForeground(new Color(0, 180, 0));
                            current.setText("2");
                            System.out.print("[\u001B[32m2\u001B[0m]");
                        } else if(current.getAdjacentBombs() == 3) {
                            current.setForeground(new Color(255, 0, 0));
                            current.setText("3");
                            System.out.print("[\u001B[31m3\u001B[0m]");
                        } else if(current.getAdjacentBombs() == 4) {
                            current.setForeground(new Color(0, 0, 128));
                            current.setText("4");
                            System.out.print("[\u001B[36m4\u001B[0m]");
                        } else if(current.getAdjacentBombs() == 5) {
                            current.setForeground(new Color(128, 0, 0));
                            current.setText("5");
                            System.out.print("[\u001B[35m5\u001B[0m]");
                        } else if(current.getAdjacentBombs() == 6) {
                            current.setForeground(new Color(64, 200, 200));
                            current.setText("6");
                            System.out.print("[\u001B[35m6\u001B[0m]");
                        } else if(current.getAdjacentBombs() == 7) {
                            current.setForeground(new Color(0, 0, 0));
                            current.setText("7");
                            System.out.print("[\u001B[35m7\u001B[0m]");
                        } else if(current.getAdjacentBombs() == 8) {
                            current.setForeground(new Color(128, 128, 128));
                            current.setText("8");
                            System.out.print("[\u001B[35m8\u001B[0m]");
                        }
                    }
                } else {
                    if(current.isFlagged()) {
                        System.out.print("[\u001B[33mF\u001B[0m]");
                    } else {
                        System.out.print("[ ]");
                    }
                }
            }
            System.out.print("\n");
        }
        if(bombCount == numBombs) {
            ret = 1; //won
        }
        state = ret;
        if(state != 0) { //If won || lost
            end();
        }
    }

    private void setupBombs(int i, int j) {
        if(numBombs > (board.length * board[0].length)) {
            numBombs = (board.length * board[0].length) - 1;
        }
        for (int k = 0; k < numBombs; k++) {
            int x = ThreadLocalRandom.current().nextInt(board.length);
            int y = ThreadLocalRandom.current().nextInt(board[x].length);
            if((board.length * board[x].length) - numBombs > 9) {
                if (!board[x][y].isBomb() && ((x < i - 1 || x > i + 1) || (y < j - 1 || y > j + 1))) {
                    board[x][y].setBomb();
                } else {
                    k--;
                }
            } else {
                if(!board[x][y].isBomb() && (x != i || y != j)) {
                    board[x][y].setBomb();
                } else {
                    k--;
                }
            }
        }
        placedBombs = true;
        //Set adjacentBomb numbers
        for (int m = 0; m < board.length; m++) {
            for (int n = 0; n < board[m].length; n++) {
                int bombCount = 0;
                if (!board[m][n].isBomb()) {
                    int mmin = (m - 1 >= 0) ? m - 1 : m;
                    int mmax = (m + 1 < board.length) ? m + 1 : m;
                    int nmin = (n - 1 >= 0) ? n - 1 : n;
                    int nmax = (n + 1 < board.length) ? n + 1 : n;
                    for (int md = mmin; md <= mmax; md++) {
                        for (int nd = nmin; nd <= nmax; nd++) {
                            if (md != m || nd != n) { //Not checking against itself
                                if (board[md][nd].isBomb()) { //If this is a bomb, increase the bombCount
                                    bombCount++;
                                }
                            }
                        }
                    }
                    board[m][n].setAdjacentBombs(bombCount);
                }
            }
        }
    }

    public void click(int i, int j) {
        if(!placedBombs) {
            setupBombs(i, j);
        }
        if(i >= 0 && i < board.length) {
            if (board[i][j].isClicked()) {
                int imin = (i - 1 >= 0) ? i - 1 : i;
                int imax = (i + 1 < board.length) ? i + 1 : i;
                int jmin = (j - 1 >= 0) ? j - 1 : j;
                int jmax = (j + 1 < board.length) ? j + 1 : j;
                for (int id = imin; id <= imax; id++) {
                    for (int jd = jmin; jd <= jmax; jd++) {
                        if ((id != i || jd != j) && !board[id][jd].isFlagged() && !board[id][jd].isClicked() &&
                                board[id][jd].isEnabled()) { //Not checking against itself, uncovering unflagged boxes
                            click(id, jd);
                        }
                    }
                }
            } else {
                if(board[i][j].isEnabled()) {
                    board[i][j].click();
                    if (board[i][j].getAdjacentBombs() == 0 && !board[i][j].isBomb()) {
                        board[i][j].setEnabled(false);
                        int imin = (i - 1 >= 0) ? i - 1 : i;
                        int imax = (i + 1 < board.length) ? i + 1 : i;
                        int jmin = (j - 1 >= 0) ? j - 1 : j;
                        int jmax = (j + 1 < board.length) ? j + 1 : j;
                        for (int id = imin; id <= imax; id++) {
                            for (int jd = jmin; jd <= jmax; jd++) {
                                if ((id != i || jd != j) && !board[id][jd].isClicked() && board[id][jd].isEnabled()) { //Not checking against itself
                                    click(id, jd);
                                }
                            }
                        }
                    }
                }
            }
        }
        print();
    }

    public void toggleFlagged(int i, int j) {
        if(i >= 0 && i < board.length) {
            if(!board[i][j].isClicked()) {
                board[i][j].toggleFlagged();
                board[i][j].setForeground((board[i][j].isFlagged()) ? new Color(224, 199, 6) : new Color(235, 235, 235));
                board[i][j].setText((board[i][j].isFlagged()) ? "⚑" : "");
            }
        }
        print();
    }

    public void move() {
        Move m = new Move(board, 0, board.length, 0, board[0].length);
        m.compute();
        if(m.click) {
            click(m.i, m.j);
        } else {
            toggleFlagged(m.i, m.j);
        }
    }

    public void solve() {
        move();
        long t = System.currentTimeMillis();
        while((System.currentTimeMillis() - t) < 350) {}
        if(state == 0) {
            solve();
        }
    }

    private void end() {
        message.setForeground((state == -1) ? Color.red : new Color(0, 180, 0));
        message.setText((state == -1) ? "Game over" : "You won!");
        automove.setEnabled(false);
        for(Tile[] row : board) {
            for(Tile tile : row) {
                tile.setEnabled(false);
            }
        }
    }

    public int getState() {
        return state;
    }

    public void newGame() {
        this.removeAll();
        setupBoard();
        state = 0;
        placedBombs = false;
        automove.setEnabled(true);
        for(Tile[] row : board) {
            for(Tile tile : row) {
                tile.setEnabled(true);
            }
        }
        this.updateUI();
    }
}
