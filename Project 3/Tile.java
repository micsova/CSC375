import javax.swing.*;

public class Tile extends JButton {
    public int x, y;
    private boolean isClicked = false;
    private boolean isFlagged = false;
    private boolean isBomb = false;
    private int adjacentBombs = -1;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void toggleFlagged() {
        isFlagged = !isFlagged;
    }

    public void setBomb() {
        isBomb = true;
    }

    public boolean isClicked() {
        return isClicked;
    }

    public void click() {
        isClicked = true;
    }

    public boolean isBomb() {
        return isBomb;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public int getAdjacentBombs() {
        return adjacentBombs;
    }

    public void setAdjacentBombs(int b) {
        adjacentBombs = b;
    }
}
