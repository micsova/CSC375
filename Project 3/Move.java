import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class Move extends RecursiveAction {
    Tile[][] board;
    boolean click = false;
    boolean random = false;
    boolean semirandom = false;
    int i, j;
    int imin, imax, jmin, jmax;
    double score = 0;
    final int THRESHOLD = 4;

    Move(Tile[][] board, int imin, int imax, int jmin, int jmax) {
        this.board = board;
        this.imin = imin;
        this.imax = imax;
        this.jmin = jmin;
        this.jmax = jmax;
    }

    @Override
    protected void compute() {
        if(imax - imin < THRESHOLD || jmax - jmin < THRESHOLD) {
            move();
        } else {
            int imid = (imin + imax) >>> 1;
            int jmid = (jmin + jmax) >>> 1;
            Move tl = new Move(board, imin, imid, jmin, jmid);
            tl.fork();
            Move tr = new Move(board, imin, imid, jmid, jmax);
            tr.fork();
            Move bl = new Move(board, imid, imax, jmin, jmid);
            bl.fork();
            Move br = new Move(board, imid, imax, jmid, jmax);
            br.compute();
            tl.join(); tr.join(); bl.join();
            ArrayList<Move> moves = new ArrayList<>();
            moves.add(tl); moves.add(tr); moves.add(bl); moves.add(br);
            Collections.shuffle(moves);
            for(Move move : moves) {
                if(move.score > score) {
                    i = move.i;
                    j = move.j;
                    click = move.click;
                    score = move.score;
                } else if (move.score == score) {
                    boolean change = ThreadLocalRandom.current().nextBoolean();
                    if(change) {
                        i = move.i;
                        j = move.j;
                        click = move.click;
                        score = move.score;
                    }
                }
            }
        }
    }

    public double getScore() {
        double score = 0;
        if(click && !semirandom && !random) {
            if(board[i][j].isClicked()) {
                int unclicked = 0;
                int imin = (i - 1 >= 0) ? i - 1 : i;
                int imax = (i + 1 < board.length) ? i + 1 : i;
                int jmin = (j - 1 >= 0) ? j - 1 : j;
                int jmax = (j + 1 < board[0].length) ? j + 1 : j;
                for(int id = imin; id <= imax; id++) {
                    for(int jd = jmin; jd <= jmax; jd++) {
                        if((id != i || jd != j) && !board[id][jd].isClicked() && !board[id][jd].isFlagged()) {
                            unclicked++;
                        }
                    }
                }
                score += unclicked;
            } else {
                score++;
            }
        } else if (!semirandom && !random) {
            score += 0.5;
        } else if (semirandom) {
            score += 0.25;
            semirandom = false;
        } else {
            score += 0.1;
            semirandom = false;
            random = false;
        }
        return score;
    }

    public void move() {
        boolean clickedSomething = false;
        //iterate through whole board
        for(int i = 0; i < this.imax; i++) {
            for(int j = 0; j < this.jmax; j++) {
                //If the spot is clicked and has adjacent bombs
                if(board[i][j].isClicked() && board[i][j].getAdjacentBombs() != 0) {
                    int adjacentBombs = board[i][j].getAdjacentBombs();
                    int flagged = 0;
                    int unclicked = 0;
                    int imin = (i - 1 >= 0) ? i - 1 : i;
                    int imax = (i + 1 < board.length) ? i + 1 : i;
                    int jmin = (j - 1 >= 0) ? j - 1 : j;
                    int jmax = (j + 1 < board[0].length) ? j + 1 : j;
                    //count how many flags/open spots it has around it
                    for(int id = imin; id <= imax; id++) {
                        for(int jd = jmin; jd <= jmax; jd++) {
                            if(id != i || jd != j) {
                                if (board[id][jd].isFlagged()) {
                                    flagged++;
                                } else if (!board[id][jd].isClicked()) {
                                    unclicked++;
                                }
                            }
                        }
                    }
                    //If it has the same number of flags as adjacent bombs, check to click the tile
                    if(flagged == adjacentBombs && unclicked != 0) {
                        //check score if you click this one. if it's a better score, keep it. otherwise keep previous move
                        boolean prevClick = this.click;
                        int prevI = this.i;
                        int prevJ = this.j;
                        this.click = true;
                        this.i = i;
                        this.j = j;
                        if(this.getScore() > score) {
                            score = this.getScore();
                        } else {
                            this.click = prevClick;
                            this.i = prevI;
                            this.j = prevJ;
                        }
                        clickedSomething = true;
                        //If the flags + open spots = the number of adjacent bombs, flag one of the surrounding tiles
                    } else if(unclicked + flagged == adjacentBombs && unclicked != 0) {
                        imin = (i - 1 >= 0) ? i - 1 : i;
                        imax = (i + 1 < board.length) ? i + 1 : i;
                        jmin = (j - 1 >= 0) ? j - 1 : j;
                        jmax = (j + 1 < board[0].length) ? j + 1 : j;
                        flagger: for(int id = imin; id <= imax; id++) {
                            for(int jd = jmin; jd <= jmax; jd++) {
                                if(!board[id][jd].isFlagged() && !board[id][jd].isClicked()) {
                                    //check score if you flag this. if it's a better score, keep it. otherwise keep previous move
                                    boolean prevClick = this.click;
                                    int prevI = this.i;
                                    int prevJ = this.j;
                                    this.click = false;
                                    this.i = id;
                                    this.j = jd;
                                    if(this.getScore() > score) {
                                        score = this.getScore();
                                    } else {
                                        this.click = prevClick;
                                        this.i = prevI;
                                        this.j = prevJ;
                                    }
                                    break flagger;
                                }
                            }
                        }
                        clickedSomething = true;
                    }
                }
            }
        }
        if(!clickedSomething) {
            //If you didn't click anything, there is no definite safe move
            int iclick = -1;
            int jclick = -1;
            double best = 1;
            for(int i = 0; i < this.imax; i++) {
                for(int j = 0; j < this.jmax; j++) {
                    if(board[i][j].isClicked()) {
                        int adjacent = board[i][j].getAdjacentBombs();
                        double spots = 0;
                        int imin = (i - 1 >= 0) ? i - 1 : i;
                        int imax = (i + 1 < board.length) ? i + 1 : i;
                        int jmin = (j - 1 >= 0) ? j - 1 : j;
                        int jmax = (j + 1 < board[0].length) ? j + 1 : j;
                        for(int id = imin; id <= imax; id++) {
                            for(int jd = jmin; jd <= jmax; jd++) {
                                if(!board[id][jd].isClicked() && (id != i || jd != j)) {
                                    spots++;
                                }
                            }
                        }
                        //Find the spot that has the smallest ratio of bombs : spots (i.e., the safest guess)
                        double chance = adjacent / spots;
                        if(chance < best) {
                            best = chance;
                            iclick = i;
                            jclick = j;
                        }
                    }
                }
            }
            if (iclick != -1 && jclick != -1) {
                // + 1 because r.nextInt is exclusive on the bound
                int imin = (iclick - 1 >= 0) ? iclick - 1 : iclick;
                int imax = ((iclick + 2 <= board.length) ? iclick + 1 : iclick) + 1;
                int jmin = (jclick - 1 >= 0) ? jclick - 1 : jclick;
                int jmax = ((jclick + 2 <= board[0].length) ? jclick + 1 : jclick) + 1;
                int xclick, yclick;
                for(;;) {
                    xclick = ThreadLocalRandom.current().nextInt(imax - imin) + imin;
                    yclick = ThreadLocalRandom.current().nextInt(jmax - jmin) + jmin;
                    if((xclick != iclick || yclick != jclick) &&
                            !board[xclick][yclick].isClicked() && !board[xclick][yclick].isFlagged()) {
                        break;
                    }
                }
                click = true;
                semirandom = true;
                this.i = xclick;
                this.j = yclick;
                score = this.getScore();
            //If you didn't find a safest guess, nothing has been clicked yet, so choose randomly
            } else {
                this.click = true;
                random = true;
                this.i = ThreadLocalRandom.current().nextInt(this.imax - this.imin) + this.imin;
                this.j = ThreadLocalRandom.current().nextInt(this.jmax - this.jmin) + this.jmin;
                score = this.getScore();
            }
        }
    }
}
