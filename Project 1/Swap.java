public class Swap {
    final Point p1;
    final Point p2;

    public Swap(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            Swap swap = (Swap) obj;
            return swap.p1 == this.p1 && swap.p2 == this.p2;
        }
        return false;
    }
}
